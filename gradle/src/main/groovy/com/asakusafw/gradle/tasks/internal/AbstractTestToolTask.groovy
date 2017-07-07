/*
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
package com.asakusafw.gradle.tasks.internal

import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Nullable
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaExecSpec

/**
 * An abstract implementation of Asakusa test tool tasks.
 */
abstract class AbstractTestToolTask extends DefaultTask {

    /**
     * The logback configuration for the tool.
     */
    @Nullable
    File logbackConf

    /**
     * The max heap size for the tool.
     */
    @Nullable
    String maxHeapSize

    /**
     * The Java system properties.
     */
    @Input
    Map<Object, Object> systemProperties = [:]

    /**
     * The Java VM arguments.
     */
    @Input
    List<Object> jvmArgs = []

    /**
     * The tool class path.
     */
    @InputFiles
    FileCollection toolClasspath = project.files()

    /**
     * The target batch arguments.
     */
    @Input
    Map<Object, Object> batchArguments = [:]

    /**
     * The extra Hadoop properties.
     */
    @Input
    Map<Object, Object> hadoopProperties = [:]

    @Option(option = 'arguments', description = 'batch arguments separated by comma')
    void setEncodedBatchArguments(String encoded) {
        getBatchArguments().putAll(decodeMap(encoded))
    }

    @Option(option = 'properties', description = 'extra Hadoop properties separated by comma')
    void setEncodedHadoopProperties(String encoded) {
        getBatchArguments().putAll(decodeMap(encoded))
    }

    private Map<String, String> decodeMap(String encoded) {
        Map<String, String> map = [:]
        StringBuilder buf = new StringBuilder()
        boolean sawEscape = false
        for (char c in encoded.toCharArray()) {
            if (sawEscape) {
                buf.append c
                sawEscape = false
            } else if (c == '\\') {
                sawEscape = true
            } else if (c == ',') {
                addKeyValue buf.toString(), map
                buf.setLength 0
            } else {
                buf.append c
            }
        }
        addKeyValue buf.toString(), map
        return map
    }

    private void addKeyValue(String string, Map<String, String> kvs) {
        int index = string.indexOf '='
        if (index < 0) {
            throw new InvalidUserDataException("Invalid key-value: \"${string}\"")
        }
        kvs.put string.substring(0, index), string.substring(index + 1)
    }

    /**
     * Executes the tool with generic tool arguments.
     * @param mainClass the main class name
     * @param toolArguments the tool-specific arguments
     */
    protected void execute(String mainClass, List<String> toolArguments) {
        project.javaexec { JavaExecSpec spec ->
            spec.main = mainClass
            spec.classpath = getToolClasspath()
            spec.jvmArgs = ResolutionUtils.resolveToStringList(getJvmArgs())
            if (getMaxHeapSize()) {
                spec.maxHeapSize = getMaxHeapSize()
            }
            if (getLogbackConf()) {
                spec.systemProperties += ['logback.configurationFile' : getLogbackConf().absolutePath]
            }
            spec.systemProperties += ResolutionUtils.resolveToStringMap(this.getSystemProperties())
            spec.args += toolArguments
            ResolutionUtils.resolveToStringMap(getBatchArguments()).each { String key, String value ->
                spec.args += ['-A', "${key}=${value}"]
            }
            ResolutionUtils.resolveToStringMap(getHadoopProperties()).each { String key, String value ->
                spec.args += ['-D', "${key}=${value}"]
            }
        }
    }
}
