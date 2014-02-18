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
 * Gradle Task for Generating test data template of TestDriver.
 * @since 0.5.3
 * @version 0.6.1
 */
class GenerateTestbookTask extends AbstractAsakusaToolTask {

    /**
     * The DMDL script source encoding.
     */
    @Input
    String sourceEncoding

    /**
     * The format of test data sheet (DATA|RULE|INOUT|INSPECT|ALL|DATAX|RULEX|INOUTX|INSPECTX|ALLX).
     */
    @Input
    String outputSheetFormat

    /**
     * The test data template output path.
     */
    @OutputDirectory
    File outputDirectory

    /**
     * Task Action of this task.
     */
    @TaskAction
    def generateTestbook() {
        project.javaexec {
            main = 'com.asakusafw.testdata.generator.excel.Main'
            delegate.classpath = this.getToolClasspathCollection()
            delegate.jvmArgs = this.getJvmArgs()
            if (this.getMaxHeapSize()) {
                delegate.maxHeapSize = this.getMaxHeapSize()
            }
            if (this.getLogbackConf()) {
                delegate.systemProperties += [ 'logback.configurationFile' : this.getLogbackConf().absolutePath ]
            }
            delegate.systemProperties += [ 'java.awt.headless' : true ]
            delegate.systemProperties += this.getSystemProperties()
            delegate.args = [
                    '-format',
                    this.getOutputSheetFormat(),
                    '-output',
                    this.getOutputDirectory(),
                    '-source',
                    this.getSourcepathCollection().asPath,
                    '-encoding',
                    this.getSourceEncoding(),
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
