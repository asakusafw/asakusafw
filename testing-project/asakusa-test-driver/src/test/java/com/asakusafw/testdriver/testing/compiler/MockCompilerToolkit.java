/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.testdriver.testing.compiler;

import java.io.IOException;
import java.util.function.Supplier;

import com.asakusafw.testdriver.compiler.ArtifactMirror;
import com.asakusafw.testdriver.compiler.CompilerConfiguration;
import com.asakusafw.testdriver.compiler.CompilerSession;
import com.asakusafw.testdriver.compiler.CompilerToolkit;
import com.asakusafw.testdriver.compiler.FlowPortMap;
import com.asakusafw.testdriver.compiler.basic.BasicCompilerConfiguration;
import com.asakusafw.vocabulary.flow.FlowDescription;

/**
 * Mock implementation of {@link CompilerToolkit}.
 * @since 0.9.0
 */
public class MockCompilerToolkit implements CompilerToolkit {

    private Supplier<? extends FlowPortMap> portMaps = MockFlowPortMap::new;

    private ClassCompiler batchCompiler = (conf, aClass) -> {
        throw new AssertionError();
    };

    private ClassCompiler jobflowCompiler = (conf, aClass) -> {
        throw new AssertionError();
    };

    private FlowCompiler flowCompiler = (conf, flow, ports) -> {
        throw new AssertionError();
    };

    /**
     * Sets a {@link FlowPortMap} supplier
     * @param value the supplier
     * @return this
     */
    public MockCompilerToolkit withPortMap(Supplier<? extends FlowPortMap> value) {
        this.portMaps = value;
        return this;
    }

    /**
     * Sets a compiler for batch classes.
     * @param value the compiler
     * @return this
     */
    public MockCompilerToolkit withBatch(ClassCompiler value) {
        this.batchCompiler = value;
        return this;
    }

    /**
     * Sets a compiler for batch classes.
     * @param value the compiler
     * @return this
     */
    public MockCompilerToolkit withJobflow(ClassCompiler value) {
        this.jobflowCompiler = value;
        return this;
    }

    /**
     * Sets a compiler for batch classes.
     * @param value the compiler
     * @return this
     */
    public MockCompilerToolkit withFlow(FlowCompiler value) {
        this.flowCompiler = value;
        return this;
    }

    ArtifactMirror doCompileBatch(CompilerConfiguration configuration, Class<?> batchClass) throws IOException {
        return batchCompiler.compile(configuration, batchClass);
    }

    ArtifactMirror doCompileJobflow(CompilerConfiguration configuration, Class<?> jobflowClass) throws IOException {
        return jobflowCompiler.compile(configuration, jobflowClass);
    }

    ArtifactMirror doCompileFlow(
            CompilerConfiguration configuration,
            FlowDescription flow, FlowPortMap portMap) throws IOException {
        return flowCompiler.compile(configuration, flow, portMap);
    }

    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public FlowPortMap newFlowPortMap() {
        return portMaps.get();
    }

    @Override
    public CompilerConfiguration newConfiguration() {
        return new BasicCompilerConfiguration();
    }

    @Override
    public CompilerSession newSession(CompilerConfiguration configuration) throws IOException {
        return new MockCompilerSession(this, configuration);
    }

    /**
     * Compiler for individual DSL classes.
     * @since 0.9.0
     */
    @FunctionalInterface
    public interface ClassCompiler {

        /**
         * Compiles the target class.
         * @param configuration the current configuration
         * @param aClass the target class
         * @return the compiled artifact
         * @throws IOException if failed
         */
        ArtifactMirror compile(CompilerConfiguration configuration, Class<?> aClass) throws IOException;
    }

    /**
     * Compiler for individual flows.
     * @since 0.9.0
     */
    @FunctionalInterface
    public interface FlowCompiler {

        /**
         * Compiles the target class.
         * @param configuration the current configuration
         * @param flow the target flow
         * @param portMap the port map
         * @return the compiled artifact
         * @throws IOException if failed
         */
        ArtifactMirror compile(
                CompilerConfiguration configuration,
                FlowDescription flow,
                FlowPortMap portMap) throws IOException;
    }
}
