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
package com.asakusafw.windgate.core;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.util.PropertiesUtil;

/**
 * A core profile for WindGate execution.
 * @since 0.2.2
 * @version 0.2.4
 */
public class CoreProfile {

    static final WindGateLogger WGLOG = new WindGateCoreLogger(CoreProfile.class);

    static final Logger LOG = LoggerFactory.getLogger(CoreProfile.class);

    private static final char QUALIFIER = '.';

    /**
     * Prefix of property keys about core.
     */
    public static final String KEY_PREFIX = "core" + QUALIFIER; // //$NON-NLS-1$

    /**
     * The key name of {@link #getMaxProcesses()} (deprecated).
     * @deprecated use instead {@link #KEY_MAX_PROCESSES}
     */
    @Deprecated
    public static final String KEY_MAX_THREADS = "maxThreads";

    /**
     * The key name of {@link #getMaxProcesses()}.
     */
    public static final String KEY_MAX_PROCESSES = "maxProcesses";

    /**
     * The default value of {@link #KEY_MAX_PROCESSES}.
     */
    public static final int DEFAULT_MAX_PROCESSES = 1;

    private final int maxProcesses;

    /**
     * Creates a new instance.
     * @param maxProcesses the number of max threads in core gate processes
     * @throws IllegalArgumentException if the {@code maxThreads} is less than {@code 1}
     */
    public CoreProfile(int maxProcesses) {
        if (maxProcesses < 1) {
            throw new IllegalArgumentException("maxProcesses must be a positive integer"); //$NON-NLS-1$
        }
        this.maxProcesses = maxProcesses;
    }

    /**
     * the number of max threads in core gate processes.
     * @return the number of max threads
     */
    public int getMaxProcesses() {
        return maxProcesses;
    }

    /**
     * Loads a core profile from the properties.
     * @param properties source properties
     * @param loader class loader to load the provider classes
     * @return the loaded profile
     * @throws IllegalArgumentException if properties are invalid, or if any parameter is {@code null}
     * @deprecated use {@link #loadFrom(Properties, ProfileContext)} instead
     */
    @Deprecated
    public static CoreProfile loadFrom(Properties properties, ClassLoader loader) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (loader == null) {
            throw new IllegalArgumentException("loader must not be null"); //$NON-NLS-1$
        }
        return loadFrom(properties, ProfileContext.system(loader));
    }

    /**
     * Loads a core profile from the properties.
     * @param properties source properties
     * @param context the current profile context
     * @return the loaded profile
     * @throws IllegalArgumentException if properties are invalid, or if any parameter is {@code null}
     * @since 0.2.4
     */
    public static CoreProfile loadFrom(Properties properties, ProfileContext context) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Restoring core profile");
        Map<String, String> config = PropertiesUtil.createPrefixMap(properties, KEY_PREFIX);
        int maxProcesses;
        if (config.containsKey(KEY_MAX_PROCESSES)) {
            maxProcesses = getMaxProcesses(config, KEY_MAX_PROCESSES);
        } else {
            // for legacy spec
            maxProcesses = getMaxProcesses(config, KEY_MAX_THREADS);
        }
        return new CoreProfile(maxProcesses);
    }

    private static int getMaxProcesses(Map<String, String> config, String key) {
        assert config != null;
        assert key != null;
        int maxProcesses = getInt(config, key, DEFAULT_MAX_PROCESSES);
        if (maxProcesses <= 0) {
            WGLOG.error("E02003",
                    key,
                    maxProcesses);
            throw new IllegalArgumentException(MessageFormat.format(
                    "Core profile item \"{0}\" must be > 0: {1}",
                    key,
                    String.valueOf(maxProcesses)));
        }
        return maxProcesses;
    }

    private static int getInt(Map<String, String> config, String name, int defaultValue) {
        assert config != null;
        assert name != null;
        String value = config.get(name);
        if (value == null) {
            LOG.debug("Core profile \"{}\" is not defined, default value \"{}\" will be used.",
                    name,
                    defaultValue);
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            WGLOG.error("E02003",
                    name,
                    value);
            throw new IllegalArgumentException(MessageFormat.format(
                    "Core profile item \"{0}\" must be an integer: {1}",
                    name,
                    value));
        }
    }

    /**
     * Stores this profile into the specified properties.
     * @param properties target properties object
     * @throws IllegalArgumentException if target properties already contains keys about this process,
     *     or if any parameter is {@code null}
     */
    public void storeTo(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Saving core profile");
        PropertiesUtil.checkAbsentKeyPrefix(properties, KEY_PREFIX);

        properties.setProperty(KEY_PREFIX + KEY_MAX_PROCESSES, String.valueOf(getMaxProcesses()));
    }

    /**
     * Removes entries corresponding to process profiles.
     * @param properties target properties
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public static void removeCorrespondingKeys(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        PropertiesUtil.removeKeyPrefix(properties, KEY_PREFIX);
    }
}
