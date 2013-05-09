/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.windgate.core.session;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.BaseProfile;
import com.asakusafw.windgate.core.ProfileContext;
import com.asakusafw.windgate.core.WindGateCoreLogger;
import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.util.PropertiesUtil;

/**
 * A configuration for sessions.
 * @since 0.2.2
 */
public class SessionProfile extends BaseProfile<SessionProfile, SessionProvider> {

    static final WindGateLogger WGLOG = new WindGateCoreLogger(SessionProfile.class);

    static final Logger LOG = LoggerFactory.getLogger(SessionProfile.class);

    /**
     * Key name of session provider.
     */
    public static final String KEY_PROVIDER = "session";

    /**
     * Prefix of property keys about session.
     */
    public static final String KEY_PREFIX = KEY_PROVIDER + QUALIFIER;

    private final Class<? extends SessionProvider> providerClass;

    private final ProfileContext context;

    private final Map<String, String> configuration;

    /**
     * Creates a new instance.
     * @param providerClass the class which can provide the session
     * @param context the current profile context
     * @param configuration the extra configuration
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public SessionProfile(
            Class<? extends SessionProvider> providerClass,
            ProfileContext context,
            Map<String, String> configuration) {
        if (providerClass == null) {
            throw new IllegalArgumentException("providerClass must not be null"); //$NON-NLS-1$
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        this.providerClass = providerClass;
        this.context = context;
        this.configuration = Collections.unmodifiableMap(new TreeMap<String, String>(configuration));
    }

    @Override
    public Class<? extends SessionProvider> getProviderClass() {
        return providerClass;
    }

    @Override
    public ProfileContext getContext() {
        return context;
    }

    /**
     * Returns the extra configuration of the session.
     * @return the configuration
     */
    public Map<String, String> getConfiguration() {
        return configuration;
    }

    @Override
    protected SessionProfile getThis() {
        return this;
    }

    /**
     * Loads a session profile from the properties.
     * @param properties source properties
     * @param loader class loader to load the {@link SessionProvider}
     * @return the loaded profile
     * @throws IllegalArgumentException if properties are invalid, or if any parameter is {@code null}
     * @deprecated use {@link #loadFrom(Properties, ProfileContext)} instead
     */
    @Deprecated
    public static SessionProfile loadFrom(Properties properties, ClassLoader loader) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (loader == null) {
            throw new IllegalArgumentException("loader must not be null"); //$NON-NLS-1$
        }
        return loadFrom(properties, ProfileContext.system(loader));
    }

    /**
     * Loads a session profile from the properties.
     * @param properties source properties
     * @param context the current profile context
     * @return the loaded profile
     * @throws IllegalArgumentException if properties are invalid, or if any parameter is {@code null}
     * @since 0.2.4
     */
    public static SessionProfile loadFrom(Properties properties, ProfileContext context) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Restoring session profile");
        String className = properties.getProperty(KEY_PROVIDER);
        if (className == null) {
            WGLOG.error("E020006");
            throw new IllegalArgumentException(MessageFormat.format(
                    "Session provider is not specified: {0}",
                    KEY_PROVIDER));
        }
        Class<? extends SessionProvider> provider = loadProviderClass(className, context, SessionProvider.class);
        Map<String, String> config = PropertiesUtil.createPrefixMap(properties, KEY_PREFIX);
        return new SessionProfile(provider, context, config);
    }

    /**
     * Stores this profile into the specified properties.
     * @param properties target properties object
     * @throws IllegalArgumentException if target properties already contains keys about this session,
     *     or if any parameter is {@code null}
     */
    public void storeTo(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Saving session profile: {}");
        PropertiesUtil.checkAbsentKey(properties, KEY_PROVIDER);
        PropertiesUtil.checkAbsentKeyPrefix(properties, KEY_PREFIX);
        properties.setProperty(KEY_PROVIDER, providerClass.getName());
        for (Map.Entry<String, String> entry : configuration.entrySet()) {
            properties.setProperty(KEY_PREFIX + entry.getKey(), entry.getValue());
        }
    }

    /**
     * Removes entries corresponding to resource profiles.
     * @param properties target properties
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public static void removeCorrespondingKeys(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        properties.remove(KEY_PROVIDER);
        PropertiesUtil.removeKeyPrefix(properties, KEY_PREFIX);
    }
}
