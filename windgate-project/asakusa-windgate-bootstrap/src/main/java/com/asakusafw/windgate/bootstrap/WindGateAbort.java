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

import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.windgate.core.AbortTask;
import com.asakusafw.windgate.core.GateProfile;
import com.asakusafw.windgate.core.ProfileContext;
import com.asakusafw.windgate.core.WindGateLogger;

/**
 * A WindGate abort main entry point.
 */
public final class WindGateAbort {

    static final WindGateLogger WGLOG = new WindGateBootstrapLogger(WindGateAbort.class);

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

    private WindGateAbort() {
        return;
    }

    /**
     * Program entry.
     * @param args program arguments
     */
    public static void main(String... args) {
        CommandLineUtil.prepareLogContext();
        CommandLineUtil.prepareRuntimeContext();
        WGLOG.info("I01000");
        long start = System.currentTimeMillis();
        int status = execute(args);
        long end = System.currentTimeMillis();
        WGLOG.info("I01999",
                status,
                end - start);
        System.exit(status);
    }

    static int execute(String[] args) {
        AbortTask task;
        try {
            Configuration conf = parseConfiguration(args);
            task = new AbortTask(conf.profile, conf.sessionId);
        } catch (Exception e) {
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
            WGLOG.error(e, "E01001");
            return 1;
        }
        try {
            if (RuntimeContext.get().canExecute(task)) {
                task.execute();
            }
            return 0;
        } catch (Exception e) {
            WGLOG.error(e, "E01002");
            return 1;
        }
    }

    static Configuration parseConfiguration(String[] args) throws ParseException {
        assert args != null;
        LOG.debug("Analyzing WindGateAbort bootstrap arguments: {}", Arrays.toString(args));

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(OPTIONS, args);

        String profile = cmd.getOptionValue(OPT_PROFILE.getOpt());
        LOG.debug("WindGate profile: {}", profile);
        String sessionId = cmd.getOptionValue(OPT_SESSION_ID.getOpt());
        LOG.debug("WindGate sessionId: {}", sessionId);
        String plugins = cmd.getOptionValue(OPT_PLUGIN.getOpt());
        LOG.debug("WindGate plugin: {}", plugins);

        LOG.debug("Loading plugins: {}", plugins);
        List<File> pluginFiles = CommandLineUtil.parseFileList(plugins);
        ClassLoader loader = CommandLineUtil.buildPluginLoader(WindGateAbort.class.getClassLoader(), pluginFiles);

        Configuration result = new Configuration();

        LOG.debug("Loading profile: {}", profile);
        try {
            ProfileContext context = ProfileContext.system(loader);
            URI uri = CommandLineUtil.toUri(profile);
            Properties properties = CommandLineUtil.loadProperties(uri, loader);
            result.profile = GateProfile.loadFrom(CommandLineUtil.toName(uri), properties, context);
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

        LOG.debug("Analyzed WindGateAbort bootstrap arguments");
        return result;
    }

    static final class Configuration {
        GateProfile profile;
        String sessionId;
    }
}
