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
package com.asakusafw.runtime.core;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;

/**
 * Resource configuration object for Hadoop environment.
 * @since 0.1.0
 * @version 0.7.3
 */
public class HadoopConfiguration extends Configured implements ResourceConfiguration {

    private final Configuration configration;

    /**
     * Creates a new instance with an empty Hadoop configuration.
     */
    public HadoopConfiguration() {
        this(new Configuration(false));
    }

    /**
     * Creates a new instance.
     * @param configuration configuration object to be wrapped
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public HadoopConfiguration(Configuration configuration) {
        super(configuration);
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        this.configration = configuration;
    }

    @Override
    public String get(String keyName, String defaultValue) {
        if (keyName == null) {
            throw new IllegalArgumentException("keyName must not be null"); //$NON-NLS-1$
        }
        return configration.get(keyName, defaultValue);
    }

    @Override
    public void set(String keyName, String value) {
        if (keyName == null) {
            throw new IllegalArgumentException("keyName must not be null"); //$NON-NLS-1$
        }
        configration.set(keyName, value);
    }

    @Override
    public ClassLoader getClassLoader() {
        return configration.getClassLoader();
    }
}
