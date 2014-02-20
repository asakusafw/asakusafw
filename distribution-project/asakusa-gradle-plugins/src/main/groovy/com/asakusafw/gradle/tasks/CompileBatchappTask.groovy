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
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import com.asakusafw.gradle.tasks.internal.AbstractAsakusaToolTask

/**
 * Gradle Task for DSL Compile.
 * @since 0.5.3
 * @version 0.6.1
 */
class CompileBatchappTask extends AbstractAsakusaToolTask {

    /**
     * The current framework version.
     */
    @Optional
    @Input
    String frameworkVersion

    /**
     * The package name for generated sources.
     */
    @Input
    String packageName

    /**
     * The Asaksua DSL compiler options
     */
    @Optional
    @Input
    String compilerOptions

    /**
     * The compiler working directory.
     */
    File workingDirectory

    /**
     * The working directory while running the compiled batch application
     * (relative path from the original Hadoop working directory).
     */
    @Input
    String hadoopWorkingDirectory

    /**
     * The batch application output base path.
     */
    @OutputDirectory
    File outputDirectory

    /**
     * {@code true} to stop compilation immediately when detects any compilation errors.
     */
    @Input
    boolean failFast = false

    /**
     * Returns the application project version.
     * @return the application project version
     */
    @Input
    def getProjectVersion() {
        return project.version
    }

    /**
     * Task Action of this task.
     */
    @TaskAction
    def compileBatchapp() {
        def timestamp = new Date().format("yyyy-MM-dd HH:mm:ss")
        project.delete(getOutputDirectory())
        project.mkdir(getOutputDirectory())

        def compilerWorkingDirectory = this.getWorkingDirectory() ?: new File(project.buildDir, UUID.randomUUID().toString())
        try {
            project.javaexec {
                main = 'com.asakusafw.compiler.bootstrap.AllBatchCompilerDriver'
                delegate.classpath = this.getSourcepathCollection() + this.getToolClasspathCollection()
                delegate.jvmArgs = this.getJvmArgs()
                if (this.getMaxHeapSize()) {
                    delegate.maxHeapSize = this.getMaxHeapSize()
                }
                if (this.getLogbackConf()) {
                    delegate.systemProperties += [ 'logback.configurationFile' : this.getLogbackConf().absolutePath ]
                }
                delegate.systemProperties += [ 'com.asakusafw.batchapp.project.version' : this.getProjectVersion() ]
                delegate.systemProperties += [ 'com.asakusafw.batchapp.build.timestamp' : timestamp ]
                delegate.systemProperties += [ 'com.asakusafw.batchapp.build.java.version' : System.properties['java.version'] ]
                if (this.getCompilerOptions()) {
                    delegate.systemProperties += [ 'com.asakusafw.compiler.options' : this.getCompilerOptions() ]
                }
                if (this.getFrameworkVersion()) {
                    delegate.systemProperties += [ 'com.asakusafw.framework.version' : this.getFrameworkVersion() ]
                }
                delegate.enableAssertions = true
                delegate.args = [
                        '-output',
                        this.getOutputDirectory(),
                        '-package',
                        this.getPackageName(),
                        '-compilerwork',
                        this.getWorkingDirectory(),
                        '-hadoopwork',
                        this.getHadoopWorkingDirectory(),
                        '-link',
                        this.getSourcepathCollection().asPath,
                        '-scanpath',
                        this.getSourcepathCollection().asPath,
                ]
                if (!isFailFast()) {
                    delegate.args << '-skiperror'
                }
                def plugins = this.getPluginClasspathCollection()
                if (plugins != null && !plugins.empty) {
                    delegate.args += [
                        '-plugin',
                        plugins.asPath
                    ]
                }
            }
        } finally {
            if (!this.getWorkingDirectory()) {
                project.delete(compilerWorkingDirectory)
            }
        }
    }
}
