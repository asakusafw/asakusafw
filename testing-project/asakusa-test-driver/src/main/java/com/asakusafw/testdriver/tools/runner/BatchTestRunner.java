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
package com.asakusafw.testdriver.tools.runner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.vocabulary.batch.Batch;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.yaess.core.BatchScript;

/**
 * The program entry point of Asakusa batch application runner.
 * @since 0.6.0
 * @version 0.7.1
 * @see BatchTestTool
 */
public final class BatchTestRunner {

    static final Logger LOG = LoggerFactory.getLogger(BatchTestRunner.class);

    /**
     * The YAESS script path (relative from batch application directory).
     */
    public static final String PATH_YAESS_SCRIPT = "etc/yaess-script.properties"; //$NON-NLS-1$

    static final Option OPT_BATCH_ID;
    static final Option OPT_ARGUMENT;
    static final Option OPT_PROPERTY;

    private static final Options OPTIONS;
    static {
        OPT_BATCH_ID = new Option("b", "batch", true, "batch ID"); //$NON-NLS-1$ //$NON-NLS-2$
        OPT_BATCH_ID.setArgName("batch_id"); //$NON-NLS-1$
        OPT_BATCH_ID.setRequired(true);

        OPT_ARGUMENT = new Option("A", "argument", true, "batch argument"); //$NON-NLS-1$ //$NON-NLS-2$
        OPT_ARGUMENT.setArgs(2);
        OPT_ARGUMENT.setValueSeparator('=');
        OPT_ARGUMENT.setArgName("name=value"); //$NON-NLS-1$
        OPT_ARGUMENT.setRequired(false);

        OPT_PROPERTY = new Option("D", "property", true, "hadoop property"); //$NON-NLS-1$ //$NON-NLS-2$
        OPT_PROPERTY.setArgs(2);
        OPT_PROPERTY.setValueSeparator('=');
        OPT_PROPERTY.setArgName("name=value"); //$NON-NLS-1$
        OPT_PROPERTY.setRequired(false);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_BATCH_ID);
        OPTIONS.addOption(OPT_ARGUMENT);
    }

    private final TestDriverContext context;

    private final String batchId;

    private final String executionIdPrefix;

    /**
     * Creates a new instance.
     * @param batchClass the target batch class
     * @since 0.7.1
     */
    public BatchTestRunner(Class<? extends BatchDescription> batchClass) {
        if (batchClass == null) {
            throw new IllegalArgumentException("batchClass must not be null"); //$NON-NLS-1$
        }
        Batch annotation = batchClass.getAnnotation(Batch.class);
        if (annotation == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "\"{0}\" must be annotated with @{1}",
                    batchClass.getName(),
                    Batch.class.getSimpleName()));
        }

        this.context = new TestDriverContext(batchClass);
        this.batchId = annotation.name();
        this.executionIdPrefix = UUID.randomUUID().toString();
        initialize();
    }

    /**
     * Creates a new instance.
     * @param batchId the target batch ID
     * @since 0.7.1
     */
    public BatchTestRunner(String batchId) {
        if (batchId == null) {
            throw new IllegalArgumentException("batchId must not be null"); //$NON-NLS-1$
        }
        this.context = new TestDriverContext(BatchTestRunner.class);
        this.batchId = batchId;
        this.executionIdPrefix = UUID.randomUUID().toString();
        initialize();
    }

    private void initialize() {
        // NOTE: We must use the system "batchapps" path instead of a temporary location
        context.useSystemBatchApplicationsInstallationPath(true);
    }

    /**
     * Sets the Asakusa Framework installation path.
     * The default value is {@code $ASAKUSA_HOME}.
     * @param path the framework installation path
     * @return this
     * @since 0.7.1
     */
    public BatchTestRunner withFramework(File path) {
        context.setFrameworkHomePath(path);
        return this;
    }

    /**
     * Sets the Asakusa batch applications installation path.
     * The default value is {@code $ASAKUSA_HOME/batchapps}.
     * @param path the batch applications installation path
     * @return this
     * @since 0.7.1
     */
    public BatchTestRunner withApplications(File path) {
        context.setBatchApplicationsInstallationPath(path);
        return this;
    }

    /**
     * Sets a batch argument for this runner.
     * @param name the argument name
     * @param value the argument value
     * @return this
     * @since 0.7.1
     */
    public BatchTestRunner withArgument(String name, String value) {
        context.getBatchArgs().put(name, value);
        return this;
    }

    /**
     * Sets batch arguments for this runner.
     * @param arguments the arguments name value map
     * @return this
     * @since 0.7.1
     */
    public BatchTestRunner withArguments(Map<String, String> arguments) {
        if (arguments != null) {
            context.getBatchArgs().putAll(arguments);
        }
        return this;
    }

    /**
     * Sets a Hadoop property for this runner.
     * @param key the property key
     * @param value the property value
     * @return this
     * @since 0.7.1
     */
    public BatchTestRunner withProperty(String key, String value) {
        context.getExtraConfigurations().put(key, value);
        return this;
    }

    /**
     * Sets Hadoop properties for this runner.
     * @param properties the properties key value map
     * @return this
     * @since 0.7.1
     */
    public BatchTestRunner withProperties(Map<String, String> properties) {
        if (properties != null) {
            context.getExtraConfigurations().putAll(properties);
        }
        return this;
    }

    /**
     * Run Asakusa batch application.
     * @return the exit code
     * @since 0.7.1
     */
    public int execute() {
        long t0 = System.currentTimeMillis();
        try {
            RunTask.Configuration configuration = loadConfiguration();
            RunTask task = new RunTask(configuration);
            task.perform();
        } catch (AssertionError e) {
            LOG.error(MessageFormat.format(
                    "Failed to executes batch application: {0}",
                    batchId), e);
            return 1;
        } catch (Exception e) {
            LOG.error(MessageFormat.format(
                    "Failed to executes batch application: {0}",
                    batchId), e);
            return 1;
        } finally {
            context.cleanUpTemporaryResources();
        }
        if (LOG.isInfoEnabled()) {
            long t1 = System.currentTimeMillis();
            LOG.info(MessageFormat.format(
                    "Elapsed: {0}ms",
                    t1 - t0));
        }
        return 0;
    }

    /**
     * Program entry.
     * @param args program arguments
     */
    public static void main(String[] args) {
        int exitCode = execute(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    /**
     * Program entry.
     * @param args program arguments
     * @return the exit code
     * @see #execute(String, Map)
     */
    public static int execute(String[] args) {
        BatchTestRunner runner;
        try {
            runner = parseArguments(args);
        } catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(Integer.MAX_VALUE);
            formatter.printHelp(
                    MessageFormat.format(
                            "java -classpath ... {0}", //$NON-NLS-1$
                            BatchTestRunner.class.getName()),
                    OPTIONS,
                    true);
            LOG.error(MessageFormat.format(
                    "Failed to parse the program arguments: {0}",
                    Arrays.toString(args)), e);
            return 1;
        }
        return runner.execute();
    }

    /**
     * Run Asakusa batch application.
     * @param batchId the target batch ID
     * @param batchArguments the batch arguments (nullable)
     * @return the exit code
     */
    public static int execute(String batchId, Map<String, String> batchArguments) {
        return new BatchTestRunner(batchId)
            .withArguments(batchArguments)
            .execute();
    }

    /**
     * Run Asakusa batch application without any batch arguments.
     * @param batchId the target batch ID
     * @return the exit code
     * @see #execute(String, Map)
     */
    public static int execute(String batchId) {
        return execute(batchId, null);
    }

    static BatchTestRunner parseArguments(String[] args) throws ParseException {
        assert args != null;
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(OPTIONS, args);

        String batchId = cmd.getOptionValue(OPT_BATCH_ID.getOpt());
        LOG.debug("Batch ID: {}", batchId); //$NON-NLS-1$

        Properties arguments = cmd.getOptionProperties(OPT_ARGUMENT.getOpt());
        LOG.debug("Batch arguments: {}", arguments); //$NON-NLS-1$

        Properties properties = cmd.getOptionProperties(OPT_PROPERTY.getOpt());
        LOG.debug("Extra properties: {}", arguments); //$NON-NLS-1$

        return new BatchTestRunner(batchId)
            .withArguments(toMap(arguments))
            .withProperties(toMap(properties));
    }

    private RunTask.Configuration loadConfiguration() {
        BatchScript script;
        File scriptFile = getScriptFile(context, batchId);
        LOG.debug("Loading script: {}", scriptFile); //$NON-NLS-1$
        try {
            Properties properties = loadProperties(scriptFile);
            script = BatchScript.load(properties);
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid script \"{0}\".",
                    scriptFile), e);
        }

        LOG.debug("Analyzed YAESS bootstrap arguments"); //$NON-NLS-1$
        return new RunTask.Configuration(
                context,
                script,
                executionIdPrefix);
    }

    private static File getScriptFile(TestDriverContext context, String batchId) {
        assert context != null;
        assert batchId != null;
        File batchappBase = context.getBatchApplicationsInstallationPath();
        File batchapp = new File(batchappBase, batchId);
        File scriptFile = new File(batchapp, PATH_YAESS_SCRIPT);
        return scriptFile;
    }

    private static Map<String, String> toMap(Properties p) {
        assert p != null;
        Map<String, String> results = new TreeMap<String, String>();
        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            results.put((String) entry.getKey(), (String) entry.getValue());
        }
        return results;
    }

    private static Properties loadProperties(File path) throws IOException {
        assert path != null;
        FileInputStream in = new FileInputStream(path);
        try {
            Properties properties = new Properties();
            BufferedInputStream bin = new BufferedInputStream(in);
            properties.load(bin);
            bin.close();
            return properties;
        } finally {
            in.close();
        }
    }
}
