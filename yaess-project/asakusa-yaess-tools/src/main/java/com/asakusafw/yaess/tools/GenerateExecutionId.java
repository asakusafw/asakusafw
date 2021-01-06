/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.yaess.tools;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Generates an execution ID.
 * @since 0.2.3
 */
public final class GenerateExecutionId {

    static final Option OPT_BATCH_ID;
    static final Option OPT_FLOW_ID;
    static final Option OPT_ARGUMENT;

    private static final Options OPTIONS;
    static {
        OPT_BATCH_ID = new Option("batch", true, "batch ID");
        OPT_BATCH_ID.setArgName("batch_id");
        OPT_BATCH_ID.setRequired(true);

        OPT_FLOW_ID = new Option("flow", true, "flow ID");
        OPT_FLOW_ID.setArgName("flow_id");
        OPT_FLOW_ID.setRequired(true);

        OPT_ARGUMENT = new Option("A", true, "name-value pair");
        OPT_ARGUMENT.setArgs(2);
        OPT_ARGUMENT.setValueSeparator('=');
        OPT_ARGUMENT.setArgName("name=value");
        OPT_ARGUMENT.setRequired(false);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_BATCH_ID);
        OPTIONS.addOption(OPT_FLOW_ID);
        OPTIONS.addOption(OPT_ARGUMENT);
    }

    private GenerateExecutionId() {
        return;
    }

    /**
     * Program entry.
     * @param args program arguments
     */
    public static void main(String... args) {
        int status = execute(args);
        System.exit(status);
    }

    static int execute(String[] args) {
        assert args != null;
        Configuration conf;
        try {
            conf = parseConfiguration(args);
        } catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(Integer.MAX_VALUE);
            formatter.printHelp(
                    MessageFormat.format(
                            "java -classpath ... {0}",
                            GenerateExecutionId.class.getName()),
                    OPTIONS,
                    true);
            e.printStackTrace(System.out);
            return 1;
        }
        try {
            String executionId = computeExecutionId(conf);
            System.out.print(executionId);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
        return 0;
    }

    static String computeExecutionId(Configuration conf) {
        assert conf != null;
        StringBuilder buf = new StringBuilder();
        buf.append(normalize(conf.batchId));
        buf.append('-');
        buf.append(normalize(conf.flowId));
        buf.append('-');

        long hash = 31 * 257 * 65537;
        final int prime = 31;
        hash = hash * prime + conf.batchId.trim().hashCode();
        hash = hash * prime + conf.flowId.trim().hashCode();
        for (Map.Entry<String, String> entry : conf.arguments.entrySet()) {
            hash = hash * prime + entry.getKey().trim().hashCode();
            hash = hash * prime + entry.getValue().trim().hashCode();
        }
        buf.append(String.format("%016x", hash));
        return buf.toString();
    }

    private static String normalize(String string) {
        assert string != null;
        StringBuilder buf = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (Character.isJavaIdentifierPart(c)) {
                buf.append(c);
            } else {
                buf.append('_');
            }
        }
        return buf.toString();
    }

    static Configuration parseConfiguration(String[] args) throws ParseException {
        assert args != null;
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(OPTIONS, args);

        String batchId = cmd.getOptionValue(OPT_BATCH_ID.getOpt());
        String flowId = cmd.getOptionValue(OPT_FLOW_ID.getOpt());
        Properties arguments = cmd.getOptionProperties(OPT_ARGUMENT.getOpt());

        SortedMap<String, String> pairs = new TreeMap<>();
        for (Map.Entry<Object, Object> entry : arguments.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key instanceof String && value instanceof String) {
                pairs.put((String) key, (String) value);
            }
        }
        Configuration result = new Configuration();
        result.batchId = batchId;
        result.flowId = flowId;
        result.arguments = pairs;
        return result;
    }

    static final class Configuration {

        String batchId;

        String flowId;

        SortedMap<String, String> arguments;
    }
}
