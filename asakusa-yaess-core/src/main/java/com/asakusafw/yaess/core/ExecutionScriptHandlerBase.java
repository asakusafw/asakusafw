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
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.util.PropertiesUtil;

/**
 * A skeletal implementation of {@link ExecutionScriptHandler}.
 * Note that this class does not implement {@link ExecutionScriptHandler},
 * so that subclasses must implement {@link HadoopScriptHandler} or {@link CommandScriptHandler}.
 * @since 0.2.3
 */
public abstract class ExecutionScriptHandlerBase implements Service {

    static final Logger LOG = LoggerFactory.getLogger(ExecutionScriptHandlerBase.class);

    private volatile String prefix;

    private volatile String resourceId;

    private volatile Map<String, String> environmentVariables;

    @Override
    public final void configure(ServiceProfile<?> profile) throws InterruptedException, IOException {
        this.prefix = profile.getPrefix();
        configureResourceId(profile);
        Map<String, String> desiredEnvironmentVariables = getDesiredEnvironmentVariables(profile);
        environmentVariables = Collections.unmodifiableMap(desiredEnvironmentVariables);
        doConfigure(profile, desiredEnvironmentVariables);
    }

    /**
     * Returns the ID of this handler.
     * @return the ID
     */
    public final String getHandlerId() {
        return prefix;
    }

    private void configureResourceId(ServiceProfile<?> profile) {
        assert profile != null;
        String override = profile.getConfiguration().get(ExecutionScriptHandler.KEY_RESOURCE);
        if (override == null) {
            LOG.debug("resourceId is not override in {}",
                    profile.getPrefix());
            resourceId = ExecutionScriptHandler.DEFAULT_RESOURCE_ID;
        } else {
            LOG.debug("resourceId is overriden in {}: {}",
                    profile.getPrefix(),
                    override);
            resourceId = override;
        }
    }

    private Map<String, String> getDesiredEnvironmentVariables(ServiceProfile<?> profile) throws IOException {
        assert profile != null;
        NavigableMap<String, String> vars = PropertiesUtil.createPrefixMap(
                profile.getConfiguration(),
                ExecutionScriptHandler.KEY_ENV_PREFIX);
        Map<String, String> resolved = new TreeMap<String, String>();
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            String key = entry.getKey();
            String unresolved = entry.getValue();
            try {
                String value = profile.getContext().getContextParameters().replace(unresolved, true);
                resolved.put(key, value);
            } catch (IllegalArgumentException e) {
                // TODO logging
                throw new IOException(MessageFormat.format(
                        "Failed to resolve environment variable: {0}.{1}.{2} = {3}",
                        profile.getPrefix(),
                        ExecutionScriptHandler.KEY_ENV_PREFIX,
                        key,
                        unresolved), e);
            }
        }
        LOG.debug("Desired environment variables for {}: {}",
                profile.getPrefix(),
                resolved);
        return resolved;
    }

    /**
     * Configures this handler internally (extention point).
     * @param profile the profile of this service
     * @param desiredEnvironmentVariables the current desired environment variables (configurable)
     * @throws InterruptedException if interrupted in configuration
     * @throws IOException if failed to configure this service
     */
    protected abstract void doConfigure(
            ServiceProfile<?> profile,
            Map<String, String> desiredEnvironmentVariables) throws InterruptedException, IOException;

    /**
     * Returns the ID of a resource which is used for executing this handler.
     * @return the required resource ID
     */
    public final String getResourceId() {
        return resourceId;
    }

    /**
     * Returns desired environment variables to execute scripts using this handler.
     * @return desired environment variables
     */
    public final Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    /**
     * Setup the target environment.
     * @param monitor the progress monitor of the operation
     * @param context the current execution context
     * @throws InterruptedException if this operation is interrupted
     * @throws IOException if failed to setup the target environment
     */
    public void setUp(
            ExecutionMonitor monitor,
            ExecutionContext context) throws InterruptedException, IOException {
        monitor.open(1);
        monitor.close();
    }

    /**
     * Cleanup the target environment.
     * @param monitor the progress monitor of the operation
     * @param context the current execution context
     * @throws InterruptedException if this operation is interrupted
     * @throws IOException if failed to setup the target environment
     */
    public void cleanUp(
            ExecutionMonitor monitor,
            ExecutionContext context) throws InterruptedException, IOException {
        monitor.open(1);
        monitor.close();
    }
}
