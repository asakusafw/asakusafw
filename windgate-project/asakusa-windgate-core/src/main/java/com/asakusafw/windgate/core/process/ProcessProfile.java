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
package com.asakusafw.windgate.core.process;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.BaseProfile;
import com.asakusafw.windgate.core.ProfileContext;
import com.asakusafw.windgate.core.WindGateCoreLogger;
import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.util.PropertiesUtil;

/**
 * A configuration for each process.
 * @since 0.2.2
 * @version 0.2.4
 */
public class ProcessProfile extends BaseProfile<ProcessProfile, ProcessProvider> {

    static final WindGateLogger WGLOG = new WindGateCoreLogger(ProcessProfile.class);

    static final Logger LOG = LoggerFactory.getLogger(ProcessProfile.class);

    /**
     * Process name pattern.
     */
    public static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z0-9_\\-]+");

    /**
     * Prefix of property keys about processs.
     */
    public static final String KEY_PREFIX = "process" + QUALIFIER; //$NON-NLS-1$

    private final String name;

    private final Class<? extends ProcessProvider> providerClass;

    private final ProfileContext context;

    private final Map<String, String> configuration;

    /**
     * Creates a new instance.
     * @param name the name of this process
     * @param providerClass the class which can provide this process
     * @param context the current profile context
     * @param configuration the extra configuration
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public ProcessProfile(
            String name,
            Class<? extends ProcessProvider> providerClass,
            ProfileContext context,
            Map<String, String> configuration) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (providerClass == null) {
            throw new IllegalArgumentException("providerClass must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        if (isValidName(name) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "name must consist of [0-9A-Za-z_-]",
                    name));
        }
        this.name = name;
        this.providerClass = providerClass;
        this.context = context;
        this.configuration = Collections.unmodifiableMap(new TreeMap<>(configuration));
    }

    /**
     * Returns this name.
     * @return this name
     */
    public String getName() {
        return name;
    }

    @Override
    public Class<? extends ProcessProvider> getProviderClass() {
        return providerClass;
    }

    @Override
    public ProfileContext getContext() {
        return context;
    }

    /**
     * Returns the extra configuration of this process.
     * @return the configuration
     */
    public Map<String, String> getConfiguration() {
        return configuration;
    }

    @Override
    protected ProcessProfile getThis() {
        return this;
    }

    /**
     * Loads process profiles from the properties.
     * @param properties source properties
     * @param loader class loader to load the {@link ProcessProvider}
     * @return the loaded profiles
     * @throws IllegalArgumentException if properties are invalid, or if any parameter is {@code null}
     * @deprecated use {@link #loadFrom(Properties, ProfileContext)} instead
     */
    @Deprecated
    public static Collection<? extends ProcessProfile> loadFrom(Properties properties, ClassLoader loader) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (loader == null) {
            throw new IllegalArgumentException("loader must not be null"); //$NON-NLS-1$
        }
        return loadFrom(properties, ProfileContext.system(loader));
    }

    /**
     * Loads process profiles from the properties.
     * @param properties source properties
     * @param context the curren profile context
     * @return the loaded profiles
     * @throws IllegalArgumentException if properties are invalid, or if any parameter is {@code null}
     */
    public static Collection<? extends ProcessProfile> loadFrom(Properties properties, ProfileContext context) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Restoring processes profile");
        List<ProcessProfile> results = new ArrayList<>();
        Map<String, Map<String, String>> processs = partitioning(properties);
        for (Map.Entry<String, Map<String, String>> partitionPair : processs.entrySet()) {
            String name = partitionPair.getKey();
            LOG.debug("Restoring process profile: {}",
                    name);
            Map<String, String> partition = partitionPair.getValue();
            assert isValidName(name);

            String className = partition.remove(name);
            assert className != null;

            Map<String, String> conf = PropertiesUtil.createPrefixMap(partition, name + QUALIFIER);
            Class<? extends ProcessProvider> loaded = loadProviderClass(className, context, ProcessProvider.class);

            ProcessProfile profile = new ProcessProfile(name, loaded, context, conf);
            results.add(profile);
        }
        return results;
    }

    private static Map<String, Map<String, String>> partitioning(Properties properties) {
        assert properties != null;
        NavigableMap<String, String> map = PropertiesUtil.createPrefixMap(properties, KEY_PREFIX);
        Map<String, Map<String, String>> results = new TreeMap<>();
        while (map.isEmpty() == false) {
            String name = map.firstKey();
            if (isValidName(name) == false) {
                WGLOG.error("E02005",
                        name);
                throw new IllegalArgumentException(MessageFormat.format(
                        "Invalid process name: \"{0}\"",
                        name));
            }
            String first = name + QUALIFIER;
            String last = name + (char) (QUALIFIER + 1);
            Map<String, String> partition = new TreeMap<>(map.subMap(first, false, last, false));
            partition.put(map.firstKey(), map.firstEntry().getValue());
            results.put(name, partition);
            for (String key : partition.keySet()) {
                map.remove(key);
            }
        }
        return results;
    }

    private static boolean isValidName(String name) {
        assert name != null;
        return NAME_PATTERN.matcher(name).matches();
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
        LOG.debug("Saving process profile: {}",
                getName());
        String providerKey = KEY_PREFIX + name;
        String keyPrefix = providerKey + QUALIFIER;
        PropertiesUtil.checkAbsentKey(properties, providerKey);
        PropertiesUtil.checkAbsentKeyPrefix(properties, keyPrefix);
        properties.setProperty(providerKey, providerClass.getName());
        for (Map.Entry<String, String> entry : configuration.entrySet()) {
            properties.setProperty(keyPrefix + entry.getKey(), entry.getValue());
        }
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
