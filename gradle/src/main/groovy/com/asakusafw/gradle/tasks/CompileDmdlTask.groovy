/*
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
package com.asakusafw.gradle.tasks

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.JavaExecSpec

import com.asakusafw.gradle.tasks.internal.AbstractAsakusaToolTask
import com.asakusafw.gradle.tasks.internal.ToolLauncherUtils

/**
 * Gradle Task for DMDL Compile.
 * @since 0.5.3
 * @version 0.8.0
 */
class CompileDmdlTask extends AbstractAsakusaToolTask {

    /**
     * The tool launcher class libraries (can empty).
     * @since 0.8.0
     */
    List<Object> launcherClasspath = []

    /**
     * The data model base package name.
     */
    @Input
    String packageName

    /**
     * The DMDL script source encoding.
     */
    @Input
    String sourceEncoding

    /**
     * The Java data model source encoding.
     */
    @Input
    String targetEncoding

    /**
     * The data model classes output base path.
     */
    @OutputDirectory
    File outputDirectory

    /**
     * Task Action of this task.
     */
    @TaskAction
    def compileDmdl() {
        String javaMain = 'com.asakusafw.dmdl.java.Main'
        FileCollection javaClasspath = project.files(getToolClasspath())
        List<String> javaArguments = createArguments()
        FileCollection launcher = project.files(getLauncherClasspath())
        if (!launcher.empty) {
            logger.info "Starting DMDL compiler using launcher"
            File script = ToolLauncherUtils.createLaunchFile(this, javaClasspath, javaMain, javaArguments)
            javaMain = ToolLauncherUtils.MAIN_CLASS
            javaClasspath = launcher
            javaArguments = [script.absolutePath]
        }
        project.javaexec { JavaExecSpec spec ->
            spec.main = javaMain
            spec.classpath = javaClasspath
            spec.jvmArgs = this.getJvmArgs()
            if (this.getMaxHeapSize() != null) {
                spec.maxHeapSize = this.getMaxHeapSize()
            }
            if (this.getLogbackConf()) {
                spec.systemProperties += [ 'logback.configurationFile' : this.getLogbackConf().absolutePath ]
            }
            spec.systemProperties += this.getSystemProperties()
            spec.enableAssertions = true
            spec.args = javaArguments
        }
    }

    private List<String> createArguments() {
        List<String> results = []
        configureString(results, '-output', getOutputDirectory().absolutePath)
        configureString(results, '-package', getPackageName())
        configureFiles(results, '-source', getSourcepathCollection())
        configureString(results, '-sourceencoding', getSourceEncoding())
        configureString(results, '-targetencoding', getTargetEncoding())
        configureFiles(results, '-plugin', getPluginClasspathCollection())
        return results
    }

    private void configureFiles(List<String> arguments, String key, Object files) {
        FileCollection f = project.files(files)
        if (f.isEmpty()) {
            return
        }
        configureString(arguments, key, f.asPath)
    }

    private void configureString(List<String> arguments, String key, Object value) {
        if (value == null) {
            return
        }
        String s = String.valueOf(value)
        if (s.isEmpty()) {
            return
        }
        logger.debug("DMDL compiler option: ${key}=${s}")
        arguments << key << s
    }
}
