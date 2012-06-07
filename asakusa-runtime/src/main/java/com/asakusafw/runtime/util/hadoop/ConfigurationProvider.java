/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.runtime.util.hadoop;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;

/**
 * Creates {@link Configuration}s with system defaults.
 * @since 0.4.0
 */
public class ConfigurationProvider {

    private static final String ENV_PATH = "PATH";

    private static final String ENV_HADOOP_CONF = "HADOOP_CONF";

    private static final String ENV_HADOOP_HOME = "HADOOP_HOME";

    private static final String PATH_HADOOP_COMMAND_FILE = "hadoop";

    private static final String PATH_HADOOP_COMMAND = "bin/hadoop";

    private static final String PATH_CONF_DIR_0 = "conf";

    private static final String PATH_CONF_DIR_0_TESTER = "conf/hadoop-env.sh";

    private static final String PATH_CONF_DIR_1 = "etc/hadoop";

    static final Log LOG = LogFactory.getLog(ConfigurationProvider.class);

    private final ClassLoader loader;

    /**
     * Creates a new instance.
     */
    public ConfigurationProvider() {
        this(System.getenv());
    }

    ConfigurationProvider(Map<String, String> envp) {
        this(getConfigurationPath(envp));
    }

    /**
     * Creates a new instance.
     * @param defaultConfigPath the default configuration path
     */
    public ConfigurationProvider(URL defaultConfigPath) {
        ClassLoader current = getBaseClassLoader();
        this.loader = createLoader(current, defaultConfigPath);
    }

    private static URL getConfigurationPath(Map<String, String> envp) {
        File conf = getConfigurationDirectory(envp);
        if (conf == null || conf.isDirectory() == false) {
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

    private static File getConfigurationDirectory(Map<String, String> envp) {
        File explicit = getExplicitConfigurationDirectory(envp);
        if (explicit != null) {
            return explicit;
        }
        File implicit = getImplicitConfigurationDirectory(envp);
        return implicit;
    }

    private static File getExplicitConfigurationDirectory(Map<String, String> envp) {
        String conf = envp.get(ENV_HADOOP_CONF);
        if (conf == null) {
            LOG.debug(MessageFormat.format(
                    "Missed explicit Hadoop conf directory: {0}",
                    ENV_HADOOP_CONF));
            return null;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Found explicit Hadoop confdir: {0}={1}",
                    ENV_HADOOP_CONF,
                    conf));
        }
        return new File(conf);
    }

    private static File getImplicitConfigurationDirectory(Map<String, String> envp) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Detecting default Hadoop configuration dir from Hadoop installation path");
        }
        File command = findHadoopCommand(envp);
        if (command == null) {
            return null;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Hadoop command: {0}",
                    command));
        }
        File home = getHadoopInstallationPath(command);
        if (home == null) {
            return null;
        }
        if (new File(home, PATH_CONF_DIR_0_TESTER).exists()) {
            return new File(home, PATH_CONF_DIR_0);
        }
        return new File(home, PATH_CONF_DIR_1);
    }

    /**
     * Searches for the installed hadoop command.
     * @return the found one, or {@code null} if not found
     */
    public static File findHadoopCommand() {
        return findHadoopCommand(System.getenv());
    }

    static File findHadoopCommand(Map<String, String> envp) {
        File command = null;
        File home = getExplicitHadoopDirectory(envp);
        if (home != null && home.isDirectory()) {
            command = new File(home, PATH_HADOOP_COMMAND);
        } else {
            command = findHadoopCommandFromPath(envp);
        }
        if (command == null || command.canExecute() == false) {
            return null;
        }
        return command;
    }

    private static File getExplicitHadoopDirectory(Map<String, String> envp) {
        String homeString = envp.get(ENV_HADOOP_HOME);
        if (homeString == null) {
            LOG.debug(MessageFormat.format(
                    "Missed explicit Hadoop home directory: {0}",
                    ENV_HADOOP_HOME));
            return null;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Found explicit Hadoop home directory: {0}={1}",
                    ENV_HADOOP_HOME,
                    homeString));
        }
        File home = new File(homeString);
        return home;
    }

    private static File getHadoopInstallationPath(File command) {
        assert command != null;
        File resolved;
        try {
            resolved = command.getCanonicalFile();
        } catch (IOException e) {
            LOG.warn(MessageFormat.format(
                    "Failed to resolve Hadoop command (for detecting Hadoop conf dir): {0}",
                    command), e);
            resolved = command;
        }
        File parent1 = resolved.getParentFile();
        if (parent1 == null || parent1.isDirectory() == false) {
            return null;
        }
        File parent2 = parent1.getParentFile();
        if (parent2 == null || parent2.isDirectory() == false) {
            return null;
        }
        return parent2;
    }

    private static File findHadoopCommandFromPath(Map<String, String> envp) {
        String path = envp.get(ENV_PATH);
        if (path != null && path.trim().isEmpty() == false) {
            String[] pathPrefixArray = path.split(Pattern.quote(File.pathSeparator));
            for (String prefix : pathPrefixArray) {
                String p = prefix.trim();
                if (p.isEmpty()) {
                    continue;
                }
                File bin = new File(p);
                if (bin.isDirectory() == false) {
                    continue;
                }
                File command = new File(bin, PATH_HADOOP_COMMAND_FILE);
                if (command.canExecute()) {
                    return command;
                }
            }
        }
        return null;
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
            configure(conf);
            return conf;
        } finally {
            Thread.currentThread().setContextClassLoader(context);
        }
    }

    /**
     * Configures before {@link #newInstance()} returns the configuration object.
     * @param configuration the configuration
     */
    protected void configure(Configuration configuration) {
        return;
    }
}
