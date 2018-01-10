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
package com.asakusafw.windgate.bootstrap;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

/**
 * Constants of Asakusa Framework Core.
 * @since 0.10.0
 */
public final class CoreConstants {

    /**
     * The environment variable name of framework installation path.
     */
    public static final String ENV_ASAKUSA_HOME = "ASAKUSA_HOME";

    /**
     * The environment variable name of batch application installation path.
     */
    public static final String ENV_ASAKUSA_BATCHAPPS_HOME = "ASAKUSA_BATCHAPPS_HOME";

    /**
     * The default path of batch application installation path (relative from framework root).
     */
    public static final String PATH_DEFAULT_BATCHAPPS_DIR = "batchapps";

    /**
     * The framework core libraries directory path (relative from framework root).
     */
    public static final String PATH_CORE_LIB_DIR = "core/lib";

    /**
     * The framework core configuration directory path (relative from framework root).
     */
    public static final String PATH_CORE_CONF_DIR = "core/conf";

    /**
     * The framework core configuration file path (relative from framework root).
     */
    public static final String PATH_CORE_CONF_FILE = PATH_CORE_CONF_DIR + "/asakusa-resources.xml";

    /**
     * The framework embedded Hadoop libraries directory path (relative from framework root).
     */
    public static final String PATH_HADOOP_EMBEDDED_LIB_DIR = "hadoop/lib";

    /**
     * The framework extension libraries directory path (relative from framework root).
     */
    public static final String PATH_EXTENSION_LIB_DIR = "ext/lib";

    /**
     * The jobflow package file path prefix (relative from batchapps root).
     */
    public static final String PATH_APP_JOBFLOW_LIB_FILE_PREFIX = "lib/jobflow-";

    /**
     * The jobflow package file path suffix.
     */
    public static final String PATH_APP_JOBFLOW_LIB_FILE_SUFFIX = ".jar";

    /**
     * The batch shared libraries directory path (relative from batchapps root).
     */
    public static final String PATH_APP_USER_LIB_DIR = "usr/lib";

    private CoreConstants() {
        return;
    }

    /**
     * Returns the framework installation directory.
     * @param env the current environment variables
     * @return the target path
     */
    public static Path getHome(Environment env) {
        return env.find(ENV_ASAKUSA_HOME)
                .filter(it -> it.isEmpty() == false)
                .map(Paths::get)
                .orElseThrow(() -> new IllegalStateException(MessageFormat.format(
                        "environment variable \"{0}\" must be defined",
                        ENV_ASAKUSA_HOME)));
    }

    /**
     * Returns the batch application installation directory.
     * @param env the current environment variables
     * @return the target path
     */
    public static Path getBatchappsHome(Environment env) {
        return env.find(ENV_ASAKUSA_BATCHAPPS_HOME)
                .filter(it -> it.isEmpty() == false)
                .map(Paths::get)
                .orElseGet(() -> getHome(env).resolve(PATH_DEFAULT_BATCHAPPS_DIR));
    }

    /**
     * Returns the application base directory.
     * @param env the current environment variables
     * @param batchId the batch ID
     * @return the target path
     */
    public static Path getApplication(Environment env, String batchId) {
        return getBatchappsHome(env).resolve(batchId);
    }

    /**
     * Returns the jobflow package file.
     * @param application {@link #getApplication(Environment, String) the application directory}
     * @param flowId the target flow ID
     * @return the target path
     */
    public static Path getAppJobflowLibFile(Path application, String flowId) {
        String entry = PATH_APP_JOBFLOW_LIB_FILE_PREFIX + flowId + PATH_APP_JOBFLOW_LIB_FILE_SUFFIX;
        return application.resolve(entry);
    }
}
