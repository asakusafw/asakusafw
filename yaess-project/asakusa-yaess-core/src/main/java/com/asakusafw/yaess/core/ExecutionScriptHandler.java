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
 * @version 0.8.0
 */
public interface ExecutionScriptHandler<T extends ExecutionScript> extends Service {

    /**
     * The configuration key prefix of {@link #getEnvironmentVariables(ExecutionContext, ExecutionScript)
     * environment variables}.
     * This value can includes local environment variables in form of <code>${VARIABLE-NAME}</code>.
     */
    String KEY_ENV_PREFIX = "env.";

    /**
     * The configuration key prefix of {@link #getProperties(ExecutionContext, ExecutionScript)
     * system or hadoop properties}.
     * This value can includes local environment variables in form of <code>${VARIABLE-NAME}</code>.
     * @since 0.2.6
     */
    String KEY_PROP_PREFIX = "prop.";

    /**
     * The configuration key name of {@link #getResourceId(ExecutionContext, ExecutionScript) resource ID}.
     */
    String KEY_RESOURCE = "resource";

    /**
     * The default value of {@link #getResourceId(ExecutionContext, ExecutionScript) resource ID}.
     */
    String DEFAULT_RESOURCE_ID = "default";

    /**
     * The prefix of environment variable names for each extension {@link Blob}s.
     * @since 0.8.0
     */
    String ENV_EXTENSION_PREFIX = "ASAKUSA_EXTENSION_";

    /**
     * Returns the ID of this handler.
     * @return the ID
     */
    String getHandlerId();

    /**
     * Returns the ID of a resource which is used for executing this handler.
     * @param context the current execution context
     * @param script the target script (nullable)
     * @return the required resource ID
     * @throws InterruptedException if this operation is interrupted
     * @throws IOException if failed to setup the target environment
     */
    String getResourceId(ExecutionContext context, ExecutionScript script) throws InterruptedException, IOException;

    /**
     * Returns desired system/hadoop properties to execute scripts using this handler.
     * @param context the current execution context
     * @param script the target script (nullable)
     * @return desired system or hadoop properties
     * @throws InterruptedException if this operation is interrupted
     * @throws IOException if failed to setup the target environment
     * @since 0.2.6
     */
    Map<String, String> getProperties(
            ExecutionContext context,
            ExecutionScript script) throws InterruptedException, IOException;

    /**
     * Returns desired environment variables to execute scripts using this handler.
     * @param context the current execution context
     * @param script the target script (nullable)
     * @return desired environment variables
     * @throws InterruptedException if this operation is interrupted
     * @throws IOException if failed to setup the target environment
     */
    Map<String, String> getEnvironmentVariables(
            ExecutionContext context,
            ExecutionScript script) throws InterruptedException, IOException;

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
