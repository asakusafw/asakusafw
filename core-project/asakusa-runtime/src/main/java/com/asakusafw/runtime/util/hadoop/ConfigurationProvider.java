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
package com.asakusafw.runtime.util.hadoop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;

/**
 * Creates {@link Configuration}s with system defaults.
 * @since 0.4.0
 * @version 0.5.0
 */
public class ConfigurationProvider {

    private static final String ENV_PATH = "PATH";

    private static final String ENV_HADOOP_CONF = "HADOOP_CONF";

    private static final String ENV_HADOOP_CMD = "HADOOP_CMD";

    private static final String ENV_HADOOP_HOME = "HADOOP_HOME";

    private static final String ENV_HADOOP_CLASSPATH = "HADOOP_CLASSPATH";

    private static final String PATH_HADOOP_COMMAND_FILE = "hadoop";

    private static final String PATH_HADOOP_COMMAND = "bin/hadoop";

    private static final String PATH_CONF_DIR_TARBALL = "conf";

    private static final String PATH_CONF_DIR_TARBALL_TESTER = "conf/hadoop-env.sh";

    private static final String PATH_CONF_DIR_PACKAGE = "etc/hadoop";

    private static final String CLASS_EXTENSION = ".class";

    private static final String PATH_SUBPROC_OUTPUT = "result";

    static final Log LOG = LogFactory.getLog(ConfigurationProvider.class);

    private static final Map<File, File> CACHE_HADOOP_CMD_CONF = new HashMap<File, File>();

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
                    "Failed to load default Hadoop configurations ({0} is not a valid installation path)",
                    conf));
            return null;
        }
        try {
            return conf.toURI().toURL();
        } catch (MalformedURLException e) {
            LOG.warn(MessageFormat.format(
                    "Failed to load default Hadoop configurations ({0} is unrecognized to convert URL)",
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
        File conf;
        conf = getHadoopConfigurationDirectoryByRelative(command);
        if (conf != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Found implicit Hadoop confdir (from hadoop command path): {0}",
                        conf));
            }
            return conf;
        }
        conf = getHadoopConfigurationDirectoryByCommand(command, envp);
        if (conf != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Found implicit Hadoop confdir (from hadoop command execution): {0}",
                        conf));
            }
            return conf;
        }
        return null;
    }

    /**
     * Searches for the installed hadoop command.
     * @return the found one, or {@code null} if not found
     */
    public static File findHadoopCommand() {
        return findHadoopCommand(System.getenv());
    }

    static File findHadoopCommand(Map<String, String> envp) {
        assert envp != null;
        File command = getExplicitHadoopCommand(envp);
        if (command != null) {
            return command;
        }
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

    private static File getExplicitHadoopCommand(Map<String, String> envp) {
        assert envp != null;
        String commandString = envp.get(ENV_HADOOP_CMD);
        if (commandString == null || commandString.isEmpty()) {
            LOG.debug(MessageFormat.format(
                    "Missed explicit Hadoop home command path: {0}",
                    ENV_HADOOP_CMD));
            return null;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Found explicit Hadoop command path: {0}={1}",
                    ENV_HADOOP_CMD,
                    commandString));
        }
        return new File(commandString);
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

    private static File getHadoopConfigurationDirectoryByRelative(File command) {
        assert command != null;
        File home = getHadoopInstallationPath(command);
        if (home != null) {
            if (new File(home, PATH_CONF_DIR_TARBALL_TESTER).exists()) {
                return new File(home, PATH_CONF_DIR_TARBALL);
            }
            if (new File(home, PATH_CONF_DIR_PACKAGE).isDirectory()) {
                return new File(home, PATH_CONF_DIR_PACKAGE);
            }
        }
        return null;
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

    private static File getHadoopConfigurationDirectoryByCommand(File command, Map<String, String> envp) {
        assert command != null;
        assert envp != null;
        synchronized (CACHE_HADOOP_CMD_CONF) {
            if (CACHE_HADOOP_CMD_CONF.containsKey(command)) {
                return CACHE_HADOOP_CMD_CONF.get(command);
            }
        }
        File conf;
        try {
            File dir = File.createTempFile("asakusa-runtime", ".tmp");
            try {
                if (dir.delete() == false) {
                    LOG.warn(MessageFormat.format("Failed to delete temporary file: {0}", dir));
                }
                if (dir.mkdirs() == false) {
                    LOG.warn(MessageFormat.format("Failed to create temporary directory: {0}", dir));
                }
                conf = detectHadoopConfigurationDirectory(command, dir, envp);
            } finally {
                if (dir.exists() && delete(dir) == false) {
                    LOG.error(MessageFormat.format("Failed to delete temporary directory: {0}", dir));
                }
            }
        } catch (IOException e) {
            LOG.error("Failed to detect Hadoop configuration directory", e);
            conf = null;
        }
        synchronized (CACHE_HADOOP_CMD_CONF) {
            if (CACHE_HADOOP_CMD_CONF.get(command) == null) {
                CACHE_HADOOP_CMD_CONF.put(command, conf);
            }
        }
        return conf;
    }

    private static File detectHadoopConfigurationDirectory(
            File command,
            File temporary,
            Map<String, String> envp) throws IOException {
        assert command != null;
        assert temporary != null;
        assert envp != null;

        prepareClasspath(temporary, ConfigurationDetecter.class);
        File resultOutput = new File(temporary, PATH_SUBPROC_OUTPUT);

        List<String> arguments = new ArrayList<String>();
        arguments.add(command.getAbsolutePath());
        arguments.add(ConfigurationDetecter.class.getName());
        arguments.add(resultOutput.getAbsolutePath());

        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.environment().clear();
        processBuilder.environment().putAll(envp);
        processBuilder.environment().put(ENV_HADOOP_CLASSPATH, temporary.getPath());

        Process process = processBuilder.start();
        try {
            Thread redirectOut = redirect(process.getInputStream(), System.out);
            Thread redirectErr = redirect(process.getErrorStream(), System.err);
            try {
                int exit = process.waitFor();
                redirectOut.join();
                redirectErr.join();
                if (exit != 0) {
                    throw new IOException(MessageFormat.format(
                            "Failed to execute Hadoop command (exitcode={1}): {0}",
                            arguments,
                            String.valueOf(exit)));
                }
            } catch (InterruptedException e) {
                throw (IOException) new InterruptedIOException(MessageFormat.format(
                        "Failed to execute Hadoop command (interrupted): {0}",
                        arguments)).initCause(e);
            }
        } finally {
            process.destroy();
        }
        if (resultOutput.isFile() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to restore Hadoop configuration path: {0}",
                    resultOutput));
        }
        File path = ConfigurationDetecter.read(resultOutput);
        return path;
    }

    private static void prepareClasspath(File temporary, Class<?> aClass) throws IOException {
        assert temporary != null;
        assert aClass != null;
        File current = temporary;
        String name = aClass.getName();
        int start = 0;
        while (true) {
            int index = name.indexOf('.', start);
            if (index < 0) {
                break;
            }
            current = new File(current, name.substring(start, index));
            if (current.mkdirs() == false && current.isDirectory() == false) {
                LOG.warn(MessageFormat.format("Failed to create a directory: {0}", current));
            }
            start = index + 1;
        }
        File target = new File(current, name.substring(start) + CLASS_EXTENSION);

        String path = name.replace('.', '/') + CLASS_EXTENSION;
        InputStream in = aClass.getClassLoader().getResourceAsStream(path);
        if (in == null) {
            throw new FileNotFoundException(MessageFormat.format(
                    "A class file binary does not found in classpath: {0}",
                    path));
        }
        try {
            OutputStream out = new FileOutputStream(target);
            try {
                byte[] buf = new byte[1024];
                while (true) {
                    int read = in.read(buf);
                    if (read < 0) {
                        break;
                    }
                    out.write(buf, 0, read);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    private static Thread redirect(InputStream in, OutputStream out) {
        Thread t = new Thread(new StreamRedirectTask(in, out));
        t.setDaemon(true);
        t.start();
        return t;
    }

    private static boolean delete(File file) {
        assert file != null;
        if (file.isDirectory()) {
            boolean rest = false;
            for (File child : file.listFiles()) {
                rest |= delete(child) == false;
            }
            if (rest) {
                return false;
            }
        }
        return file.delete() || file.exists() == false;
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

    private static class StreamRedirectTask implements Runnable {

        private final InputStream input;

        private final OutputStream output;

        StreamRedirectTask(InputStream input, OutputStream output) {
            assert input != null;
            assert output != null;
            this.input = input;
            this.output = output;
        }

        @Override
        public void run() {
            boolean outputFailed = false;
            try {
                InputStream in = input;
                OutputStream out = output;
                byte[] buf = new byte[256];
                while (true) {
                    int read = in.read(buf);
                    if (read == -1) {
                        break;
                    }
                    if (outputFailed == false) {
                        try {
                            out.write(buf, 0, read);
                        } catch (IOException e) {
                            outputFailed = true;
                            LOG.warn(MessageFormat.format("Failed to redirect stdout of subprocess", e));
                        }
                    }
                }
            } catch (IOException e) {
                LOG.warn(MessageFormat.format("Failed to redirect stdio of subprocess", e));
            }
        }
    }
}
