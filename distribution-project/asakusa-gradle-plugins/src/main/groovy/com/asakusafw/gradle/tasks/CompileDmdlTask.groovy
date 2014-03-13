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

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import com.asakusafw.gradle.tasks.internal.AbstractAsakusaToolTask

/**
 * Gradle Task for DMDL Compile.
 * @since 0.5.3
 * @version 0.6.1
 */
class CompileDmdlTask extends AbstractAsakusaToolTask {

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
        project.javaexec {
            delegate.main = 'com.asakusafw.dmdl.java.Main'
            delegate.classpath = this.getToolClasspathCollection()
            delegate.jvmArgs = this.getJvmArgs()
            if (this.getMaxHeapSize()) {
                delegate.maxHeapSize = this.getMaxHeapSize()
            }
            if (this.getLogbackConf()) {
                delegate.systemProperties += [ 'logback.configurationFile' : this.getLogbackConf().absolutePath ]
            }
            delegate.systemProperties += this.getSystemProperties()
            delegate.args = [
                    '-output',
                    getOutputDirectory(),
                    '-package',
                    getPackageName(),
                    '-source',
                    getSourcepathCollection().asPath,
                    '-sourceencoding',
                    getSourceEncoding(),
                    '-targetencoding',
                    getTargetEncoding()
            ]
            def plugins = this.getPluginClasspathCollection()
            if (plugins != null && !plugins.empty) {
                delegate.args += [
                    '-plugin',
                    plugins.asPath
                ]
            }
        }
    }
}
