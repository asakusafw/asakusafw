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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.DriverElementBase;
import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.core.Difference;
import com.asakusafw.testdriver.core.TestDataToolProvider;
import com.asakusafw.testdriver.core.TestModerator;
import com.asakusafw.testdriver.core.TestRule;
import com.asakusafw.testdriver.core.VerifierFactory;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.vocabulary.external.ExporterDescription;

/**
 * Verifies batch execution results.
 * @see BatchTestRunner
 * @since 0.7.3
 */
public class BatchTestVerifier extends DriverElementBase {

    static final Logger LOG = LoggerFactory.getLogger(BatchTestVerifier.class);

    static final Option OPT_EXPORTER;
    static final Option OPT_DATA;
    static final Option OPT_RULE;
    static final Option OPT_ARGUMENT;
    static final Option OPT_PROPERTY;

    private static final Options OPTIONS;
    static {
        OPT_EXPORTER = new Option(null, "exporter", true, "exporter description class"); //$NON-NLS-1$ //$NON-NLS-2$
        OPT_EXPORTER.setArgName("exporter class name"); //$NON-NLS-1$
        OPT_EXPORTER.setRequired(true);

        OPT_DATA = new Option(null, "data", true, "expected data path"); //$NON-NLS-1$ //$NON-NLS-2$
        OPT_DATA.setArgName("data URI"); //$NON-NLS-1$
        OPT_DATA.setRequired(true);

        OPT_RULE = new Option(null, "rule", true, "verification rule path"); //$NON-NLS-1$ //$NON-NLS-2$
        OPT_RULE.setArgName("rule URI"); //$NON-NLS-1$
        OPT_RULE.setRequired(false);

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
        OPTIONS.addOption(OPT_EXPORTER);
        OPTIONS.addOption(OPT_DATA);
        OPTIONS.addOption(OPT_RULE);
        OPTIONS.addOption(OPT_ARGUMENT);
        OPTIONS.addOption(OPT_PROPERTY);
    }

    private final TestDriverContext context;

    /**
     * Creates a new instance.
     * @param context the current context
     */
    public BatchTestVerifier(TestDriverContext context) {
        this.context = context;
    }

    @Override
    protected Class<?> getCallerClass() {
        return context.getCallerClass();
    }

    @Override
    protected TestDataToolProvider getTestTools() {
        return context.getRepository();
    }

    /**
     * Verifies jobflow output.
     * @param description the target exporter description
     * @param expectedPath the expected data URI
     * @param verifyRulePath the verification rule URI
     * @return verify differences
     * @throws IOException if failed to verify the output
     */
    public List<Difference> verify(
            ExporterDescription description,
            String expectedPath,
            String verifyRulePath) throws IOException {
        try {
            VerifierFactory verifier = getTestTools().toVerifierFactory(
                    toDataModelSourceFactory(expectedPath),
                    getTestTools().getVerifyRuleFactory(toUri(verifyRulePath), Collections.<TestRule>emptyList()));
            return verify(description, verifier);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "failed to prepare verify output: {0}",
                    description.getClass().getName()), e);
        }
    }

    /**
     * Verifies jobflow output.
     * @param description the exporter description
     * @param verifier the verifier
     * @return verify differences
     * @throws IOException if failed to verify the output
     */
    public List<Difference> verify(ExporterDescription description, VerifierFactory verifier) throws IOException {
        TestModerator moderator = new TestModerator(context.getRepository(), context);
        return moderator.inspect(
                description.getModelType(),
                description,
                new VerifyContext(context),
                verifier);
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
     */
    public static int execute(String[] args) {
        Conf conf;
        try {
            conf = parseArguments(args);
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
        try {
            BatchTestVerifier verifier = new BatchTestVerifier(conf.context);
            List<Difference> diffList = verifier.verify(
                    conf.exporter,
                    conf.data, conf.rule);
            if (diffList.isEmpty()) {
                LOG.info("successfully verified");
                return 0;
            } else {
                for (Difference diff : diffList) {
                    LOG.error(diff.toString());
                }
                return 1;
            }
        } catch (Exception e) {
            LOG.error(MessageFormat.format(
                    "failed to verify output: {0}",
                    Arrays.toString(args)), e);
            return 1;
        }
    }

    static Conf parseArguments(String[] args) throws ParseException {
        assert args != null;
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(OPTIONS, args);

        Conf conf = new Conf();
        String exporterClass = cmd.getOptionValue(OPT_EXPORTER.getLongOpt());
        try {
            conf.exporter = Class.forName(exporterClass).asSubclass(ExporterDescription.class).newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "failed to instantiate exporter description: {0}",
                    exporterClass), e);
        }
        conf.data = cmd.getOptionValue(OPT_DATA.getLongOpt());
        conf.rule = cmd.getOptionValue(OPT_RULE.getLongOpt());
        conf.context = new TestDriverContext(conf.exporter.getClass());

        Properties arguments = cmd.getOptionProperties(OPT_ARGUMENT.getOpt());
        conf.context.getBatchArgs().putAll(toMap(arguments));

        Properties properties = cmd.getOptionProperties(OPT_PROPERTY.getOpt());
        conf.context.getExtraConfigurations().putAll(toMap(properties));

        return conf;
    }


    private static Map<String, String> toMap(Properties p) {
        assert p != null;
        Map<String, String> results = new TreeMap<String, String>();
        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            results.put((String) entry.getKey(), (String) entry.getValue());
        }
        return results;
    }

    private static final class Conf {

        ExporterDescription exporter;

        String data;

        String rule;

        TestDriverContext context;

        Conf() {
            return;
        }
    }
}
