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
import com.asakusafw.yaess.core.BatchScript;

/**
 * The program entry point of Asakusa batch application runner.
 * @since 0.6.0
 * @version 0.6.1
 */
public final class BatchTestRunner {

    static final Logger LOG = LoggerFactory.getLogger(BatchTestRunner.class);

    /**
     * The YAESS script path (relative from batch application directory).
     */
    public static final String PATH_YAESS_SCRIPT = "etc/yaess-script.properties";

    static final Option OPT_BATCH_ID;
    static final Option OPT_ARGUMENT;

    private static final Options OPTIONS;
    static {
        OPT_BATCH_ID = new Option("b", "batch", true, "batch ID");
        OPT_BATCH_ID.setArgName("batch_id");
        OPT_BATCH_ID.setRequired(true);

        OPT_ARGUMENT = new Option("A", "argument", true, "batch argument");
        OPT_ARGUMENT.setArgs(2);
        OPT_ARGUMENT.setValueSeparator('=');
        OPT_ARGUMENT.setArgName("name=value");
        OPT_ARGUMENT.setRequired(false);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_BATCH_ID);
        OPTIONS.addOption(OPT_ARGUMENT);
    }

    private BatchTestRunner() {
        return;
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
        Arguments arguments;
        try {
            arguments = parseArguments(args);
        } catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(Integer.MAX_VALUE);
            formatter.printHelp(
                    MessageFormat.format(
                            "java -classpath ... {0}",
                            BatchTestRunner.class.getName()),
                    OPTIONS,
                    true);
            LOG.error(MessageFormat.format(
                    "Failed to parse the program arguments: {0}",
                    Arrays.toString(args)), e);
            return 1;
        }
        return execute(arguments);
    }

    /**
     * Run Asakusa batch application.
     * @param batchId the target batch ID
     * @param batchArguments the batch arguments (nullable)
     * @return the exit code
     */
    public static int execute(String batchId, Map<String, String> batchArguments) {
        TestDriverContext context = createContext();
        if (batchArguments != null) {
            context.getBatchArgs().putAll(batchArguments);
        }
        Arguments arguments = new Arguments(context, batchId, generateExecutionId());
        return execute(arguments);
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

    private static int execute(Arguments arguments) {
        try {
            RunTask.Configuration configuration = loadConfiguration(arguments);
            RunTask task = new RunTask(configuration);
            task.perform();
        } catch (AssertionError e) {
            LOG.error(MessageFormat.format(
                    "Failed to executes batch application: {0}",
                    arguments.batchId), e);
            return 1;
        } catch (Exception e) {
            LOG.error(MessageFormat.format(
                    "Failed to executes batch application: {0}",
                    arguments.batchId), e);
            return 1;
        } finally {
            arguments.context.cleanUpTemporaryResources();
        }
        return 0;
    }

    static Arguments parseArguments(String[] args) throws ParseException {
        assert args != null;
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(OPTIONS, args);

        String batchId = cmd.getOptionValue(OPT_BATCH_ID.getOpt());
        LOG.debug("Batch ID: {}", batchId);

        String executionIdPrefix = generateExecutionId();
        LOG.debug("Exec ID (prefix): {}", batchId);

        Properties arguments = cmd.getOptionProperties(OPT_ARGUMENT.getOpt());
        LOG.debug("Batch arguments: {}", arguments);

        TestDriverContext context = createContext();
        context.getBatchArgs().putAll(toMap(arguments));

        return new Arguments(context, batchId, executionIdPrefix);
    }

    private static String generateExecutionId() {
        return UUID.randomUUID().toString();
    }

    static RunTask.Configuration loadConfiguration(Arguments args) {
        assert args != null;
        BatchScript script;
        File scriptFile = getScriptFile(args.context, args.batchId);
        LOG.debug("Loading script: {}", scriptFile);
        try {
            Properties properties = loadProperties(scriptFile);
            script = BatchScript.load(properties);
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid script \"{0}\".",
                    scriptFile), e);
        }

        LOG.debug("Analyzed YAESS bootstrap arguments");
        return new RunTask.Configuration(
                args.context,
                script,
                args.executionIdPrefix);
    }

    private static TestDriverContext createContext() {
        TestDriverContext context = new TestDriverContext(BatchTestRunner.class);

        // NOTE: We must use the system "batchapps" path instead of a temporary location
        context.useSystemBatchApplicationsInstallationPath(true);
        return context;
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

    static final class Arguments {

        final TestDriverContext context;

        final String batchId;

        final String executionIdPrefix;

        public Arguments(TestDriverContext context, String batchId, String executionIdPrefix) {
            this.context = context;
            this.batchId = batchId;
            this.executionIdPrefix = executionIdPrefix;
        }
    }
}
