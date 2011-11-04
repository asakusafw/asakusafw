/**
 * Copyright 2011 Asakusa Framework Team.
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
import java.util.List;
import java.util.Map;

/**
 * Executes programs.
 * @since 0.2.3
 */
public interface ProcessExecutor {

    /**
     * Executes a process.
     * @param commandLineTokens target command
     * @param environmentVariables environment variables
     * @return exit code
     * @throws InterruptedException if interrupted while waiting process exit
     * @throws IOException if failed to execute the command
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    int execute(
            List<String> commandLineTokens,
            Map<String, String> environmentVariables) throws InterruptedException, IOException;
}
