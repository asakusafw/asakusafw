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
package com.asakusafw.testdriver.mapreduce;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.asakusafw.compiler.batch.BatchDriver;
import com.asakusafw.compiler.batch.Workflow;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider;
import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.JobFlowClass;
import com.asakusafw.compiler.flow.JobFlowDriver;
import com.asakusafw.compiler.flow.jobflow.JobflowModel;
import com.asakusafw.compiler.testing.BatchInfo;
import com.asakusafw.compiler.testing.DirectBatchCompiler;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.compiler.testing.StageInfo;
import com.asakusafw.testdriver.compiler.ArtifactMirror;
import com.asakusafw.testdriver.compiler.CompilerConstants;
import com.asakusafw.testdriver.compiler.CompilerSession;
import com.asakusafw.testdriver.compiler.FlowPortMap;
import com.asakusafw.testdriver.compiler.JobflowMirror;
import com.asakusafw.testdriver.compiler.TaskMirror;
import com.asakusafw.testdriver.compiler.basic.BasicArtifactMirror;
import com.asakusafw.testdriver.compiler.basic.BasicBatchMirror;
import com.asakusafw.testdriver.compiler.basic.BasicCommandTaskMirror;
import com.asakusafw.testdriver.compiler.basic.BasicHadoopTaskMirror;
import com.asakusafw.testdriver.compiler.basic.BasicJobflowMirror;
import com.asakusafw.testdriver.compiler.basic.BasicPortMirror;
import com.asakusafw.testdriver.compiler.util.DeploymentUtil;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;

class MapReduceCompilerSession implements CompilerSession {

    private final MapReduceCompilerConfiguration configuration;

    MapReduceCompilerSession(MapReduceCompilerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public ArtifactMirror compileBatch(Class<?> dsl) throws IOException {
        if (BatchDescription.class.isAssignableFrom(dsl) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "unsupported batch source: {0}",
                    dsl.getName()));
        }
        BatchDriver driver = BatchDriver.analyze(dsl.asSubclass(BatchDescription.class));
        if (driver.hasError()) {
            throw new IOException(MessageFormat.format(
                    "error occurred while compiling batch class: {0}",
                    driver.getDiagnostics()));
        }

        File workingDirectory = MapReduceCompierUtil.createTemporaryDirectory(configuration.getWorkingDirectory());
        File outputDirectory = new File(workingDirectory, "output"); //$NON-NLS-1$
        File buildDirectory = new File(workingDirectory, "build"); //$NON-NLS-1$
        String runtimeDirectory = CompilerConstants.getRuntimeWorkingDirectory();
        BatchInfo batchInfo = DirectBatchCompiler.compile(
                driver.getDescription(),
                "test.batch", //$NON-NLS-1$
                MapReduceCompierUtil.createWorkingLocation(runtimeDirectory),
                outputDirectory,
                buildDirectory,
                MapReduceCompierUtil.computeEmbeddedLibraries(configuration, dsl),
                configuration.getClassLoader(),
                configuration.getFlowCompilerOptions());
        return toArtifact(batchInfo, outputDirectory);
    }

    @Override
    public ArtifactMirror compileJobflow(Class<?> dsl) throws IOException {
        if (FlowDescription.class.isAssignableFrom(dsl) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "unsupported jobflow source: {0}",
                    dsl.getName()));
        }
        JobFlowDriver driver = JobFlowDriver.analyze(dsl.asSubclass(FlowDescription.class));
        if (driver.hasError()) {
            throw new IOException(MessageFormat.format(
                    "error occurred while compiling jobflow class: {0}",
                    driver.getDiagnostics()));
        }
        JobFlowClass jobFlowClass = driver.getJobFlowClass();
        FlowGraph flowGraph = jobFlowClass.getGraph();
        String batchId = "testing"; //$NON-NLS-1$
        String flowId = jobFlowClass.getConfig().name();
        File workingDirectory = MapReduceCompierUtil.createTemporaryDirectory(configuration.getWorkingDirectory());
        File outputDirectory = new File(workingDirectory, "output"); //$NON-NLS-1$
        File buildDirectory = new File(workingDirectory, "build"); //$NON-NLS-1$
        String runtimeDirectory = CompilerConstants.getRuntimeWorkingDirectory();
        JobflowInfo jobflowInfo = DirectFlowCompiler.compile(
                flowGraph,
                batchId,
                flowId,
                "test.jobflow", //$NON-NLS-1$
                MapReduceCompierUtil.createWorkingLocation(runtimeDirectory),
                buildDirectory,
                MapReduceCompierUtil.computeEmbeddedLibraries(configuration, dsl),
                configuration.getClassLoader(),
                configuration.getFlowCompilerOptions());
        return toArtifact(jobflowInfo, outputDirectory);
    }

    @Override
    public ArtifactMirror compileFlow(FlowDescription flow, FlowPortMap portMap) throws IOException {
        if ((portMap instanceof MapReduceFlowPortMap) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "unsupported port map: {0}",
                    portMap));
        }
        FlowGraph entity = ((MapReduceFlowPortMap) portMap).resolve(flow);
        String batchId = "testing"; //$NON-NLS-1$
        String flowId = "flowpart"; //$NON-NLS-1$
        File workingDirectory = MapReduceCompierUtil.createTemporaryDirectory(configuration.getWorkingDirectory());
        File outputDirectory = new File(workingDirectory, "output"); //$NON-NLS-1$
        File buildDirectory = new File(workingDirectory, "build"); //$NON-NLS-1$
        String runtimeDirectory = CompilerConstants.getRuntimeWorkingDirectory();
        JobflowInfo jobflowInfo = DirectFlowCompiler.compile(
                entity,
                batchId,
                flowId,
                "test.flowpart", //$NON-NLS-1$
                MapReduceCompierUtil.createWorkingLocation(runtimeDirectory),
                buildDirectory,
                MapReduceCompierUtil.computeEmbeddedLibraries(configuration, entity.getDescription()),
                configuration.getClassLoader(),
                configuration.getFlowCompilerOptions());
        return toArtifact(jobflowInfo, outputDirectory);
    }

    private ArtifactMirror toArtifact(BatchInfo info, File outputDirectory) {
        Map<String, BasicJobflowMirror> elements = new LinkedHashMap<>();
        String batchId = null;
        for (JobflowInfo jobflowInfo : info.getJobflows()) {
            if (batchId == null) {
                batchId = jobflowInfo.getJobflow().getBatchId();
            }
            BasicJobflowMirror jobflow = toJobflow(jobflowInfo);
            elements.put(jobflow.getFlowId(), jobflow);
        }
        assert batchId != null;
        for (Graph.Vertex<Workflow.Unit> vertex : info.getWorkflow().getGraph()) {
            BasicJobflowMirror jobflow = elements.get(vertex.getNode().getDescription().getName());
            assert jobflow != null;
            for (Workflow.Unit connected : vertex.getConnected()) {
                BasicJobflowMirror blocker = elements.get(connected.getDescription().getName());
                assert blocker != null;
                jobflow.addBlocker(blocker);
            }
        }
        BasicBatchMirror batch = new BasicBatchMirror(batchId);
        for (JobflowMirror jobflow : elements.values()) {
            batch.addElement(jobflow);
        }
        return new BasicArtifactMirror(batch, outputDirectory);
    }

    private ArtifactMirror toArtifact(JobflowInfo info, File outputDirectory) throws IOException {
        JobflowMirror jobflow = toJobflow(info);
        deploy(info, outputDirectory);
        BasicBatchMirror batch = new BasicBatchMirror(info.getJobflow().getBatchId());
        batch.addElement(jobflow);
        return new BasicArtifactMirror(batch, outputDirectory);
    }

    private BasicJobflowMirror toJobflow(JobflowInfo info) {
        BasicJobflowMirror result = new BasicJobflowMirror(info.getJobflow().getFlowId());
        processInput(info, result);
        processOutput(info, result);
        processMain(info, result);
        CommandContext context = MapReduceCompierUtil.createMockCommandContext();
        for (ExternalIoCommandProvider provider : info.getCommandProviders()) {
            processPhase(info, result, TaskMirror.Phase.INITIALIZE, provider.getInitializeCommand(context));
            processPhase(info, result, TaskMirror.Phase.IMPORT, provider.getImportCommand(context));
            processPhase(info, result, TaskMirror.Phase.EXPORT, provider.getExportCommand(context));
            processPhase(info, result, TaskMirror.Phase.FINALIZE, provider.getFinalizeCommand(context));
        }
        return result;
    }

    private void processPhase(
            JobflowInfo info, BasicJobflowMirror result,
            TaskMirror.Phase phase, List<ExternalIoCommandProvider.Command> commands) {
        TaskMirror last = null;
        for (ExternalIoCommandProvider.Command command : commands) {
            LinkedList<String> tokens = new LinkedList<>(command.getCommandTokens());
            String file = tokens.removeFirst();
            BasicCommandTaskMirror task = new BasicCommandTaskMirror(
                    command.getModuleName(),
                    command.getProfileName(),
                    MapReduceCompierUtil.resolveCommand(file),
                    MapReduceCompierUtil.resolveArguments(tokens));
            if (last != null) {
                task.addBlocker(last);
            }
            result.addTask(phase, task);
            last = task;
        }
    }

    private void processMain(JobflowInfo info, BasicJobflowMirror result) {
        TaskMirror last = null;
        for (StageInfo stage : info.getStages()) {
            BasicHadoopTaskMirror task = new BasicHadoopTaskMirror(stage.getClassName());
            if (last != null) {
                task.addBlocker(last);
            }
            result.addTask(TaskMirror.Phase.MAIN, task);
            last = task;
        }
    }

    private void processInput(JobflowInfo info, BasicJobflowMirror result) {
        for (JobflowModel.Import v : info.getJobflow().getImports()) {
            InputDescription desc = v.getDescription();
            result.addInput(new BasicPortMirror<>(
                    desc.getName(),
                    (Class<?>) desc.getDataType(),
                    desc.getImporterDescription()));
        }
    }

    private void processOutput(JobflowInfo info, BasicJobflowMirror result) {
        for (JobflowModel.Export v : info.getJobflow().getExports()) {
            OutputDescription desc = v.getDescription();
            result.addOutput(new BasicPortMirror<>(
                    desc.getName(),
                    (Class<?>) desc.getDataType(),
                    desc.getExporterDescription()));
        }
    }

    private void deploy(JobflowInfo info, File outputDirectory) throws IOException {
        assert info.getPackageFile().isFile();
        File dest = CompilerConstants.getJobflowLibraryPath(outputDirectory, info.getJobflow().getFlowId());
        File parent = dest.getParentFile();
        if (parent.mkdirs() == false && parent.isDirectory() == false) {
            throw new IOException(MessageFormat.format(
                    "failed to create file: {0}",
                    dest));
        }
        DeploymentUtil.deploy(info.getPackageFile(), dest);
    }

    @Override
    public void close() throws IOException {
        return;
    }
}
