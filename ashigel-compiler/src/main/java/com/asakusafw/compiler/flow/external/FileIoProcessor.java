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
package com.asakusafw.compiler.flow.external;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.hadoop.mapreduce.InputFormat;

import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.epilogue.parallel.ParallelSortClientEmitter;
import com.asakusafw.compiler.flow.epilogue.parallel.ResolvedSlot;
import com.asakusafw.compiler.flow.epilogue.parallel.Slot;
import com.asakusafw.compiler.flow.epilogue.parallel.SlotResolver;
import com.asakusafw.compiler.flow.jobflow.CompiledStage;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.FileExporterDescription;
import com.asakusafw.vocabulary.external.FileImporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;

/**
 * ファイルの入出力を処理する。
 */
public class FileIoProcessor extends ExternalIoDescriptionProcessor {

    private static final Pattern VALID_OUTPUT_NAME = Pattern.compile("[0-9A-Za-z]+");

    private static final String MODULE_NAME = "fileio";

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
        boolean valid = true;
        for (OutputDescription output : outputs) {
            FileExporterDescription desc = extract(output);
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

    @SuppressWarnings("rawtypes")
    @Override
    public Class<? extends InputFormat> getInputFormatType(InputDescription description) {
        return extract(description).getInputFormat();
    }

    @Override
    public Set<Location> getInputLocations(InputDescription description) {
        FileImporterDescription desc = extract(description);
        Set<Location> results = new HashSet<Location>();
        for (String path : desc.getPaths()) {
            results.add(Location.fromPath(path, '/'));
        }
        return results;
    }

    @Override
    public List<CompiledStage> emitEpilogue(IoContext context) throws IOException {
        List<CompiledStage> results = new ArrayList<CompiledStage>();
        for (Map.Entry<Location, List<Slot>> entry : groupByOutputLocation(context).entrySet()) {
            List<Slot> slots = entry.getValue();
            List<ResolvedSlot> resolved = new SlotResolver(getEnvironment()).resolve(slots);
            if (getEnvironment().hasError()) {
                return Collections.emptyList();
            }
            ParallelSortClientEmitter emitter = new ParallelSortClientEmitter(getEnvironment());
            CompiledStage stage = emitter.emit(
                    MODULE_NAME,
                    resolved,
                    entry.getKey());
            results.add(stage);
        }
        return results;
    }

    private Map<Location, List<Slot>> groupByOutputLocation(IoContext context) {
        assert context != null;
        Map<Location, List<Slot>> results = new TreeMap<Location, List<Slot>>(new Comparator<Location>() {
            @Override
            public int compare(Location o1, Location o2) {
                // o1.parent が o2.parent の祖先パスである場合に、o2がo1より手前に来るように並び替える。
                // これは、Hadoopの出力先にディレクトリが既に存在する場合にエラーとするため。
                // 逆もまた然り。

                // 親パスを文字列で比較
                // AがBの祖先パス => A.toString < B.toString
                // と言う関係をもとに、親パスの文字列が異なればその順序の*逆*で整列
                String parentPath1 = (o1.getParent() == null) ? "" : o1.getParent().toPath('/');
                String parentPath2 = (o2.getParent() == null) ? "" : o2.getParent().toPath('/');
                int parentDiff = parentPath1.compareTo(parentPath2);
                if (parentDiff != 0) {
                    // Integer.MIN_VALUE の関係で単純に符号反転はできない
                    return (parentDiff > 0) ? -1 : +1;
                }

                // 親パスまでが同じなので名前のみ比較
                return o1.getName().compareTo(o2.getName());
            }
        });
        for (Output output : context.getOutputs()) {
            FileExporterDescription desc = extract(output.getDescription());
            Location path = Location.fromPath(desc.getPathPrefix(), '/');
            Location parent = path.getParent();
            List<Slot> slots = results.get(parent);
            if (slots == null) {
                slots = new ArrayList<Slot>();
                results.put(parent, slots);
            }
            slots.add(toSlot(output, path.getName()));
        }
        return results;
    }

    private Slot toSlot(Output output, String name) {
        assert output != null;
        assert name != null;
        List<Slot.Input> inputs = new ArrayList<Slot.Input>();
        for (OutputSource source : output.getSources()) {
            Class<? extends InputFormat<?, ?>> format = source.getFormat();
            for (Location location : source.getLocations()) {
                inputs.add(new Slot.Input(location, format));
            }
        }
        return new Slot(
                name,
                output.getDescription().getDataType(),
                Collections.<String>emptyList(),
                inputs,
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
