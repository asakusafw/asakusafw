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
package com.asakusafw.testdriver.file;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates {@link Configuration}s with system defaults.
 * @since 0.2.0
 */
public class ConfigurationFactory {

    static final Logger LOG = LoggerFactory.getLogger(ConfigurationFactory.class);

    private final ClassLoader loader;

    /**
     * Creates a new instance.
     * @param defaultConfigPath the default configuration path (ordinary $HADOOP_HOME/conf)
     * @see #getDefault()
     */
    public ConfigurationFactory(URL defaultConfigPath) {
        ClassLoader current = getBaseClassLoader();
        this.loader = createLoader(current, defaultConfigPath);
    }

    /**
     * Returns a default factory which referes the System Hadoop configuration path.
     * <p>
     * If $HADOOP_HOME is not defined your system,
     * this returns a factory which use the current context classloader.
     * </p>
     * @return a default factory object
     */
    public static ConfigurationFactory getDefault() {
        URL path = getConfigurationPath();
        return new ConfigurationFactory(path);
    }

    private static URL getConfigurationPath() {
        LOG.debug("Detecting default configuration");
        String homeString = System.getenv("HADOOP_HOME");
        if (homeString == null) {
            LOG.warn(MessageFormat.format(
                    "Failed to load default Hadoop configurations (${0} is not defined)",
                    "HADOOP_HOME"));
            return null;
        }
        LOG.debug("Hadoop is installed at: {}", homeString);
        File home = new File(homeString);
        File conf = new File(home, "conf");
        if (conf.isDirectory() == false) {
            LOG.warn(MessageFormat.format(
                    "Failed to load default Hadoop configurations (${0} is not a valid installation path)",
                    conf));
            return null;
        }
        try {
            return conf.toURI().toURL();
        } catch (MalformedURLException e) {
            LOG.warn(MessageFormat.format(
                    "Failed to load default Hadoop configurations (${0} is unrecognized to convert URL)",
                    conf), e);
            return null;
        }
    }

    private ClassLoader getBaseClassLoader() {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        if (current == null) {
            current = getClass().getClassLoader();
        }
        return current;
    }

    private ClassLoader createLoader(
            final ClassLoader current,
            final URL defaultConfigPath) {
        if (defaultConfigPath != null) {
            ClassLoader ehnahced = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                @Override
                public ClassLoader run() {
                    return new URLClassLoader(new URL[] { defaultConfigPath }, current);
                }
            });
            if (ehnahced != null) {
                return ehnahced;
            }
        }
        return current;
    }

    /**
     * Creates a new default configuration.
     * @return the created configuration
     */
    public Configuration newInstance() {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            Configuration conf = new Configuration(true);
            conf.setStrings("fs.file.impl", AsakusaTestLocalFileSystem.class.getName());
            return conf;
        } finally {
            Thread.currentThread().setContextClassLoader(context);
        }
    }
}
