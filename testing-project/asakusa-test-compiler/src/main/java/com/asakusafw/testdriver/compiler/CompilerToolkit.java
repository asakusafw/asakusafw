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
package com.asakusafw.testdriver.compiler;

import java.io.IOException;

/**
 * Entry API for Asakusa DSL TestKit Compiler.
 * @since 0.8.0
 */
public interface CompilerToolkit {

    /**
     * Returns the toolkit name.
     * @return the toolkit name
     */
    String getName();

    /**
     * Creates a new flow I/O port map for the compiler.
     * @return the created port map
     */
    FlowPortMap newFlowPortMap();

    /**
     * Creates a new configurations.
     * The returned object is initialized with default values.
     * @return the created configuration
     */
    CompilerConfiguration newConfiguration();

    /**
     * Creates a new compiling session.
     * @param configuration the compiler configuration
     * @return the created instance
     * @throws IOException if I/O error was occurred while starting the session
     */
    CompilerSession newSession(CompilerConfiguration configuration) throws IOException;
}
