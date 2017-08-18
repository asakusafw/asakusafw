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
package com.asakusafw.operation.tools.setup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

/**
 * Validates Asakusa framework installation.
 * @since 0.10.0
 */
public final class Setup {

    static final String NAME = System.getProperty("cli.name", "<self.jar>");

    static final String ENV_INSTALLATION = "ASAKUSA_HOME";

    static final String PATH_VERSION = "VERSION";

    private Setup() {
        return;
    }

    /**
     * Program entry.
     * @param args installation paths
     */
    public static void main(String... args) {
        int exitValue = exec(args);
        if (exitValue != 0) {
            System.exit(exitValue);
        }
    }

    static int exec(String... args) {
        if (args.length >= 2) {
            usage();
            return 1;
        } else if (args.length == 1) {
            switch (args[0]) {
            case "-h":
            case "--help":
                usage();
                return 0;
            default:
                break;
            }
            return exec(Paths.get(args[0]));
        } else {
            String path = System.getenv(ENV_INSTALLATION);
            if (path == null || path.isEmpty()) {
                System.out.printf("environment variable %s must be defined.%n", ENV_INSTALLATION);
                return 2;
            }
            return exec(Paths.get(path));
        }
    }

    static int exec(Path path) {
        System.out.printf("setup: %s%n", path);
        if (Files.exists(path) == false) {
            System.out.printf("installation not found: %s%n", path);
            usage();
            return 2;
        }
        if (check(path) == false) {
            System.out.printf("installation is not valid: %s%n", path);
            usage();
            return 2;
        }
        try {
            MakeExecutable.setExecutable(path);
        } catch (IOException e) {
            e.printStackTrace();
            return 2;
        }
        return 0;
    }

    private static boolean check(Path path) {
        Path versionFile = path.resolve(PATH_VERSION);
        if (Files.isRegularFile(versionFile) == false) {
            return false;
        }
        String version;
        try (InputStream input = Files.newInputStream(versionFile)) {
            Properties props = new Properties();
            props.load(input);
            version = props.getProperty("asakusafw.version");
            if (version == null) {
                return false;
            } else {
                System.out.printf("framework version: %s%n", version);
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void usage() {
        System.out.printf("Usage:%n");
        System.out.printf("    java -jar %s%n", NAME);
        System.out.printf("        Processes the default installation (%s=%s).%n",
                ENV_INSTALLATION,
                Optional.ofNullable(System.getenv(ENV_INSTALLATION))
                        .filter(it -> it.isEmpty() == false)
                        .orElse("(not defined)"));
        System.out.printf("    java -jar %s /path/to/installation%n", NAME);
        System.out.printf("        Processes the specified installation.%n");
        System.out.printf("    java -jar %s --help%n", NAME);
        System.out.printf("        Prints this message.%n");
    }
}
