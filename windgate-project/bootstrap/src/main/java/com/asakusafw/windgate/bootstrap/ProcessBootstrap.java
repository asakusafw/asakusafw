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
package com.asakusafw.windgate.bootstrap;

import static com.asakusafw.windgate.bootstrap.CoreConstants.*;
import static com.asakusafw.windgate.bootstrap.WindGateConstants.*;

import java.io.File;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * Executes a WindGate task.
 * @since 0.10.0
 */
public class ProcessBootstrap {

    private final Environment environment;

    private final ClassLoader classLoader;

    /**
     * Creates a new instance.
     * @param environment the environment variables
     * @param classLoader the class loader
     */
    public ProcessBootstrap(Environment environment, ClassLoader classLoader) {
        this.environment = environment;
        this.classLoader = classLoader;
    }

    /**
     * Program entry.
     * @param args the program arguments
     */
    public static void main(String... args) {
        ProcessBootstrap bootstrap = new ProcessBootstrap(Environment.system(), ClassLoader.getSystemClassLoader());
        bootstrap.exec(Context.parseForProcess(args));
    }

    /**
     * Executes Vanilla application.
     * @param context the application context
     */
    public void exec(Context context) {
        context.installLogSettings();
        Classpath classpath = buildClasspath(context);
        String[] arguments = buildArguments(context);
        classpath.exec(classLoader, CLASS_WINDGATE_PROCESS, arguments);
    }

    private Classpath buildClasspath(Context context) {
        Classpath cp = new Classpath();

        // application
        Path application = getApplication(environment, context.getBatchId());
        cp.add(getAppJobflowLibFile(application, context.getFlowId()), true);
        cp.add(application.resolve(PATH_APP_USER_LIB_DIR), false);

        // framework
        Path home = getHome(environment);
        cp.add(home.resolve(PATH_WINDGATE_CONF_DIR), false);
        cp.addEntries(home.resolve(PATH_WINDGATE_LIB_DIR), true);
        cp.addEntries(home.resolve(PATH_EXTENSION_LIB_DIR), false);
        cp.addEntries(home.resolve(PATH_CORE_LIB_DIR), true);

        // hadoop
        cp.addEntries(home.resolve(PATH_HADOOP_EMBEDDED_LIB_DIR), true);

        return cp;
    }

    private String[] buildArguments(Context context) {
        Path home = getHome(environment);
        return new String[] {
                "-mode", context.getSessionKind(),
                "-profile", getProfileUri(home, context.getProfileName()).toString(),
                "-script", context.getScriptPath(),
                "-session", context.getExecutionId(),
                "-plugin", getPluginPath(home).stream()
                        .map(Path::toString)
                        .collect(Collectors.joining(File.pathSeparator)),
                "-arguments", context.getWindGateArguments(),
        };
    }
}
