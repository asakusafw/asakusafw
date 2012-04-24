/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.yaess.basic;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.asakusafw.yaess.core.ExecutionContext;

/**
 * Executes programs.
 * @since 0.2.3
 * @version 0.2.6
 */
public interface ProcessExecutor {

    /**
     * Executes a process.
     * @param context current execution context
     * @param commandLineTokens target command
     * @param environmentVariables environment variables
     * @return exit code
     * @throws InterruptedException if interrupted while waiting process exit
     * @throws IOException if failed to execute the command
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @deprecated Use {@link #execute(ExecutionContext, List, Map, OutputStream)} inste
     */
    @Deprecated
    int execute(
            ExecutionContext context,
            List<String> commandLineTokens,
            Map<String, String> environmentVariables) throws InterruptedException, IOException;

    /**
     * Executes a process.
     * @param context current execution context
     * @param commandLineTokens target command
     * @param environmentVariables environment variables
     * @param output information output
     * @return exit code
     * @throws InterruptedException if interrupted while waiting process exit
     * @throws IOException if failed to execute the command
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.6
     */
    int execute(
            ExecutionContext context,
            List<String> commandLineTokens,
            Map<String, String> environmentVariables,
            OutputStream output) throws InterruptedException, IOException;
}
