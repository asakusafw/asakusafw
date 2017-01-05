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
package com.asakusafw.compiler.batch;

import java.io.File;
import java.util.List;

import com.asakusafw.compiler.batch.WorkflowProcessor.Repository;
import com.asakusafw.compiler.flow.DataClassRepository;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.FlowElementProcessor;
import com.asakusafw.compiler.flow.FlowGraphRewriter;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.utils.java.model.syntax.ModelFactory;

/**
 * Represents compiler settings for compiling individual jobflows.
 */
public class BatchCompilerConfiguration {

    private ModelFactory factory;

    private FlowElementProcessor.Repository flowElements;

    private DataClassRepository dataClasses;

    private ExternalIoDescriptionProcessor.Repository externals;

    private FlowGraphRewriter.Repository graphRewriters;

    private String batchId;

    private String rootPackageName;

    private Location rootLocation;

    private File workingDirectory;

    private List<? extends ResourceRepository> linkingResources;

    private File outputDirectory;

    private Repository workflows;

    private ClassLoader serviceClassLoader;

    private FlowCompilerOptions flowCompilerOptions;

    /**
     * Returns the Java DOM factory for generating Java sources.
     * @return Java DOM factory
     */
    public ModelFactory getFactory() {
        return factory;
    }

    /**
     * Sets the Java DOM factory for generating Java sources.
     * @param factory the Java DOM factory
     */
    public void setFactory(ModelFactory factory) {
        this.factory = factory;
    }

    /**
     * Returns the repository of the workflow processors.
     * @return the repository of the workflow processors
     */
    public WorkflowProcessor.Repository getWorkflows() {
        return workflows;
    }

    /**
     * Sets the repository of the workflow processors.
     * @param workflows the repository
     */
    public void setWorkflows(Repository workflows) {
        this.workflows = workflows;
    }

    /**
     * Returns the repository of the flow element processors.
     * @return the repository of the flow element processors
     */
    public FlowElementProcessor.Repository getFlowElements() {
        return flowElements;
    }

    /**
     * Sets the repository of the flow element processors.
     * @param flowElements the repository
     */
    public void setFlowElements(FlowElementProcessor.Repository flowElements) {
        this.flowElements = flowElements;
    }

    /**
     * Returns the repository of the data model classes.
     * @return the repository of the data model classes
     */
    public DataClassRepository getDataClasses() {
        return dataClasses;
    }

    /**
     * Sets the repository of the data model classes.
     * @param dataClasses the repository
     */
    public void setDataClasses(DataClassRepository dataClasses) {
        this.dataClasses = dataClasses;
    }

    /**
     * Returns the repository of external I/O processors.
     * @return the repository of external I/O processors
     */
    public ExternalIoDescriptionProcessor.Repository getExternals() {
        return externals;
    }

    /**
     * Sets the repository of external I/O processors.
     * @param externals the repository
     */
    public void setExternals(ExternalIoDescriptionProcessor.Repository externals) {
        this.externals = externals;
    }

    /**
     * Returns the repository of the flow graph rewriters.
     * @return the repository of the flow graph rewriter
     */
    public FlowGraphRewriter.Repository getGraphRewriters() {
        return graphRewriters;
    }

    /**
     * Sets the repository of the flow graph rewriters.
     * @param graphRewriters the repository
     */
    public void setGraphRewriters(FlowGraphRewriter.Repository graphRewriters) {
        this.graphRewriters = graphRewriters;
    }

    /**
     * Returns the target batch ID.
     * @return the target batch ID
     */
    public String getBatchId() {
        return batchId;
    }

    /**
     * Sets the target batch ID.
     * @param batchId the batch ID
     */
    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    /**
     * Returns the root package name of the generated Java sources.
     * @return the root package name
     */
    public String getRootPackageName() {
        return rootPackageName;
    }

    /**
     * Sets the root package name of the generated Java sources.
     * @param rootPackageName the root package name
     */
    public void setRootPackageName(String rootPackageName) {
        this.rootPackageName = rootPackageName;
    }

    /**
     * Returns the base location of the remote file system for runtime working area.
     * @return the base location of the remote file system
     */
    public Location getRootLocation() {
        return rootLocation;
    }

    /**
     * Sets the base location of the remote file system for runtime working area.
     * @param rootLocation the base location of the remote file system
     */
    public void setRootLocation(Location rootLocation) {
        this.rootLocation = rootLocation;
    }

    /**
     * Returns the compiler working directory.
     * @return the compiler working directory
     */
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Sets the compiler working directory.
     * @param workingDirectory the compiler working directory
     */
    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Returns the resource repositories for embedding into the jobflow packages.
     * @return the resource repositories
     */
    public List<? extends ResourceRepository> getLinkingResources() {
        return linkingResources;
    }

    /**
     * Sets the resource repositories for embedding into the jobflow packages.
     * @param linkingResources the resource repositories
     */
    public void setLinkingResources(List<? extends ResourceRepository> linkingResources) {
        this.linkingResources = linkingResources;
    }

    /**
     * Returns the output directory.
     * @return the output directory
     */
    public File getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Sets the output directory.
     * @param outputDirectory the output directory
     */
    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Returns the class loader for loading compiler service classes.
     * @return the class loader
     */
    public ClassLoader getServiceClassLoader() {
        return serviceClassLoader;
    }

    /**
     * Sets the class loader for loading compiler service classes.
     * @param serviceClassLoader the class loader
     */
    public void setServiceClassLoader(ClassLoader serviceClassLoader) {
        this.serviceClassLoader = serviceClassLoader;
    }

    /**
     * Returns the flow DSL compiler options.
     * @return the flow DSL compiler options
     */
    public FlowCompilerOptions getFlowCompilerOptions() {
        return flowCompilerOptions;
    }

    /**
     * Sets the flow DSL compiler options.
     * @param flowCompilerOptions the flow DSL compiler options
     */
    public void setFlowCompilerOptions(FlowCompilerOptions flowCompilerOptions) {
        this.flowCompilerOptions = flowCompilerOptions;
    }
}
