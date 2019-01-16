/*
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
package com.asakusafw.gradle.tasks.internal

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.SkipWhenEmpty

/**
 * Abstract implementation of Gradle Task for Asakusa tools.
 * @since 0.6.1
 */
abstract class AbstractAsakusaToolTask extends DefaultTask {

    /**
     * The logback configuration for the tool.
     */
    File logbackConf

    /**
     * The max heap size for the tool.
     */
    String maxHeapSize

    /**
     * The Java system properties.
     */
    @Input
    Map<String, String> systemProperties = [:]

    /**
     * The Java VM arguments.
     */
    @Input
    List<String> jvmArgs = []

    /**
     * The source path (this will be resolved using {@code project.files(...)}).
     */
    List<Object> sourcepath = []

    /**
     * The tool class path (this will be resolved using {@code project.files(...)}).
     */
    @InputFiles
    List<Object> toolClasspath = []

    /**
     * The plugin libraries (this will be resolved using {@code project.files(...)}).
     */
    @Optional
    @InputFiles
    List<Object> pluginClasspath = []

    /**
     * Returns the source path.
     * @return the source path
     */
    protected FileCollection getSourcepathCollection() {
        return project.files(sourcepath)
    }

    /**
     * Returns the tool class path.
     * @return the tool class path
     */
    protected FileCollection getToolClasspathCollection() {
        return project.files(toolClasspath)
    }

    /**
     * Returns the plug-in class path.
     * @return the plug-in class path
     */
    protected FileCollection getPluginClasspathCollection() {
        return project.files(pluginClasspath)
    }

    /**
     * Returns the source files.
     * @return the source files
     */
    @SkipWhenEmpty
    @Optional
    @InputFiles
    FileCollection getSourceFiles() {
        def results = project.files()
        getSourcepathCollection().each { file ->
            if (file.isFile()) {
                results += project.files(file)
            } else if (file.isDirectory()) {
                results += project.fileTree(file)
            }
        }
        return results
    }

}
