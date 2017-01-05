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
package com.asakusafw.compiler.yaess;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.AbstractWorkflowProcessor;
import com.asakusafw.compiler.batch.WorkDescriptionProcessor;
import com.asakusafw.compiler.batch.Workflow;
import com.asakusafw.compiler.batch.WorkflowProcessor;
import com.asakusafw.compiler.batch.processor.JobFlowWorkDescriptionProcessor;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider.Command;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.jobflow.CompiledStage;
import com.asakusafw.compiler.flow.jobflow.JobflowModel;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.vocabulary.batch.JobFlowWorkDescription;
import com.asakusafw.yaess.core.BatchScript;
import com.asakusafw.yaess.core.CommandScript;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.ExecutionScript;
import com.asakusafw.yaess.core.FlowScript;
import com.asakusafw.yaess.core.HadoopScript;

/**
 * An implementation of {@link WorkflowProcessor} for YAESS.
 * @since 0.2.3
 */
public class YaessWorkflowProcessor extends AbstractWorkflowProcessor {

    static final Logger LOG = LoggerFactory.getLogger(YaessWorkflowProcessor.class);

    /**
     * The output path.
     */
    public static final String PATH = "etc/yaess-script.properties"; //$NON-NLS-1$

    /**
     * Computes and returns the path to the YAESS script output.
     *
     * @param outputDir
     *            compilation output path
     * @return the JSON output path
     * @throws IllegalArgumentException
     *             if some parameters were {@code null}
     */
    public static File getScriptOutput(File outputDir) {
        Precondition.checkMustNotBeNull(outputDir, "outputDir"); //$NON-NLS-1$
        return new File(outputDir, PATH);
    }

    @Override
    public Collection<Class<? extends WorkDescriptionProcessor<?>>> getDescriptionProcessors() {
        List<Class<? extends WorkDescriptionProcessor<?>>> results = new ArrayList<>();
        results.add(JobFlowWorkDescriptionProcessor.class);
        return results;
    }

    @Override
    public void process(Workflow workflow) throws IOException {
        LOG.debug("Anayzing Workflow Structure for YAESS"); //$NON-NLS-1$
        List<FlowScript> scripts = processJobflowList(workflow);

        LOG.debug("Building YAESS Batch Script"); //$NON-NLS-1$
        Properties properties = new Properties();
        properties.setProperty(BatchScript.KEY_ID, getBatchId());
        properties.setProperty(BatchScript.KEY_VERSION, BatchScript.VERSION);
        properties.setProperty(BatchScript.KEY_VERIFICATION_CODE, getEnvironment().getBuildId());

        for (FlowScript script : scripts) {
            LOG.trace("Building YAESS Flow Script: {}", script.getId()); //$NON-NLS-1$
            script.storeTo(properties);
        }

        LOG.debug("Exporting YAESS Batch Script"); //$NON-NLS-1$
        try (OutputStream output = getEnvironment().openResource(PATH)) {
            properties.store(output, MessageFormat.format(
                    "YAESS Batch Script for \"{0}\", version {1}", //$NON-NLS-1$
                    getBatchId(),
                    BatchScript.VERSION));
        }
        LOG.debug("Exported YAESS Batch Script"); //$NON-NLS-1$
    }

    private List<FlowScript> processJobflowList(Workflow workflow) {
        assert workflow != null;
        List<FlowScript> jobflows = new ArrayList<>();
        for (Graph.Vertex<Workflow.Unit> vertex : sortJobflow(workflow.getGraph())) {
            FlowScript jobflow = processJobflow(vertex.getNode(), vertex.getConnected());
            jobflows.add(jobflow);
        }
        return jobflows;
    }

    private List<Graph.Vertex<JobflowModel.Stage>> sortStage(Iterable<Graph.Vertex<JobflowModel.Stage>> vertices) {
        assert vertices != null;
        List<Graph.Vertex<JobflowModel.Stage>> results = new ArrayList<>();
        for (Graph.Vertex<JobflowModel.Stage> vertex : vertices) {
            results.add(vertex);
        }
        Collections.sort(results, (o1, o2) -> Integer.compare(o1.getNode().getNumber(), o2.getNode().getNumber()));
        return results;
    }

    private List<Graph.Vertex<Workflow.Unit>> sortJobflow(Iterable<Graph.Vertex<Workflow.Unit>> vertices) {
        assert vertices != null;
        List<Graph.Vertex<Workflow.Unit>> results = new ArrayList<>();
        for (Graph.Vertex<Workflow.Unit> vertex : vertices) {
            results.add(vertex);
        }
        Collections.sort(results, (o1, o2) ->
                o1.getNode().getDescription().getName().compareTo(o2.getNode().getDescription().getName()));
        return results;
    }

    private FlowScript processJobflow(Workflow.Unit unit, Set<Workflow.Unit> blockers) {
        assert unit != null;
        assert blockers != null;
        JobflowModel model = toJobflowModel(unit);
        CommandContext context = new CommandContext(
                ExecutionScript.PLACEHOLDER_HOME + '/',
                ExecutionScript.PLACEHOLDER_EXECUTION_ID,
                ExecutionScript.PLACEHOLDER_ARGUMENTS);
        Map<ExecutionPhase, List<ExecutionScript>> scripts = new HashMap<>();
        scripts.put(ExecutionPhase.INITIALIZE, processInitializers(model, context));
        scripts.put(ExecutionPhase.IMPORT, processImporters(model, context));
        scripts.put(ExecutionPhase.PROLOGUE, processPrologues(model, context));
        scripts.put(ExecutionPhase.MAIN, processMain(model, context));
        scripts.put(ExecutionPhase.EPILOGUE, processEpilogues(model, context));
        scripts.put(ExecutionPhase.EXPORT, processExporters(model, context));
        scripts.put(ExecutionPhase.FINALIZE, processFinalizers(model, context));
        return new FlowScript(
                model.getFlowId(), toUnitNames(blockers),
                scripts, EnumSet.allOf(ExecutionScript.Kind.class));
    }

    private List<ExecutionScript> processInitializers(JobflowModel model, CommandContext context) {
        assert model != null;
        assert context != null;
        List<ExecutionScript> results = new ArrayList<>();
        for (ExternalIoCommandProvider provider : model.getCompiled().getCommandProviders()) {
            List<Command> commands = provider.getInitializeCommand(context);
            List<ExecutionScript> scripts = processCommands(provider, commands);
            results.addAll(scripts);
        }
        return results;
    }

    private List<ExecutionScript> processImporters(JobflowModel model, CommandContext context) {
        assert model != null;
        assert context != null;
        List<ExecutionScript> results = new ArrayList<>();
        for (ExternalIoCommandProvider provider : model.getCompiled().getCommandProviders()) {
            List<ExecutionScript> scripts = processCommands(provider, provider.getImportCommand(context));
            results.addAll(scripts);
        }
        return results;
    }

    private List<ExecutionScript> processExporters(JobflowModel model, CommandContext context) {
        assert model != null;
        assert context != null;
        List<ExecutionScript> results = new ArrayList<>();
        for (ExternalIoCommandProvider provider : model.getCompiled().getCommandProviders()) {
            List<ExecutionScript> scripts = processCommands(provider, provider.getExportCommand(context));
            results.addAll(scripts);
        }
        return results;
    }

    private List<ExecutionScript> processFinalizers(JobflowModel model, CommandContext context) {
        assert model != null;
        assert context != null;
        List<ExecutionScript> results = new ArrayList<>();
        for (ExternalIoCommandProvider provider : model.getCompiled().getCommandProviders()) {
            List<ExecutionScript> scripts = processCommands(provider, provider.getFinalizeCommand(context));
            results.addAll(scripts);
        }
        return results;
    }

    private List<ExecutionScript> processCommands(ExternalIoCommandProvider provider, List<Command> commands) {
        assert provider != null;
        assert commands != null;
        List<ExecutionScript> scripts = new ArrayList<>();
        for (Command command : commands) {
            String profile = command.getProfileName();
            scripts.add(new CommandScript(
                    command.getId(),
                    Collections.emptySet(),
                    profile == null ? CommandScript.DEFAULT_PROFILE_NAME : profile,
                    command.getModuleName(),
                    command.getCommandTokens(),
                    command.getEnvironment()));
        }
        return scripts;
    }

    private List<ExecutionScript> processPrologues(JobflowModel model, CommandContext context) {
        assert model != null;
        assert context != null;
        return processStages(model.getCompiled().getPrologueStages());
    }

    private List<ExecutionScript> processEpilogues(JobflowModel model, CommandContext context) {
        assert model != null;
        assert context != null;
        return processStages(model.getCompiled().getEpilogueStages());
    }

    private List<ExecutionScript> processMain(JobflowModel model, CommandContext context) {
        assert model != null;
        assert context != null;
        List<ExecutionScript> results = new ArrayList<>();
        for (Graph.Vertex<JobflowModel.Stage> stage : sortStage(model.getDependencyGraph())) {
            results.add(processStage(stage.getNode().getCompiled(), stage.getConnected()));
        }
        return results;
    }

    private List<ExecutionScript> processStages(List<CompiledStage> stages) {
        assert stages != null;
        List<ExecutionScript> results = new ArrayList<>();
        for (CompiledStage stage : stages) {
            results.add(processStage(stage, Collections.emptySet()));
        }
        return results;
    }

    private ExecutionScript processStage(CompiledStage stage, Set<JobflowModel.Stage> blockers) {
        assert stage != null;
        assert blockers != null;
        String stageId = stage.getStageId();
        Set<String> blockerIds = toStageNames(blockers);
        String className = stage.getQualifiedName().toNameString();
        Map<String, String> props = Collections.emptyMap();
        Map<String, String> envs = Collections.emptyMap();
        return new HadoopScript(stageId, blockerIds, className, props, envs);
    }

    private JobflowModel toJobflowModel(Workflow.Unit unit) {
        assert unit != null;
        assert unit.getDescription() instanceof JobFlowWorkDescription;
        return (JobflowModel) unit.getProcessed();
    }

    private Set<String> toUnitNames(Set<Workflow.Unit> blockers) {
        assert blockers != null;
        Set<String> names = new HashSet<>();
        for (Workflow.Unit unit : blockers) {
            names.add(unit.getDescription().getName());
        }
        return names;
    }

    private Set<String> toStageNames(Set<JobflowModel.Stage> blockers) {
        assert blockers != null;
        Set<String> names = new HashSet<>();
        for (JobflowModel.Stage stage : blockers) {
            names.add(stage.getCompiled().getStageId());
        }
        return names;
    }

    private String getBatchId() {
        return getEnvironment().getConfiguration().getBatchId();
    }
}
