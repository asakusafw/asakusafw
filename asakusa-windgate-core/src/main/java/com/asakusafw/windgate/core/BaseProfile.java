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

import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract superclass of any profiles.
 * @param <S> this class type
 * @param <T> target provider type
 * @since 0.2.3
 */
public abstract class BaseProfile<S extends BaseProfile<S, T>, T extends BaseProvider<S>> {

    static final Logger LOG = LoggerFactory.getLogger(BaseProfile.class);

    /**
     * Key qualifier.
     */
    public static final char QUALIFIER = '.';

    /**
     * Returns the class of corresponded provider.
     * @return the provider class
     */
    public abstract Class<? extends T> getProviderClass();

    /**
     * Returns the plugin class loader.
     * @return the class loader
     */
    public abstract ClassLoader getClassLoader();

    /**
     * Returns this object.
     * @return this object
     */
    protected abstract S getThis();

    /**
     * Creates a new provider instance
     * and attach this profile to the created instance.
     * @return the created instance
     * @throws IOException if failed to create an instance or attach this profile
     */
    public T createProvider() throws IOException {
        LOG.debug("Creating a provider instance: {}",
                getProviderClass().getName());
        try {
            T instance = getProviderClass().newInstance();
            instance.configure(getThis());
            return instance;
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException(MessageFormat.format(
                    "Failed to create a provider: {0}",
                    getProviderClass().getName()), e);
        }
    }

    /**
     * Loads the specified class from the class loader.
     * The target class must be a subclass of the specified provider.
     * @param <T> provider interface type
     * @param className target class name
     * @param loader target class loader
     * @param providerInterface provider interface
     * @return the loaded class
     * @throws IllegalArgumentException if failed to load the specified class, or any parameter is {@code null}
     */
    protected static <T extends BaseProvider<?>> Class<? extends T> loadProviderClass(
            String className,
            ClassLoader loader,
            Class<T> providerInterface) {
        if (className == null) {
            throw new IllegalArgumentException("className must not be null"); //$NON-NLS-1$
        }
        if (loader == null) {
            throw new IllegalArgumentException("loader must not be null"); //$NON-NLS-1$
        }
        if (providerInterface == null) {
            throw new IllegalArgumentException("providerInterface must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Loading provider class: {}",
                className);
        Class<?> loaded;
        try {
            loaded = loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Failed to load a provider \"{0}\"",
                    className), e);
        }
        if (providerInterface.isAssignableFrom(loaded) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Class \"{0}\" must be subtype of \"{1}\"",
                    className,
                    providerInterface.getName()));
        }
        return loaded.asSubclass(providerInterface);
    }
}
