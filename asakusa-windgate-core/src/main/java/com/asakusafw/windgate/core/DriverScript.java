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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * A script describes drivers' behaviour.
 * @since 0.2.2
 */
public class DriverScript {

    /**
     * Prefix of property keys about source drivers.
     */
    public static final String PREFIX_SOURCE = "source";

    /**
     * Prefix of property keys about drain drivers.
     */
    public static final String PREFIX_DRAIN = "drain";

    private final String resourceName;

    private final Map<String, String> configuration;

    /**
     * Creates a new instance.
     * @param resource the name of target resource
     * @param configuration configurations for this driver
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public DriverScript(String resource, Map<String, String> configuration) {
        if (resource == null) {
            throw new IllegalArgumentException("resource must not be null"); //$NON-NLS-1$
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        this.resourceName = resource;
        this.configuration = Collections.unmodifiableMap(new TreeMap<String, String>(configuration));
    }

    /**
     * Returns the name of target resource.
     * This represents a {@code resource name} defined in profile.
     * @return the resource name
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Returns the configuration for this driver.
     * @return the configuration
     */
    public Map<String, String> getConfiguration() {
        return configuration;
    }

    /**
     * Driver kind (source / drain).
     * @since 0.2.2
     */
    public enum Kind {

        /**
         * Source drivers.
         */
        SOURCE(PREFIX_SOURCE) {
            @Override
            public Kind opposite() {
                return DRAIN;
            }
        },

        /**
         * Drain drivers.
         */
        DRAIN(PREFIX_DRAIN) {
            @Override
            public Kind opposite() {
                return SOURCE;
            }
        },
        ;

        /**
         * Property key prefix for this kind.
         */
        public final String prefix;

        private Kind(String prefix) {
            assert prefix != null;
            this.prefix = prefix;
        }

        /**
         * Returns the opposite kind.
         * @return the opposite kind
         */
        public abstract Kind opposite();
    }
}
