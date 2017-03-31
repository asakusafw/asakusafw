/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.LocalFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;
import com.asakusafw.testdriver.core.TestingEnvironmentConfigurator;

/**
 * Creates {@link Configuration}s with system defaults.
 * @since 0.2.5
 * @version 0.6.0
 */
public class ConfigurationFactory extends ConfigurationProvider {

    static final Logger LOG = LoggerFactory.getLogger(ConfigurationFactory.class);

    /**
     * The system property key of {@link LocalFileSystem} implementation class name.
     * @since 0.6.0
     */
    public static final String KEY_LOCAL_FILE_SYSTEM = "asakusa.testdriver.fs"; //$NON-NLS-1$

    /**
     * The system property key of Hadoop configuration path.
     * @since 0.6.0
     */
    public static final String KEY_EXPLICIT_HADOOP_CONF = "asakusa.testdriver.hadoop.conf"; //$NON-NLS-1$

    /**
     * The system property key of Hadoop command path.
     * @since 0.6.0
     */
    public static final String KEY_EXPLICIT_HADOOP_COMMAND = "asakusa.testdriver.hadoop.command"; //$NON-NLS-1$

    /**
     * The default value of {@link LocalFileSystem} implementation class name.
     */
    static final String DEFAULT_LOCAL_FILE_SYSTEM = AsakusaTestLocalFileSystem.class.getName();

    static {
        TestingEnvironmentConfigurator.initialize();
    }

    private final Preferences preferences;

    /**
     * Creates a new instance.
     * @see #getDefault()
     */
    private ConfigurationFactory() {
        this(Preferences.getDefault());
    }

    /**
     * Creates a new instance.
     * @param defaultConfigPath the default configuration path
     * @see #getDefault()
     */
    public ConfigurationFactory(URL defaultConfigPath) {
        super(defaultConfigPath);
        this.preferences = Preferences.getDefault();
    }

    /**
     * Creates a new instance.
     * @param preferences the preferences for this instance
     * @see #getDefault()
     * @since 0.6.0
     */
    public ConfigurationFactory(Preferences preferences) {
        super(preferences.getConfigurationPath());
        this.preferences = preferences;
    }

    /**
     * Returns a default factory which refers the System Hadoop configuration path.
     * <p>
     * If Hadoop installation is not found in your system,
     * this returns a factory which use the current context classloader.
     * </p>
     * @return a default factory object
     */
    public static ConfigurationFactory getDefault() {
        return new ConfigurationFactory();
    }

    /**
     * Returns the Hadoop command path for this configuration.
     * @return the Hadoop command path, or {@code null} if it is not found
     */
    public File getHadoopCommand() {
        return preferences.getHadoopCommand();
    }

    @Override
    protected void configure(Configuration configuration) {
        if (preferences.getLocalFileSystemClassName() != null) {
            configuration.set("fs.file.impl", preferences.getLocalFileSystemClassName()); //$NON-NLS-1$
            configuration.setBoolean("fs.file.impl.disable.cache", true); //$NON-NLS-1$
        }
    }

    /**
     * Preferences for {@link ConfigurationFactory}.
     * @since 0.6.0
     */
    public static class Preferences {

        private String localFileSystemClassName;

        private File explicitConfigurationPath;

        private File explicitCommandPath;

        private final Map<String, String> environmentVariables = new HashMap<>(System.getenv());

        /**
         * Sets the implementation class name of {@link LocalFileSystem}.
         * @param className the class name
         */
        public void setLocalFileSystemClassName(String className) {
            this.localFileSystemClassName = className;
        }

        /**
         * Sets the explicit Hadoop configuration path.
         * @param path the Hadoop configuration path, or {@code null} if it should be inferred
         */
        public void setExplicitConfigurationPath(File path) {
            this.explicitConfigurationPath = path;
        }

        /**
         * Sets the explicit Hadoop command path.
         * @param path the Hadoop command path, or {@code null} if it should be inferred
         */
        public void setExplicitCommandPath(File path) {
            this.explicitCommandPath = path;
        }

        /**
         * Returns the implementation class name of {@link LocalFileSystem}.
         * @return the class name, or {@code null} if it is not defined
         */
        public String getLocalFileSystemClassName() {
            return localFileSystemClassName;
        }

        /**
         * Returns the current environment variables.
         * Clients can modify the returned map.
         * @return the environment variables
         */
        public Map<String, String> getEnvironmentVariables() {
            return environmentVariables;
        }

        /**
         * Returns the explicitly defined Hadoop configuration path.
         * @return the explicit Hadoop configuration path, or {@code null} if it is not defined
         */
        public File getExplicitConfigurationPath() {
            return explicitConfigurationPath;
        }

        /**
         * Returns the explicitly defined Hadoop command path.
         * @return the explicit Hadoop command path, or {@code null} if it is not defined
         */
        public File getExplicitCommandPath() {
            return explicitCommandPath;
        }

        /**
         * Returns the Hadoop configuration path.
         * @return the Hadoop configuration path, or {@code null} if it is not found
         * @see #getExplicitConfigurationPath()
         */
        public URL getConfigurationPath() {
            if (explicitConfigurationPath != null) {
                try {
                    return explicitConfigurationPath.toURI().toURL();
                } catch (MalformedURLException e) {
                    LOG.error(MessageFormat.format(
                            Messages.getString(
                                    "ConfigurationFactory.errorInvalidHadoopConfigurationPath"), //$NON-NLS-1$
                            explicitConfigurationPath), e);
                }
            }
            return ConfigurationProvider.getConfigurationPath(environmentVariables);
        }

        /**
         * Returns the Hadoop command path.
         * @return the Hadoop command path, or {@code null} if it is not found
         */
        public File getHadoopCommand() {
            if (explicitCommandPath != null) {
                return explicitCommandPath;
            }
            return ConfigurationProvider.findHadoopCommand(environmentVariables);
        }

        /**
         * Returns a {@link Preferences} with default values.
         * @return default preferences
         */
        public static Preferences getDefault() {
            Preferences prefs = new Preferences();
            prefs.setLocalFileSystemClassName(System.getProperty(KEY_LOCAL_FILE_SYSTEM, DEFAULT_LOCAL_FILE_SYSTEM));
            prefs.setExplicitConfigurationPath(getFile(KEY_EXPLICIT_HADOOP_CONF));
            prefs.setExplicitCommandPath(getFile(KEY_EXPLICIT_HADOOP_COMMAND));
            return prefs;
        }

        private static File getFile(String key) {
            String value = System.getProperty(key);
            if (value == null) {
                return null;
            }
            return new File(value);
        }
    }
}
