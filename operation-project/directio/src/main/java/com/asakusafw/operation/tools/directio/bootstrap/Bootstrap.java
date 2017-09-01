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
package com.asakusafw.operation.tools.directio.bootstrap;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

import com.asakusafw.operation.tools.directio.DirectIo;

/**
 * A bootstrap for non-BASH platform.
 * @since 0.10.0
 */
public final class Bootstrap {

    private static final String MAIN_CLASS = DirectIo.class.getName();

    private static final String ENV_ASAKUSA_HOME = "ASAKUSA_HOME";

    private static final String PATH_TOOLS_LIB_FILE = "tools/lib/asakusa-directio-tools.jar";

    private static final String PATH_CORE_LIB = "core/lib";

    private static final String PATH_EMBEDDED_HADOOP_LIB = "hadoop/lib";

    private static final String PATH_EMBEDDED_HADOOP_LOGGING_LIB = PATH_EMBEDDED_HADOOP_LIB + "/logging";

    private Bootstrap() {
        return;
    }

    /**
     * Program entry.
     * @param args program arguments
     */
    public static void main(String... args) {
        exec(Environment.system(), args);
    }

    /**
     * Executes the given class.
     * @param env the current environment variables
     * @param args the program arguments
     */
    public static void exec(Environment env, String... args) {
        Path home = env.find(ENV_ASAKUSA_HOME)
                .filter(it -> it.isEmpty() == false)
                .map(Paths::get)
                .orElseThrow(() -> new IllegalStateException(MessageFormat.format(
                        "environment variable \"{0}\" must be defined",
                        ENV_ASAKUSA_HOME)));

        Classpath classpath = new Classpath();
        classpath.add(home.resolve(PATH_TOOLS_LIB_FILE), true);
        classpath.addEntries(home.resolve(PATH_CORE_LIB), true);
        classpath.addEntries(home.resolve(PATH_EMBEDDED_HADOOP_LIB), true);
        classpath.addEntries(home.resolve(PATH_EMBEDDED_HADOOP_LOGGING_LIB), false);

        classpath.exec(ClassLoader.getSystemClassLoader(), MAIN_CLASS, args);
    }
}
