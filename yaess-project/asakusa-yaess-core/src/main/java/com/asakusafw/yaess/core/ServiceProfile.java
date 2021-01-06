/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
import java.util.Properties;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.util.PropertiesUtil;

/**
 * A common service profile format for configurable instances.
<pre><code>
&lt;prefix&gt; = &lt;fully qualified class name which extends T&gt;
&lt;prefix&gt;.&lt;key1&gt; = &lt;value1&gt;
&lt;prefix&gt;.&lt;key2&gt; = &lt;value2&gt;
...
</code></pre>
 * @param <T> the base service class
 * @since 0.2.3
 * @version 0.4.0
 */
public class ServiceProfile<T extends Service> {

    static final Logger LOG = LoggerFactory.getLogger(ServiceProfile.class);

    private final String prefix;

    private final Class<? extends T> serviceClass;

    private final Map<String, String> configuration;

    private final ProfileContext context;

    /**
     * Creates a new instance.
     * @param prefix the key prefix of this profile
     * @param serviceClass the service class
     * @param configuration configuration for service class
     * @param context the current profile context
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ServiceProfile(
            String prefix,
            Class<? extends T> serviceClass,
            Map<String, String> configuration,
            ProfileContext context) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null"); //$NON-NLS-1$
        }
        if (serviceClass == null) {
            throw new IllegalArgumentException("serviceClass must not be null"); //$NON-NLS-1$
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        this.prefix = prefix;
        this.serviceClass = serviceClass;
        this.configuration = Collections.unmodifiableMap(new TreeMap<>(configuration));
        this.context = context;
    }

    /**
     * Returns the key prefix of this profile.
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the service class.
     * @return the service class
     */
    public Class<? extends T> getServiceClass() {
        return serviceClass;
    }

    /**
     * Return the optional configuration for the service.
     * @return the configuration
     */
    public Map<String, String> getConfiguration() {
        return configuration;
    }

    /**
     * Returns the target configuration.
     * @param key the configuration key
     * @param mandatory whether the configuration is mandatory
     * @param resolve whether resolves the configuration
     * @return the corresponded configuration, or {@code null} if is not defined/empty
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.4.0
     */
    public String getConfiguration(String key, boolean mandatory, boolean resolve) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
        }
        String value = getConfiguration().get(key);
        return normalize(key, value, mandatory, resolve);
    }

    /**
     * Normalizes the configuration value.
     * @param key the configuration key
     * @param value the configuration value (nullable if only is not mandatory)
     * @param mandatory whether the configuration is mandatory
     * @param resolve whether resolves the configuration
     * @return the normalized value, or {@code null} if is not defined/empty
     * @throws IllegalArgumentException if some parameters were {@code null}, or failed to normalize
     * @since 0.4.0
     */
    public String normalize(String key, String value, boolean mandatory, boolean resolve) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null"); //$NON-NLS-1$
        }
        String string = value;
        if (string == null) {
            if (mandatory) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "The profile \"{0}\" must not be defined",
                        getPrefix() + '.' + key));
            } else {
                return null;
            }
        }
        string = string.trim();
        if (resolve) {
            try {
                string = getContext().getContextParameters().replace(string, true);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Failed to resolve the profile \"{0}\": {1}",
                        getPrefix() + '.' + key,
                        string), e);
            }
        }
        if (string.isEmpty()) {
            if (mandatory) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "The profile \"{0}\" must not be defined",
                        getPrefix() + '.' + key));
            } else {
                return null;
            }
        }
        return string;
    }

    /**
     * Returns the current profile context.
     * @return the current profile context
     */
    public ProfileContext getContext() {
        return context;
    }

    /**
     * Returns the class loader which loaded this service class.
     * @return the class loader
     * @deprecated use {@link #getContext()} instead
     */
    @Deprecated
    public ClassLoader getClassLoader() {
        return getContext().getClassLoader();
    }

    /**
     * Creates a new instance using context parameters.
     * The created service will automatically {@link Service#configure(ServiceProfile) configured}
     * by using this profile.
     * @return the created instance.
     * @throws InterruptedException if interrupted in configuring the target service
     * @throws IOException if failed to create or configure the service
     * @since 0.2.4
     */
    public T newInstance() throws InterruptedException, IOException {
        LOG.debug("Creating new instance for {}: {}", prefix, serviceClass.getName());
        T instance;
        try {
            instance = serviceClass.newInstance();
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Failed to create a new service instance for {0}: {1}",
                    getPrefix(),
                    getServiceClass().getName()), e);
        }
        instance.configure(this);
        return instance;
    }

    /**
     * Creates a new instance.
     * The created service will automatically {@link Service#configure(ServiceProfile) configured}
     * by using this profile.
     * @param variables the variable resolver
     * @return the created instance.
     * @throws InterruptedException if interrupted in configuring the target service
     * @throws IOException if failed to create or configure the service
     * @deprecated use {@link #newInstance()} instead
     */
    @Deprecated
    public T newInstance(VariableResolver variables) throws InterruptedException, IOException {
        if (variables == null) {
            throw new IllegalArgumentException("variables must not be null"); //$NON-NLS-1$
        }
        return newInstance();
    }

    /**
     * Loads a service profile with the specified key prefix.
     * @param <T> the base class of target class
     * @param properties source properties
     * @param prefix the key prefix
     * @param serviceBaseClass the base class of service class
     * @param classLoader the class loader to load the service class
     * @return the loaded profile
     * @throws IllegalArgumentException if the target profile is invalid, or parameters contain {@code null}
     * @deprecated use {@link #load(Properties, String, Class, ProfileContext)} instead
     */
    @Deprecated
    public static <T extends Service> ServiceProfile<T> load(
            Properties properties,
            String prefix,
            Class<T> serviceBaseClass,
            ClassLoader classLoader) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null"); //$NON-NLS-1$
        }
        if (serviceBaseClass == null) {
            throw new IllegalArgumentException("serviceBaseClass must not be null"); //$NON-NLS-1$
        }
        if (classLoader == null) {
            throw new IllegalArgumentException("classLoader must not be null"); //$NON-NLS-1$
        }
        return load(properties, prefix, serviceBaseClass, ProfileContext.system(classLoader));
    }

    /**
     * Loads a service profile with the specified key prefix.
     * @param <T> the base class of target class
     * @param properties source properties
     * @param prefix the key prefix
     * @param serviceBaseClass the base class of service class
     * @param context the current profile context
     * @return the loaded profile
     * @throws IllegalArgumentException if the target profile is invalid, or parameters contain {@code null}
     * @since 0.2.4
     */
    public static <T extends Service> ServiceProfile<T> load(
            Properties properties,
            String prefix,
            Class<T> serviceBaseClass,
            ProfileContext context) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null"); //$NON-NLS-1$
        }
        if (serviceBaseClass == null) {
            throw new IllegalArgumentException("serviceBaseClass must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        String targetClassName = properties.getProperty(prefix);
        if (targetClassName == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "\"{0}\" is not defined in properties",
                    prefix));
        }
        Class<?> loaded;
        try {
            loaded = context.getClassLoader().loadClass(targetClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Faild load a service class defined in \"{0}\": {1}",
                    prefix,
                    targetClassName));
        }
        if (serviceBaseClass.isAssignableFrom(loaded) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid service class defined in \"{0}\", it must be subtype of {2}: {1}",
                    prefix,
                    targetClassName,
                    serviceBaseClass.getName()));
        }
        Class<? extends T> targetClass = loaded.asSubclass(serviceBaseClass);
        Map<String, String> conf = PropertiesUtil.createPrefixMap(properties, prefix + '.');
        return new ServiceProfile<>(prefix, targetClass, conf, context);
    }

    /**
     * Merges this profile into the specified properties.
     * If properties already contains entries related to this profile,
     * then this method will overwrite them.
     * @param properties target properties
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void storeTo(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        properties.setProperty(prefix, getServiceClass().getName());
        for (Map.Entry<String, String> entry : getConfiguration().entrySet()) {
            properties.setProperty(prefix + '.' + entry.getKey(), entry.getValue());
        }
    }
}
