/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.yaess.bootstrap;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
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

import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.ProfileContext;
import com.asakusafw.yaess.core.YaessProfile;
import com.asakusafw.yaess.core.task.ExecutionTask;

/**
 * A YAESS program main entry point.
 * @since 0.2.3
 */
public class Yaess {

    static final Logger LOG = LoggerFactory.getLogger(Yaess.class);

    static final Option OPT_PROFILE;
    static final Option OPT_SCRIPT;
    static final Option OPT_BATCH_ID;
    static final Option OPT_FLOW_ID;
    static final Option OPT_EXECUTION_ID;
    static final Option OPT_PHASE_NAME;
    static final Option OPT_PLUGIN;
    static final Option OPT_ARGUMENT;

    private static final Options OPTIONS;
    static {
        OPT_PROFILE = new Option("profile", true, "profile path");
        OPT_PROFILE.setArgName("/path/to/profile");
        OPT_PROFILE.setRequired(true);

        OPT_SCRIPT = new Option("script", true, "script path");
        OPT_SCRIPT.setArgName("/path/to/script");
        OPT_SCRIPT.setRequired(true);

        OPT_BATCH_ID = new Option("batch", true, "batch ID");
        OPT_BATCH_ID.setArgName("batch_id");
        OPT_BATCH_ID.setRequired(true);

        OPT_FLOW_ID = new Option("flow", true, "flow ID");
        OPT_FLOW_ID.setArgName("flow_id");
        OPT_FLOW_ID.setRequired(false);

        OPT_EXECUTION_ID = new Option("execution", true, "execution ID");
        OPT_EXECUTION_ID.setArgName("execution-id");
        OPT_EXECUTION_ID.setRequired(false);

        OPT_PHASE_NAME = new Option("phase", true, "target phase name");
        OPT_PHASE_NAME.setArgName("phase-name");
        OPT_PHASE_NAME.setRequired(false);

        OPT_PLUGIN = new Option("plugin", true, "YAESS plug-ins");
        OPT_PLUGIN.setArgName("plugin-1.jar" + File.pathSeparatorChar + "plugin-2.jar");
        OPT_PLUGIN.setRequired(false);

        OPT_ARGUMENT = new Option("A", true, "name-value pair");
        OPT_ARGUMENT.setArgs(2);
        OPT_ARGUMENT.setValueSeparator('=');
        OPT_ARGUMENT.setArgName("name=value");
        OPT_ARGUMENT.setRequired(false);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_PROFILE);
        OPTIONS.addOption(OPT_SCRIPT);
        OPTIONS.addOption(OPT_BATCH_ID);
        OPTIONS.addOption(OPT_FLOW_ID);
        OPTIONS.addOption(OPT_EXECUTION_ID);
        OPTIONS.addOption(OPT_PHASE_NAME);
        OPTIONS.addOption(OPT_PLUGIN);
        OPTIONS.addOption(OPT_ARGUMENT);
    }

    /**
     * Program entry.
     * @param args program arguments
     */
    public static void main(String... args) {
        CommandLineUtil.prepareLogContext();
        int status = execute(args);
        System.exit(status);
    }

    static int execute(String[] args) {
        assert args != null;
        Configuration conf;
        try {
            conf = parseConfiguration(args);
        } catch (Exception e) {
            // TODO logging
            LOG.error(MessageFormat.format(
                    "Failed to analyze program arguments ({0})",
                    Arrays.toString(args)), e);
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(Integer.MAX_VALUE);
            formatter.printHelp(
                    MessageFormat.format(
                            "java -classpath ... {0}",
                            Yaess.class.getName()),
                    OPTIONS,
                    true);
            System.out.println("Phase name is one of:");
            for (ExecutionPhase phase : ExecutionPhase.values()) {
                System.out.printf("    %s%n", phase.getSymbol());
            }
            return 1;
        }
        ExecutionTask task;
        try {
            task = ExecutionTask.load(conf.profile, conf.script, conf.arguments);
        } catch (Exception e) {
            // TODO logging
            LOG.error(MessageFormat.format(
                    "Failed to load service components ({0})",
                    conf), e);
            return 1;
        }
        try {
            switch (conf.mode) {
            case BATCH:
                task.executeBatch(conf.batchId);
                break;
            case FLOW:
                task.executeFlow(conf.batchId, conf.flowId, conf.executionId);
                break;
            case PHASE:
                task.executePhase(conf.batchId, conf.flowId, conf.executionId, conf.phase);
                break;
            default:
                throw new AssertionError(conf.mode);
            }
            return 0;
        } catch (Exception e) {
            // TODO logging
            LOG.error(MessageFormat.format(
                    "Failed to execute a jobnet ({0})",
                    conf), e);
            return 1;
        }
    }

    static Configuration parseConfiguration(String[] args) throws ParseException {
        assert args != null;
        LOG.debug("Analyzing YAESS bootstrap arguments: {}", Arrays.toString(args));

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(OPTIONS, args);

        String profile = cmd.getOptionValue(OPT_PROFILE.getOpt());
        LOG.debug("Profile: {}", profile);
        String script = cmd.getOptionValue(OPT_SCRIPT.getOpt());
        LOG.debug("Script: {}", script);
        String batchId = cmd.getOptionValue(OPT_BATCH_ID.getOpt());
        LOG.debug("Batch ID: {}", batchId);
        String flowId = cmd.getOptionValue(OPT_FLOW_ID.getOpt());
        LOG.debug("Flow ID: {}", flowId);
        String executionId = cmd.getOptionValue(OPT_EXECUTION_ID.getOpt());
        LOG.debug("Execution ID: {}", executionId);
        String phaseName = cmd.getOptionValue(OPT_PHASE_NAME.getOpt());
        LOG.debug("Phase name: {}", phaseName);
        String plugins = cmd.getOptionValue(OPT_PLUGIN.getOpt());
        LOG.debug("Plug-ins: {}", plugins);
        Properties arguments = cmd.getOptionProperties(OPT_ARGUMENT.getOpt());
        LOG.debug("Execution arguments: {}", arguments);

        LOG.debug("Loading plugins: {}", plugins);
        List<File> pluginFiles = CommandLineUtil.parseFileList(plugins);
        ClassLoader loader = CommandLineUtil.buildPluginLoader(Yaess.class.getClassLoader(), pluginFiles);

        Configuration result = new Configuration();
        result.mode = computeMode(flowId, executionId, phaseName);

        LOG.debug("Loading profile: {}", profile);
        try {
            ProfileContext context = ProfileContext.system(loader);
            Properties properties = CommandLineUtil.loadProperties(new File(profile));
            result.profile = YaessProfile.load(properties, context);
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid profile \"{0}\".",
                    profile), e);
        }

        LOG.debug("Loading script: {}", script);
        try {
            Properties properties = CommandLineUtil.loadProperties(new File(script));
            result.script = properties;
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid script \"{0}\".",
                    script), e);
        }

        result.batchId = batchId;
        result.flowId = flowId;
        result.executionId = executionId;
        if (phaseName != null) {
            result.phase = ExecutionPhase.findFromSymbol(phaseName);
            if (result.phase == null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Unknown phase name \"{0}\".",
                        phaseName));
            }
        }

        result.arguments = new TreeMap<String, String>();
        for (Map.Entry<Object, Object> entry : arguments.entrySet()) {
            result.arguments.put((String) entry.getKey(), (String) entry.getValue());
        }

        LOG.debug("Analyzed YAESS bootstrap arguments");
        return result;
    }

    private static Mode computeMode(String flowId, String executionId, String phaseName) {
        if (flowId == null) {
            if (executionId != null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Argument \"-{0}\" must NOT be set if \"-{1}\" does not exists",
                        OPT_EXECUTION_ID.getOpt(),
                        OPT_FLOW_ID.getOpt()));
            }
            if (phaseName != null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Argument \"-{0}\" must NOT be set if \"-{1}\" does not exists",
                        OPT_PHASE_NAME.getOpt(),
                        OPT_FLOW_ID.getOpt()));
            }
            return Mode.BATCH;
        } else {
            if (executionId == null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Argument \"-{0}\" must be set if \"-{1}\" exists",
                        OPT_EXECUTION_ID.getOpt(),
                        OPT_FLOW_ID.getOpt()));
            }
            if (phaseName == null) {
                return Mode.FLOW;
            } else {
                return Mode.PHASE;
            }
        }
    }

    static final class Configuration {
        Mode mode;
        YaessProfile profile;
        Properties script;
        String batchId;
        String flowId;
        String executionId;
        ExecutionPhase phase;
        Map<String, String> arguments;
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Configuration [mode=");
            builder.append(mode);
            builder.append(", batchId=");
            builder.append(batchId);
            builder.append(", flowId=");
            builder.append(flowId);
            builder.append(", executionId=");
            builder.append(executionId);
            builder.append(", phase=");
            builder.append(phase);
            builder.append("]");
            return builder.toString();
        }
    }

    enum Mode {
        BATCH,
        FLOW,
        PHASE,
    }
}
