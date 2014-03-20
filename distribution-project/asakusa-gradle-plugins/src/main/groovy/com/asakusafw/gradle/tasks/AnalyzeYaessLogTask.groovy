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
import org.gradle.api.Nullable
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaExecSpec

import com.asakusafw.gradle.tasks.internal.ResolutionUtils

/**
 * Gradle Task for analyzing YAESS log files.
 * @since 0.6.2
 */
class AnalyzeYaessLogTask extends DefaultTask {

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
     * The input driver class name.
     */
    @Input
    String inputDriver

    /**
     * The output driver class name.
     */
    @Input
    String outputDriver

    /**
     * The input file path.
     */
    @Optional
    @InputFile
    File inputFile

    /**
     * The output file path.
     */
    @Optional
    @OutputFile
    File outputFile

    /**
     * The input driver arguments.
     */
    @Input
    Map<Object, Object> inputArguments = [:]

    /**
     * The output driver arguments.
     */
    @Input
    Map<Object, Object> outputArguments = [:]

    /**
     * Sets the input log file path.
     * @param path the input log file path
     */
    @Option(option = 'input', description = 'The input file')
    void setInput(String path) {
        setInputFile(project.file(path))
    }

    /**
     * Sets the output file path.
     * @param path the output file path
     */
    @Option(option = 'output', description = 'The output file')
    void setOutput(String path) {
        setOutputFile(project.file(path))
    }

    /**
     * Performs this task.
     */
    @TaskAction
    void perform() {
        def inputArgs = ResolutionUtils.resolveToStringMap(getInputArguments())
        if (getInputFile() != null) {
            inputArgs += [ file : getInputFile().absolutePath ]
        }
        def outputArgs = ResolutionUtils.resolveToStringMap(getOutputArguments())
        if (getOutputFile() != null) {
            outputArgs += [ file : getOutputFile().absolutePath ]
        }
        project.javaexec { JavaExecSpec spec ->
            spec.main = 'com.asakusafw.yaess.tools.log.cli.Main'
            spec.classpath = this.getToolClasspath()
            spec.jvmArgs = ResolutionUtils.resolveToStringList(this.getJvmArgs())
            if (this.getMaxHeapSize()) {
                spec.maxHeapSize = this.getMaxHeapSize()
            }
            if (this.getLogbackConf()) {
                spec.systemProperties += ['logback.configurationFile' : this.getLogbackConf().absolutePath]
            }
            spec.systemProperties += ResolutionUtils.resolveToStringMap(this.getSystemProperties())
            spec.args += ['--input', this.getInputDriver()]
            spec.args += ['--output', this.getOutputDriver()]
            for (Map.Entry<String, String> entry in inputArgs.entrySet()) {
                spec.args += ['-I', "${entry.key}=${entry.value}"]
            }
            for (Map.Entry<String, String> entry in outputArgs.entrySet()) {
                spec.args += ['-O', "${entry.key}=${entry.value}"]
            }
        }
    }
}
