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
package com.asakusafw.windgate.core;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.process.ProcessProvider;
import com.asakusafw.windgate.core.util.PropertiesUtil;

/**
 * A core profile for WindGate execution.
 * @since 0.2.3
 */
public class CoreProfile {

    static final Logger LOG = LoggerFactory.getLogger(CoreProfile.class);

    private static final char QUALIFIER = '.';

    /**
     * Prefix of property keys about core.
     */
    public static final String KEY_PREFIX = "core" + QUALIFIER; // //$NON-NLS-1$

    /**
     * The key name of {@link #getMaxThreads()}.
     */
    public static final String KEY_MAX_THREADS = "maxThreads";

    /**
     * The default value of {@link #KEY_MAX_THREADS}.
     */
    public static final int DEFAULT_MAX_THREADS = 1;

    private final int maxThreads;

    /**
     * Creates a new instance.
     * @param maxThreads the number of max threads in core gate processes
     * @throws IllegalArgumentException if the {@code maxThreads} is less than {@code 1}
     */
    public CoreProfile(int maxThreads) {
        if (maxThreads < 1) {
            throw new IllegalArgumentException("maxThreads must be a positive integer");
        }
        this.maxThreads = maxThreads;
    }

    /**
     * the number of max threads in core gate processes.
     * @return the number of max threads
     */
    public int getMaxThreads() {
        return maxThreads;
    }

    /**
     * Loads a core profile from the properties.
     * @param properties source properties
     * @param loader class loader to load the {@link ProcessProvider}
     * @return the loaded profile
     * @throws IllegalArgumentException if properties are invalid, or if any parameter is {@code null}
     */
    public static CoreProfile loadFrom(Properties properties, ClassLoader loader) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (loader == null) {
            throw new IllegalArgumentException("loader must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Restoring core profile");
        Map<String, String> config = PropertiesUtil.createPrefixMap(properties, KEY_PREFIX);
        int maxThreads = getInt(config, KEY_MAX_THREADS, DEFAULT_MAX_THREADS);
        return new CoreProfile(maxThreads);
    }

    private static int getInt(Map<String, String> config, String name, int defaultValue) {
        assert config != null;
        assert name != null;
        String value = config.get(name);
        if (value == null) {
            LOG.debug("Core profile \"{}\" is not defined, \"{}\" will be used.",
                    name,
                    defaultValue);
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "The profile item \"{0}{1}\" must be an integer: {2}",
                    KEY_PREFIX,
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

        properties.setProperty(KEY_PREFIX + KEY_MAX_THREADS, String.valueOf(getMaxThreads()));
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
