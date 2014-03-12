/*
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
package com.asakusafw.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Nullable
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaExecSpec

import com.asakusafw.gradle.tasks.internal.ResolutionUtils

/**
 * Gradle Task for Running Asakusa batch application.
 * @since 0.6.1
 */
class RunBatchappTask extends DefaultTask {

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
    Map<Object, Object> systemProperties = [:]

    /**
     * The Java VM arguments.
     */
    List<Object> jvmArgs = []

    /**
     * The tool class path.
     */
    @InputFiles
    FileCollection toolClasspath = project.files()

    /**
     * The target batch ID.
     */
    @Input
    String batchId

    /**
     * The target batch arguments.
     */
    @Input
    Map<Object, Object> batchArguments = [:]

    /**
     * Sets the target batch ID.
     * @param batchId the target batch ID
     */
    @Option(option = 'id', description = 'The target batch ID')
    void setBatchId(String batchId) {
        this.batchId = batchId
    }

    @Option(option = 'arguments', description = 'The target batch arguments separated by comma')
    void setEncodedBatchArguments(String encoded) {
        Map<String, String> args = [:]
        StringBuilder buf = new StringBuilder()
        boolean sawEscape = false
        for (char c in encoded.toCharArray()) {
            if (sawEscape) {
                buf.append c
                sawEscape = false
            } else if (c == '\\') {
                sawEscape = true
            } else if (c == ',') {
                addArg buf.toString(), args
                buf.setLength 0
            } else {
                buf.append c
            }
        }
        addArg buf.toString(), args
        getBatchArguments().putAll(args)
    }

    private void addArg(String string, Map<String, String> kvs) {
        int index = string.indexOf '='
        if (index < 0) {
            throw new InvalidUserDataException("Invalid batch argument: \"${string}\"")
        }
        kvs.put string.substring(0, index), string.substring(index + 1)
    }

    /**
     * Performs this task.
     */
    @TaskAction
    void perform() {
        project.javaexec { JavaExecSpec spec ->
            spec.main = 'com.asakusafw.testdriver.tools.runner.BatchTestRunner'
            spec.classpath = this.getToolClasspath()
            spec.jvmArgs = ResolutionUtils.resolveToStringList(this.getJvmArgs())
            if (this.getMaxHeapSize()) {
                spec.maxHeapSize = this.getMaxHeapSize()
            }
            if (this.getLogbackConf()) {
                spec.systemProperties += ['logback.configurationFile' : this.getLogbackConf().absolutePath]
            }
            spec.systemProperties += ResolutionUtils.resolveToStringMap(this.getSystemProperties())
            spec.args += ['--batch', this.getBatchId()]
            for (Map.Entry<String, String> entry in ResolutionUtils.resolveToStringMap(this.getBatchArguments()).entrySet()) {
                spec.args += ['-A', "${entry.key}=${entry.value}"]
            }
        }
    }
}
