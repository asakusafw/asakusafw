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
package com.asakusafw.compiler.directio;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.hadoop.mapreduce.InputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.directio.OutputPattern.CompiledOrder;
import com.asakusafw.compiler.directio.OutputPattern.CompiledResourcePattern;
import com.asakusafw.compiler.directio.emitter.NamingClassEmitter;
import com.asakusafw.compiler.directio.emitter.OrderingClassEmitter;
import com.asakusafw.compiler.directio.emitter.Slot;
import com.asakusafw.compiler.directio.emitter.StageEmitter;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor;
import com.asakusafw.compiler.flow.FlowCompilerOptions.GenericOptionValue;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.jobflow.CompiledStage;
import com.asakusafw.compiler.flow.jobflow.ExternalIoStage;
import com.asakusafw.compiler.flow.mapreduce.copy.CopierClientEmitter;
import com.asakusafw.compiler.flow.mapreduce.copy.CopyDescription;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.DirectDataSourceConstants;
import com.asakusafw.runtime.directio.FilePattern;
import com.asakusafw.runtime.directio.FilePattern.PatternElementKind;
import com.asakusafw.runtime.stage.input.BridgeInputFormat;
import com.asakusafw.runtime.stage.input.TemporaryInputFormat;
import com.asakusafw.runtime.stage.output.TemporaryOutputFormat;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.directio.DirectFileInputDescription;
import com.asakusafw.vocabulary.directio.DirectFileOutputDescription;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;

/**
 * Processes {@link DirectFileInputDescription} and {@link DirectFileOutputDescription}.
 * @since 0.2.5
 * @version 0.7.3
 */
public class DirectFileIoProcessor extends ExternalIoDescriptionProcessor {

    static final Logger LOG = LoggerFactory.getLogger(DirectFileIoProcessor.class);

    private static final Set<PatternElementKind> INVALID_BASE_PATH_KIND =
            EnumSet.of(PatternElementKind.WILDCARD, PatternElementKind.SELECTION);

    private static final String PREFIX_OPTION = "directio."; //$NON-NLS-1$

    /**
     * The compiler option name whether filter feature is enabled or not.
     * @since 0.7.3
     */
    public static final String OPTION_FILTER_ENABLED = PREFIX_OPTION + "input.filter.enabled"; //$NON-NLS-1$

    private static final String METHOD_RESOURCE_PATTERN = "getResourcePattern"; //$NON-NLS-1$

    private static final String METHOD_ORDER = "getOrder"; //$NON-NLS-1$

    private static final String MODULE_NAME = "directio"; //$NON-NLS-1$

    private static final Class<? extends InputFormat<?, ?>> INPUT_FORMAT = BridgeInputFormat.class;

    @Override
    public String getId() {
        return MODULE_NAME;
    }

    @Override
    public Class<? extends ImporterDescription> getImporterDescriptionType() {
        return DirectFileInputDescription.class;
    }

    @Override
    public Class<? extends ExporterDescription> getExporterDescriptionType() {
        return DirectFileOutputDescription.class;
    }

    @Override
    public boolean validate(List<InputDescription> inputs, List<OutputDescription> outputs) {
        LOG.debug("Checking Direct I/O vocabularies: batch={}, flow={}", //$NON-NLS-1$
                getEnvironment().getBatchId(),
                getEnvironment().getFlowId());
        boolean valid = true;
        for (InputDescription input : inputs) {
            LOG.debug("Checking Direct I/O input: {}", //$NON-NLS-1$
                    input.getName());
            valid &= validateInput(input);
        }
        for (OutputDescription output : outputs) {
            LOG.debug("Checking Direct I/O output: {}", //$NON-NLS-1$
                    output.getName());
            valid &= validateOutput(output);
        }
        LOG.debug("Checking Direct I/O paths"); //$NON-NLS-1$
        valid &= validatePaths(inputs, outputs);
        return valid;
    }

    private boolean validateInput(InputDescription input) {
        boolean valid = true;
        DirectFileInputDescription desc = extract(input);
        valid &= checkBasePath(
                desc.getClass(), desc.getBasePath(),
                Messages.getString("DirectFileIoProcessor.nameInputBasePath")); //$NON-NLS-1$
        String pattern = desc.getResourcePattern();
        try {
            FilePattern.compile(pattern);
        } catch (IllegalArgumentException e) {
            getEnvironment().error(
                    Messages.getString("DirectFileIoProcessor.errorInvalidInputResourcePattern"), //$NON-NLS-1$
                    e.getMessage(),
                    desc.getClass().getName());
            valid = false;
        }
        valid &= validateFormat(desc.getClass(), desc.getModelType(), desc.getFormat());
        return valid;
    }

    private boolean validateOutput(OutputDescription output) {
        boolean valid = true;
        DirectFileOutputDescription desc = extract(output);
        valid &= checkBasePath(
                desc.getClass(), desc.getBasePath(),
                Messages.getString("DirectFileIoProcessor.nameOutputBasePath")); //$NON-NLS-1$
        DataClass dataType = getEnvironment().getDataClasses().load(desc.getModelType());
        String pattern = desc.getResourcePattern();
        List<CompiledResourcePattern> compiledPattern;
        try {
            compiledPattern = OutputPattern.compileResourcePattern(pattern, dataType);
        } catch (IllegalArgumentException e) {
            getEnvironment().error(
                    Messages.getString("DirectFileIoProcessor.errorInvalidOutputResourcePattern"), //$NON-NLS-1$
                    e.getMessage(),
                    desc.getClass().getName());
            valid = false;
            compiledPattern = Collections.emptyList();
        }

        for (String patternString : desc.getDeletePatterns()) {
            try {
                FilePattern.compile(patternString);
            } catch (IllegalArgumentException e) {
                getEnvironment().error(
                        Messages.getString("DirectFileIoProcessor.errorInvalidDeletePattern"), //$NON-NLS-1$
                        e.getMessage(),
                        desc.getClass().getName(),
                        patternString);
                valid = false;
            }
        }

        List<String> orders = desc.getOrder();
        try {
            OutputPattern.compileOrder(orders, dataType);
        } catch (IllegalArgumentException e) {
            getEnvironment().error(
                    Messages.getString("DirectFileIoProcessor.errorInvalidOrder"), //$NON-NLS-1$
                    e.getMessage(),
                    desc.getClass().getName());
            valid = false;
        }

        Set<OutputPattern.SourceKind> kinds = pickSourceKinds(compiledPattern);
        if (kinds.contains(OutputPattern.SourceKind.ENVIRONMENT)) {
            if (kinds.contains(OutputPattern.SourceKind.PROPERTY)) {
                getEnvironment().error(
                        Messages.getString("DirectFileIoProcessor.errorWildcardOutputWithPlaceholder"), //$NON-NLS-1$
                        pattern,
                        desc.getClass().getName(),
                        METHOD_RESOURCE_PATTERN);
                valid = false;
            }
            if (kinds.contains(OutputPattern.SourceKind.RANDOM)) {
                getEnvironment().error(
                        Messages.getString("DirectFileIoProcessor.errorWildcardOutputWithRandomNumber"), //$NON-NLS-1$
                        pattern,
                        desc.getClass().getName(),
                        METHOD_RESOURCE_PATTERN);
                valid = false;
            }
            if (orders.isEmpty() == false) {
                getEnvironment().error(
                        Messages.getString("DirectFileIoProcessor.errorWildcardOutputWithOrder"), //$NON-NLS-1$
                        pattern,
                        desc.getClass().getName(),
                        METHOD_ORDER);
                valid = false;
            }
        }

        valid &= validateFormat(desc.getClass(), desc.getModelType(), desc.getFormat());
        return valid;
    }

    private boolean checkBasePath(Class<?> theClass, String basePath, String name) {
        boolean valid = true;
        try {
            FilePattern pattern = FilePattern.compile(basePath);
            if (pattern.containsTraverse()) {
                getEnvironment().error(
                        Messages.getString("DirectFileIoProcessor.errorInvalidBasePathTraverse"), //$NON-NLS-1$
                        name,
                        theClass.getName());
                valid = false;
            }
            Set<PatternElementKind> kinds = pattern.getPatternElementKinds();
            for (PatternElementKind kind : kinds) {
                if (INVALID_BASE_PATH_KIND.contains(kind)) {
                    getEnvironment().error(
                            Messages.getString("DirectFileIoProcessor.errorInvalidBasePathToken"), //$NON-NLS-1$
                            name,
                            kind.getSymbol(),
                            theClass.getName());
                    valid = false;
                }
            }
        } catch (IllegalArgumentException e) {
            getEnvironment().error(
                    Messages.getString("DirectFileIoProcessor.errorInvalidBasePathUnknown"), //$NON-NLS-1$
                    name,
                    e.getMessage(),
                    theClass.getName());
            valid = false;
        }
        return valid;
    }

    private boolean validatePaths(List<InputDescription> inputs, List<OutputDescription> outputs) {
        assert inputs != null;
        assert outputs != null;
        boolean valid = true;
        TreeMap<String, InputDescription> inputPaths = new TreeMap<>();
        for (InputDescription input : inputs) {
            DirectFileInputDescription desc = extract(input);
            String path = normalizePath(desc.getBasePath());
            inputPaths.put(path, input);
        }
        TreeMap<String, OutputDescription> outputPaths = new TreeMap<>();
        for (OutputDescription output : outputs) {
            DirectFileOutputDescription desc = extract(output);
            String path = normalizePath(desc.getBasePath());
            for (Map.Entry<String, InputDescription> entry : inputPaths.tailMap(path, true).entrySet()) {
                if (entry.getKey().startsWith(path) == false) {
                    break;
                }
                DirectFileInputDescription other = extract(entry.getValue());
                getEnvironment().error(
                        Messages.getString("DirectFileIoProcessor.errorConflictInputOutputBasePath"), //$NON-NLS-1$
                        desc.getClass().getName(),
                        desc.getBasePath(),
                        other.getClass().getName(),
                        other.getBasePath());
                valid = false;
            }
            if (outputPaths.containsKey(path)) {
                DirectFileOutputDescription other = extract(outputPaths.get(path));
                getEnvironment().error(
                        Messages.getString("DirectFileIoProcessor.errorDuplicateOutputBasePath"), //$NON-NLS-1$
                        desc.getClass().getName(),
                        desc.getBasePath(),
                        other.getClass().getName(),
                        other.getBasePath());
                valid = false;
            } else {
                outputPaths.put(path, output);
            }
        }
        for (Map.Entry<String, OutputDescription> base : outputPaths.entrySet()) {
            String path = base.getKey();
            DirectFileOutputDescription desc = extract(base.getValue());
            for (Map.Entry<String, OutputDescription> entry : outputPaths.tailMap(path, false).entrySet()) {
                if (entry.getKey().startsWith(path) == false) {
                    break;
                }
                DirectFileOutputDescription other = extract(entry.getValue());
                getEnvironment().error(
                        Messages.getString("DirectFileIoProcessor.errorConflictOutputBasePath"), //$NON-NLS-1$
                        desc.getClass().getName(),
                        desc.getBasePath(),
                        other.getClass().getName(),
                        other.getBasePath());
                valid = false;
            }
        }
        return valid;
    }

    private String normalizePath(String path) {
        assert path != null;
        boolean sawSeparator = false;
        StringBuilder buf = new StringBuilder();
        for (int i = 0, n = path.length(); i < n; i++) {
            char c = path.charAt(i);
            if (c == '/') {
                sawSeparator = true;
            } else {
                if (sawSeparator && buf.length() > 0) {
                    buf.append('/');
                }
                sawSeparator = false;
                buf.append(c);
            }
        }
        if (sawSeparator == false) {
            buf.append('/');
        }
        return buf.toString();
    }

    private boolean validateFormat(Class<?> desc, Class<?> model, Class<? extends DataFormat<?>> format) {
        assert desc != null;
        if (format == null) {
            getEnvironment().error(
                    Messages.getString("DirectFileIoProcessor.errorMissingDataFormat"), //$NON-NLS-1$
                    desc.getName());
            return false;
        }
        DataFormat<?> formatObject;
        try {
            formatObject = format.getConstructor().newInstance();
        } catch (Exception e) {
            getEnvironment().error(
                    Messages.getString("DirectFileIoProcessor.errorFailedToInstantiateDataFormat"), //$NON-NLS-1$
                    desc.getName(),
                    format.getName());
            return false;
        }
        if (formatObject.getSupportedType().isAssignableFrom(model) == false) {
            getEnvironment().error(
                    Messages.getString("DirectFileIoProcessor.errorIncompatibleDataFormatType"), //$NON-NLS-1$
                    desc.getName(),
                    model.getName(),
                    format.getName());
            return false;
        }
        return true;
    }

    @Override
    public SourceInfo getInputInfo(InputDescription description) {
        DirectFileInputDescription desc = extract(description);
        if (isCacheTarget(desc)) {
            String outputName = getProcessedInputName(description);
            Location location = getEnvironment().getPrologueLocation(MODULE_NAME).append(outputName).asPrefix();
            return new SourceInfo(Collections.singleton(location), TemporaryInputFormat.class);
        } else {
            return getOriginalInputInfo(description);
        }
    }

    private SourceInfo getOriginalInputInfo(InputDescription description) {
        DirectFileInputDescription desc = extract(description);
        Set<Location> locations = Collections.singleton(
                Location.fromPath("__DIRECTIO__", '/') //$NON-NLS-1$
                .append(description.getName())
                .append(Location.fromPath(desc.getBasePath(), '/')));
        return new SourceInfo(locations, INPUT_FORMAT, getAttributes(desc));
    }

    private Map<String, String> getAttributes(DirectFileInputDescription desc) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(DirectDataSourceConstants.KEY_DATA_CLASS, desc.getModelType().getName());
        attributes.put(DirectDataSourceConstants.KEY_FORMAT_CLASS, desc.getFormat().getName());
        attributes.put(DirectDataSourceConstants.KEY_BASE_PATH, desc.getBasePath());
        attributes.put(DirectDataSourceConstants.KEY_RESOURCE_PATH, desc.getResourcePattern());
        if (desc.getFilter() != null) {
            if (isFilterEnabled()) {
                attributes.put(DirectDataSourceConstants.KEY_FILTER_CLASS, desc.getFilter().getName());
            } else {
                LOG.info(MessageFormat.format(
                        Messages.getString("DirectFileIoProcessor.infoDisabledInputFilter"), //$NON-NLS-1$
                        desc.getClass().getName(),
                        desc.getFilter().getName()));
            }
        }
        attributes.put(DirectDataSourceConstants.KEY_OPTIONAL, String.valueOf(desc.isOptional()));
        return attributes;
    }

    private boolean isFilterEnabled() {
        GenericOptionValue value = getEnvironment().getOptions().getGenericExtraAttribute(
                OPTION_FILTER_ENABLED,
                GenericOptionValue.ENABLED);
        return value != GenericOptionValue.DISABLED;
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
                buf.append(String.format("%02x", (int) c)); //$NON-NLS-1$
            } else {
                buf.append("0u"); //$NON-NLS-1$
                buf.append(String.format("%04x", (int) c)); //$NON-NLS-1$
            }
        }
        return buf.toString();
    }

    @Override
    public List<ExternalIoStage> emitPrologue(IoContext context) throws IOException {
        IoContextBuilder builder = new IoContextBuilder();
        List<CopyDescription> targets = new ArrayList<>();
        for (Input input : context.getInputs()) {
            InputDescription description = input.getDescription();
            DirectFileInputDescription desc = extract(description);
            if (isCacheTarget(desc)) {
                LOG.debug("Input will be copied in prologue: {}", description.getName()); //$NON-NLS-1$
                targets.add(new CopyDescription(
                        getProcessedInputName(description),
                        getEnvironment().getDataClasses().load(description.getDataType()),
                        getOriginalInputInfo(description),
                        TemporaryOutputFormat.class));
                builder.addInput(input);
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
        return Collections.singletonList(new ExternalIoStage(getId(), stage, builder.build()));
    }

    @Override
    public List<ExternalIoStage> emitEpilogue(IoContext context) throws IOException {
        ModelFactory f = getEnvironment().getModelFactory();
        NamingClassEmitter namingEmitter = new NamingClassEmitter(getEnvironment(), MODULE_NAME);
        OrderingClassEmitter orderingEmitter = new OrderingClassEmitter(getEnvironment(), MODULE_NAME);
        List<Slot> slots = new ArrayList<>();
        for (Output output : context.getOutputs()) {
            DirectFileOutputDescription desc = extract(output.getDescription());
            DataClass dataType = getEnvironment().getDataClasses().load(desc.getModelType());
            List<CompiledResourcePattern> namingInfo =
                OutputPattern.compileResourcePattern(desc.getResourcePattern(), dataType);
            Set<OutputPattern.SourceKind> kinds = pickSourceKinds(namingInfo);
            if (kinds.contains(OutputPattern.SourceKind.ENVIRONMENT)) {
                assert kinds.contains(OutputPattern.SourceKind.PROPERTY) == false;
                assert kinds.contains(OutputPattern.SourceKind.RANDOM) == false;
                assert desc.getOrder().isEmpty();
                String outputName = output.getDescription().getName();
                Slot slot = new Slot(
                        outputName,
                        output.getSources(),
                        Models.toName(f, desc.getModelType().getName().replace('$', '.')),
                        desc.getBasePath(),
                        desc.getResourcePattern(),
                        Models.toName(f, desc.getFormat().getName().replace('$', '.')),
                        null,
                        null,
                        desc.getDeletePatterns());
                slots.add(slot);
            } else {
                List<CompiledOrder> orderingInfo = OutputPattern.compileOrder(desc.getOrder(), dataType);
                String outputName = output.getDescription().getName();
                Name naming = namingEmitter.emit(outputName, slots.size() + 1, dataType, namingInfo);
                Name ordering = orderingEmitter.emit(outputName, slots.size() + 1, dataType, orderingInfo);
                Slot slot = new Slot(
                        outputName,
                        output.getSources(),
                        Models.toName(f, desc.getModelType().getName().replace('$', '.')),
                        desc.getBasePath(),
                        desc.getResourcePattern(),
                        Models.toName(f, desc.getFormat().getName().replace('$', '.')),
                        naming,
                        ordering,
                        desc.getDeletePatterns());
                slots.add(slot);
            }
        }
        if (slots.isEmpty()) {
            return Collections.emptyList();
        }
        StageEmitter stageEmitter = new StageEmitter(getEnvironment(), MODULE_NAME);
        CompiledStage result = stageEmitter.emit(slots, getEnvironment().getEpilogueLocation(MODULE_NAME));
        return Collections.singletonList(new ExternalIoStage(getId(), result, context.getOutputContext()));
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

    private Set<OutputPattern.SourceKind> pickSourceKinds(List<CompiledResourcePattern> fragments) {
        assert fragments != null;
        Set<OutputPattern.SourceKind> results = EnumSet.noneOf(OutputPattern.SourceKind.class);
        for (CompiledResourcePattern fragment : fragments) {
            results.add(fragment.getKind());
        }
        return results;
    }

    private DirectFileInputDescription extract(InputDescription description) {
        assert description != null;
        ImporterDescription importer = description.getImporterDescription();
        assert importer != null;
        assert importer instanceof DirectFileInputDescription;
        return (DirectFileInputDescription) importer;
    }

    private DirectFileOutputDescription extract(OutputDescription description) {
        assert description != null;
        ExporterDescription exporter = description.getExporterDescription();
        assert exporter != null;
        assert exporter instanceof DirectFileOutputDescription;
        return (DirectFileOutputDescription) exporter;
    }
}
