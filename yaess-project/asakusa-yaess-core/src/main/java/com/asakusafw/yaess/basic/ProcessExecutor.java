/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.asakusafw.yaess.core.Blob;
import com.asakusafw.yaess.core.ExecutionContext;

/**
 * Executes programs.
 * @since 0.2.3
 * @version 0.8.0
 */
@FunctionalInterface
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
     */
    default int execute(
            ExecutionContext context,
            List<String> commandLineTokens,
            Map<String, String> environmentVariables) throws InterruptedException, IOException {
        return execute(context, commandLineTokens, environmentVariables, System.out);
    }

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
    default int execute(
            ExecutionContext context,
            List<String> commandLineTokens,
            Map<String, String> environmentVariables,
            OutputStream output) throws InterruptedException, IOException {
        return execute(context, commandLineTokens, environmentVariables, Collections.emptyMap(), output);
    }

    /**
     * Executes a process.
     * @param context current execution context
     * @param commandLineTokens target command
     * @param environmentVariables environment variables
     * @param extensions the extension BLOBs
     * @param output information output
     * @return exit code
     * @throws InterruptedException if interrupted while waiting process exit
     * @throws IOException if failed to execute the command
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.8.0
     */
    int execute(
            ExecutionContext context,
            List<String> commandLineTokens,
            Map<String, String> environmentVariables,
            Map<String, Blob> extensions,
            OutputStream output) throws InterruptedException, IOException;
}
