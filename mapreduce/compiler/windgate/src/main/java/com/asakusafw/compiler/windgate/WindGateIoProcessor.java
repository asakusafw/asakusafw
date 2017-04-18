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
package com.asakusafw.compiler.windgate;

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;
import com.asakusafw.vocabulary.windgate.Constants;
import com.asakusafw.vocabulary.windgate.WindGateExporterDescription;
import com.asakusafw.vocabulary.windgate.WindGateImporterDescription;
import com.asakusafw.vocabulary.windgate.WindGateProcessDescription;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.vocabulary.FileProcess;

/**
 * Processes WindGate vocabularies.
 * @since 0.2.2
 */
public class WindGateIoProcessor extends ExternalIoDescriptionProcessor {

    static final Logger LOG = LoggerFactory.getLogger(WindGateIoProcessor.class);

    /**
     * The module name of WindGate.
     */
    public static final String MODULE_NAME = Constants.MODULE_NAME;

    private static final String CMD_PROCESS = "windgate/bin/process.sh"; //$NON-NLS-1$

    private static final String CMD_FINALIZE = "windgate/bin/finalize.sh"; //$NON-NLS-1$

    private static final String OPT_IMPORT = "import"; //$NON-NLS-1$

    private static final String OPT_EXPORT = "export"; //$NON-NLS-1$

    private static final String PATTERN_SCRIPT_LOCATION = "META-INF/windgate/{0}-{1}.properties"; //$NON-NLS-1$

    static final String OPT_BEGIN = "begin"; //$NON-NLS-1$

    static final String OPT_END = "end"; //$NON-NLS-1$

    static final String OPT_ONESHOT = "oneshot"; //$NON-NLS-1$

    @Override
    public String getId() {
        return MODULE_NAME;
    }

    @Override
    public Class<? extends ImporterDescription> getImporterDescriptionType() {
        return WindGateImporterDescription.class;
    }

    @Override
    public Class<? extends ExporterDescription> getExporterDescriptionType() {
        return WindGateExporterDescription.class;
    }

    @Override
    public boolean validate(List<InputDescription> inputs, List<OutputDescription> outputs) {
        LOG.debug("Validating WindGate Vocabularies (batch={}, flow={})", //$NON-NLS-1$
                getEnvironment().getBatchId(),
                getEnvironment().getFlowId());
        boolean valid = true;
        for (InputDescription input : inputs) {
            WindGateImporterDescription desc = extract(input);
            try {
                validateCommonProperties(desc);
                if (desc.getDriverScript() == null) {
                    throw new IllegalStateException(MessageFormat.format(
                            Messages.getString("WindGateIoProcessor.errorMissingDriverScript"), //$NON-NLS-1$
                            desc.getClass().getName()));
                }
            } catch (IllegalStateException e) {
                getEnvironment().error(
                        Messages.getString("WindGateIoProcessor.errorInvalidImporter"), //$NON-NLS-1$
                        input.getName(),
                        getEnvironment().getBatchId(),
                        getEnvironment().getFlowId(),
                        e.getMessage());
                valid = false;
            }
        }
        for (OutputDescription output : outputs) {
            WindGateExporterDescription desc = extract(output);
            try {
                validateCommonProperties(desc);
                if (desc.getDriverScript() == null) {
                    throw new IllegalStateException(MessageFormat.format(
                            Messages.getString("WindGateIoProcessor.errorMissingDriverScript"), //$NON-NLS-1$
                            desc.getClass().getName()));
                }
            } catch (IllegalStateException e) {
                getEnvironment().error(
                        Messages.getString("WindGateIoProcessor.errorInvalidExporter"), //$NON-NLS-1$
                        output.getName(),
                        getEnvironment().getBatchId(),
                        getEnvironment().getFlowId(),
                        e.getMessage());
                valid = false;
            }
        }
        return valid;
    }

    private void validateCommonProperties(WindGateProcessDescription desc) {
        String profileName = desc.getProfileName();
        if (profileName == null) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("WindGateIoProcessor.errorNullProperty"), //$NON-NLS-1$
                    desc.getClass().getName(),
                    "getProfileName()")); //$NON-NLS-1$
        }
        if (profileName.isEmpty()) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("WindGateIoProcessor.errorEmptyProperty"), //$NON-NLS-1$
                    desc.getClass().getName(),
                    "getProfileName()")); //$NON-NLS-1$
        }
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
        LOG.debug("Emitting epilogue stages for WindGate (batch={}, flow={})", //$NON-NLS-1$
                getEnvironment().getBatchId(),
                getEnvironment().getFlowId());

        List<Slot> slots = new ArrayList<>();
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
        assert output != null;
        String name = normalize(output.getDescription().getName());
        return new Slot(
                name,
                output.getDescription().getDataType(),
                Collections.emptyList(),
                output.getSources(),
                TemporaryOutputFormat.class);
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
            buf.append("0"); //$NON-NLS-1$
        }
        return buf.toString();
    }

    @Override
    public void emitPackage(IoContext context) throws IOException {
        LOG.debug("Emitting process scripts for WindGate (batch={}, flow={})", //$NON-NLS-1$
                getEnvironment().getBatchId(),
                getEnvironment().getFlowId());
        Map<String, GateScript> importers = toImporterScripts(context.getInputs());
        Map<String, GateScript> exporters = toExporterScripts(context.getOutputs());

        for (Map.Entry<String, GateScript> entry : importers.entrySet()) {
            String script = getScriptLocation(true, entry.getKey());
            LOG.debug("Emitting importer script {} (batch={}, flow={})", new Object[] { //$NON-NLS-1$
                    script,
                    getEnvironment().getBatchId(),
                    getEnvironment().getFlowId(),
            });
            emitScript(script, entry.getValue());
        }
        for (Map.Entry<String, GateScript> entry : exporters.entrySet()) {
            String script = getScriptLocation(false, entry.getKey());
            LOG.debug("Emitting importer script {} (batch={}, flow={})", new Object[] { //$NON-NLS-1$
                    script,
                    getEnvironment().getBatchId(),
                    getEnvironment().getFlowId(),
            });
            emitScript(script, entry.getValue());
        }
    }

    static String getScriptLocation(boolean importer, String profileName) {
        assert profileName != null;
        return MessageFormat.format(
                PATTERN_SCRIPT_LOCATION,
                importer ? OPT_IMPORT : OPT_EXPORT,
                profileName);
    }

    private Map<String, GateScript> toImporterScripts(Collection<Input> inputs) {
        assert inputs != null;
        Map<String, List<ProcessScript<?>>> processes = new HashMap<>();
        for (Input input : inputs) {
            String profileName = extract(input.getDescription()).getProfileName();
            ProcessScript<?> process = toProcessScript(input);
            Maps.addToList(processes, profileName, process);
        }
        return toGateScripts(processes);
    }

    private Map<String, GateScript> toExporterScripts(Collection<Output> outputs) {
        assert outputs != null;
        Map<String, List<ProcessScript<?>>> processes = new HashMap<>();
        for (Output output : outputs) {
            String profileName = extract(output.getDescription()).getProfileName();
            ProcessScript<?> process = toProcessScript(output);
            Maps.addToList(processes, profileName, process);
        }
        return toGateScripts(processes);
    }

    private ProcessScript<?> toProcessScript(Input input) {
        assert input != null;
        WindGateImporterDescription desc = extract(input.getDescription());
        String location = getInputLocation(input.getDescription()).toPath('/');
        DriverScript source = desc.getDriverScript();
        DriverScript drain = new DriverScript(
                Constants.HADOOP_FILE_RESOURCE_NAME,
                Collections.singletonMap(FileProcess.FILE.key(), location));
        return createProcessScript(
                input.getDescription().getName(),
                desc.getModelType(),
                source,
                drain);
    }

    private ProcessScript<?> toProcessScript(Output output) {
        assert output != null;
        WindGateExporterDescription desc = extract(output.getDescription());
        String location = getOutputLocation(output.getDescription()).toPath('/');
        DriverScript source = new DriverScript(
                Constants.HADOOP_FILE_RESOURCE_NAME,
                Collections.singletonMap(FileProcess.FILE.key(), location));
        DriverScript drain = desc.getDriverScript();
        return createProcessScript(
                output.getDescription().getName(),
                desc.getModelType(),
                source,
                drain);
    }

    private <T> ProcessScript<T> createProcessScript(
            String profileName,
            Class<T> modelType,
            DriverScript source,
            DriverScript drain) {
        assert profileName != null;
        assert modelType != null;
        assert source != null;
        assert drain != null;
        return new ProcessScript<>(
                profileName,
                Constants.DEFAULT_PROCESS_NAME,
                modelType,
                source,
                drain);
    }

    private Map<String, GateScript> toGateScripts(Map<String, List<ProcessScript<?>>> processes) {
        assert processes != null;
        Map<String, GateScript> results = new TreeMap<>();
        for (Map.Entry<String, List<ProcessScript<?>>> entry : processes.entrySet()) {
            results.put(entry.getKey(), new GateScript(entry.getKey(), entry.getValue()));
        }
        return results;
    }

    private void emitScript(String path, GateScript script) throws IOException {
        assert path != null;
        assert script != null;
        Properties properties = new Properties();
        script.storeTo(properties);
        try (OutputStream output = getEnvironment().openResource(null, path)) {
            properties.store(output, getEnvironment().getTargetId());
        }
    }

    private WindGateImporterDescription extract(InputDescription description) {
        assert description != null;
        ImporterDescription importer = description.getImporterDescription();
        assert importer != null;
        assert importer instanceof WindGateImporterDescription;
        return (WindGateImporterDescription) importer;
    }

    private WindGateExporterDescription extract(OutputDescription description) {
        assert description != null;
        ExporterDescription exporter = description.getExporterDescription();
        assert exporter != null;
        assert exporter instanceof WindGateExporterDescription;
        return (WindGateExporterDescription) exporter;
    }

    @Override
    public ExternalIoCommandProvider createCommandProvider(IoContext context) {
        Map<String, IoContextBuilder> importers = new HashMap<>();
        for (Input input : context.getInputs()) {
            WindGateImporterDescription desc = extract(input.getDescription());
            add(importers, desc.getProfileName(), input);
        }
        Map<String, IoContextBuilder> exporters = new HashMap<>();
        for (Output output : context.getOutputs()) {
            WindGateExporterDescription desc = extract(output.getDescription());
            add(exporters, desc.getProfileName(), output);
        }
        return new CommandProvider(
                getEnvironment().getBatchId(),
                getEnvironment().getFlowId(),
                build(importers),
                build(exporters));
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
        Map<String, IoContext> results = new TreeMap<>();
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

    static String resolveProfileName(String profileName) {
        assert profileName != null;
        return profileName;
    }

    static String resolveModuleName(String profileName) {
        assert profileName != null;
        return MessageFormat.format("{0}.{1}", MODULE_NAME, profileName); //$NON-NLS-1$
    }

    /**
     * Provides lifecycle commands for WindGate.
     */
    public static class CommandProvider extends ExternalIoCommandProvider {

        private final String batchId;

        private final String flowId;

        private final Map<String, IoContext> importers;

        private final Map<String, IoContext> exporters;

        CommandProvider(
                String batchId, String flowId, Map<String, IoContext> importers, Map<String, IoContext> exporters) {
            assert batchId != null;
            assert flowId != null;
            assert importers != null;
            assert exporters != null;
            this.batchId = batchId;
            this.flowId = flowId;
            this.importers = new TreeMap<>(importers);
            this.exporters = new TreeMap<>(exporters);
        }

        @Override
        public String getName() {
            return MODULE_NAME;
        }

        @Override
        public List<Command> getImportCommand(CommandContext context) {
            List<Command> results = new ArrayList<>();
            for (Map.Entry<String, IoContext> entry : importers.entrySet()) {
                String profile = entry.getKey();
                List<String> commands = new ArrayList<>();
                commands.add(context.getHomePathPrefix() + CMD_PROCESS);
                commands.add(profile);
                if (exporters.containsKey(profile)) {
                    commands.add(OPT_BEGIN);
                } else {
                    commands.add(OPT_ONESHOT);
                }
                commands.add(String.format("classpath:%s", getScriptLocation(true, profile))); //$NON-NLS-1$
                commands.add(batchId);
                commands.add(flowId);
                commands.add(context.getExecutionId());
                commands.add(context.getVariableList());
                results.add(new Command(
                        String.format("%s%s%04d", MODULE_NAME, '.', results.size()), //$NON-NLS-1$
                        commands,
                        resolveModuleName(profile),
                        resolveProfileName(profile),
                        getEnvironment(context),
                        entry.getValue().getInputContext()));
            }
            return results;
        }

        @Override
        public List<Command> getExportCommand(CommandContext context) {
            List<Command> results = new ArrayList<>();
            for (Map.Entry<String, IoContext> entry : exporters.entrySet()) {
                String profile = entry.getKey();
                List<String> commands = new ArrayList<>();
                commands.add(context.getHomePathPrefix() + CMD_PROCESS);
                commands.add(profile);
                if (importers.containsKey(profile)) {
                    commands.add(OPT_END);
                } else {
                    commands.add(OPT_ONESHOT);
                }
                commands.add(String.format("classpath:%s", getScriptLocation(false, profile))); //$NON-NLS-1$
                commands.add(batchId);
                commands.add(flowId);
                commands.add(context.getExecutionId());
                commands.add(context.getVariableList());
                results.add(new Command(
                        String.format("%s%s%04d", MODULE_NAME, '.', results.size()), //$NON-NLS-1$
                        commands,
                        resolveModuleName(profile),
                        resolveProfileName(profile),
                        getEnvironment(context),
                        entry.getValue().getOutputContext()));
            }
            return results;
        }

        @Override
        public List<Command> getFinalizeCommand(CommandContext context) {
            Map<String, IoContextBuilder> union = new TreeMap<>();
            for (Map.Entry<String, IoContext> entry : importers.entrySet()) {
                add(union, entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, IoContext> entry : exporters.entrySet()) {
                add(union, entry.getKey(), entry.getValue());
            }
            List<Command> results = new ArrayList<>();
            for (Map.Entry<String, IoContextBuilder> entry : union.entrySet()) {
                String profile = entry.getKey();
                List<String> commands = new ArrayList<>();
                commands.add(context.getHomePathPrefix() + CMD_FINALIZE);
                commands.add(profile);
                commands.add(batchId);
                commands.add(flowId);
                commands.add(context.getExecutionId());
                results.add(new Command(
                        String.format("%s%s%04d", MODULE_NAME, '.', results.size()), //$NON-NLS-1$
                        commands,
                        resolveModuleName(profile),
                        resolveProfileName(profile),
                        getEnvironment(context),
                        entry.getValue().build()));
            }
            return results;
        }

        private void add(Map<String, IoContextBuilder> union, String key, IoContext value) {
            IoContextBuilder builder = union.get(key);
            if (builder == null) {
                builder = new IoContextBuilder();
                union.put(key, builder);
            }
            for (Input input : value.getInputs()) {
                builder.addInput(input);
            }
            for (Output output : value.getOutputs()) {
                builder.addOutput(output);
            }
        }

        private Map<String, String> getEnvironment(CommandContext context) {
            return Collections.emptyMap();
        }
    }
}
