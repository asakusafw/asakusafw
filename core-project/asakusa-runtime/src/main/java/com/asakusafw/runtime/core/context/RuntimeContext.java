/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.runtime.core.context;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import com.asakusafw.runtime.core.BatchRuntime;

/**
 * Holds runtime context information.
 * @since 0.4.0
 */
public final class RuntimeContext {

    static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(RuntimeContext.class.getName());

    static {
        // install JDK13Logger to SLF4J if enabled
        try {
            Class<?> bridge = Class.forName("org.slf4j.bridge.SLF4JBridgeHandler"); //$NON-NLS-1$
            Method isInstalled = bridge.getMethod("isInstalled"); //$NON-NLS-1$
            Method install = bridge.getMethod("install"); //$NON-NLS-1$
            Method clean = bridge.getMethod("removeHandlersForRootLogger"); //$NON-NLS-1$
            if (Boolean.FALSE.equals(isInstalled.invoke(null))) {
                clean.invoke(null);
                install.invoke(null);
            }
        } catch (Exception e) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "jul-to-slf4j is not enabled", e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Default context.
     */
    public static final RuntimeContext DEFAULT = new RuntimeContext();

    /**
     * The relative path from classpath of application info.
     * This may includes following information.
     * <ul>
     * <li> batch ID (key={@link #KEY_BATCH_ID}) </li>
     * <li> flow ID (key={@link #KEY_FLOW_ID}) </li>
     * <li> build ID (key={@link #KEY_BUILD_ID}) </li>
     * </ul>
     */
    public static final String PATH_APPLICATION_INFO = "META-INF/asakusa/application.properties"; //$NON-NLS-1$

    /**
     * The value map key of {@link #mode(ExecutionMode)}.
     * @see #apply(Map)
     * @see #unapply()
     */
    public static final String KEY_EXECUTION_MODE = "_ASAKUSA_APP_EXECUTION_MODE"; //$NON-NLS-1$

    /**
     * The value map key of {@link #batchId(String)}.
     * @see #apply(Map)
     * @see #unapply()
     */
    public static final String KEY_BATCH_ID = "_ASAKUSA_APP_BATCH_ID"; //$NON-NLS-1$

    /**
     * The value map key of flow ID.
     * This is only for information path.
     */
    public static final String KEY_FLOW_ID = "_ASAKUSA_APP_FLOW_ID"; //$NON-NLS-1$

    /**
     * The value map key of {@link #buildId(String)}.
     * @see #apply(Map)
     * @see #unapply()
     */
    public static final String KEY_BUILD_ID = "_ASAKUSA_APP_BUILD_ID"; //$NON-NLS-1$

    /**
     * The value map key of build date.
     */
    public static final String KEY_BUILD_DATE = "_ASAKUSA_APP_BUILD_DATE"; //$NON-NLS-1$

    /**
     * The value map key of {@link #getRuntimeVersion() runtime version}.
     */
    public static final String KEY_RUNTIME_VERSION = "_ASAKUSA_RUNTIME_VERSION"; //$NON-NLS-1$

    private static final AtomicReference<RuntimeContext> GLOBAL =  new AtomicReference<>(DEFAULT);

    private final String batchId;

    private final ExecutionMode mode;

    private final String buildId;

    private RuntimeContext() {
        this(ExecutionMode.PRODUCTION, null, null);
    }

    private RuntimeContext(ExecutionMode mode, String batchId, String verificationCode) {
        assert mode != null;
        this.mode = mode;
        this.batchId = batchId;
        this.buildId = verificationCode;
    }

    /**
     * Returns the global context.
     * @return the global context
     */
    public static RuntimeContext get() {
        return GLOBAL.get();
    }

    /**
     * Replaces the global context.
     * @param context the replacement
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static void set(RuntimeContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        GLOBAL.set(context);
    }

    /**
     * Returns the current runtime version.
     * @return current runtime version
     */
    public static String getRuntimeVersion() {
        return BatchRuntime.getLabel();
    }

    /**
     * Creates an inherited context with specified execution mode.
     * @param newValue execution mode
     * @return the inherited context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public RuntimeContext mode(ExecutionMode newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException("newMode must not be null"); //$NON-NLS-1$
        }
        return new RuntimeContext(newValue, batchId, buildId);
    }

    /**
     * Creates an inherited context with specified batch ID.
     * @param newValue batch ID, or {@code null} to unset batch ID
     * @return the inherited context
     */
    public RuntimeContext batchId(String newValue) {
        return new RuntimeContext(mode, newValue, buildId);
    }

    /**
     * Creates an inherited context with specified verification code.
     * @param newValue verification code, or {@code null} to unset verification code
     * @return the inherited context
     */
    public RuntimeContext buildId(String newValue) {
        return new RuntimeContext(mode, batchId, newValue);
    }

    /**
     * Creates an inherited context from a value map.
     * @param newValueMap value map
     * @return the inherited context
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #KEY_BATCH_ID
     * @see #KEY_EXECUTION_MODE
     * @see #KEY_BUILD_ID
     */
    public RuntimeContext apply(Map<String, String> newValueMap) {
        if (newValueMap == null) {
            throw new IllegalArgumentException("newValueMap must not be null"); //$NON-NLS-1$
        }
        RuntimeContext current = this;
        String newModeString = normalize(newValueMap.get(KEY_EXECUTION_MODE));
        if (newModeString != null) {
            ExecutionMode newMode = ExecutionMode.fromSymbol(newModeString);
            if (newMode != null) {
                current = current.mode(newMode);
            } else {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.warning(MessageFormat.format(
                            "Invalid execution mode: {0}={1}",
                            KEY_EXECUTION_MODE,
                            newModeString));
                }
            }
        }
        String newBatchId = normalize(newValueMap.get(KEY_BATCH_ID));
        if (newBatchId != null) {
            current = current.batchId(newBatchId);
        }
        String newVerificationCode = normalize(newValueMap.get(KEY_BUILD_ID));
        if (newVerificationCode != null) {
            current = current.buildId(newVerificationCode);
        }
        return current;
    }

    /**
     * Creates a value map that describes this context.
     * @return the value map
     * @see #apply(Map)
     */
    public Map<String, String> unapply() {
        Map<String, String> results = new HashMap<>();
        put(results, KEY_EXECUTION_MODE, mode.getSymbol());
        put(results, KEY_BATCH_ID, batchId);
        put(results, KEY_BUILD_ID, buildId);
        return results;
    }

    /**
     * Returns whether this execution is in simulation mode.
     * @return {@code true} if this execution is in simulation mode, otherwise {@code false}
     */
    public boolean isSimulation() {
        return mode == ExecutionMode.SIMULATION;
    }

    /**
     * Returns whether target object is executable in current execution mode.
     * @param object target object
     * @return {@code true} if is executable, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public boolean canExecute(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("object must not be null"); //$NON-NLS-1$
        }
        switch (mode) {
        case PRODUCTION:
            return true;
        case SIMULATION:
            return isSimulationSupported(object);
        default:
            throw new AssertionError(mode);
        }
    }

    private boolean isSimulationSupported(Object object) {
        assert object != null;
        boolean annotated = object.getClass().isAnnotationPresent(SimulationSupport.class);
        return annotated;
    }

    /**
     * Verifies application by using current context.
     * @param classLoader application classes
     * @throws InconsistentApplicationException if failed to verify
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void verifyApplication(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new IllegalArgumentException("classLoader must not be null"); //$NON-NLS-1$
        }
        if (batchId == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Application Verfication was skipped (Batch ID is not defined)"); //$NON-NLS-1$
            }
            return;
        }
        if (buildId == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Application verification was skipped (Verification code is not defined)"); //$NON-NLS-1$
            }
            return;
        }
        boolean verified = false;
        try {
            Enumeration<URL> infoEnum = classLoader.getResources(PATH_APPLICATION_INFO);
            while (infoEnum.hasMoreElements()) {
                URL url = infoEnum.nextElement();
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine(MessageFormat.format("Loading application info: {0}", url)); //$NON-NLS-1$
                }
                Properties properties = new Properties();
                try (InputStream in = url.openStream()) {
                    properties.load(in);
                }
                verifyRuntime(url, properties);
                verified |= verifyBuildId(url, properties);
            }
        } catch (IOException e) {
            throw new InconsistentApplicationException("Error occurred while verifying application version", e);
        }
        if (verified) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(MessageFormat.format(
                        "Application was successfully verified: batchId={0}, verificationCode={1}", //$NON-NLS-1$
                        batchId,
                        buildId));
            }
            return;
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(MessageFormat.format(
                        "Missing veirification info in class loader: " //$NON-NLS-1$
                        + "batchId={0}, buildId={1}, loader={2}", //$NON-NLS-1$
                        batchId,
                        buildId,
                        classLoader));
            }
            return;
        }
    }

    private boolean verifyBuildId(URL url, Properties properties) {
        assert batchId != null;
        assert buildId != null;
        assert url != null;
        assert properties != null;
        String targetBatchId = normalize(properties.getProperty(KEY_BATCH_ID));
        String targetBuildId = normalize(properties.getProperty(KEY_BUILD_ID));
        if (targetBatchId == null || targetBatchId.equals(batchId) == false) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(MessageFormat.format(
                        "Not a corresponding application info for \"{1}\": {0}", //$NON-NLS-1$
                        url,
                        batchId));
            }
            return false;
        } else if (targetBuildId != null && targetBuildId.equals(buildId)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(MessageFormat.format(
                        "Found verified application for \"{1}\": {0}", //$NON-NLS-1$
                        url,
                        batchId));
            }
            return true;
        } else {
            throw new InconsistentApplicationException(MessageFormat.format(
                    "Inconsistent application, please check your deployed application: "
                    + "url=\"{0}\", batchId=\"{1}\", caller=\"{2}\", callee=\"{3}\", hostname=\"{4}\"",
                    url,
                    batchId,
                    buildId,
                    targetBuildId,
                    getHostName()));
        }
    }

    private void verifyRuntime(URL url, Properties properties) {
        assert url != null;
        assert properties != null;
        String runtimeVersion = normalize(properties.getProperty(KEY_RUNTIME_VERSION));
        if (runtimeVersion == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(MessageFormat.format(
                        "Missing runtime version: {0}", //$NON-NLS-1$
                        url));
            }
            return;
        } else if (runtimeVersion.equals(BatchRuntime.getLabel())) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(MessageFormat.format(
                        "Runtime version is verified: {0}", //$NON-NLS-1$
                        url));
            }
            return;
        } else {
            throw new InconsistentApplicationException(MessageFormat.format(
                    "Inconsistent runtime version, please check your deployed framework: "
                    + "url=\"{0}\", batchId=\"{1}\", caller=\"{2}\", callee=\"{3}\", hostname=\"{4}\"",
                    url,
                    batchId,
                    BatchRuntime.getLabel(),
                    runtimeVersion,
                    getHostName()));
        }
    }

    private void put(Map<String, String> map, String key, String value) {
        assert map != null;
        assert key != null;
        if (value != null) {
            map.put(key, value);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }

    private String getHostName() {
        String hostname = System.getenv("HOSTNAME"); //$NON-NLS-1$
        if (hostname == null) {
            hostname = System.getenv("SSH_CONNECTION"); //$NON-NLS-1$
        }
        return hostname;

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((batchId == null) ? 0 : batchId.hashCode());
        result = prime * result + mode.hashCode();
        result = prime * result + ((buildId == null) ? 0 : buildId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RuntimeContext other = (RuntimeContext) obj;
        if (batchId == null) {
            if (other.batchId != null) {
                return false;
            }
        } else if (!batchId.equals(other.batchId)) {
            return false;
        }
        if (mode != other.mode) {
            return false;
        }
        if (buildId == null) {
            if (other.buildId != null) {
                return false;
            }
        } else if (!buildId.equals(other.buildId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "RuntimeContext(batchId={0}, mode={1}, buildId={2})", //$NON-NLS-1$
                batchId,
                mode,
                buildId);
    }

    /**
     * Represents execution type.
     * @since 0.4.0
     */
    public enum ExecutionMode {

        /**
         * Normal execution.
         */
        PRODUCTION("production"), //$NON-NLS-1$

        /**
         * Simulated execution.
         */
        SIMULATION("simulation"), //$NON-NLS-1$
        ;

        private final String symbol;

        ExecutionMode(String symbol) {
            assert symbol != null;
            this.symbol = symbol;
        }

        /**
         * Returns the symbol of this mode.
         * @return the symbol
         */
        public String getSymbol() {
            return symbol;
        }

        /**
         * Returns a member which is represented by the symbol.
         * @param symbol target symbol
         * @return related member, or {@code null} if there is no such member
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public static ExecutionMode fromSymbol(String symbol) {
            if (symbol == null) {
                throw new IllegalArgumentException("symbol must not be null"); //$NON-NLS-1$
            }
            String s = symbol.toLowerCase(Locale.ENGLISH);
            for (ExecutionMode mode : values()) {
                if (mode.symbol.equals(s)) {
                    return mode;
                }
            }
            return null;
        }
    }
}
