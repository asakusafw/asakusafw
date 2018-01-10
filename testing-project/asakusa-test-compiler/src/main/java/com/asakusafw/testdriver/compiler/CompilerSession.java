/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.testdriver.compiler;

import java.io.Closeable;
import java.io.IOException;

import com.asakusafw.vocabulary.flow.FlowDescription;

/**
 * An abstract super interface of Asakusa DSL TestKit Compiler.
 * @since 0.8.0
 */
public interface CompilerSession extends Closeable {

    /**
     * Compiles a batch class.
     * @param batchClass the source batch class
     * @return the compiled artifact
     * @throws IOException if compilation was failed
     */
    ArtifactMirror compileBatch(Class<?> batchClass) throws IOException;

    /**
     * Compiles a jobflow class.
     * @param jobflowClass the source jobflow class
     * @return the compiled artifact
     * @throws IOException if compilation was failed
     */
    ArtifactMirror compileJobflow(Class<?> jobflowClass) throws IOException;

    /**
     * Compiles a flow.
     * @param flow the source flow object
     * @param portMap the I/O port map for the flow
     * @return the compiled artifact
     * @throws IOException if compilation was failed
     */
    ArtifactMirror compileFlow(FlowDescription flow, FlowPortMap portMap) throws IOException;
}
