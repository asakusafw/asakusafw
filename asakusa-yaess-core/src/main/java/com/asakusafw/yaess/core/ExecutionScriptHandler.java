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
package com.asakusafw.yaess.core;

import java.io.IOException;
import java.util.Map;

/**
 * An abstract super interface of {@link ExecutionScript} handler.
 * An implementation of this will be handles {@code ExecutionScript},
 * and then execute some commands on the suitable environment.
 * Clients must not implement this interface directly,
 * extend {@link ExecutionScriptHandlerBase} and
 * implement {@link HadoopScriptHandler} or {@link CommandScriptHandler} instead.
 * @param <T> the type of script
 * @since 0.2.3
 */
public interface ExecutionScriptHandler<T extends ExecutionScript> extends Service {

    /**
     * The configuration key prefix of {@link #getEnvironmentVariables() environment variables}.
     * This value can includes local environment variables in form of <code>${VARIABLE-NAME}</code>.
     */
    String KEY_ENV_PREFIX = "env.";

    /**
     * The configuration key name of {@link #getResourceId() resource ID}.
     */
    String KEY_RESOURCE = "resource";

    /**
     * The default value of {@link #getResourceId() resource ID}.
     */
    String DEFAULT_RESOURCE_ID = "default";

    /**
     * Returns the ID of this handler.
     * @return the ID
     */
    String getHandlerId();

    /**
     * Returns the ID of a resource which is used for executing this handler.
     * @return the required resource ID
     */
    String getResourceId();

    /**
     * Returns desired environment variables to execute scripts using this handler.
     * @return desired environment variables
     */
    Map<String, String> getEnvironmentVariables();

    /**
     * Setup the target environment.
     * @param monitor the progress monitor of the operation
     * @param context the current execution context
     * @throws InterruptedException if this operation is interrupted
     * @throws IOException if failed to setup the target environment
     */
    void setUp(
            ExecutionMonitor monitor,
            ExecutionContext context) throws InterruptedException, IOException;

    /**
     * Executes the specified script.
     * @param monitor the progress monitor of the execution
     * @param context current execution context
     * @param script target script
     * @throws InterruptedException if the execution was interrupted
     * @throws IOException if execution was failed
     */
    void execute(
            ExecutionMonitor monitor,
            ExecutionContext context,
            T script) throws InterruptedException, IOException;

    /**
     * Cleanup the target environment.
     * @param monitor the progress monitor of the operation
     * @param context the current execution context
     * @throws InterruptedException if this operation is interrupted
     * @throws IOException if failed to setup the target environment
     */
    void cleanUp(
            ExecutionMonitor monitor,
            ExecutionContext context) throws InterruptedException, IOException;
}
