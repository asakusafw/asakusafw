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

import com.asakusafw.utils.java.model.syntax.ModelFactory;

/**
 * Represents configurations of a flow DSL compiler.
 * @since 0.1.0
 * @version 0.4.0
 */
public class FlowCompilerConfiguration {

    private ModelFactory factory;

    private Packager packager;

    private FlowElementProcessor.Repository processors;

    private DataClassRepository dataClasses;

    private ExternalIoDescriptionProcessor.Repository externals;

    private FlowGraphRewriter.Repository graphRewriters;

    private String batchId;

    private String flowId;

    private String rootPackageName;

    private Location rootLocation;

    private ClassLoader serviceClassLoader;

    private FlowCompilerOptions options;

    private String buildId;

    /**
     * Returns the Java DOM factory.
     * @return the Java DOM factory
     */
    public ModelFactory getFactory() {
        return factory;
    }

    /**
     * Sets the Java DOM factory.
     * @param factory the Java DOM factory
     */
    public void setFactory(ModelFactory factory) {
        this.factory = factory;
    }

    /**
     * Returns the packaging utilities.
     * @return the packaging utilities
     */
    public Packager getPackager() {
        return packager;
    }

    /**
     * Sets the packaging utilities.
     * @param packager the packaging utilities
     */
    public void setPackager(Packager packager) {
        this.packager = packager;
    }

    /**
     * Returns the repository of flow element processors.
     * @return the repository of flow element processors
     */
    public FlowElementProcessor.Repository getProcessors() {
        return processors;
    }

    /**
     * Sets the repository of flow element processors.
     * @param processors the repository
     */
    public void setProcessors(FlowElementProcessor.Repository processors) {
        this.processors = processors;
    }

    /**
     * Returns the repository of the data model object classes.
     * @return the repository of the data model object classes
     */
    public DataClassRepository getDataClasses() {
        return dataClasses;
    }

    /**
     * Sets the repository of the data model object classes.
     * @param dataClasses the repository
     */
    public void setDataClasses(DataClassRepository dataClasses) {
        this.dataClasses = dataClasses;
    }

    /**
     * Returns the repository of the external I/O description processors.
     * @return the repository of the external I/O description processors
     */
    public ExternalIoDescriptionProcessor.Repository getExternals() {
        return externals;
    }

    /**
     * Sets the repository of the external I/O description processors.
     * @param externals the repository
     */
    public void setExternals(ExternalIoDescriptionProcessor.Repository externals) {
        this.externals = externals;
    }

    /**
     * Returns the repository of the flow graph rewriters.
     * @return the repository of the flow graph rewriters
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
     * Returns the ID of the target batch.
     * @return the target batch ID
     */
    public String getBatchId() {
        return batchId;
    }

    /**
     * Sets the ID of the target batch.
     * @param batchId the batch ID
     */
    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    /**
     * Returns the ID of the target jobflow ({@literal a.k.a. flow ID}).
     * @return the ID of the target jobflow
     */
    public String getFlowId() {
        return flowId;
    }

    /**
     * Sets the ID of the target jobflow.
     * @param flowId the flow ID
     */
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    /**
     * Returns the root package name of generating Java source files.
     * @return the root package name of generating Java source files
     */
    public String getRootPackageName() {
        return rootPackageName;
    }

    /**
     * Sets the root package name of generating Java source files.
     * @param rootPackageName the package name
     */
    public void setRootPackageName(String rootPackageName) {
        this.rootPackageName = rootPackageName;
    }

    /**
     * Returns the root location of runtime working area where the target jobflow uses.
     * @return the root location of runtime working area
     */
    public Location getRootLocation() {
        return rootLocation;
    }

    /**
     * Sets the root location of runtime working area.
     * @param rootLocation the target location
     */
    public void setRootLocation(Location rootLocation) {
        this.rootLocation = rootLocation;
    }

    /**
     * Returns the class loader for loading service classes.
     * @return the class loader for loading service classes
     */
    public ClassLoader getServiceClassLoader() {
        return serviceClassLoader;
    }

    /**
     * Sets the class loader for loading service classes.
     * @param serviceClassLoader the class loader
     */
    public void setServiceClassLoader(ClassLoader serviceClassLoader) {
        this.serviceClassLoader = serviceClassLoader;
    }

    /**
     * Returns the flow DSL compiler options.
     * @return the flow DSL compiler options
     */
    public FlowCompilerOptions getOptions() {
        return options;
    }

    /**
     * Sets the flow DSL compiler options.
     * @param options the compiler options
     */
    public void setOptions(FlowCompilerOptions options) {
        this.options = options;
    }

    /**
     * Returns the current build ID.
     * @return current build ID, or {@code null} if not defined
     * @since 0.4.0
     */
    public String getBuildId() {
        return buildId;
    }

    /**
     * Sets the current build ID.
     * @param buildId build ID
     * @since 0.4.0
     */
    public void setBuildId(String buildId) {
        this.buildId = buildId;
    }
}
