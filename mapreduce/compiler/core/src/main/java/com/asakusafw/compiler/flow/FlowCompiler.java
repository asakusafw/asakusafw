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
package com.asakusafw.compiler.flow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.external.ExternalIoAnalyzer;
import com.asakusafw.compiler.flow.jobflow.JobflowCompiler;
import com.asakusafw.compiler.flow.jobflow.JobflowModel;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.compiler.flow.plan.StagePlanner;
import com.asakusafw.compiler.flow.plan.StagePlanner.Diagnostic;
import com.asakusafw.compiler.flow.stage.StageCompiler;
import com.asakusafw.compiler.flow.stage.StageModel;
import com.asakusafw.compiler.flow.visualizer.FlowVisualizer;
import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * Compiles {@link FlowGraph}.
 */
public class FlowCompiler {

    static final Logger LOG = LoggerFactory.getLogger(FlowCompiler.class);

    private final FlowCompilerConfiguration configuration;

    private final FlowCompilingEnvironment environment;

    /**
     * Creates a new instance.
     * @param configuration the current configuration
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public FlowCompiler(FlowCompilerConfiguration configuration) {
        Precondition.checkMustNotBeNull(configuration, "configuration"); //$NON-NLS-1$
        this.configuration = configuration;
        this.environment = createEnvironment();
    }

    /**
     * Returns the target flow ID.
     * @return the target flow ID
     */
    public String getTargetFlowId() {
        return configuration.getFlowId();
    }

    /**
     * Compiles the target flow graph and returns its jobflow model.
     * @param graph the target flow graph
     * @return the compiled model
     * @throws IOException if error was occurred while creating artifacts
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @see #buildSources(File)
     */
    public JobflowModel compile(FlowGraph graph) throws IOException {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        validate(graph);
        StageGraph stageGraph = plan(graph);
        visualize(graph, stageGraph);
        List<StageModel> stages = compileStages(stageGraph);
        JobflowModel jobflow = compileJobflow(stageGraph, stages);
        addApplicationInfo();
        return jobflow;
    }

    /**
     * Builds the generated Java source files, and create a new archive file from them.
     * @param output the target file
     * @throws IOException if error was occurred while creating artifacts
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void buildSources(File output) throws IOException {
        Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
        try (OutputStream stream = open(output)) {
            buildSources(stream);
        }
    }

    /**
     * Collects the generated Java source files, and create a new archive file from them.
     * @param output the target file
     * @throws IOException if error was occurred while creating artifacts
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void collectSources(File output) throws IOException {
        Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
        try (OutputStream stream = open(output)) {
            collectSources(stream);
        }
    }

    private OutputStream open(File file) throws IOException {
        assert file != null;
        if (file.exists() == false) {
            File parent = file.getParentFile();
            assert parent != null;
            if (parent.isDirectory() == false && parent.mkdirs() == false) {
                throw new IOException(MessageFormat.format(
                        Messages.getString("FlowCompiler.errorFailedToCreateParentDirectory"), //$NON-NLS-1$
                        file));
            }
        }
        return new FileOutputStream(file);
    }

    /**
     * Builds the generated Java source files, and create a new archive file from them.
     * @param output the target output stream
     * @throws IOException if error was occurred while creating artifacts
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void buildSources(OutputStream output) throws IOException {
        Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
        configuration.getPackager().build(output);
    }

    /**
     * Collects the generated Java source files, and create a new archive file from them.
     * @param output the target output stream
     * @throws IOException if error was occurred while creating artifacts
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void collectSources(OutputStream output) throws IOException {
        Precondition.checkMustNotBeNull(output, "output"); //$NON-NLS-1$
        configuration.getPackager().packageSources(output);
    }

    private FlowCompilingEnvironment createEnvironment() {
        assert configuration != null;
        FlowCompilingEnvironment result = new FlowCompilingEnvironment(configuration);
        result.bless();
        return result;
    }

    private void validate(FlowGraph graph) throws IOException {
        assert graph != null;
        ExternalIoAnalyzer analyzer = new ExternalIoAnalyzer(environment);
        if (analyzer.validate(graph) == false) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("FlowCompiler.errorInvalidFlow"), //$NON-NLS-1$
                    environment.getErrorMessage()));
        }
    }

    private StageGraph plan(FlowGraph flowGraph) throws IOException {
        assert flowGraph != null;
        StagePlanner planner = new StagePlanner(
                configuration.getGraphRewriters().getRewriters(),
                configuration.getOptions());
        StageGraph plan = planner.plan(flowGraph);
        if (plan == null) {
            for (Diagnostic diagnostic : planner.getDiagnostics()) {
                LOG.error(diagnostic.toString());
            }
            throw new IOException(Messages.getString("FlowCompiler.errorFailedToGenerateExecutionPlan")); //$NON-NLS-1$
        }
        return plan;
    }

    private void visualize(FlowGraph flowGraph, StageGraph stageGraph) throws IOException {
        assert flowGraph != null;
        assert stageGraph != null;
        FlowVisualizer visualizer = new FlowVisualizer(environment);
        visualizer.visualize(flowGraph);
        visualizer.visualize(stageGraph);
        for (StageBlock stage : stageGraph.getStages()) {
            visualizer.visualize(stage);
        }
    }

    private void addApplicationInfo() throws IOException {
        Properties properties = new Properties();
        properties.put(RuntimeContext.KEY_BATCH_ID, environment.getBatchId());
        properties.put(RuntimeContext.KEY_FLOW_ID, environment.getFlowId());
        properties.put(RuntimeContext.KEY_BUILD_ID, environment.getBuildId());
        properties.put(RuntimeContext.KEY_BUILD_DATE,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())); //$NON-NLS-1$
        properties.put(RuntimeContext.KEY_RUNTIME_VERSION, RuntimeContext.getRuntimeVersion());

        try (OutputStream output = environment.openResource(null, RuntimeContext.PATH_APPLICATION_INFO)) {
            properties.store(output, "Created by Asakusa DSL compiler"); //$NON-NLS-1$
        }
    }

    private List<StageModel> compileStages(
            StageGraph stageGraph) throws IOException {
        assert stageGraph != null;
        StageCompiler compiler = new StageCompiler(environment);
        return compiler.compile(stageGraph);
    }

    private JobflowModel compileJobflow(
            StageGraph stageGraph,
            List<StageModel> model) throws IOException {
        assert stageGraph != null;
        assert model != null;
        JobflowCompiler compiler = new JobflowCompiler(environment);
        return compiler.compile(stageGraph, model);
    }
}
