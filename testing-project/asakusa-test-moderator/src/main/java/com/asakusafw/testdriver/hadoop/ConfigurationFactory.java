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
package com.asakusafw.testdriver.hadoop;

import java.net.URL;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;

/**
 * Creates {@link Configuration}s with system defaults.
 * @since 0.2.5
 */
public class ConfigurationFactory extends ConfigurationProvider {

    static final Logger LOG = LoggerFactory.getLogger(ConfigurationFactory.class);

    /**
     * Creates a new instance.
     * @param defaultConfigPath the default configuration path
     * @see #getDefault()
     */
    public ConfigurationFactory(URL defaultConfigPath) {
        super(defaultConfigPath);
    }

    /**
     * Creates a new instance.
     * @see #getDefault()
     */
    public ConfigurationFactory() {
        super();
    }

    /**
     * Returns a default factory which referes the System Hadoop configuration path.
     * <p>
     * If Hadoop installation is not found in your system,
     * this returns a factory which use the current context classloader.
     * </p>
     * @return a default factory object
     */
    public static ConfigurationFactory getDefault() {
        return new ConfigurationFactory();
    }

    @Override
    protected void configure(Configuration configuration) {
        configuration.set("fs.file.impl", AsakusaTestLocalFileSystem.class.getName());
    }
}
