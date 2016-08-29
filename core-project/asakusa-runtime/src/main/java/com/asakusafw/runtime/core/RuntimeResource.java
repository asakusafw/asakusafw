/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.runtime.core;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Runtime resource which has resource lifecycle.
 */
public interface RuntimeResource {

    /**
     * Initializes this resource.
     * @param configuration the current configuration
     * @throws IOException if failed to initialize this resource
     * @throws InterruptedException if interrupted while initializing this resource
     * @throws IllegalArgumentException if configuration is not valid
     * @throws IllegalStateException if resource lifecycle has something wrong
     */
    default void setup(ResourceConfiguration configuration) throws IOException, InterruptedException {
        return;
    }

    /**
     * Finalizes this resource.
     * @param configuration the current configuration
     * @throws IOException if failed to finalizing this resource
     * @throws InterruptedException if interrupted while finalizing this resource
     * @throws IllegalArgumentException if configuration is not valid
     * @throws IllegalStateException if resource lifecycle has something wrong
     */
    default void cleanup(ResourceConfiguration configuration) throws IOException, InterruptedException {
        return;
    }

    /**
     * A skeletal implementation of registering/unregistering resource delegation objects.
     * @param <D> the delegation object type
     */
    abstract class DelegateRegisterer<D> implements RuntimeResource {

        static final Log LOG = LogFactory.getLog(RuntimeResource.DelegateRegisterer.class);

        private D registered;

        /**
         * Returns the configuration key of the delegation object class name.
         * @return the delegation object class name
         */
        protected abstract String getClassNameKey();

        /**
         * Returns the interface type of the delegation object.
         * @return the delegation object interface type
         */
        protected abstract Class<? extends D> getInterfaceType();

        /**
         * Registers the delegation object.
         * @param delegate the delegation object
         * @param configuration the current configuration
         * @throws IOException if failed to register the object by I/O error
         * @throws InterruptedException if interrupted while registering the object
         */
        protected abstract void register(D delegate, ResourceConfiguration configuration)
                throws IOException, InterruptedException;

        /**
         * Unregisters the delegation object.
         * @param delegate the delegation object
         * @param configuration the current configuration
         * @throws IOException if failed to unregister the object by I/O error
         * @throws InterruptedException if interrupted while unregistering the object
         */
        protected abstract void unregister(D delegate, ResourceConfiguration configuration)
                throws IOException, InterruptedException;

        @Override
        public void setup(ResourceConfiguration configuration) throws IOException,
                InterruptedException {
            String className = configuration.get(getClassNameKey(), null);
            if (className == null) {
                LOG.warn(MessageFormat.format(
                        "Missing \"{0}\" in plugin configurations (API:{1})",
                        getClassNameKey(),
                        getInterfaceType().getName()));
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Loading plugin for {0}: key={1}, value={2}", //$NON-NLS-1$
                        getInterfaceType().getName(),
                        getClassNameKey(),
                        className));
            }
            D loaded = loadDelegate(configuration, className);

            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Registering plugin {2} into {0}", //$NON-NLS-1$
                        getInterfaceType().getName(),
                        getClassNameKey(),
                        className));
            }
            register(loaded, configuration);

            this.registered = loaded;
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Registered plugin {2} into {0}", //$NON-NLS-1$
                        getInterfaceType().getName(),
                        getClassNameKey(),
                        className));
            }
        }

        @Override
        public void cleanup(ResourceConfiguration configuration)
                throws IOException, InterruptedException {
            if (registered != null) {
                unregister(registered, configuration);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Unregistered plugin {1} from {0}", //$NON-NLS-1$
                            getInterfaceType().getName(),
                            registered.getClass().getName()));
                }
                registered = null;
            }
        }

        private D loadDelegate(ResourceConfiguration configuration, String className) throws IOException {
            assert configuration != null;
            assert className != null;
            try {
                Class<?> aClass = configuration.getClassLoader().loadClass(className);
                Class<? extends D> delegate = aClass.asSubclass(getInterfaceType());
                D instance = delegate.newInstance();
                return instance;
            } catch (Exception e) {
                throw new IOException(MessageFormat.format(
                        "Failed to initialize a plugin {0}",
                        className), e);
            }
        }
    }
}
