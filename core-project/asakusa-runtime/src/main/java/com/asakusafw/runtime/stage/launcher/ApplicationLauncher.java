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
package com.asakusafw.runtime.stage.launcher;

import static com.asakusafw.runtime.stage.launcher.Util.*;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**W
 * The Asakusa application launcher.
 * @since 0.7.0
 */
public final class ApplicationLauncher {

    static final Log LOG = LogFactory.getLog(ApplicationLauncher.class);

    /**
     * The configuration key of whether the application may be launched via {@link ApplicationLauncher}.
     * This will be only set by this class.
     */
    public static final String KEY_LAUNCHER_USED = "com.asakusafw.launcher.used";

    /**
     * Failed to prepare the application.
     */
    public static final int LAUNCH_ERROR = -2;

    /**
     * Exception occurred in the application.
     */
    public static final int CLIENT_ERROR = -1;

    private ApplicationLauncher() {
        return;
    }

    /**
     * The program entry.
     * @param args the launcher arguments
     */
    public static void main(String... args) {
        int status = exec(args);
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Executes launcher with default configuration.
     * @param args the launcher arguments
     * @return the exit status
     */
    public static int exec(String... args) {
        return exec(new Configuration(), args);
    }

    /**
     * Executes launcher.
     * @param configuration the Hadoop configuration for the application
     * @param args the launcher arguments
     * @return the exit status
     */
    public static int exec(Configuration configuration, String... args) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Preparing application: {0}",
                    Arrays.toString(args)));
        }
        configuration.setBoolean(KEY_LAUNCHER_USED, true);
        LauncherOptions options;
        try {
            options = LauncherOptionsParser.parse(configuration, args);
        } catch (Exception e) {
            LOG.error(MessageFormat.format(
                    "Exception occurred in launcher: {0}",
                    Arrays.toString(args)), e);
            return LAUNCH_ERROR;
        }
        try {
            Configuration conf = options.getConfiguration();
            conf.setClassLoader(options.getApplicationClassLoader());
            Tool tool;
            try {
                tool = ReflectionUtils.newInstance(options.getApplicationClass(), conf);
            } catch (Exception e) {
                LOG.error(MessageFormat.format(
                        "Exception occurred in launcher: {0}{1}",
                        options.getApplicationClass().getName(),
                        options.getApplicationArguments()), e);
                return LAUNCH_ERROR;
            }
            try {
                return launch(conf, tool, options.getApplicationArgumentArray());
            } catch (Exception e) {
                LOG.error(MessageFormat.format(
                        "Exception occurred in launcher: {0}{1}",
                        options.getApplicationClass().getName(),
                        options.getApplicationArguments()), e);
                return CLIENT_ERROR;
            }
        } finally {
            closeQuiet(options.getApplicationClassLoader());
            for (File file : options.getApplicationCacheDirectories()) {
                if (delete(file) == false) {
                    LOG.warn(MessageFormat.format(
                            "Failed to delete the application cache directory: {0}",
                            file));
                }
            }
        }
    }

    private static int launch(Configuration conf, Tool tool, String[] args) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Launching application: {0}{1}",
                    tool.getClass().getName(),
                    Arrays.toString(args)));
        }
        ClassLoader contextClassLoader = setContextClassLoader(conf.getClassLoader());
        try {
            return ToolRunner.run(conf, tool, args);
        } finally {
            setContextClassLoader(contextClassLoader);
        }
    }

    private static ClassLoader setContextClassLoader(final ClassLoader classLoader) {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                ClassLoader old = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(classLoader);
                return old;
            }
        });
    }
}
