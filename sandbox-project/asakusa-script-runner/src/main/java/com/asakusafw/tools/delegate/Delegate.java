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
package com.asakusafw.tools.delegate;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Executes a script in {@code $ASAKUSA_HOME}.
 */
public class Delegate {

    private static final String ENV_HOME = "HOME";

    private static final String ENV_ASAKUSA_HOME = "ASAKUSA_HOME";

    private static final String DEFAUT_HOME_PATH = "/home/hadoop";

    private static final String DEFAULT_ASAKUSA_HOME_NAME = "asakusa";

    private static final Pattern PREFIX_ASAKUSA_HOME = Pattern.compile("^"
            + "(\\Q$ASAKUSA_HOME\\E"
            + "|\\Q${ASAKUSA_HOME}\\E"
            + ")");

    private static final String HOME_PATH = System.getProperty("user.home", DEFAUT_HOME_PATH);

    // ASAKUSA_HOME detection order
    // 0. predefined $ASAKUSA_HOME
    // 1. ~/asakusa
    // 2. /opt/asakusa
    private static final String[] DEFAULT_ASAKUSA_HOME_CANDIDATES = {
            new File(HOME_PATH, DEFAULT_ASAKUSA_HOME_NAME).getAbsolutePath(),
            "/opt/" + DEFAULT_ASAKUSA_HOME_NAME,
    };

    /**
     * Executes a script in {@code $ASAKUSA_HOME}.
     * @param args the script path and the its arguments;
     *     the script path should start with {@code "$ASAKUSA_HOME/"} or
     *     be a relative path from the {@code $ASAKUSA_HOME}
     * @throws IOException if failed
     * @throws InterruptedException if failed
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        int status = new Delegate().exec(System.getenv(), args);
        if (status != 0) {
            System.exit(status);
        }
    }

    int exec(Map<String, String> env, String[] args) throws IOException, InterruptedException {
        Arguments arguments = createArguments(env, args);
        return exec(arguments);
    }

    private Arguments createArguments(Map<String, String> env, String[] args) {
        LinkedList<String> restArguments = new LinkedList<>();
        Collections.addAll(restArguments, args);
        File home = computeAsakusaHome(env);
        File script = computeScriptPath(home, restArguments);
        Map<String, String> envDiff = computeEnvDiff(home);
        return new Arguments(script, envDiff, restArguments);
    }

    private File computeAsakusaHome(Map<String, String> env) {
        File home = validateHome(env.get(ENV_ASAKUSA_HOME));
        if (home != null) {
            return home;
        }
        for (String path : DEFAULT_ASAKUSA_HOME_CANDIDATES) {
            File hc = validateHome(path);
            if (hc != null) {
                return hc;
            }
        }
        throw new IllegalStateException(MessageFormat.format(
                "Missing {0}",
                ENV_ASAKUSA_HOME));
    }

    private File validateHome(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        File home = new File(path);
        if (home.isDirectory()) {
            return home;
        }
        return null;
    }

    private File computeScriptPath(File home, LinkedList<String> arguments) {
        if (arguments.isEmpty()) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "The first argument must be a script path (relative from ${0})",
                    ENV_ASAKUSA_HOME));
        }
        String relativePath = arguments.removeFirst();
        Matcher matcher = PREFIX_ASAKUSA_HOME.matcher(relativePath);
        if (matcher.find()) {
            relativePath = relativePath.substring(matcher.end());
        }
        while (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        if (relativePath.isEmpty()) {
            throw new IllegalStateException(MessageFormat.format(
                    "Invalid script path: \"{0}\"",
                    relativePath));
        }

        File fullPath = new File(home, relativePath);
        if (fullPath.isFile() == false || fullPath.canExecute() == false) {
            throw new IllegalStateException(MessageFormat.format(
                    "Cannot detect valid script: \"{0}\"",
                    fullPath.getAbsolutePath()));
        }
        return fullPath;
    }

    private Map<String, String> computeEnvDiff(File home) {
        Map<String, String> envDiff = new HashMap<>();
        envDiff.put(ENV_HOME, HOME_PATH);
        envDiff.put(ENV_ASAKUSA_HOME, home.getAbsolutePath());
        return envDiff;
    }

    private int exec(Arguments arguments) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(arguments.script.getAbsolutePath());
        command.addAll(arguments.args);
        ProcessBuilder builder = new ProcessBuilder(command)
            .redirectInput(Redirect.INHERIT)
            .redirectOutput(Redirect.INHERIT)
            .redirectError(Redirect.INHERIT);
        Map<String, String> environment = builder.environment();
        for (Map.Entry<String, String> entry : arguments.env.entrySet()) {
            if (entry.getValue() == null) {
                environment.remove(entry.getKey());
            } else {
                environment.put(entry.getKey(), entry.getValue());
            }
        }
        Process process = builder.start();
        try {
            return process.waitFor();
        } finally {
            process.destroy();
        }
    }

    private static final class Arguments {

        final File script;

        final Map<String, String> env;

        final List<String> args;

        public Arguments(File script, Map<String, String> env, List<String> args) {
            this.script = script;
            this.env = env;
            this.args = args;
        }
    }
}
