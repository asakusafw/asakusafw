/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.bulkloader.BulkLoaderScript.DuplicateRecordErrorTable;
import com.asakusafw.compiler.bulkloader.BulkLoaderScript.ExportTable;
import com.asakusafw.compiler.bulkloader.BulkLoaderScript.ImportTable;
import com.asakusafw.compiler.bulkloader.BulkLoaderScript.LockType;
import com.asakusafw.compiler.bulkloader.BulkLoaderScript.LockedOperation;
import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.jobflow.CompiledStage;
import com.asakusafw.compiler.flow.jobflow.ExternalIoStage;
import com.asakusafw.compiler.flow.mapreduce.parallel.ParallelSortClientEmitter;
import com.asakusafw.compiler.flow.mapreduce.parallel.ResolvedSlot;
import com.asakusafw.compiler.flow.mapreduce.parallel.Slot;
import com.asakusafw.compiler.flow.mapreduce.parallel.SlotResolver;
import com.asakusafw.runtime.stage.input.TemporaryInputFormat;
import com.asakusafw.runtime.stage.output.TemporaryOutputFormat;
import com.asakusafw.thundergate.runtime.cache.CacheStorage;
import com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport;
import com.asakusafw.thundergate.runtime.property.PathConstants;
import com.asakusafw.thundergate.runtime.property.PropertyLoader;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.vocabulary.bulkloader.BulkLoadExporterDescription;
import com.asakusafw.vocabulary.bulkloader.BulkLoadExporterDescription.DuplicateRecordCheck;
import com.asakusafw.vocabulary.bulkloader.BulkLoadImporterDescription;
import com.asakusafw.vocabulary.bulkloader.BulkLoadImporterDescription.Mode;
import com.asakusafw.vocabulary.bulkloader.SecondaryImporterDescription;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription.DataSize;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;

/**
 * バルクローダーからの入出力を処理する。
 */
public class BulkLoaderIoProcessor extends ExternalIoDescriptionProcessor {

    static final Logger LOG = LoggerFactory.getLogger(BulkLoaderIoProcessor.class);

    private static final String CMD_IMPORTER = PathConstants.PATH_IMPORTER;

    private static final String CMD_EXPORTER = PathConstants.PATH_EXPORTER;

    private static final String CMD_FINALIZER = PathConstants.PATH_FINALIZER;

    private static final String CMD_CACHE_FINALIZER = PathConstants.PATH_CACHE_FINALIZER;

    private static final String CMD_ARG_PRIMARY = "primary";

    private static final String CMD_ARG_SECONDARY = "secondary";

    private static final String MODULE_NAME = "bulkloader";

    private static final String MODULE_NAME_PREFIX = MODULE_NAME + ".";

    private static final String CACHE_FEATURE_PREFIX = "bulkloader-cache.";

    private static final Location CACHE_HEAD_CONTENTS = new Location(null, CacheStorage.HEAD_DIRECTORY_NAME)
        .append(TemporaryOutputFormat.DEFAULT_FILE_NAME)
        .asPrefix();

    @Override
    public String getId() {
        return MODULE_NAME;
    }

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
        LOG.debug("validating ThunderGate I/O: {}->{}", inputs, outputs);
        boolean valid = true;
        valid &= checkImports(inputs);
        valid &= checkExports(outputs);
        valid &= checkAssignment(inputs, outputs);
        return valid;
    }

    private boolean checkImports(List<InputDescription> inputs) {
        assert inputs != null;
        boolean valid = true;
        for (InputDescription input : inputs) {
            BulkLoadImporterDescription desc = extract(input);
            boolean cacheEnabled = desc.isCacheEnabled();
            if (cacheEnabled) {
                if (ThunderGateCacheSupport.class.isAssignableFrom(desc.getModelType()) == false) {
                    getEnvironment().error(
                            "\"{0}\"のデータモデル型はキャッシュをサポートしていません: {1}",
                            desc.getClass().getName(),
                            desc.getModelType().getName());
                    valid = false;
                }
                if (desc.getWhere() != null && desc.getWhere().trim().isEmpty() == false) {
                    getEnvironment().error(
                            "\"{0}\"は検索条件を指定しているためキャッシュを利用できません: {1}",
                            desc.getClass().getName(),
                            desc.getWhere());
                    valid = false;
                }
                if (desc.getLockType() == BulkLoadImporterDescription.LockType.ROW
                        || desc.getLockType() == BulkLoadImporterDescription.LockType.ROW_OR_SKIP) {
                    getEnvironment().error(
                            "\"{0}\"に指定されたロック方法ではキャッシュを利用できません: {1}",
                            desc.getClass().getName(),
                            desc.getLockType());
                    valid = false;
                }
                if (desc.getDataSize() == DataSize.TINY || desc.getDataSize() == DataSize.SMALL) {
                    getEnvironment().error(
                            "\"{0}\"に指定されたデータサイズではキャッシュを利用できません: {1}",
                            desc.getClass().getName(),
                            desc.getDataSize());
                    valid = false;
                }
            }
        }
        return valid;
    }

    private boolean checkExports(List<OutputDescription> outputs) {
        assert outputs != null;
        boolean valid = true;
        for (OutputDescription output : outputs) {
            BulkLoadExporterDescription desc = extract(output);
            Set<String> columns = Sets.from(desc.getColumnNames());
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
        Set<String> primaryTargets = Sets.create();
        Set<String> secondaryTargets = Sets.create();
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

        Set<String> exportTargets = Sets.create();
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

    @Override
    public SourceInfo getInputInfo(InputDescription description) {
        Set<Location> locations = Collections.singleton(getInputLocation(description));
        return new SourceInfo(locations, TemporaryInputFormat.class);
    }

    @Override
    public List<ExternalIoStage> emitEpilogue(IoContext context) throws IOException {
        if (context.getOutputs().isEmpty()) {
            return Collections.emptyList();
        }

        List<Slot> slots = Lists.create();
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

        return Collections.singletonList(new ExternalIoStage(getId(), stage, context.getOutputContext()));
    }

    private Slot toSlot(Output output) {
        BulkLoadExporterDescription desc = extract(output.getDescription());
        String name = normalize(output.getDescription().getName());
        return new Slot(
                name,
                output.getDescription().getDataType(),
                desc.getPrimaryKeyNames(),
                output.getSources(),
                TemporaryOutputFormat.class);
    }

    private Location getImporterDestination(InputDescription input) {
        assert input != null;
        if (isCacheEnabled(input)) {
            BulkLoadImporterDescription desc = extract(input);
            return computeCacheDirectory(desc.calculateCacheId(), desc.getTargetName(), desc.getTableName());
        } else {
            String name = normalize(input.getName());
            return getEnvironment()
                .getPrologueLocation(MODULE_NAME)
                .append(name);
        }
    }

    private Location getInputLocation(InputDescription input) {
        assert input != null;
        if (isCacheEnabled(input)) {
            return getImporterDestination(input).append(CACHE_HEAD_CONTENTS);
        } else {
            return getImporterDestination(input);
        }
    }

    private Location getOutputLocation(OutputDescription output) {
        assert output != null;
        String name = normalize(output.getName());
        return getEnvironment()
            .getEpilogueLocation(MODULE_NAME)
            .append(name)
            .asPrefix();
    }

    private boolean isCacheEnabled(InputDescription description) {
        assert description != null;
        return extract(description).isCacheEnabled();
    }

    /**
     * Computes and returns the default cache directory path (relative path from working directory).
     * @param cacheId target cache ID
     * @param targetName target profile name
     * @param tableName target table name
     * @return the computed path
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Location computeCacheDirectory(String cacheId, String targetName, String tableName) {
        if (cacheId == null) {
            throw new IllegalArgumentException("cacheId must not be null"); //$NON-NLS-1$
        }
        if (targetName == null) {
            throw new IllegalArgumentException("targetName must not be null"); //$NON-NLS-1$
        }
        if (tableName == null) {
            throw new IllegalArgumentException("tableName must not be null"); //$NON-NLS-1$
        }
        return new Location(null, "thundergate")
            .append("cache")
            .append(targetName)
            .append(tableName)
            .append(cacheId);
    }

    private String normalize(String name) {
        assert name != null;
        assert name.trim().isEmpty() == false;
        String memberName = JavaName.of(name).toMemberName();
        StringBuilder buf = new StringBuilder();
        for (char c : memberName.toCharArray()) {
            if (('A' <= c && c <= 'Z')
                    || ('a' <= c && c <= 'z')
                    || ('0' <= c && c <= '9')) {
                buf.append(c);
            }
        }
        if (buf.length() == 0) {
            buf.append("0");
        }
        return buf.toString();
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
        Map<String, List<Input>> inputs = Maps.create();
        for (Input input : context.getInputs()) {
            String target = extract(input.getDescription()).getTargetName();
            Maps.addToList(inputs, target, input);
        }

        Map<String, List<Output>> outputs = Maps.create();
        for (Output output : context.getOutputs()) {
            String target = extract(output.getDescription()).getTargetName();
            Maps.addToList(outputs, target, output);
        }

        Set<String> targets = Sets.create();
        targets.addAll(inputs.keySet());
        targets.addAll(outputs.keySet());

        Map<String, BulkLoaderScript> results = Maps.create();
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
        List<ImportTable> imports = Lists.create();
        List<ExportTable> exports = Lists.create();
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
                desc.isCacheEnabled() ? desc.calculateCacheId() : null,
                lockType,
                lockedOperation,
                getImporterDestination(input));
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
        Map<String, IoContextBuilder> targets = new TreeMap<String, IoContextBuilder>();
        Map<String, IoContextBuilder> caches = new TreeMap<String, IoContextBuilder>();
        for (Input input : context.getInputs()) {
            BulkLoadImporterDescription desc = extract(input.getDescription());
            String target = desc.getTargetName();
            if (desc.getMode() == Mode.PRIMARY) {
                assert primary == null || primary.equals(target);
                primary = target;
            }
            add(targets, target, input);
            if (isCacheEnabled(input.getDescription())) {
                add(caches, target, input);
            }
        }
        for (Output output : context.getOutputs()) {
            BulkLoadExporterDescription desc = extract(output.getDescription());
            String target = desc.getTargetName();
            assert primary == null || primary.equals(target);
            primary = target;
            add(targets, target, output);
        }
        return new CommandProvider(
                getEnvironment().getBatchId(),
                getEnvironment().getFlowId(),
                primary,
                build(targets),
                build(caches));
    }

    private void add(Map<String, IoContextBuilder> targets, String target, Input input) {
        IoContextBuilder builder = targets.get(target);
        if (builder == null) {
            builder = new IoContextBuilder();
            targets.put(target, builder);
        }
        builder.addInput(input);
    }

    private void add(Map<String, IoContextBuilder> targets, String target, Output output) {
        IoContextBuilder builder = targets.get(target);
        if (builder == null) {
            builder = new IoContextBuilder();
            targets.put(target, builder);
        }
        builder.addOutput(output);
    }

    private Map<String, IoContext> build(Map<String, IoContextBuilder> builders) {
        Map<String, IoContext> results = new TreeMap<String, IoContext>();
        for (Map.Entry<String, IoContextBuilder> entry : builders.entrySet()) {
            results.put(entry.getKey(), entry.getValue().build());
        }
        return results;
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

        private final Map<String, IoContext> targets;

        private final Map<String, IoContext> cacheEntries;

        CommandProvider(
                String batchId,
                String flowId,
                String primary,
                Map<String, IoContext> targets,
                Map<String, IoContext> cacheEntries) {
            assert batchId != null;
            assert flowId != null;
            assert targets != null;
            assert cacheEntries != null;
            assert primary == null || targets.containsKey(primary);
            this.batchId = batchId;
            this.flowId = flowId;
            this.primary = primary;
            this.targets = Maps.from(targets);
            this.cacheEntries = Maps.from(cacheEntries);
        }

        @Override
        public String getName() {
            return MODULE_NAME;
        }

        @Override
        public List<Command> getImportCommand(CommandContext context) {
            List<Command> results = Lists.create();
            if (primary != null) {
                IoContext io = targets.get(primary);
                assert io != null;
                results.add(new Command(
                        String.format("%s%s%04d", MODULE_NAME, '.', results.size()),
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
                        MODULE_NAME_PREFIX + primary,
                        getProfileName(primary),
                        getEnvironment(context),
                        io.getInputContext()));
            }
            for (Map.Entry<String, IoContext> entry : targets.entrySet()) {
                if (primary != null && entry.getKey().equals(primary)) {
                    continue;
                }
                String secondary = entry.getKey();
                results.add(new Command(
                        String.format("%s%s%04d", MODULE_NAME, '.', results.size()),
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
                        getEnvironment(context),
                        entry.getValue().getInputContext()));
            }
            return results;
        }

        @Override
        public List<Command> getExportCommand(CommandContext context) {
            List<Command> results = Lists.create();
            if (primary != null) {
                IoContext io = targets.get(primary);
                assert io != null;
                results.add(new Command(
                        String.format("%s%s%04d", MODULE_NAME, '.', results.size()),
                            Arrays.asList(new String[] {
                                    context.getHomePathPrefix() + CMD_EXPORTER,
                                    primary,
                                    batchId,
                                    flowId,
                                    context.getExecutionId(),
                                    context.getVariableList(),
                            }),
                            MODULE_NAME_PREFIX + primary,
                            getProfileName(primary),
                            getEnvironment(context),
                            io.getOutputContext()));
            }
            return results;
        }

        @Override
        public List<Command> getFinalizeCommand(CommandContext context) {
            List<Command> results = Lists.create();
            if (primary != null) {
                IoContext io = targets.get(primary);
                assert io != null;
                results.add(new Command(
                        String.format("%s%s%04d", MODULE_NAME, '.', results.size()),
                        Arrays.asList(new String[] {
                                context.getHomePathPrefix() + CMD_FINALIZER,
                                primary,
                                batchId,
                                flowId,
                                context.getExecutionId(),
                        }),
                        MODULE_NAME_PREFIX + primary,
                        getProfileName(primary),
                        getEnvironment(context),
                        io));
            }
            for (Map.Entry<String, IoContext> entry : cacheEntries.entrySet()) {
                String cacheUser = entry.getKey();
                results.add(new Command(
                        String.format("%s%s%04d", MODULE_NAME, '.', results.size()),
                        Arrays.asList(new String[] {
                                context.getHomePathPrefix() + CMD_CACHE_FINALIZER,
                                cacheUser,
                                context.getExecutionId(),
                        }),
                        CACHE_FEATURE_PREFIX + cacheUser,
                        getProfileName(cacheUser),
                        getEnvironment(context),
                        entry.getValue()));
            }
            return results;
        }

        private Map<String, String> getEnvironment(CommandContext context) {
            return Collections.emptyMap();
        }
    }
}
