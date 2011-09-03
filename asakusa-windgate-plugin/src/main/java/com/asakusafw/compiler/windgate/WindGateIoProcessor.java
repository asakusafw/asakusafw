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
package com.asakusafw.compiler.windgate;

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.epilogue.parallel.ParallelSortClientEmitter;
import com.asakusafw.compiler.flow.epilogue.parallel.ResolvedSlot;
import com.asakusafw.compiler.flow.epilogue.parallel.Slot;
import com.asakusafw.compiler.flow.epilogue.parallel.SlotResolver;
import com.asakusafw.compiler.flow.jobflow.CompiledStage;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;
import com.asakusafw.vocabulary.windgate.Constants;
import com.asakusafw.vocabulary.windgate.WindGateExporterDescription;
import com.asakusafw.vocabulary.windgate.WindGateImporterDescription;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.vocabulary.FileProcess;

/**
 * Processes WindGate vocabularies.
 * @since 0.2.3
 */
public class WindGateIoProcessor extends ExternalIoDescriptionProcessor {

    static final Logger LOG = LoggerFactory.getLogger(WindGateIoProcessor.class);

    /**
     * The module name of WindGate.
     */
    public static final String MODULE_NAME = "windgate";

    private static final String CMD_PROCESS = "windgate/bin/process.sh";

    private static final String CMD_ABORT = "windgate/bin/abort.sh";

    private static final String OPT_IMPORT = "import";

    private static final String OPT_EXPORT = "export";

    private static final String PATTERN_SCRIPT_LOCATION = "META-INF/windgate/{0}-{1}.properties";

    static final String OPT_BEGIN = "begin";

    static final String OPT_END = "end";

    static final String OPT_ONESHOT = "oneshot";

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
        LOG.debug("Validating WindGate Vocabularies (batch={}, flow={})",
                getEnvironment().getBatchId(),
                getEnvironment().getFlowId());
        boolean valid = true;
        for (InputDescription input : inputs) {
            WindGateImporterDescription desc = extract(input);
            try {
                if (desc.getDriverScript() == null) {
                    throw new IllegalStateException(MessageFormat.format(
                            "Driver script is not defined: {0}",
                            desc.getClass().getName()));
                }
            } catch (IllegalStateException e) {
                getEnvironment().error(
                        "Importer description \"{0}\" is invalid (batch={1}, flow={2}): {3}",
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
                if (desc.getDriverScript() == null) {
                    throw new IllegalStateException(MessageFormat.format(
                            "Driver script is not defined: {0}",
                            desc.getClass().getName()));
                }
            } catch (IllegalStateException e) {
                getEnvironment().error(
                        "Exporter description \"{0}\" is invalid (batch={1}, flow={2}): {3}",
                        output.getName(),
                        getEnvironment().getBatchId(),
                        getEnvironment().getFlowId(),
                        e.getMessage());
                valid = false;
            }
        }
        return valid;
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
        LOG.debug("Emitting epilogue stages for WindGate (batch={}, flow={})",
                getEnvironment().getBatchId(),
                getEnvironment().getFlowId());

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
                Collections.<String>emptyList(),
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
        return JavaName.of(name).toMemberName();
    }

    @Override
    public void emitPackage(IoContext context) throws IOException {
        LOG.debug("Emitting process scripts for WindGate (batch={}, flow={})",
                getEnvironment().getBatchId(),
                getEnvironment().getFlowId());
        Map<String, GateScript> importers = toImporterScripts(context.getInputs());
        Map<String, GateScript> exporters = toExporterScripts(context.getOutputs());

        for (Map.Entry<String, GateScript> entry : importers.entrySet()) {
            String script = getScriptLocation(true, entry.getKey());
            LOG.debug("Emitting importer script {} (batch={}, flow={})", new Object[] {
                    script,
                    getEnvironment().getBatchId(),
                    getEnvironment().getFlowId(),
            });
            emitScript(script, entry.getValue());
        }
        for (Map.Entry<String, GateScript> entry : exporters.entrySet()) {
            String script = getScriptLocation(false, entry.getKey());
            LOG.debug("Emitting importer script {} (batch={}, flow={})", new Object[] {
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
        Map<String, List<ProcessScript<?>>> processes = new HashMap<String, List<ProcessScript<?>>>();
        for (Input input : inputs) {
            String profileName = extract(input.getDescription()).getProfileName();
            List<ProcessScript<?>> list = processes.get(profileName);
            if (list == null) {
                list = new ArrayList<ProcessScript<?>>();
                processes.put(profileName, list);
            }
            ProcessScript<?> process = toProcessScript(input);
            list.add(process);
        }
        return toGateScripts(processes);
    }

    private Map<String, GateScript> toExporterScripts(Collection<Output> outputs) {
        assert outputs != null;
        Map<String, List<ProcessScript<?>>> processes = new HashMap<String, List<ProcessScript<?>>>();
        for (Output output : outputs) {
            String profileName = extract(output.getDescription()).getProfileName();
            List<ProcessScript<?>> list = processes.get(profileName);
            if (list == null) {
                list = new ArrayList<ProcessScript<?>>();
                processes.put(profileName, list);
            }
            ProcessScript<?> process = toProcessScript(output);
            list.add(process);
        }
        return toGateScripts(processes);
    }

    private ProcessScript<?> toProcessScript(Input input) {
        assert input != null;
        WindGateImporterDescription desc = extract(input.getDescription());
        String location = getInputLocation(input.getDescription()).toPath('/');
        DriverScript drain = new DriverScript(
                Constants.HADOOP_FILE_RESOURCE_NAME,
                Collections.singletonMap(FileProcess.FILE.key(), location));
        return createProcessScript(
                input.getDescription().getName(),
                desc.getModelType(),
                desc.getDriverScript(),
                drain);
    }

    private ProcessScript<?> toProcessScript(Output output) {
        assert output != null;
        WindGateExporterDescription desc = extract(output.getDescription());
        String location = getOutputLocation(output.getDescription()).toPath('/');
        DriverScript source = new DriverScript(
                Constants.HADOOP_FILE_RESOURCE_NAME,
                Collections.singletonMap(FileProcess.FILE.key(), location));
        return createProcessScript(
                output.getDescription().getName(),
                desc.getModelType(),
                source,
                desc.getDriverScript());
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
        return new ProcessScript<T>(
                profileName,
                Constants.DEFAULT_PROCESS_NAME,
                modelType,
                source,
                drain);
    }

    private Map<String, GateScript> toGateScripts(Map<String, List<ProcessScript<?>>> processes) {
        assert processes != null;
        Map<String, GateScript> results = new TreeMap<String, GateScript>();
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

        OutputStream output = getEnvironment().openResource(null, path);
        try {
            properties.store(output, getEnvironment().getTargetId());
        } finally {
            output.close();
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
        Set<String> importers = new HashSet<String>();
        for (Input input : context.getInputs()) {
            WindGateImporterDescription desc = extract(input.getDescription());
            importers.add(desc.getProfileName());
        }
        Set<String> exporters = new HashSet<String>();
        for (Output output : context.getOutputs()) {
            WindGateExporterDescription desc = extract(output.getDescription());
            exporters.add(desc.getProfileName());
        }
        return new CommandProvider(
                getEnvironment().getBatchId(),
                getEnvironment().getFlowId(),
                importers,
                exporters);
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

        private static final long serialVersionUID = -3916072136449360144L;

        private final String batchId;

        private final String flowId;

        private final Set<String> importers;

        private final Set<String> exporters;

        CommandProvider(String batchId, String flowId, Set<String> importers, Set<String> exporters) {
            assert batchId != null;
            assert flowId != null;
            assert importers != null;
            assert exporters != null;
            this.batchId = batchId;
            this.flowId = flowId;
            this.importers = new TreeSet<String>(importers);
            this.exporters = new TreeSet<String>(exporters);
        }

        @Override
        public String getName() {
            return MODULE_NAME;
        }

        @Override
        public List<Command> getImportCommand(CommandContext context) {
            List<Command> results = new ArrayList<Command>();
            for (String profile : importers) {
                List<String> commands = new ArrayList<String>();
                commands.add(context.getHomePathPrefix() + CMD_PROCESS);
                commands.add(profile);
                if (exporters.contains(profile)) {
                    commands.add(OPT_BEGIN);
                } else {
                    commands.add(OPT_ONESHOT);
                }
                commands.add("classpath:" + getScriptLocation(true, profile));
                commands.add(batchId);
                commands.add(flowId);
                commands.add(context.getExecutionId());
                commands.add(context.getVariableList());
                results.add(new Command(
                        commands,
                        resolveModuleName(profile),
                        resolveProfileName(profile),
                        getEnvironment(context)));
            }
            return results;
        }

        @Override
        public List<Command> getExportCommand(CommandContext context) {
            List<Command> results = new ArrayList<Command>();
            for (String profile : exporters) {
                List<String> commands = new ArrayList<String>();
                commands.add(context.getHomePathPrefix() + CMD_PROCESS);
                commands.add(profile);
                if (importers.contains(profile)) {
                    commands.add(OPT_END);
                } else {
                    commands.add(OPT_ONESHOT);
                }
                commands.add("classpath:" + getScriptLocation(false, profile));
                commands.add(batchId);
                commands.add(flowId);
                commands.add(context.getExecutionId());
                commands.add(context.getVariableList());
                results.add(new Command(
                        commands,
                        resolveModuleName(profile),
                        resolveProfileName(profile),
                        getEnvironment(context)));
            }
            return results;
        }

        @Override
        public List<Command> getFinalizeCommand(CommandContext context) {
            SortedSet<String> union = new TreeSet<String>();
            union.addAll(importers);
            union.addAll(exporters);
            List<Command> results = new ArrayList<Command>();
            for (String profile : union) {
                List<String> commands = new ArrayList<String>();
                commands.add(context.getHomePathPrefix() + CMD_ABORT);
                commands.add(profile);
                commands.add(context.getExecutionId());
                results.add(new Command(
                        commands,
                        resolveModuleName(profile),
                        resolveProfileName(profile),
                        getEnvironment(context)));
            }
            return results;
        }

        private Map<String, String> getEnvironment(CommandContext context) {
            return Collections.emptyMap();
        }
    }
}
