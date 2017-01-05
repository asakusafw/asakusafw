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
package com.asakusafw.yaess.tools.log.cli;

import java.io.IOException;
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

import com.asakusafw.utils.io.Sink;
import com.asakusafw.utils.io.Source;
import com.asakusafw.yaess.tools.log.YaessLogInput;
import com.asakusafw.yaess.tools.log.YaessLogOutput;
import com.asakusafw.yaess.tools.log.YaessLogRecord;

/**
 * YAESS log analyzer program entry.
 * @since 0.6.2
 */
public final class Main {

    static final Logger LOG = LoggerFactory.getLogger(Main.class);

    static final Option OPT_INPUT;
    static final Option OPT_OUTPUT;
    static final Option OPT_INPUT_ARGUMENT;
    static final Option OPT_OUTPUT_ARGUMENT;

    private static final Options OPTIONS;
    static {
        OPT_INPUT = new Option("i", "input", true, "input driver class name");
        OPT_INPUT.setArgName("com.example.SourceFactory");
        OPT_INPUT.setRequired(true);

        OPT_OUTPUT = new Option("o", "output", true, "output driver class name");
        OPT_OUTPUT.setArgName("com.example.SinkFactory");
        OPT_OUTPUT.setRequired(true);

        OPT_INPUT_ARGUMENT = new Option("I", "input-argument", true, "input driver argument");
        OPT_INPUT_ARGUMENT.setArgs(2);
        OPT_INPUT_ARGUMENT.setValueSeparator('=');
        OPT_INPUT_ARGUMENT.setArgName("name=value");
        OPT_INPUT_ARGUMENT.setRequired(false);

        OPT_OUTPUT_ARGUMENT = new Option("O", "output-argument", true, "output driver argument");
        OPT_OUTPUT_ARGUMENT.setArgs(2);
        OPT_OUTPUT_ARGUMENT.setValueSeparator('=');
        OPT_OUTPUT_ARGUMENT.setArgName("name=value");
        OPT_OUTPUT_ARGUMENT.setRequired(false);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_INPUT);
        OPTIONS.addOption(OPT_OUTPUT);
        OPTIONS.addOption(OPT_INPUT_ARGUMENT);
        OPTIONS.addOption(OPT_OUTPUT_ARGUMENT);
    }

    private Main() {
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
     * @return exit code
     */
    public static int execute(String[] args) {
        Configuration conf;
        try {
            conf = parseConfiguration(args);
        } catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(Integer.MAX_VALUE);
            formatter.printHelp(
                    MessageFormat.format(
                            "java -classpath ... {0}",
                            Main.class.getName()),
                    OPTIONS,
                    true);
            LOG.error(MessageFormat.format(
                    "Invalid program arguments: {0}",
                    Arrays.toString(args)), e);
            return 2;
        }
        try {
            execute(conf);
        } catch (IllegalArgumentException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(Integer.MAX_VALUE);
            formatter.printHelp(
                    MessageFormat.format(
                            "java -classpath ... {0}",
                            Main.class.getName()),
                    OPTIONS,
                    true);
            System.out.println("Input Driver Arguments:");
            printArguments(conf.sourceFactory.getOptionsInformation());
            System.out.println("Output Driver Arguments:");
            printArguments(conf.sinkFactory.getOptionsInformation());
            LOG.error(MessageFormat.format(
                    "Invalid driver arguments: {0}",
                    Arrays.toString(args)), e);
            return 2;
        } catch (Exception e) {
            LOG.error(MessageFormat.format(
                    "Failed to analyze YAESS log: {0}",
                    Arrays.toString(args)), e);
            return 1;
        }
        return 0;
    }

    private static void printArguments(Map<String, String> optionsInformation) {
        assert optionsInformation != null;
        for (Map.Entry<String, String> entry : optionsInformation.entrySet()) {
            System.out.printf("    %s: %s%n", entry.getKey(), entry.getValue());
        }
    }

    static Configuration parseConfiguration(String[] args) throws ParseException {
        assert args != null;
        LOG.debug("Analyzing arguments: {}", Arrays.toString(args));

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(OPTIONS, args);

        ClassLoader classLoader = Main.class.getClassLoader();

        YaessLogInput source = create(cmd, OPT_INPUT, YaessLogInput.class, classLoader);
        YaessLogOutput sink = create(cmd, OPT_OUTPUT, YaessLogOutput.class, classLoader);
        Map<String, String> sourceArgs = parseArgs(cmd, OPT_INPUT_ARGUMENT);
        Map<String, String> sinkArgs = parseArgs(cmd, OPT_OUTPUT_ARGUMENT);

        return new Configuration(source, sourceArgs, sink, sinkArgs);
    }

    private static <T> T create(CommandLine cmd, Option opt, Class<T> type, ClassLoader loader) {
        assert cmd != null;
        assert opt != null;
        assert type != null;
        assert loader != null;
        String value = cmd.getOptionValue(opt.getOpt());
        Class<?> aClass;
        try {
            aClass = Class.forName(value, false, loader);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Failed to initialize the class \"{1}\" (-{0})",
                    opt.getOpt(), value), e);
        }
        if (type.isAssignableFrom(aClass) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "\"{1}\" must be a subtype of \"{2}\" (-{0})",
                    opt.getOpt(), value, type.getName()));
        }
        try {
            return aClass.asSubclass(type).getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Failed to initialize the class \"{1}\" (-{0})",
                    opt.getOpt(), value), e);
        }
    }

    private static Map<String, String> parseArgs(CommandLine cmd, Option opt) {
        Properties props = cmd.getOptionProperties(opt.getOpt());
        Map<String, String> results = new TreeMap<>();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            results.put((String) entry.getKey(), (String) entry.getValue());
        }
        return results;
    }

    private static void execute(Configuration conf) throws IOException, InterruptedException {
        assert conf != null;
        LOG.info("Start analyzing YAESS log");
        long count = 0L;
        try (Source<? extends YaessLogRecord> source = conf.sourceFactory.createSource(conf.sourceOptions);
                Sink<? super YaessLogRecord> sink = conf.sinkFactory.createSink(conf.sinkOptions)) {
            while (source.next()) {
                count++;
                sink.put(source.get());
            }
        }
        LOG.info("Finish analyzing YAESS log: {} records", count);
    }

    private static class Configuration {

        final YaessLogInput sourceFactory;

        final Map<String, String> sourceOptions;

        final YaessLogOutput sinkFactory;

        final Map<String, String> sinkOptions;

        Configuration(
                YaessLogInput sourceFactory, Map<String, String> sourceOptions,
                YaessLogOutput sinkFactory, Map<String, String> sinkOptions) {
            this.sourceFactory = sourceFactory;
            this.sourceOptions = sourceOptions;
            this.sinkFactory = sinkFactory;
            this.sinkOptions = sinkOptions;
        }
    }
}
