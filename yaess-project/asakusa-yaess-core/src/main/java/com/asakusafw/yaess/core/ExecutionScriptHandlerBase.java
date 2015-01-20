/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
 * @version 0.2.6
 */
public abstract class ExecutionScriptHandlerBase implements Service {

    static final YaessLogger YSLOG = new YaessCoreLogger(ExecutionScriptHandlerBase.class);

    static final Logger LOG = LoggerFactory.getLogger(ExecutionScriptHandlerBase.class);

    private volatile String prefix;

    private volatile String resourceId;

    private volatile Map<String, String> properties;

    private volatile Map<String, String> environmentVariables;

    @Override
    public final void configure(ServiceProfile<?> profile) throws InterruptedException, IOException {
        this.prefix = profile.getPrefix();
        try {
            configureResourceId(profile);
            Map<String, String> desiredProperties = getDesiredProperties(profile);
            Map<String, String> desiredEnvironmentVariables = getDesiredEnvironmentVariables(profile);
            this.properties = Collections.unmodifiableMap(desiredProperties);
            this.environmentVariables = Collections.unmodifiableMap(desiredEnvironmentVariables);
            doConfigure(profile, desiredProperties, desiredEnvironmentVariables);
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to configure \"{0}\" ({1})",
                    profile.getPrefix(),
                    profile.getPrefix()), e);
        }
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
        String override = profile.getConfiguration(ExecutionScriptHandler.KEY_RESOURCE, false, true);
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

    private Map<String, String> getDesiredProperties(ServiceProfile<?> profile) throws IOException {
        assert profile != null;
        NavigableMap<String, String> vars = PropertiesUtil.createPrefixMap(
                profile.getConfiguration(),
                ExecutionScriptHandler.KEY_PROP_PREFIX);
        Map<String, String> resolved = new TreeMap<String, String>();
        for (Map.Entry<String, String> entry : vars.entrySet()) {
            String key = entry.getKey();
            String unresolved = entry.getValue();
            try {
                String value = profile.getContext().getContextParameters().replace(unresolved, true);
                resolved.put(key, value);
            } catch (IllegalArgumentException e) {
                YSLOG.error(e, "E10001",
                        profile.getPrefix(),
                        ExecutionScriptHandler.KEY_PROP_PREFIX,
                        key,
                        unresolved);
                throw new IOException(MessageFormat.format(
                        "Failed to resolve a property: {0}.{1}.{2} = {3}",
                        profile.getPrefix(),
                        ExecutionScriptHandler.KEY_PROP_PREFIX,
                        key,
                        unresolved), e);
            }
        }
        LOG.debug("Desired properties for {}: {}",
                profile.getPrefix(),
                resolved);
        return resolved;
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
                YSLOG.error(e, "E10001",
                        profile.getPrefix(),
                        ExecutionScriptHandler.KEY_ENV_PREFIX,
                        key,
                        unresolved);
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
     * @param desiredProperties the current desired system/hadoop properties (configurable)
     * @param desiredEnvironmentVariables the current desired environment variables (configurable)
     * @throws InterruptedException if interrupted in configuration
     * @throws IOException if failed to configure this service
     */
    protected abstract void doConfigure(
            ServiceProfile<?> profile,
            Map<String, String> desiredProperties,
            Map<String, String> desiredEnvironmentVariables) throws InterruptedException, IOException;

    /**
     * Returns the ID of a resource which is used for executing this handler.
     * @param context the current execution context
     * @param script the target script (nullable)
     * @return the required resource ID
     * @throws InterruptedException if this operation is interrupted
     * @throws IOException if failed to setup the target environment
     */
    public final String getResourceId(
            ExecutionContext context,
            ExecutionScript script) throws InterruptedException, IOException {
        return resourceId;
    }

    /**
     * Returns desired system/hadoop properties to execute scripts using this handler.
     * @param context the current execution context
     * @param script the target script (nullable)
     * @return desired system or hadoop properties
     * @throws InterruptedException if this operation is interrupted
     * @throws IOException if failed to setup the target environment
     */
    public Map<String, String> getProperties(
            ExecutionContext context,
            ExecutionScript script) throws InterruptedException, IOException {
        return properties;
    }

    /**
     * Returns desired environment variables to execute scripts using this handler.
     * @param context the current execution context
     * @param script the target script (nullable)
     * @return desired environment variables
     * @throws InterruptedException if this operation is interrupted
     * @throws IOException if failed to setup the target environment
     */
    public Map<String, String> getEnvironmentVariables(
            ExecutionContext context,
            ExecutionScript script) throws InterruptedException, IOException {
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
        try {
            voidSetUp(context);
        } finally {
            monitor.close();
        }
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
        try {
            voidCleanUp(context);
        } finally {
            monitor.close();
        }
    }

    /**
     * Performs as {@link #setUp(ExecutionMonitor, ExecutionContext)} that does nothing.
     * @param context current context
     * @since 0.4.0
     */
    protected final void voidSetUp(ExecutionContext context) {
        YSLOG.info("I51001",
                context.getBatchId(),
                context.getFlowId(),
                context.getExecutionId(),
                context.getPhase(),
                getHandlerId());
    }

    /**
     * Performs as {@link #cleanUp(ExecutionMonitor, ExecutionContext)} that does nothing.
     * @param context current context
     * @since 0.4.0
     */
    protected final void voidCleanUp(ExecutionContext context) {
        YSLOG.info("I51002",
                context.getBatchId(),
                context.getFlowId(),
                context.getExecutionId(),
                context.getPhase(),
                getHandlerId());
    }
}
