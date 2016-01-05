/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

import com.asakusafw.compiler.batch.BatchDriver;
import com.asakusafw.compiler.flow.JobFlowDriver;
import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.vocabulary.batch.JobFlowWorkDescription;
import com.asakusafw.vocabulary.batch.Work;
import com.asakusafw.vocabulary.batch.WorkDescription;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowIn;
import com.asakusafw.vocabulary.flow.graph.FlowOut;

/**
 * Truncates batch execution inputs/outputs.
 * @see BatchTestRunner
 * @since 0.7.3
 */
public class BatchTestTruncator extends BatchTestTool {

    static final Logger LOG = LoggerFactory.getLogger(BatchTestTruncator.class);

    static final Option OPT_DESCRIPTION;
    static final Option OPT_ARGUMENT;
    static final Option OPT_PROPERTY;

    private static final Options OPTIONS;
    static {
        OPT_DESCRIPTION = new Option(null, "description", true, //$NON-NLS-1$
                Messages.getString("BatchTestTruncator.optDescription")); //$NON-NLS-1$
        OPT_DESCRIPTION.setArgName("description class name"); //$NON-NLS-1$
        OPT_DESCRIPTION.setRequired(true);

        OPT_ARGUMENT = new Option("A", "argument", true, //$NON-NLS-1$ //$NON-NLS-2$
                Messages.getString("BatchTestTruncator.optArgument")); //$NON-NLS-1$
        OPT_ARGUMENT.setArgs(2);
        OPT_ARGUMENT.setValueSeparator('=');
        OPT_ARGUMENT.setArgName("name=value"); //$NON-NLS-1$
        OPT_ARGUMENT.setRequired(false);

        OPT_PROPERTY = new Option("D", "property", true, //$NON-NLS-1$ //$NON-NLS-2$
                Messages.getString("BatchTestTruncator.optProperty")); //$NON-NLS-1$
        OPT_PROPERTY.setArgs(2);
        OPT_PROPERTY.setValueSeparator('=');
        OPT_PROPERTY.setArgName("name=value"); //$NON-NLS-1$
        OPT_PROPERTY.setRequired(false);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_DESCRIPTION);
        OPTIONS.addOption(OPT_ARGUMENT);
        OPTIONS.addOption(OPT_PROPERTY);
    }

    /**
     * Creates a new instance.
     * @param context the current context
     */
    public BatchTestTruncator(TestDriverContext context) {
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
                    Messages.getString("BatchTestTruncator.errorInvalidArgument"), //$NON-NLS-1$
                    Arrays.toString(args)), e);
            return 1;
        }
        try {
            BatchTestTruncator truncator = new BatchTestTruncator(conf.context);
            for (ImporterDescription desc : conf.importers) {
                truncator.truncate(desc);
            }
            for (ExporterDescription desc : conf.exporters) {
                truncator.truncate(desc);
            }
            return 0;
        } catch (Exception e) {
            LOG.error(MessageFormat.format(
                    Messages.getString("BatchTestTruncator.errorFailedToTruncate"), //$NON-NLS-1$
                    Arrays.toString(args)), e);
            return 1;
        }
    }

    static Conf parseArguments(String[] args) throws ParseException {
        assert args != null;
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(OPTIONS, args);

        Conf conf = new Conf();
        String descriptionClass = cmd.getOptionValue(OPT_DESCRIPTION.getLongOpt());
        Class<?> description;
        try {
            description = Class.forName(descriptionClass);
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("BatchTestTruncator.errorFailedToAnalyze"), //$NON-NLS-1$
                    descriptionClass), e);
        }
        conf.context = new TestDriverContext(description);
        resolveDescription(conf, description);

        Properties arguments = cmd.getOptionProperties(OPT_ARGUMENT.getOpt());
        conf.context.getBatchArgs().putAll(toMap(arguments));

        Properties properties = cmd.getOptionProperties(OPT_PROPERTY.getOpt());
        conf.context.getExtraConfigurations().putAll(toMap(properties));

        return conf;
    }

    private static void resolveDescription(Conf conf, Class<?> description) {
        if (BatchDescription.class.isAssignableFrom(description)) {
            resolveBatch(conf, description.asSubclass(BatchDescription.class));
        } else if (FlowDescription.class.isAssignableFrom(description)) {
            resolveFlow(conf, description.asSubclass(FlowDescription.class));
        } else if (ImporterDescription.class.isAssignableFrom(description)) {
            resolveImporter(conf, description.asSubclass(ImporterDescription.class));
        } else if (ExporterDescription.class.isAssignableFrom(description)) {
            resolveExporter(conf, description.asSubclass(ExporterDescription.class));
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("BatchTestTruncator.errorInvalidTarget"), //$NON-NLS-1$
                    description.getName()));
        }
    }

    private static void resolveBatch(Conf conf, Class<? extends BatchDescription> aClass) {
        LOG.debug("analyzing batch: {}", aClass.getName()); //$NON-NLS-1$
        BatchDriver driver = BatchDriver.analyze(aClass);
        if (driver.hasError()) {
            for (String message : driver.getDiagnostics()) {
                LOG.error(message);
            }
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("BatchTestTruncator.errorInvalidBatchClass"), //$NON-NLS-1$
                    aClass.getName()));
        }
        Collection<Work> works = driver.getBatchClass().getDescription().getWorks();
        for (Work work : works) {
            WorkDescription desc = work.getDescription();
            if (desc instanceof JobFlowWorkDescription) {
                Class<? extends FlowDescription> flow = ((JobFlowWorkDescription) desc).getFlowClass();
                resolveFlow(conf, flow);
            }
        }
    }

    private static void resolveFlow(Conf conf, Class<? extends FlowDescription> aClass) {
        LOG.debug("analyzing jobflow: {}", aClass.getName()); //$NON-NLS-1$
        JobFlowDriver driver = JobFlowDriver.analyze(aClass);
        if (driver.hasError()) {
            for (String message : driver.getDiagnostics()) {
                LOG.error(message);
            }
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("BatchTestTruncator.errorInvalidJobflowClass"), //$NON-NLS-1$
                    aClass.getName()));
        }
        FlowGraph graph = driver.getJobFlowClass().getGraph();
        for (FlowIn<?> port : graph.getFlowInputs()) {
            ImporterDescription desc = port.getDescription().getImporterDescription();
            if (desc != null) {
                conf.importers.add(desc);
            }
        }
        for (FlowOut<?> port : graph.getFlowOutputs()) {
            ExporterDescription desc = port.getDescription().getExporterDescription();
            if (desc != null) {
                conf.exporters.add(desc);
            }
        }
    }

    private static void resolveImporter(Conf conf, Class<? extends ImporterDescription> aClass) {
        try {
            conf.importers.add(aClass.newInstance());
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("BatchTestTruncator.errorInvalidImporterClass"), //$NON-NLS-1$
                    aClass.getName()), e);
        }
    }

    private static void resolveExporter(Conf conf, Class<? extends ExporterDescription> aClass) {
        try {
            conf.exporters.add(aClass.newInstance());
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("BatchTestTruncator.errorInvalidExporterClass"), //$NON-NLS-1$
                    aClass.getName()), e);
        }
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

        final List<ImporterDescription> importers = new ArrayList<>();

        final List<ExporterDescription> exporters = new ArrayList<>();

        TestDriverContext context;

        Conf() {
            return;
        }
    }
}
