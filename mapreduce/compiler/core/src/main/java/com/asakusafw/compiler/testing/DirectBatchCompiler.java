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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.BatchCompiler;
import com.asakusafw.compiler.batch.BatchCompilerConfiguration;
import com.asakusafw.compiler.batch.BatchDriver;
import com.asakusafw.compiler.batch.Workflow;
import com.asakusafw.compiler.batch.processor.JobFlowWorkDescriptionProcessor;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.jobflow.CompiledStage;
import com.asakusafw.compiler.flow.jobflow.JobflowModel;
import com.asakusafw.compiler.repository.SpiDataClassRepository;
import com.asakusafw.compiler.repository.SpiExternalIoDescriptionProcessorRepository;
import com.asakusafw.compiler.repository.SpiFlowElementProcessorRepository;
import com.asakusafw.compiler.repository.SpiFlowGraphRewriterRepository;
import com.asakusafw.compiler.repository.SpiWorkflowProcessorRepository;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.vocabulary.batch.JobFlowWorkDescription;

/**
 * Compiles batch classes and generates batch applications.
 */
public final class DirectBatchCompiler {

    static final Logger LOG = LoggerFactory.getLogger(DirectBatchCompiler.class);

    /**
     * Compiles the target batch class and returns its structural information.
     * @param batchClass the target batch class
     * @param basePackageName the base package name of generated Java source files
     * @param clusterWorkingDirectory the runtime working directory
     * @param outputDirectory the output directory
     * @param localWorkingDirectory the working directory for compiler
     * @param extraResources the extra resources for embedding contents into each jobflow package file
     * @param serviceClassLoader the class loader for loading compiler services
     * @param flowCompilerOptions the compiler options for flow DSL
     * @return the compile results
     * @throws IOException if failed to compile
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static BatchInfo compile(
            Class<? extends BatchDescription> batchClass,
            String basePackageName,
            Location clusterWorkingDirectory,
            File outputDirectory,
            File localWorkingDirectory,
            List<File> extraResources,
            ClassLoader serviceClassLoader,
            FlowCompilerOptions flowCompilerOptions) throws IOException {
        Precondition.checkMustNotBeNull(batchClass, "batchClass"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(clusterWorkingDirectory, "clusterWorkingDirectory"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputDirectory, "outputDirectory"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(localWorkingDirectory, "localWorkingDirectory"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(extraResources, "extraResources"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(serviceClassLoader, "serviceClassLoader"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(flowCompilerOptions, "flowCompilerOptions"); //$NON-NLS-1$

        if (localWorkingDirectory.exists()) {
            delete(localWorkingDirectory);
        }
        if (outputDirectory.exists()) {
            delete(outputDirectory);
        }
        BatchDriver driver = BatchDriver.analyze(batchClass);
        if (driver.hasError()) {
            throw new IOException(driver.getDiagnostics().toString());
        }

        String batchId = driver.getBatchClass().getConfig().name();
        BatchCompilerConfiguration config = createConfig(
                batchId,
                basePackageName,
                clusterWorkingDirectory,
                outputDirectory,
                localWorkingDirectory,
                extraResources,
                serviceClassLoader,
                flowCompilerOptions);

        BatchCompiler compiler = new BatchCompiler(config);
        Workflow workflow = compiler.compile(driver.getBatchClass().getDescription());
        return toInfo(workflow, outputDirectory);
    }

    /**
     * Returns the structural information of the batch application from a compiled workflow.
     * @param workflow the target workflow
     * @param outputDirectory the output directory
     * @return the structural information
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static BatchInfo toInfo(Workflow workflow, File outputDirectory) {
        Precondition.checkMustNotBeNull(workflow, "workflow"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(outputDirectory, "outputDirectory"); //$NON-NLS-1$
        List<JobflowInfo> jobflows = new ArrayList<>();
        for (Workflow.Unit unit : Graphs.sortPostOrder(workflow.getGraph())) {
            JobflowInfo jobflow = toJobflow(unit, outputDirectory);
            if (jobflow != null) {
                jobflows.add(jobflow);
            }
        }
        return new BatchInfo(workflow, outputDirectory, jobflows);
    }

    private static boolean delete(File target) {
        assert target != null;
        boolean success = true;
        if (target.isDirectory()) {
            for (File child : list(target)) {
                success &= delete(child);
            }
        }
        success &= target.delete();
        return success;
    }

    private static List<File> list(File file) {
        return Optional.ofNullable(file.listFiles())
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }

    /**
     * Creates a compiler configuration object.
     * @param batchId the target batch ID
     * @param basePackageName the base package name of generated Java source files
     * @param clusterWorkingLocation the runtime working directory
     * @param outputDirectory the output directory
     * @param localWorkingDirectory the working directory for compiler
     * @param extraResources the extra resources for embedding contents into each jobflow package file
     * @param serviceClassLoader the class loader for loading compiler services
     * @param flowCompilerOptions the compiler options for flow DSL
     * @return the created object
     * @throws IOException if failed to extract the configuration
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static BatchCompilerConfiguration createConfig(
            String batchId,
            String basePackageName,
            Location clusterWorkingLocation,
            File outputDirectory,
            File localWorkingDirectory,
            List<File> extraResources,
            ClassLoader serviceClassLoader,
            FlowCompilerOptions flowCompilerOptions) throws IOException {
        assert batchId != null;
        assert basePackageName != null;
        assert clusterWorkingLocation != null;
        assert outputDirectory != null;
        assert localWorkingDirectory != null;
        assert extraResources != null;
        assert serviceClassLoader != null;
        assert flowCompilerOptions != null;
        BatchCompilerConfiguration config = new BatchCompilerConfiguration();
        config.setBatchId(batchId);
        config.setDataClasses(new SpiDataClassRepository());
        config.setExternals(new SpiExternalIoDescriptionProcessorRepository());
        config.setGraphRewriters(new SpiFlowGraphRewriterRepository());
        config.setFactory(Models.getModelFactory());
        config.setFlowElements(new SpiFlowElementProcessorRepository());
        config.setLinkingResources(DirectFlowCompiler.createRepositories(serviceClassLoader, extraResources));
        config.setOutputDirectory(outputDirectory);
        config.setRootLocation(clusterWorkingLocation);
        config.setRootPackageName(basePackageName);
        config.setWorkflows(new SpiWorkflowProcessorRepository());
        config.setServiceClassLoader(serviceClassLoader);
        config.setWorkingDirectory(localWorkingDirectory);
        config.setFlowCompilerOptions(flowCompilerOptions);
        return config;
    }

    private static JobflowInfo toJobflow(
            Workflow.Unit unit,
            File outputDirectory) {
        assert unit != null;
        assert outputDirectory != null;
        if ((unit.getDescription() instanceof JobFlowWorkDescription) == false) {
            return null;
        }
        JobflowModel model = (JobflowModel) unit.getProcessed();
        String flowId = model.getFlowId();
        return new JobflowInfo(
                model,
                JobFlowWorkDescriptionProcessor.getPackageLocation(outputDirectory, flowId),
                JobFlowWorkDescriptionProcessor.getSourceLocation(outputDirectory, flowId),
                toStagePlan(model));
    }

    private static List<StageInfo> toStagePlan(JobflowModel jobflow) {
        assert jobflow != null;
        List<StageInfo> results = new ArrayList<>();
        for (CompiledStage compiled : jobflow.getCompiled().getPrologueStages()) {
            results.add(toInfo(compiled));
        }
        Graph<JobflowModel.Stage> depenedencies = jobflow.getDependencyGraph();
        for (JobflowModel.Stage stage : Graphs.sortPostOrder(depenedencies)) {
            results.add(toInfo(stage.getCompiled()));
        }
        for (CompiledStage compiled : jobflow.getCompiled().getEpilogueStages()) {
            results.add(toInfo(compiled));
        }
        return results;
    }

    private static StageInfo toInfo(CompiledStage stage) {
        assert stage != null;
        String className = stage.getQualifiedName().toNameString();
        return new StageInfo(className);
    }

    private DirectBatchCompiler() {
        return;
    }
}
