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
package com.asakusafw.compiler.util.tester;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.MultipleModelInput;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.mapreduce.simple.SimpleJobRunner;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.stage.launcher.ApplicationLauncher;
import com.asakusafw.runtime.stage.optimizer.LibraryCopySuppressionConfigurator;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;
import com.asakusafw.runtime.util.hadoop.InstallationUtil;

/**
 * A driver for control Hadoop jobs for testing.
 * @since 0.1.0
 * @version 0.7.5
 */
public final class HadoopDriver implements Closeable {

    static {
        InstallationUtil.verifyFrameworkVersion();
    }

    static final Logger LOG = LoggerFactory.getLogger(HadoopDriver.class);

    private volatile Logger logger;

    /**
     * Cluster working directory.
     */
    public static final String RUNTIME_WORK_ROOT = "target/testing";

    static final String PREFIX_KEY = "hadoop.";

    static final String KEY_INPROCESS = PREFIX_KEY + "inprocess";

    static final String KEY_SMALLJOB = PREFIX_KEY + "smalljob";

    static final String KEY_BUILTIN_MAPREDUCE = PREFIX_KEY + "builtin";

    private final File command;

    private final ConfigurationProvider configurations;

    private final Configuration configuration;

    private boolean inProcess;

    private final boolean simpleMr;

    private HadoopDriver(ConfigurationProvider configurations) {
        this.command = ConfigurationProvider.findHadoopCommand();
        this.configurations = configurations;
        this.configuration = configurations.newInstance();
        this.logger = LOG;
        this.simpleMr = isSet(KEY_BUILTIN_MAPREDUCE, true) || isSet(KEY_SMALLJOB);
        this.inProcess = simpleMr || isSet(KEY_INPROCESS);
    }

    private static boolean isSet(String key, boolean defaultValue) {
        String value = System.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return value.isEmpty() || value.equalsIgnoreCase("true");
    }

    private static boolean isSet(String key) {
        return isSet(key, false);
    }

    /**
     * Changes current logger.
     * @param logger the logger to set
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void setLogger(Logger logger) {
        if (logger == null) {
            throw new IllegalArgumentException("logger must not be null"); //$NON-NLS-1$
        }
        this.logger = logger;
    }

    /**
     * Changes fork mode.
     * @param fork {@code true} to run with fork, otherwise {@code false}
     */
    public void setFork(boolean fork) {
        this.inProcess = fork == false;
    }

    /**
     * Returns the current configuration.
     * @return the configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Converts the path segments into the temporary file system path.
     * @param segments the path segments
     * @return the temporary path
     */
    public Location toPath(String...segments) {
        if (segments == null) {
            throw new IllegalArgumentException("segments must not be null"); //$NON-NLS-1$
        }
        Location path = Location.fromPath(RUNTIME_WORK_ROOT, '/');
        for (String segment : segments) {
            path = path.append(segment);
        }
        return path;
    }

    /**
     * Creates a new instance.
     * @return the created instance
     */
    public static HadoopDriver createInstance() {
        return new HadoopDriver(new ConfigurationProvider());
    }

    /**
     * Creates a new instance.
     * @param configurations the Hadoop configuration provider
     * @return the created instance
     * @since 0.6.1
     */
    public static HadoopDriver createInstance(ConfigurationProvider configurations) {
        return new HadoopDriver(configurations);
    }

    /**
     * Creates a data model input for reading data model objects in the target path.
     * @param <T> the data model type
     * @param modelType the data model type
     * @param location the target location
     * @return the created input
     * @throws IOException if failed to create the input
     */
    public <T extends Writable> ModelInput<T> openInput(Class<T> modelType, Location location) throws IOException {
        if (modelType == null) {
            throw new IllegalArgumentException("modelType must not be null"); //$NON-NLS-1$
        }
        if (location == null) {
            throw new IllegalArgumentException("location must not be null"); //$NON-NLS-1$
        }
        File temp = createTempFile(modelType);
        if (temp.delete() == false) {
            logger.debug("failed to delete a placeholder file: {}", temp); //$NON-NLS-1$
        }
        if (temp.mkdirs() == false) {
            throw new IOException(temp.getAbsolutePath());
        }
        copyFromHadoop(location, temp);

        List<ModelInput<T>> sources = new ArrayList<>();
        if (location.isPrefix()) {
            for (File file : temp.listFiles()) {
                if (isCopyTarget(file)) {
                    sources.add(TemporaryStorage.openInput(configuration, modelType, new Path(file.toURI())));
                }
            }
        } else {
            for (File folder : temp.listFiles()) {
                for (File file : folder.listFiles()) {
                    if (isCopyTarget(file)) {
                        sources.add(TemporaryStorage.openInput(configuration, modelType, new Path(file.toURI())));
                    }
                }
            }
        }
        return new MultipleModelInput<T>(sources) {
            final AtomicBoolean closed = new AtomicBoolean();
            @Override
            public void close() throws IOException {
                if (closed.compareAndSet(false, true) == false) {
                    return;
                }
                super.close();
                onInputCompleted(temp, location);
            }
        };
    }

    private boolean isCopyTarget(File file) {
        return file.isFile()
                && file.getName().startsWith("_") == false
                && file.getName().startsWith(".") == false;
    }

    /**
     * Creates a data model output to write objects into the specified location.
     * @param <T> the data model type
     * @param modelType data model type
     * @param path the target location
     * @return the created output
     * @throws IOException if failed to initialize the output
     */
    public <T extends Writable> ModelOutput<T> openOutput(Class<T> modelType, Location path) throws IOException {
        return TemporaryStorage.openOutput(
                configuration,
                modelType,
                new Path(path.toPath('/')));
    }

    private <T> File createTempFile(Class<T> modelType) throws IOException {
        assert modelType != null;
        return File.createTempFile(
                modelType.getSimpleName() + "_",
                ".seq");
    }

    void onInputCompleted(File temp, Location path) {
        assert temp != null;
        assert path != null;
        logger.debug("Input completed: {} -> {}", path, temp);
        if (delete(temp) == false) {
            logger.warn("Failed to delete temporary file: {}", temp);
        }
    }

    private boolean delete(File temp) {
        boolean success = true;
        if (temp.isDirectory()) {
            for (File child : temp.listFiles()) {
                success &= delete(child);
            }
        }
        success &= temp.delete();
        return success;
    }

    /**
     * Executes the target job using {@link ApplicationLauncher}.
     * @param runtimeLib core runtime library
     * @param className target class name
     * @param libjars extra libraries
     * @param conf configuration file (nullable)
     * @param properties hadoop properties
     * @return {@code true} if succeeded, otherwise {@code false}
     * @throws IOException if failed to execute
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public boolean runJob(
            File runtimeLib,
            List<File> libjars,
            String className,
            File conf,
            Map<String, String> properties) throws IOException {
        if (runtimeLib == null) {
            throw new IllegalArgumentException("runtime must not be null"); //$NON-NLS-1$
        }
        if (className == null) {
            throw new IllegalArgumentException("className must not be null"); //$NON-NLS-1$
        }
        if (libjars == null) {
            throw new IllegalArgumentException("libjars must not be null"); //$NON-NLS-1$
        }
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (inProcess) {
            return runInProcess(runtimeLib, libjars, className, conf, properties);
        } else {
            return runFork(runtimeLib, libjars, className, conf, properties);
        }
    }

    private boolean runFork(
            File runtimeLib,
            List<File> libjars,
            String className,
            File conf,
            Map<String, String> properties) throws IOException {
        logger.info("FORK EXEC: {} with {}", className, libjars);
        List<String> arguments = new ArrayList<>();
        arguments.add("jar");
        arguments.add(runtimeLib.getAbsolutePath());
        arguments.add(ApplicationLauncher.class.getName());
        arguments.add(className);

        if (libjars.isEmpty() == false) {
            StringBuilder buf = new StringBuilder();
            for (File f : libjars) {
                buf.append(f.getAbsolutePath());
                buf.append(",");
            }
            buf.deleteCharAt(buf.length() - 1);
            arguments.add("-libjars");
            arguments.add(buf.toString());
        }

        if (conf != null) {
            arguments.add("-conf");
            arguments.add(conf.getCanonicalPath());
        }
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            arguments.add("-D");
            arguments.add(MessageFormat.format("{0}={1}", entry.getKey(), entry.getValue()));
        }
        //addSuppressCopyLibraries(arguments);

        int exitValue = invoke(arguments.toArray(new String[arguments.size()]));
        if (exitValue != 0) {
            logger.info("running {} returned {}", className, exitValue);
            return false;
        }
        return true;
    }

    private boolean runInProcess(
            File runtimeLib,
            List<File> libjars,
            String className,
            File confFile,
            Map<String, String> properties) {
        logger.info("EMULATE: {} with {}", className, libjars);
        List<String> arguments = new ArrayList<>();
        arguments.add(className);
        addHadoopConf(arguments, confFile);
        addHadoopLibjars(libjars, arguments);
        addBuiltInMapReduceJobRunner(arguments);
        addSuppressCopyLibraries(arguments);
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Configuration conf = configurations.newInstance();
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                conf.set(entry.getKey(), entry.getValue());
            }
            try {
                int exitValue = ApplicationLauncher.exec(conf, arguments.toArray(new String[arguments.size()]));
                if (exitValue != 0) {
                    logger.info("running {} returned {}", className, exitValue);
                    return false;
                }
                return true;
            } catch (Exception e) {
                logger.info("error occurred", e);
                return false;
            }
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    private void addHadoopConf(List<String> arguments, File confFile) {
        if (confFile == null) {
            return;
        }
        arguments.add("-conf");
        arguments.add(confFile.getAbsolutePath());
    }

    private void addHadoopLibjars(List<File> jars, List<String> arguments) {
        assert jars != null;
        if (jars.isEmpty()) {
            return;
        }
        arguments.add("-libjars");
        StringBuilder libjars = new StringBuilder();
        for (File file : jars) {
            if (libjars.length() != 0) {
                libjars.append(',');
            }
            libjars.append(file.toURI());
        }
        arguments.add(libjars.toString());
    }

    private void addSuppressCopyLibraries(List<String> arguments) {
        arguments.add("-D");
        arguments.add(MessageFormat.format("{0}={1}",
                LibraryCopySuppressionConfigurator.KEY_ENABLED,
                String.valueOf(true)));
    }

    private void addBuiltInMapReduceJobRunner(List<String> arguments) {
        if (simpleMr) {
            arguments.add("-D");
            arguments.add(MessageFormat.format("{0}={1}",
                    StageConstants.PROP_JOB_RUNNER,
                    SimpleJobRunner.class.getName()));
        }
    }

    private void copyFromHadoop(Location location, File targetDirectory) throws IOException {
        targetDirectory.mkdirs();
        logger.info("copy {} to {}", location, targetDirectory);

        Path path = new Path(location.toPath('/'));
        FileSystem fs = path.getFileSystem(configuration);
        FileStatus[] list = fs.globStatus(path);
        if (list == null) {
            throw new IOException(MessageFormat.format(
                    "Failed to fs -get: source={0}, destination={1}",
                    path,
                    targetDirectory));
        }
        for (FileStatus status : list) {
            Path p = status.getPath();
            try {
                fs.copyToLocalFile(p, new Path(new File(targetDirectory, p.getName()).toURI()));
            } catch (IOException e) {
                throw new IOException(MessageFormat.format(
                        "Failed to fs -get: source={0}, destination={1}",
                        p,
                        targetDirectory), e);
            }
        }
    }

    /**
     * Cleans up the temporary working area.
     * @throws IOException if failed to clean up
     */
    public void clean() throws IOException {
        logger.info("clean user directory");
        Path path = new Path(toPath().toPath('/'));
        FileSystem fs = path.getFileSystem(configuration);
        try {
            if (fs.exists(path)) {
                fs.delete(path, true);
            }
        } catch (IOException e) {
            logger.info(MessageFormat.format(
                    "Failed to fs -rmr {0}",
                    toPath()), e);
        }
    }

    private int invoke(String... arguments) throws IOException {
        String hadoop = getHadoopCommand();
        if (hadoop == null) {
            throw new IOException(MessageFormat.format(
                    "Failed to detect \"{0}\" command for testing: {1}",
                    "hadoop",
                    Arrays.toString(arguments)));
        }
        List<String> commands = new ArrayList<>();
        commands.add(hadoop);
        Collections.addAll(commands, arguments);

        logger.info("invoke: {}", commands);
        ProcessBuilder builder = new ProcessBuilder()
            .command(commands)
            .redirectErrorStream(true);
        Process process = builder.start();
        try (InputStream stream = process.getInputStream();
                Scanner scanner = new Scanner(stream)) {
            while (scanner.hasNextLine()) {
                logger.info(scanner.nextLine());
            }
            return process.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e);
        } finally {
            process.destroy();
        }
    }

    private String getHadoopCommand() {
        if (command == null) {
            return null;
        }
        return command.getAbsolutePath();
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }
}
