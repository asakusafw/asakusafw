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
package com.asakusafw.compiler.fileio;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor;
import com.asakusafw.compiler.flow.FlowCompilerOptions.GenericOptionValue;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.jobflow.CompiledStage;
import com.asakusafw.compiler.flow.jobflow.ExternalIoStage;
import com.asakusafw.compiler.flow.mapreduce.copy.CopierClientEmitter;
import com.asakusafw.compiler.flow.mapreduce.copy.CopyDescription;
import com.asakusafw.compiler.flow.mapreduce.parallel.ParallelSortClientEmitter;
import com.asakusafw.compiler.flow.mapreduce.parallel.ResolvedSlot;
import com.asakusafw.compiler.flow.mapreduce.parallel.Slot;
import com.asakusafw.compiler.flow.mapreduce.parallel.SlotResolver;
import com.asakusafw.runtime.stage.input.TemporaryInputFormat;
import com.asakusafw.runtime.stage.output.TemporaryOutputFormat;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.FileExporterDescription;
import com.asakusafw.vocabulary.external.FileImporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;

/**
 * ファイルの入出力を処理する。
 * @since 0.1.0
 */
public class HadoopFileIoProcessor extends ExternalIoDescriptionProcessor {

    private static final Pattern VALID_OUTPUT_NAME = Pattern.compile("[0-9A-Za-z]+");

    private static final String MODULE_NAME = "fileio";

    /**
     * The option name for {@link FileExporterDescription} is enabled.
     */
    public static final String OPTION_EXPORTER_ENABLED = "MAPREDUCE-370";

    private static final GenericOptionValue DEFAULT_EXPORTER_ENABLED = GenericOptionValue.AUTO;

    @Override
    public String getId() {
        return MODULE_NAME;
    }

    @Override
    public Class<? extends ImporterDescription> getImporterDescriptionType() {
        return FileImporterDescription.class;
    }

    @Override
    public Class<? extends ExporterDescription> getExporterDescriptionType() {
        return FileExporterDescription.class;
    }

    @Override
    public boolean validate(List<InputDescription> inputs, List<OutputDescription> outputs) {
        boolean valid = validateOutputs(outputs);
        return valid;
    }

    private boolean validateOutputs(List<OutputDescription> outputs) {
        assert outputs != null;
        boolean valid = true;
        GenericOptionValue exporterEnabled = getEnvironment().getOptions().getGenericExtraAttribute(
                OPTION_EXPORTER_ENABLED,
                DEFAULT_EXPORTER_ENABLED);
        if (exporterEnabled == GenericOptionValue.INVALID) {
            getEnvironment().error(
                    "Invalid valud for compiler option \"{0}\" ({1}), this must be {2}",
                    getEnvironment().getOptions().getExtraAttributeKeyName(OPTION_EXPORTER_ENABLED),
                    getEnvironment().getOptions().getExtraAttribute(OPTION_EXPORTER_ENABLED),
                    GenericOptionValue.ENABLED.getSymbol() + "|" + GenericOptionValue.DISABLED.getSymbol());
            exporterEnabled = DEFAULT_EXPORTER_ENABLED;
            valid = false;
        }
        boolean mr370applied = checkClassExists("org.apache.hadoop.mapreduce.lib.output.MultipleOutputs");
        for (OutputDescription output : outputs) {
            FileExporterDescription desc = extract(output);
            if (exporterEnabled == GenericOptionValue.DISABLED) {
                valid = false;
                getEnvironment().error(
                        "出力{0}を利用するにはコンパイルオプション\"{1}={2}\"の指定が必要です",
                        desc.getClass().getName(),
                        getEnvironment().getOptions().getExtraAttributeKeyName(OPTION_EXPORTER_ENABLED),
                        GenericOptionValue.ENABLED.getSymbol());
            } else if (mr370applied == false && exporterEnabled == GenericOptionValue.AUTO) {
                valid = false;
                getEnvironment().error(
                        "現在のディストリビューションは{1}に対応していません。"
                        + "別のディストリビューションを利用するか、{2}に置き換えてください (出力{0})。",
                        desc.getClass().getName(),
                        FileExporterDescription.class.getSimpleName(),
                        "DirectFileOutputDescription (directio)");
            }
            String pathPrefix = desc.getPathPrefix();
            if (pathPrefix == null) {
                valid = false;
                getEnvironment().error(
                        "{0}のパスが指定されていません",
                        desc.getClass().getName());
            } else {
                Location location = Location.fromPath(pathPrefix, '/');
                if (location.isPrefix() == false) {
                    valid = false;
                    getEnvironment().error(
                            "{0}はパスの接尾辞(-*)でなければなりません: {1}",
                            desc.getClass().getName(),
                            pathPrefix);
                }
                if (location.getParent() == null) {
                    valid = false;
                    getEnvironment().error(
                            "{0}には最低ひとつのディレクトリの指定が必要です: {1}",
                            desc.getClass().getName(),
                            pathPrefix);
                }
                if (VALID_OUTPUT_NAME.matcher(location.getName()).matches() == false) {
                    valid = false;
                    getEnvironment().error(
                            "{0}のファイル名(末尾のセグメント)は英数字のみ利用できます: {1}",
                            desc.getClass().getName(),
                            pathPrefix);
                }
            }
        }
        return valid;
    }
    private boolean checkClassExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public SourceInfo getInputInfo(InputDescription description) {
        FileImporterDescription desc = extract(description);
        if (isCacheTarget(desc)) {
            String outputName = getProcessedInputName(description);
            Location location = getEnvironment().getPrologueLocation(MODULE_NAME).append(outputName).asPrefix();
            return new SourceInfo(Collections.singleton(location), TemporaryInputFormat.class);
        } else {
            return getOrifinalInputInfo(desc);
        }
    }

    private SourceInfo getOrifinalInputInfo(FileImporterDescription desc) {
        assert desc != null;
        Set<Location> locations = Sets.create();
        for (String path : desc.getPaths()) {
            locations.add(Location.fromPath(path, '/'));
        }
        return new SourceInfo(locations, desc.getInputFormat());
    }

    private boolean isCacheTarget(ImporterDescription desc) {
        assert desc != null;
        switch (desc.getDataSize()) {
        case TINY:
            return getEnvironment().getOptions().isHashJoinForTiny();
        case SMALL:
            return getEnvironment().getOptions().isHashJoinForSmall();
        default:
            return false;
        }
    }

    private String getProcessedInputName(InputDescription description) {
        assert description != null;
        StringBuilder buf = new StringBuilder();
        for (char c : description.getName().toCharArray()) {
            // 0 as escape character
            if ('1' <= c && c <= '9' || 'A' <= c && c <= 'Z' || 'a' <= c && c <= 'z') {
                buf.append(c);
            } else if (c <= 0xff) {
                buf.append('0');
                buf.append(String.format("%02x", (int) c));
            } else {
                buf.append("0u");
                buf.append(String.format("%04x", (int) c));
            }
        }
        return buf.toString();
    }

    @Override
    public List<ExternalIoStage> emitPrologue(IoContext context) throws IOException {
        List<CopyDescription> targets = Lists.create();
        for (Input input : context.getInputs()) {
            InputDescription description = input.getDescription();
            FileImporterDescription desc = extract(description);
            if (isCacheTarget(desc)) {
                targets.add(new CopyDescription(
                        getProcessedInputName(description),
                        getEnvironment().getDataClasses().load(description.getDataType()),
                        getOrifinalInputInfo(desc),
                        TemporaryOutputFormat.class));
            }
        }
        if (targets.isEmpty()) {
            return Collections.emptyList();
        }
        CopierClientEmitter emitter = new CopierClientEmitter(getEnvironment());
        CompiledStage stage = emitter.emitPrologue(
                MODULE_NAME,
                targets,
                getEnvironment().getPrologueLocation(MODULE_NAME));
        return Collections.singletonList(new ExternalIoStage(getId(), stage, context.getInputContext()));
    }

    @Override
    public List<ExternalIoStage> emitEpilogue(IoContext context) throws IOException {
        Set<String> saw = Sets.create();
        List<ExternalIoStage> results = Lists.create();
        for (Map.Entry<Location, List<Slot>> entry : groupByOutputLocation(context).entrySet()) {
            List<Slot> slots = entry.getValue();
            List<ResolvedSlot> resolved = new SlotResolver(getEnvironment()).resolve(slots);
            if (getEnvironment().hasError()) {
                return Collections.emptyList();
            }
            ParallelSortClientEmitter emitter = new ParallelSortClientEmitter(getEnvironment());
            String moduleId = generateModuleName(saw, entry.getKey());
            CompiledStage stage = emitter.emit(moduleId, resolved, entry.getKey());
            // TODO not sure
            results.add(new ExternalIoStage(getId(), stage, context.getOutputContext()));
        }
        return results;
    }

    private String generateModuleName(Set<String> saw, Location target) {
        assert saw != null;
        assert target != null;
        String simpleSuffix = generateSuffix(target);
        String baseModuleId = MessageFormat.format("{0}.{1}", MODULE_NAME, simpleSuffix);
        if (saw.contains(baseModuleId) == false) {
            saw.add(baseModuleId);
            return baseModuleId;
        }
        int index = 1;
        while (true) {
            String moduleIdCandidate = baseModuleId + index;
            if (saw.contains(moduleIdCandidate) == false) {
                saw.add(moduleIdCandidate);
                return moduleIdCandidate;
            }
            index++;
        }
    }

    private String generateSuffix(Location target) {
        assert target != null;
        String name = target.getName();
        if (name.isEmpty()) {
            return "_";
        }
        StringBuilder buf = new StringBuilder();
        if (Character.isJavaIdentifierStart(name.charAt(0)) == false) {
            buf.append('_');
        }
        for (char c : name.toCharArray()) {
            if (Character.isJavaIdentifierPart(c)) {
                buf.append(c);
            }
        }
        assert buf.length() >= 1;
        return buf.toString();
    }

    private Map<Location, List<Slot>> groupByOutputLocation(IoContext context) {
        assert context != null;
        Map<Location, List<Slot>> results = new TreeMap<Location, List<Slot>>(new Comparator<Location>() {
            @Override
            public int compare(Location o1, Location o2) {
                // o1.parent が o2.parent の祖先パスである場合に、o1がo2より手前に来るように並び替える。
                // これは、Hadoopの出力先にディレクトリが既に存在する場合にエラーとするため。
                // 逆もまた然り。

                // 親パスを文字列で比較
                // AがBの祖先パス => A.toString < B.toString
                // と言う関係をもとに、親パスの文字列が異なればその順序で整列
                String parentPath1 = (o1.getParent() == null) ? "" : o1.getParent().toPath('/');
                String parentPath2 = (o2.getParent() == null) ? "" : o2.getParent().toPath('/');
                int parentDiff = parentPath1.compareTo(parentPath2);
                if (parentDiff != 0) {
                    return (parentDiff > 0) ? +1 : -1;
                }

                // 親パスまでが同じなので名前のみ比較
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Output output : context.getOutputs()) {
            FileExporterDescription desc = extract(output.getDescription());
            Location path = Location.fromPath(desc.getPathPrefix(), '/');
            Location parent = path.getParent();
            Maps.addToList(results, parent, toSlot(output, path.getName()));
        }
        return results;
    }

    private Slot toSlot(Output output, String name) {
        assert output != null;
        assert name != null;
        return new Slot(
                name,
                output.getDescription().getDataType(),
                Collections.<String>emptyList(),
                output.getSources(),
                extract(output.getDescription()).getOutputFormat());
    }

    private FileImporterDescription extract(InputDescription description) {
        assert description != null;
        ImporterDescription importer = description.getImporterDescription();
        assert importer != null;
        assert importer instanceof FileImporterDescription;
        return (FileImporterDescription) importer;
    }

    private FileExporterDescription extract(OutputDescription description) {
        assert description != null;
        ExporterDescription exporter = description.getExporterDescription();
        assert exporter != null;
        assert exporter instanceof FileExporterDescription;
        return (FileExporterDescription) exporter;
    }
}
