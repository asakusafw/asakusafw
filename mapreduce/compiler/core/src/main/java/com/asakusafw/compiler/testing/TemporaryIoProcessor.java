/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.compiler.testing;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

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
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;

/**
 * Processes temporary I/O.
 * @since 0.2.5
 */
public class TemporaryIoProcessor extends ExternalIoDescriptionProcessor {

    private static final Pattern VALID_OUTPUT_NAME = Pattern.compile("[0-9A-Za-z]+"); //$NON-NLS-1$

    private static final String MODULE_NAME = "temporary"; //$NON-NLS-1$

    @Override
    public String getId() {
        return MODULE_NAME;
    }

    @Override
    public Class<? extends ImporterDescription> getImporterDescriptionType() {
        return TemporaryInputDescription.class;
    }

    @Override
    public Class<? extends ExporterDescription> getExporterDescriptionType() {
        return TemporaryOutputDescription.class;
    }

    @Override
    public boolean validate(List<InputDescription> inputs, List<OutputDescription> outputs) {
        boolean valid = true;
        for (OutputDescription output : outputs) {
            TemporaryOutputDescription desc = extract(output);
            String pathPrefix = desc.getPathPrefix();
            if (pathPrefix == null) {
                valid = false;
                getEnvironment().error(
                        Messages.getString("TemporaryIoProcessor.errorMissingOutputPath"), //$NON-NLS-1$
                        desc.getClass().getName());
            } else {
                Location location = Location.fromPath(pathPrefix, '/');
                if (location.isPrefix() == false) {
                    valid = false;
                    getEnvironment().error(
                            Messages.getString("TemporaryIoProcessor.errorMissingOutputPathSuffix"), //$NON-NLS-1$
                            desc.getClass().getName(),
                            pathPrefix);
                }
                if (location.getParent() == null) {
                    valid = false;
                    getEnvironment().error(
                            Messages.getString("TemporaryIoProcessor.errorMissingOutputParentDirectory"), //$NON-NLS-1$
                            desc.getClass().getName(),
                            pathPrefix);
                }
                if (VALID_OUTPUT_NAME.matcher(location.getName()).matches() == false) {
                    valid = false;
                    getEnvironment().error(
                            Messages.getString("TemporaryIoProcessor.errorInvalidOutputName"), //$NON-NLS-1$
                            desc.getClass().getName(),
                            pathPrefix);
                }
            }
        }
        return valid;
    }

    @Override
    public SourceInfo getInputInfo(InputDescription description) {
        TemporaryInputDescription desc = extract(description);
        Set<Location> locations = new HashSet<>();
        for (String path : desc.getPaths()) {
            locations.add(Location.fromPath(path, '/'));
        }
        return new SourceInfo(locations, TemporaryInputFormat.class);
    }

    @Override
    public List<ExternalIoStage> emitEpilogue(IoContext context) throws IOException {
        Set<String> saw = new HashSet<>();
        List<ExternalIoStage> results = new ArrayList<>();
        for (Map.Entry<Location, List<Slot>> entry : groupByOutputLocation(context).entrySet()) {
            List<Slot> slots = entry.getValue();
            List<ResolvedSlot> resolved = new SlotResolver(getEnvironment()).resolve(slots);
            if (getEnvironment().hasError()) {
                return Collections.emptyList();
            }
            ParallelSortClientEmitter emitter = new ParallelSortClientEmitter(getEnvironment());
            String moduleId = generateModuleName(saw, entry.getKey());
            CompiledStage stage = emitter.emit(
                    moduleId,
                    resolved,
                    entry.getKey());
            results.add(new ExternalIoStage(getId(), stage, context.getOutputContext()));
        }
        return results;
    }

    private String generateModuleName(Set<String> saw, Location target) {
        assert saw != null;
        assert target != null;
        String simpleSuffix = generateSuffix(target);
        String baseModuleId = MessageFormat.format("{0}.{1}", MODULE_NAME, simpleSuffix); //$NON-NLS-1$
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
            return "_"; //$NON-NLS-1$
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
        Map<Location, List<Slot>> results = new TreeMap<>((o1, o2) -> {
            String parentPath1 = (o1.getParent() == null) ? "" : o1.getParent().toPath('/'); //$NON-NLS-1$
            String parentPath2 = (o2.getParent() == null) ? "" : o2.getParent().toPath('/'); //$NON-NLS-1$
            int parentDiff = parentPath1.compareTo(parentPath2);
            if (parentDiff != 0) {
                return (parentDiff > 0) ? +1 : -1;
            }
            return o1.getName().compareTo(o2.getName());
        });
        for (Output output : context.getOutputs()) {
            TemporaryOutputDescription desc = extract(output.getDescription());
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
                Collections.emptyList(),
                output.getSources(),
                TemporaryOutputFormat.class);
    }

    private TemporaryInputDescription extract(InputDescription description) {
        assert description != null;
        ImporterDescription importer = description.getImporterDescription();
        assert importer != null;
        return (TemporaryInputDescription) importer;
    }

    private TemporaryOutputDescription extract(OutputDescription description) {
        assert description != null;
        ExporterDescription exporter = description.getExporterDescription();
        assert exporter != null;
        return (TemporaryOutputDescription) exporter;
    }
}
