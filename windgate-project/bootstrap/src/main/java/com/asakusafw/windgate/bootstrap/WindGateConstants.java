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
package com.asakusafw.windgate.bootstrap;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Constants of Asakusa Vanilla.
 * @since 0.10.0
 */
public final class WindGateConstants {

    static final String PATH_WINDGATE_BASE = "windgate";

    /**
     * The path of the WindGate configuration directory.
     */
    public static final String PATH_WINDGATE_CONF_DIR = PATH_WINDGATE_BASE + "/conf";

    /**
     * The path of the WindGate profile directory.
     */
    public static final String PATH_WINDGATE_PROFILE_DIR = PATH_WINDGATE_BASE + "/profile";

    /**
     * The suffix of WindGate profile file.
     */
    public static final String PATH_WINDGATE_PROFILE_SUFFIX = ".properties";

    /**
     * The path of the WindGate core libraries directory.
     */
    public static final String PATH_WINDGATE_LIB_DIR = PATH_WINDGATE_BASE + "/lib";

    /**
     * The path of the WindGate plug-in libraries directory.
     */
    public static final String PATH_WINDGATE_PLUGIN_DIR = PATH_WINDGATE_BASE + "/plugin";

    /**
     * The class name of WindGate process command.
     */
    public static final String CLASS_WINDGATE_PROCESS = "com.asakusafw.windgate.cli.WindGate";

    /**
     * The class name of WindGate finalize command.
     */
    public static final String CLASS_WINDGATE_FINALIZE = "com.asakusafw.windgate.cli.WindGateAbort";

    private WindGateConstants() {
        return;
    }

    /**
     * Returns the profile path.
     * @param home the framework installation directory
     * @param profileName the profile name
     * @return the profile path
     */
    public static URI getProfileUri(Path home, String profileName) {
        return home
                .resolve(PATH_WINDGATE_PROFILE_DIR)
                .resolve(profileName + PATH_WINDGATE_PROFILE_SUFFIX)
                .toUri();
    }

    /**
     * Returns the WindGate plug-in files.
     * @param home the framework installation home directory
     * @return the plug-in files
     */
    public static List<Path> getPluginPath(Path home) {
        Path directory = home.resolve(PATH_WINDGATE_PLUGIN_DIR);
        if (Files.isDirectory(directory)) {
            try {
                return Files.list(directory)
                        .filter(Files::isRegularFile)
                        .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyList();
    }
}
