/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.workflow.hadoop.bootstrap;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;

/**
 * A Hadoop bridge bootstrap for non-BASH platform.
 * @since 0.10.0
 */
public final class Bootstrap {

    private static final String ENV_ASAKUSA_HOME = "ASAKUSA_HOME";

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
        if (args.length < 2) {
            throw new IllegalArgumentException(Arrays.toString(args));
        }
        Path library = Paths.get(args[0]);
        String mainClass = args[1];
        String[] delegateArgs = Arrays.copyOfRange(args, 2, args.length);
        exec(Environment.system(), library, mainClass, delegateArgs);
    }

    /**
     * Executes the given class.
     * @param env the current environment variables
     * @param library the launcher library path
     * @param mainClass the main class name
     * @param args the program arguments
     */
    public static void exec(Environment env, Path library, String mainClass, String... args) {
        Path home = env.find(ENV_ASAKUSA_HOME)
                .filter(it -> it.isEmpty() == false)
                .map(Paths::get)
                .orElseThrow(() -> new IllegalStateException(MessageFormat.format(
                        "environment variable \"{0}\" must be defined",
                        ENV_ASAKUSA_HOME)));

        Classpath classpath = new Classpath();
        classpath.add(library, true);
        classpath.addEntries(home.resolve(PATH_EMBEDDED_HADOOP_LIB), true);
        classpath.addEntries(home.resolve(PATH_EMBEDDED_HADOOP_LOGGING_LIB), false);

        classpath.exec(ClassLoader.getSystemClassLoader(), mainClass, args);
    }
}
