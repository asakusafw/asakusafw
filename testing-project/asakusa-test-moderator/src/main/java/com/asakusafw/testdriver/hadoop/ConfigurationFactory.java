/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
import org.apache.hadoop.fs.LocalFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;

/**
 * Creates {@link Configuration}s with system defaults.
 * @since 0.2.5
 * @version 0.6.0
 */
public class ConfigurationFactory extends ConfigurationProvider {

    /**
     * The system property key of {@link LocalFileSystem} implementation class name.
     */
    public static String KEY_LOCAL_FILE_SYSTEM = "asakusa.testdriver.fs";

    static final Logger LOG = LoggerFactory.getLogger(ConfigurationFactory.class);

    private Preferences preferences;

    /**
     * Creates a new instance.
     * @see #getDefault()
     */
    public ConfigurationFactory() {
        this(Preferences.getDefault());
    }

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
     * @param preferences the preferences for this instance
     * @see #getDefault()
     * @since 0.6.0
     */
    public ConfigurationFactory(Preferences preferences) {
        super(preferences.defaultConfigPath);
        this.preferences = preferences;
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
        if (preferences.localFileSystemClassName != null) {
            configuration.set("fs.file.impl", preferences.localFileSystemClassName);
            configuration.setBoolean("fs.fs.impl.disable.cache", true);
        }
    }

    /**
     * Preferences for {@link ConfigurationFactory}.
     * @since 0.6.0
     */
    public static class Preferences {

        String localFileSystemClassName;

        URL defaultConfigPath;

        /**
         * Sets a implementation class name of {@link LocalFileSystem}.
         * @param className the class name
         */
        public void setLocalFileSystemClassName(String className) {
            this.localFileSystemClassName = className;
        }

        /**
         * Returns a {@link Preferences} with default values.
         * @return default preferences
         */
        public static Preferences getDefault() {
            Preferences prefs = new Preferences();
            prefs.localFileSystemClassName =
                    System.getProperty(KEY_LOCAL_FILE_SYSTEM, AsakusaTestLocalFileSystem.class.getName());
            prefs.defaultConfigPath = null;
            return prefs;
        }
    }
}
