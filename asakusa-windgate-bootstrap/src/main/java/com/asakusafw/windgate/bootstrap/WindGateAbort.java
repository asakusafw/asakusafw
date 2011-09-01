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
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
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

import com.asakusafw.windgate.core.AbortTask;
import com.asakusafw.windgate.core.GateProfile;

/**
 * A WindGate abort main entry point.
 */
public class WindGateAbort {

    static final Logger LOG = LoggerFactory.getLogger(WindGateAbort.class);

    static final Option OPT_PROFILE;
    static final Option OPT_SESSION_ID;
    static final Option OPT_PLUGIN;

    private static final Options OPTIONS;
    static {
        OPT_PROFILE = new Option("profile", true, "profile path");
        OPT_PROFILE.setArgName("/path/to/profile");
        OPT_PROFILE.setRequired(true);

        OPT_SESSION_ID = new Option("session", true, "session ID");
        OPT_SESSION_ID.setArgName("session-id");
        OPT_SESSION_ID.setRequired(false);

        OPT_PLUGIN = new Option("plugin", true, "WindGate plug-ins");
        OPT_PLUGIN.setArgName("plugin-1.jar" + File.pathSeparatorChar + "plugin-2.jar");
        OPT_PLUGIN.setRequired(false);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_PROFILE);
        OPTIONS.addOption(OPT_SESSION_ID);
        OPTIONS.addOption(OPT_PLUGIN);
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
        AbortTask task;
        try {
            Configuration conf = parseConfiguration(args);
            task = new AbortTask(conf.profile, conf.sessionId);
        } catch (Exception e) {
            // TODO logging
            e.printStackTrace(System.out);
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(Integer.MAX_VALUE);
            formatter.printHelp(
                    MessageFormat.format(
                            "java -classpath ... {0}",
                            WindGateAbort.class.getName()),
                    OPTIONS,
                    true);
            System.out.println("Note:");
            System.out.println("  If session ID is not specified, this removes all sessions");
            System.out.println("");
            System.out.println("  Profile path accepts following URI formats:");
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
        } catch (IOException e) {
            // TODO logging
            e.printStackTrace();
            return 1;
        } catch (InterruptedException e) {
            // TODO logging
            e.printStackTrace();
            return 1;
        }
    }

    static Configuration parseConfiguration(String[] args) throws ParseException {
        assert args != null;
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(OPTIONS, args);

        String profile = cmd.getOptionValue(OPT_PROFILE.getOpt());
        String sessionId = cmd.getOptionValue(OPT_SESSION_ID.getOpt());
        String plugins = cmd.getOptionValue(OPT_PLUGIN.getOpt());

        List<File> pluginFiles = CommandLineUtil.parseFileList(plugins);
        ClassLoader loader = CommandLineUtil.buildPluginLoader(WindGateAbort.class.getClassLoader(), pluginFiles);

        Configuration result = new Configuration();
        try {
            Properties properties = CommandLineUtil.loadProperties(new URI(profile), loader);
            result.profile = GateProfile.loadFrom(properties, loader);
        } catch (Exception e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid profile \"{0}\".",
                    profile), e);
        }
        if (sessionId == null || sessionId.isEmpty()) {
            result.sessionId = null;
        } else {
            result.sessionId = sessionId;
        }

        return result;
    }

    static final class Configuration {
        GateProfile profile;
        String sessionId;
    }
}
