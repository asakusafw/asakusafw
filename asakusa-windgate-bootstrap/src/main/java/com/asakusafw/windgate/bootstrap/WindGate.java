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
package com.asakusafw.windgate.bootstrap;

import java.io.File;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.GateProfile;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.GateTask;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.WindGateLogger;

/**
 * A WindGate main entry point.
 */
public class WindGate {

    static final WindGateLogger WGLOG = new WindGateBootstrapLogger(WindGate.class);

    static final Logger LOG = LoggerFactory.getLogger(WindGate.class);

    static final Option OPT_MODE;
    static final Option OPT_PROFILE;
    static final Option OPT_SCRIPT;
    static final Option OPT_SESSION_ID;
    static final Option OPT_PLUGIN;
    static final Option OPT_ARGUMENTS;

    private static final Options OPTIONS;
    static {
        OPT_MODE = new Option("mode", true, "execution mode");
        {
            StringBuilder buf = new StringBuilder();
            for (ExecutionKind kind : ExecutionKind.values()) {
                if (buf.length() > 0) {
                    buf.append('|');
                }
                buf.append(kind.symbol);
            }
            OPT_MODE.setArgName(buf.toString());
        }
        OPT_MODE.setRequired(true);

        OPT_PROFILE = new Option("profile", true, "profile path");
        OPT_PROFILE.setArgName("/path/to/profile");
        OPT_PROFILE.setRequired(true);

        OPT_SCRIPT = new Option("script", true, "gate script path");
        OPT_SCRIPT.setArgName("/path/to/script");
        OPT_SCRIPT.setRequired(true);

        OPT_SESSION_ID = new Option("session", true, "session ID");
        OPT_SESSION_ID.setArgName("session-id");
        OPT_SESSION_ID.setRequired(true);

        OPT_PLUGIN = new Option("plugin", true, "WindGate plug-ins");
        OPT_PLUGIN.setArgName("plugin-1.jar" + File.pathSeparatorChar + "plugin-2.jar");
        OPT_PLUGIN.setRequired(false);

        OPT_ARGUMENTS = new Option("arguments", true, "key-value arguments");
        OPT_ARGUMENTS.setArgName("key1=val1,key2=val2");
        OPT_ARGUMENTS.setRequired(false);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_MODE);
        OPTIONS.addOption(OPT_PROFILE);
        OPTIONS.addOption(OPT_SCRIPT);
        OPTIONS.addOption(OPT_SESSION_ID);
        OPTIONS.addOption(OPT_PLUGIN);
        OPTIONS.addOption(OPT_ARGUMENTS);
    }

    /**
     * Program entry.
     * @param args program arguments
     */
    public static void main(String... args) {
        CommandLineUtil.prepareLogContext();
        WGLOG.info("I00000");
        int status = execute(args);
        WGLOG.info("I00999",
                status);
        System.exit(status);
    }

    static int execute(String[] args) {
        GateTask task;
        try {
            Configuration conf = parseConfiguration(args);
            task = new GateTask(
                    conf.profile,
                    conf.script,
                    conf.sessionId,
                    conf.mode.createsSession,
                    conf.mode.completesSession,
                    conf.arguments);
        } catch (Exception e) {
            WGLOG.error(e, "E00001");
            e.printStackTrace(System.out);
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(Integer.MAX_VALUE);
            formatter.printHelp(
                    MessageFormat.format(
                            "java -classpath ... {0}",
                            WindGate.class.getName()),
                    OPTIONS,
                    true);
            System.out.println("Note:");
            System.out.println("  Profile and script path accept following URI formats:");
            System.out.println("    no scheme -");
            System.out.println("      Local file system path");
            System.out.println("    \"classpath\" scheme -");
            System.out.println("      Absolute path on class path (includes plugin libraries)");
            System.out.println("    other schemes (e.g. http://...)-");
            System.out.println("      Processed as a URL");
            return 1;
        }
        try {
            task.execute();
            return 0;
        } catch (Exception e) {
            WGLOG.error(e, "E00002");
            e.printStackTrace();
            return 1;
        } finally {
            task.close();
        }
    }

    static Configuration parseConfiguration(String[] args) throws ParseException {
        assert args != null;
        LOG.debug("Analyzing WindGate bootstrap arguments: {}", Arrays.toString(args));

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(OPTIONS, args);

        String mode = cmd.getOptionValue(OPT_MODE.getOpt());
        LOG.debug("WindGate mode: {}", mode);
        String profile = cmd.getOptionValue(OPT_PROFILE.getOpt());
        LOG.debug("WindGate profile: {}", profile);
        String script = cmd.getOptionValue(OPT_SCRIPT.getOpt());
        LOG.debug("WindGate script: {}", script);
        String sessionId = cmd.getOptionValue(OPT_SESSION_ID.getOpt());
        LOG.debug("WindGate sessionId: {}", sessionId);
        String plugins = cmd.getOptionValue(OPT_PLUGIN.getOpt());
        LOG.debug("WindGate plugin: {}", plugins);
        String arguments = cmd.getOptionValue(OPT_ARGUMENTS.getOpt());
        LOG.debug("WindGate arguments: {}", arguments);

        LOG.debug("Loading plugins: {}", plugins);
        List<File> pluginFiles = CommandLineUtil.parseFileList(plugins);
        ClassLoader loader = CommandLineUtil.buildPluginLoader(WindGate.class.getClassLoader(), pluginFiles);

        Configuration result = new Configuration();
        result.mode = ExecutionKind.parse(mode);
        if (result.mode == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid mode \"{0}\". The mode must be one of {1}",
                    mode,
                    Arrays.toString(ExecutionKind.values())));
        }

        LOG.debug("Loading profile: {}", profile);
        try {
            URI uri = CommandLineUtil.toUri(profile);
            Properties properties = CommandLineUtil.loadProperties(uri, loader);
            result.profile = GateProfile.loadFrom(CommandLineUtil.toName(uri), properties, loader);
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid profile \"{0}\".",
                    profile), e);
        }

        LOG.debug("Loading script: {}", script);
        try {
            URI uri = CommandLineUtil.toUri(script);
            Properties properties = CommandLineUtil.loadProperties(uri, loader);
            result.script = GateScript.loadFrom(CommandLineUtil.toName(uri), properties, loader);
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid script \"{0}\".",
                    script), e);
        }
        if (sessionId.isEmpty()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid session ID \"{0}\". The session ID must not be empty.",
                    sessionId));
        }
        result.sessionId = sessionId;

        LOG.debug("Parsing arguments: {}", arguments);
        result.arguments = CommandLineUtil.parseArguments(arguments);

        LOG.debug("Analyzed WindGate bootstrap arguments");
        return result;
    }

    static final class Configuration {
        ExecutionKind mode;
        GateProfile profile;
        GateScript script;
        String sessionId;
        ParameterList arguments;
    }
}
