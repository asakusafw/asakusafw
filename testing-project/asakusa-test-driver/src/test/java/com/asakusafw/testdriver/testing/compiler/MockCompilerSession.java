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
package com.asakusafw.testdriver.testing.compiler;

import java.io.IOException;

import com.asakusafw.testdriver.compiler.ArtifactMirror;
import com.asakusafw.testdriver.compiler.CompilerConfiguration;
import com.asakusafw.testdriver.compiler.CompilerSession;
import com.asakusafw.testdriver.compiler.FlowPortMap;
import com.asakusafw.vocabulary.flow.FlowDescription;

/**
 * Mock implementation of {@link CompilerSession}.
 * @since 0.9.0
 */
class MockCompilerSession implements CompilerSession {

    private final MockCompilerToolkit toolkit;

    private final CompilerConfiguration configuration;

    MockCompilerSession(MockCompilerToolkit toolkit, CompilerConfiguration configuration) {
        this.toolkit = toolkit;
        this.configuration = configuration;
    }

    @Override
    public ArtifactMirror compileBatch(Class<?> batchClass) throws IOException {
        return toolkit.doCompileBatch(configuration, batchClass);
    }

    @Override
    public ArtifactMirror compileJobflow(Class<?> jobflowClass) throws IOException {
        return toolkit.doCompileJobflow(configuration, jobflowClass);
    }

    @Override
    public ArtifactMirror compileFlow(FlowDescription flow, FlowPortMap portMap) throws IOException {
        return toolkit.doCompileFlow(configuration, flow, portMap);
    }

    @Override
    public void close() throws IOException {
        return;
    }
}
