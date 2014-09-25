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

import java.io.File;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.Tool;

/**
 * Options for application launcher.
 */
public class LauncherOptions {

    private final Configuration configuration;

    private final Class<? extends Tool> applicationClass;

    private final List<String> applicationArguments;

    private final URLClassLoader applicationClassLoader;

    private final Set<File> applicationCacheDirectories;

    /**
     * Creates a new instance.
     * @param configuration the current configuration
     * @param applicationClass the application class
     * @param applicationArguments the application arguments
     * @param applicationClassLoader the application class loader
     * @param applicationCacheDirectories the application cache directories (may not exist)
     */
    public LauncherOptions(
            Configuration configuration,
            Class<? extends Tool> applicationClass,
            List<String> applicationArguments,
            URLClassLoader applicationClassLoader,
            Set<File> applicationCacheDirectories) {
        this.configuration = configuration;
        this.applicationClass = applicationClass;
        this.applicationArguments = applicationArguments;
        this.applicationClassLoader = applicationClassLoader;
        this.applicationCacheDirectories = applicationCacheDirectories;
    }

    /**
     * Returns the Hadoop configuration.
     * @return the Hadoop configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Returns the application class.
     * @return the application class
     */
    public Class<? extends Tool> getApplicationClass() {
        return applicationClass;
    }

    /**
     * Returns the application arguments.
     * @return the application arguments
     */
    public List<String> getApplicationArguments() {
        return applicationArguments;
    }

    /**
     * Returns the application arguments.
     * @return the application arguments
     */
    public String[] getApplicationArgumentArray() {
        return applicationArguments.toArray(new String[applicationArguments.size()]);
    }

    /**
     * Returns the application class loader.
     * This can be disposed after application was finished.
     * @return the application class loader
     */
    public URLClassLoader getApplicationClassLoader() {
        return applicationClassLoader;
    }

    /**
     * Returns the application cache directory.
     * This can be deleted after application was finished.
     * @return the application cache directory (may not exist)
     */
    public Set<File> getApplicationCacheDirectories() {
        return applicationCacheDirectories;
    }
}
