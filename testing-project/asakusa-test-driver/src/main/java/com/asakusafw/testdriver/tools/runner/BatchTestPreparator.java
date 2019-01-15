/**
 * Copyright 2011-2019 Asakusa Framework Team.
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

import java.text.MessageFormat;
import java.util.Arrays;
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

import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * Prepares batch execution inputs.
 * @see BatchTestRunner
 * @since 0.7.3
 */
public class BatchTestPreparator extends BatchTestTool {

    static final Logger LOG = LoggerFactory.getLogger(BatchTestPreparator.class);

    static final Option OPT_IMPORTER;
    static final Option OPT_DATA;
    static final Option OPT_ARGUMENT;
    static final Option OPT_PROPERTY;

    private static final Options OPTIONS;
    static {
        OPT_IMPORTER = new Option(null, "importer", true, //$NON-NLS-1$
                Messages.getString("BatchTestPreparator.optImporter")); //$NON-NLS-1$
        OPT_IMPORTER.setArgName("importer class name"); //$NON-NLS-1$
        OPT_IMPORTER.setRequired(true);

        OPT_DATA = new Option(null, "data", true, //$NON-NLS-1$
                Messages.getString("BatchTestPreparator.optData")); //$NON-NLS-1$
        OPT_DATA.setArgName("data URI"); //$NON-NLS-1$
        OPT_DATA.setRequired(true);

        OPT_ARGUMENT = new Option("A", "argument", true, //$NON-NLS-1$ //$NON-NLS-2$
                Messages.getString("BatchTestPreparator.optArgument")); //$NON-NLS-1$
        OPT_ARGUMENT.setArgs(2);
        OPT_ARGUMENT.setValueSeparator('=');
        OPT_ARGUMENT.setArgName("name=value"); //$NON-NLS-1$
        OPT_ARGUMENT.setRequired(false);

        OPT_PROPERTY = new Option("D", "property", true, //$NON-NLS-1$ //$NON-NLS-2$
                Messages.getString("BatchTestPreparator.optProperty")); //$NON-NLS-1$
        OPT_PROPERTY.setArgs(2);
        OPT_PROPERTY.setValueSeparator('=');
        OPT_PROPERTY.setArgName("name=value"); //$NON-NLS-1$
        OPT_PROPERTY.setRequired(false);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_IMPORTER);
        OPTIONS.addOption(OPT_DATA);
        OPTIONS.addOption(OPT_ARGUMENT);
        OPTIONS.addOption(OPT_PROPERTY);
    }

    /**
     * Creates a new instance.
     * @param context the current context
     */
    public BatchTestPreparator(TestDriverContext context) {
        super(context);
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
                    Messages.getString("BatchTestPreparator.errorInvalidArgument"), //$NON-NLS-1$
                    Arrays.toString(args)), e);
            return 1;
        }
        try {
            BatchTestPreparator preparator = new BatchTestPreparator(conf.context);
            preparator.prepare(conf.importer, conf.data);
            return 0;
        } catch (Exception e) {
            LOG.error(MessageFormat.format(
                    Messages.getString("BatchTestPreparator.errorFailedToPrepareInput"), //$NON-NLS-1$
                    Arrays.toString(args)), e);
            return 1;
        }
    }

    static Conf parseArguments(String[] args) throws ParseException {
        assert args != null;
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(OPTIONS, args);

        Conf conf = new Conf();
        String importerClass = cmd.getOptionValue(OPT_IMPORTER.getLongOpt());
        try {
            conf.importer = Class.forName(importerClass).asSubclass(ImporterDescription.class).newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("BatchTestPreparator.errorInvalidImporterDescription"), //$NON-NLS-1$
                    importerClass), e);
        }
        conf.data = cmd.getOptionValue(OPT_DATA.getLongOpt());
        conf.context = new TestDriverContext(conf.importer.getClass());

        Properties arguments = cmd.getOptionProperties(OPT_ARGUMENT.getOpt());
        conf.context.getBatchArgs().putAll(toMap(arguments));

        Properties properties = cmd.getOptionProperties(OPT_PROPERTY.getOpt());
        conf.context.getExtraConfigurations().putAll(toMap(properties));

        return conf;
    }


    private static Map<String, String> toMap(Properties p) {
        assert p != null;
        Map<String, String> results = new TreeMap<>();
        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            results.put((String) entry.getKey(), (String) entry.getValue());
        }
        return results;
    }

    private static final class Conf {

        ImporterDescription importer;

        String data;

        TestDriverContext context;

        Conf() {
            return;
        }
    }
}
