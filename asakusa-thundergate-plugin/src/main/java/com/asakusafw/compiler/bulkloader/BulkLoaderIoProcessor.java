/**
 * Copyright 2011 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.compiler.bulkloader;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.bulkloader.BulkLoaderScript.Cache;
import com.asakusafw.compiler.bulkloader.BulkLoaderScript.DuplicateRecordErrorTable;
import com.asakusafw.compiler.bulkloader.BulkLoaderScript.ExportTable;
import com.asakusafw.compiler.bulkloader.BulkLoaderScript.ImportTable;
import com.asakusafw.compiler.bulkloader.BulkLoaderScript.LockType;
import com.asakusafw.compiler.bulkloader.BulkLoaderScript.LockedOperation;
import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.epilogue.parallel.ParallelSortClientEmitter;
import com.asakusafw.compiler.flow.epilogue.parallel.ResolvedSlot;
import com.asakusafw.compiler.flow.epilogue.parallel.Slot;
import com.asakusafw.compiler.flow.epilogue.parallel.SlotResolver;
import com.asakusafw.compiler.flow.jobflow.CompiledStage;
import com.asakusafw.runtime.io.property.PropertyLoader;
import com.asakusafw.vocabulary.bulkloader.BulkLoadExporterDescription;
import com.asakusafw.vocabulary.bulkloader.BulkLoadExporterDescription.DuplicateRecordCheck;
import com.asakusafw.vocabulary.bulkloader.BulkLoadImporterDescription;
import com.asakusafw.vocabulary.bulkloader.BulkLoadImporterDescription.Mode;
import com.asakusafw.vocabulary.bulkloader.SecondaryImporterDescription;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;

/**
 * バルクローダーからの入出力を処理する。
 */
public class BulkLoaderIoProcessor extends ExternalIoDescriptionProcessor {

    static final Logger LOG = LoggerFactory.getLogger(BulkLoaderIoProcessor.class);

    private static final String CMD_IMPORTER = "bulkloader/bin/importer.sh";

    private static final String CMD_EXPORTER = "bulkloader/bin/exporter.sh";

    private static final String CMD_FINALIZER = "bulkloader/bin/finalizer.sh";

    private static final String CMD_ARG_PRIMARY = "primary";

    private static final String CMD_ARG_SECONDARY = "secondary";

    private static final String MODULE_NAME = "bulkloader";

    private static final String MODULE_NAME_PREFIX = "bulkloader.";

    @Override
    public Class<? extends ImporterDescription> getImporterDescriptionType() {
        return BulkLoadImporterDescription.class;
    }

    @Override
    public Class<? extends ExporterDescription> getExporterDescriptionType() {
        return BulkLoadExporterDescription.class;
    }

    @Override
    public boolean validate(List<InputDescription> inputs, List<OutputDescription> outputs) {
        LOG.debug("バルクローダーの入出力を検査中: {}->{}", inputs, outputs);
        boolean valid = true;
        valid &= checkImports(inputs);
        valid &= checkExports(outputs);
        valid &= checkAssignment(inputs, outputs);
        return valid;
    }

    private boolean checkImports(List<InputDescription> inputs) {
        assert inputs != null;
        boolean valid = true;
        return valid;
    }

    private boolean checkExports(List<OutputDescription> outputs) {
        assert outputs != null;
        boolean valid = true;
        for (OutputDescription output : outputs) {
            BulkLoadExporterDescription desc = extract(output);
            Set<String> columns = new HashSet<String>(desc.getColumnNames());
            if (columns.containsAll(desc.getTargetColumnNames()) == false) {
                getEnvironment().error(
                        "\"{0}\"の正常テーブルには全体の出力カラムに含まれないカラムが存在します: {1}",
                        desc.getClass().getName(),
                        diff(desc.getTargetColumnNames(), columns));
                valid = false;
            }
            DuplicateRecordCheck dupCheck = desc.getDuplicateRecordCheck();
            if (dupCheck != null) {
                if (columns.containsAll(dupCheck.getColumnNames()) == false) {
                    getEnvironment().error(
                            "\"{0}\"の重複エラーテーブルには全体の出力カラムに含まれないカラムが存在します",
                            desc.getClass().getName(),
                            diff(dupCheck.getColumnNames(), columns));
                    valid = false;
                }
                if (columns.containsAll(dupCheck.getCheckColumnNames()) == false) {
                    getEnvironment().error(
                            "\"{0}\"の重複検査項目には全体の出力カラムに含まれないカラムが存在します",
                            diff(dupCheck.getCheckColumnNames(), columns));
                    valid = false;
                }
            }
        }
        return valid;
    }

    private Set<String> diff(Collection<String> a, Collection<String> b) {
        assert a != null;
        assert b != null;
        Set<String> diff = new TreeSet<String>(a);
        diff.removeAll(b);
        return diff;
    }

    private boolean checkAssignment(List<InputDescription> inputs, List<OutputDescription> outputs) {
        assert inputs != null;
        assert outputs != null;
        Set<String> primaryTargets = new HashSet<String>();
        Set<String> secondaryTargets = new HashSet<String>();
        for (InputDescription description : inputs) {
            BulkLoadImporterDescription desc = extract(description);
            if (desc.getMode() == Mode.PRIMARY) {
                primaryTargets.add(desc.getTargetName());
            } else {
                secondaryTargets.add(desc.getTargetName());
                if (desc.getLockType() != BulkLoadImporterDescription.LockType.UNUSED) {
                    getEnvironment().error(
                            "補助インポーターはロックを指定できません: {0}",
                            desc.getClass().getName());
                    return false;
                }
            }
        }
        if (primaryTargets.size() >= 2) {
            getEnvironment().error(
                    "ジョブフロー内で複数のインポーターを起動できません。{1}を利用してください: {0}",
                    primaryTargets,
                    SecondaryImporterDescription.class.getSimpleName());
            return false;
        }

        for (String primary : primaryTargets) {
            if (secondaryTargets.contains(primary)) {
                LOG.warn("補助インポーターの指定がある{}は通常のインポーターでロードされます", primary);
            }
        }

        Set<String> exportTargets = new HashSet<String>();
        for (OutputDescription description : outputs) {
            BulkLoadExporterDescription desc = extract(description);
            exportTargets.add(desc.getTargetName());
        }
        if (exportTargets.size() >= 2) {
            getEnvironment().error(
                    "ジョブフロー内で複数のエクスポーターを起動できません: {0}",
                    primaryTargets);
            return false;
        }
        if (primaryTargets.isEmpty() || exportTargets.isEmpty()) {
            return true;
        }
        if (primaryTargets.equals(exportTargets) == false) {
            getEnvironment().error(
                    "インポーターとエクスポーターの対象データベースが一致しません: {0}, {1}",
                    primaryTargets,
                    exportTargets);
            return false;
        }

        return true;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class<? extends InputFormat> getInputFormatType(InputDescription description) {
        return SequenceFileInputFormat.class;
    }

    @Override
    public Set<Location> getInputLocations(InputDescription description) {
        return Collections.singleton(getInputLocation(description));
    }

    @Override
    public List<CompiledStage> emitEpilogue(IoContext context) throws IOException {
        if (context.getOutputs().isEmpty()) {
            return Collections.emptyList();
        }

        List<Slot> slots = new ArrayList<Slot>();
        for (Output output : context.getOutputs()) {
            Slot slot = toSlot(output);
            slots.add(slot);
        }
        List<ResolvedSlot> resolved = new SlotResolver(getEnvironment()).resolve(slots);
        if (getEnvironment().hasError()) {
            return Collections.emptyList();
        }

        ParallelSortClientEmitter emitter = new ParallelSortClientEmitter(getEnvironment());
        CompiledStage stage = emitter.emit(
                MODULE_NAME,
                resolved,
                getEnvironment().getEpilogueLocation(MODULE_NAME));

        return Collections.singletonList(stage);
    }

    private Slot toSlot(Output output) {
        BulkLoadExporterDescription desc = extract(output.getDescription());
        List<Slot.Input> inputs = new ArrayList<Slot.Input>();
        for (OutputSource source : output.getSources()) {
            Class<? extends InputFormat<?, ?>> format = source.getFormat();
            for (Location location : source.getLocations()) {
                inputs.add(new Slot.Input(location, format));
            }
        }
        String name = normalize(output.getDescription().getName());
        return new Slot(
                name,
                output.getDescription().getDataType(),
                desc.getPrimaryKeyNames(),
                inputs,
                SequenceFileOutputFormat.class);
    }

    private Location getInputLocation(InputDescription description) {
        assert description != null;
        String name = normalize(description.getName());
        return getEnvironment()
            .getPrologueLocation(MODULE_NAME)
            .append(name);
    }

    private Location getOutputLocation(OutputDescription description) {
        assert description != null;
        String name = normalize(description.getName());
        return getEnvironment()
            .getEpilogueLocation(MODULE_NAME)
            .append(name)
            .asPrefix();
    }

    private String normalize(String name) {
        assert name != null;
        assert name.trim().isEmpty() == false;
        return JavaName.of(name).toMemberName();
    }

    @Override
    public void emitPackage(IoContext context) throws IOException {
        Map<String, BulkLoaderScript> scripts = toScripts(context);
        for (Map.Entry<String, BulkLoaderScript> entry : scripts.entrySet()) {
            String targetName = entry.getKey();
            BulkLoaderScript script = entry.getValue();
            emitProperties(
                    PropertyLoader.getImporterPropertiesPath(targetName),
                    script.getImporterProperties());
            emitProperties(
                    PropertyLoader.getExporterPropertiesPath(targetName),
                    script.getExporterProperties());
        }
    }

    private Map<String, BulkLoaderScript> toScripts(IoContext context) {
        assert context != null;
        Map<String, List<Input>> inputs = new HashMap<String, List<Input>>();
        for (Input input : context.getInputs()) {
            String target = extract(input.getDescription()).getTargetName();
            List<Input> in = inputs.get(target);
            if (in == null) {
                in = new ArrayList<Input>();
                inputs.put(target, in);
            }
            in.add(input);
        }

        Map<String, List<Output>> outputs = new HashMap<String, List<Output>>();
        for (Output output : context.getOutputs()) {
            String target = extract(output.getDescription()).getTargetName();
            List<Output> out = outputs.get(target);
            if (out == null) {
                out = new ArrayList<Output>();
                outputs.put(target, out);
            }
            out.add(output);
        }

        Set<String> targets = new HashSet<String>();
        targets.addAll(inputs.keySet());
        targets.addAll(outputs.keySet());

        Map<String, BulkLoaderScript> results = new HashMap<String, BulkLoaderScript>();
        for (String target : targets) {
            List<Input> in = inputs.get(target);
            List<Output> out = outputs.get(target);
            in = (in == null) ? Collections.<Input>emptyList() : in;
            out = (out == null) ? Collections.<Output>emptyList() : out;
            results.put(target, toScript(in, out));
        }

        return results;
    }

    private BulkLoaderScript toScript(List<Input> inputs, List<Output> outputs) {
        List<ImportTable> imports = new ArrayList<ImportTable>();
        List<ExportTable> exports = new ArrayList<ExportTable>();
        for (Input input : inputs) {
            imports.add(convert(input.getDescription()));
        }
        for (Output output : outputs) {
            exports.add(convert(output.getDescription()));
        }
        BulkLoaderScript script = new BulkLoaderScript(imports, exports);
        return script;
    }

    private ImportTable convert(InputDescription input) {
        assert input != null;
        BulkLoadImporterDescription desc = extract(input);
        LockType lockType;
        LockedOperation lockedOperation;
        switch (desc.getLockType()) {
        case CHECK:
            lockType = LockType.UNLOCKED;
            lockedOperation = LockedOperation.ERROR;
            break;
        case ROW:
            lockType = LockType.ROW;
            lockedOperation = LockedOperation.ERROR;
            break;
        case ROW_OR_SKIP:
            lockType = LockType.ROW;
            lockedOperation = LockedOperation.SKIP;
            break;
        case TABLE:
            lockType = LockType.TABLE;
            lockedOperation = LockedOperation.ERROR;
            break;
        case UNUSED:
            lockType = LockType.UNLOCKED;
            lockedOperation = LockedOperation.FORCE;
            break;
        default:
            throw new AssertionError(desc.getLockType());
        }
        return new ImportTable(
                desc.getModelType(),
                desc.getTableName(),
                desc.getColumnNames(),
                desc.getWhere(),
                desc.isCacheEnabled() ? Cache.ENABLED : Cache.DISABLED,
                lockType,
                lockedOperation,
                getInputLocation(input));
    }

    private ExportTable convert(OutputDescription output) {
        assert output != null;
        BulkLoadExporterDescription desc = extract(output);
        DuplicateRecordCheck duplicate = desc.getDuplicateRecordCheck();
        if (duplicate == null) {
            return new ExportTable(
                    desc.getModelType(),
                    desc.getTableName(),
                    desc.getColumnNames(),
                    desc.getTargetColumnNames(),
                    null,
                    Collections.singletonList(getOutputLocation(output)));
        } else {
            return new ExportTable(
                    desc.getModelType(),
                    desc.getTableName(),
                    desc.getColumnNames(),
                    desc.getTargetColumnNames(),
                    new DuplicateRecordErrorTable(
                            duplicate.getTableName(),
                            duplicate.getColumnNames(),
                            duplicate.getCheckColumnNames(),
                            duplicate.getErrorCodeColumnName(),
                            duplicate.getErrorCodeValue()),
                    Collections.singletonList(getOutputLocation(output)));
        }
    }

    private void emitProperties(String path, Properties properties) throws IOException {
        assert path != null;
        assert properties != null;
        OutputStream output = getEnvironment().openResource(null, path);
        try {
            properties.store(output, getEnvironment().getTargetId());
        } finally {
            output.close();
        }
    }

    private BulkLoadImporterDescription extract(InputDescription description) {
        assert description != null;
        ImporterDescription importer = description.getImporterDescription();
        assert importer != null;
        assert importer instanceof BulkLoadImporterDescription;
        return (BulkLoadImporterDescription) importer;
    }

    private BulkLoadExporterDescription extract(OutputDescription description) {
        assert description != null;
        ExporterDescription exporter = description.getExporterDescription();
        assert exporter != null;
        assert exporter instanceof BulkLoadExporterDescription;
        return (BulkLoadExporterDescription) exporter;
    }

    @Override
    public ExternalIoCommandProvider createCommandProvider(IoContext context) {
        String primary = null;
        Set<String> targets = new TreeSet<String>();
        for (Input input : context.getInputs()) {
            BulkLoadImporterDescription desc = extract(input.getDescription());
            String target = desc.getTargetName();
            if (desc.getMode() == Mode.PRIMARY) {
                assert primary == null || primary.equals(target);
                primary = target;
            }
            targets.add(target);
        }
        for (Output output : context.getOutputs()) {
            BulkLoadExporterDescription desc = extract(output.getDescription());
            String target = desc.getTargetName();
            assert primary == null || primary.equals(target);
            primary = target;
            targets.add(target);
        }
        if (primary != null) {
            targets.remove(primary);
        }

        return new CommandProvider(
                getEnvironment().getBatchId(),
                getEnvironment().getFlowId(),
                primary,
                new ArrayList<String>(targets));
    }

    static ExternalIoCommandProvider findRelated(List<ExternalIoCommandProvider> commands) {
        for (ExternalIoCommandProvider provider : commands) {
            if (provider instanceof CommandProvider) {
                return provider;
            }
        }
        return null;
    }

    static String getProfileName(String targetName) {
        assert targetName != null;
        return targetName;
    }

    /**
     * Provides lifecycle commands for ThunderGate.
     */
    public static class CommandProvider extends ExternalIoCommandProvider {

        private static final long serialVersionUID = 5091727772482760422L;

        private final String batchId;

        private final String flowId;

        private final String primary;

        private final List<String> secondaries;

        CommandProvider(String batchId, String flowId, String primary, List<String> secondaries) {
            assert batchId != null;
            assert flowId != null;
            assert secondaries != null;
            this.batchId = batchId;
            this.flowId = flowId;
            this.primary = primary;
            this.secondaries = new ArrayList<String>(secondaries);
        }

        @Override
        public String getName() {
            return MODULE_NAME;
        }

        @Override
        public List<Command> getImportCommand(CommandContext context) {
            List<Command> results = new ArrayList<Command>();
            if (primary != null) {
                results.add(new Command(
                        Arrays.asList(new String[] {
                                context.getHomePathPrefix() + CMD_IMPORTER,
                                CMD_ARG_PRIMARY,
                                primary,
                                batchId,
                                flowId,
                                context.getExecutionId(),
                                "20380101000000",
                                context.getVariableList(),
                        }),
                        MODULE_NAME_PREFIX + primary ,
                        getProfileName(primary),
                        getEnvironment(context)));
            }
            for (String secondary : secondaries) {
                results.add(new Command(
                        Arrays.asList(new String[] {
                                context.getHomePathPrefix() + CMD_IMPORTER,
                                CMD_ARG_SECONDARY,
                                secondary,
                                batchId,
                                flowId,
                                context.getExecutionId(),
                                "20380101000000",
                                context.getVariableList(),
                        }),
                        MODULE_NAME_PREFIX + secondary,
                        getProfileName(secondary),
                        getEnvironment(context)));
            }
            return results;
        }

        @Override
        public List<Command> getExportCommand(CommandContext context) {
            if (primary == null) {
                return Collections.emptyList();
            }
            return Arrays.asList(new Command[] {
                    new Command(
                            Arrays.asList(new String[] {
                                    context.getHomePathPrefix() + CMD_EXPORTER,
                                    primary,
                                    batchId,
                                    flowId,
                                    context.getExecutionId(),
                                    context.getVariableList(),
                            }),
                            MODULE_NAME_PREFIX + primary ,
                            getProfileName(primary),
                            getEnvironment(context)),
            });
        }

        @Override
        public List<Command> getFinalizeCommand(CommandContext context) {
            if (primary == null) {
                return Collections.emptyList();
            }
            return Arrays.asList(new Command[] {
                    new Command(
                            Arrays.asList(new String[] {
                                    context.getHomePathPrefix() + CMD_FINALIZER,
                                    primary,
                                    batchId,
                                    flowId,
                                    context.getExecutionId(),
                            }),
                            MODULE_NAME_PREFIX + primary ,
                            getProfileName(primary),
                            getEnvironment(context)),
            });
        }

        private Map<String, String> getEnvironment(CommandContext context) {
            return Collections.emptyMap();
        }
    }
}
