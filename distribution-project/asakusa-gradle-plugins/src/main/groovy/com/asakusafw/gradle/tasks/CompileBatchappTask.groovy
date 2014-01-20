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
import org.gradle.api.tasks.TaskAction

/**
 * Gradle Task for DSL Compile.
 */
class CompileBatchappTask extends DefaultTask {

    /**
     * Task Action of this task.
     */
    @TaskAction
    def compileBatchapp() {
        def compilerPluginPath = ant.path {
            fileset(dir:"${System.env.ASAKUSA_HOME}/compiler/plugin", includes: '**/*.jar', erroronmissingdir: false)
        }
        def timestamp = new Date().format("yyyy-MM-dd HH:mm:ss")
        def javaVersion = System.properties['java.version']

        project.delete(project.asakusafw.compiler.compiledSourceDirectory)
        project.mkdir(project.asakusafw.compiler.compiledSourceDirectory)

        project.javaexec {
            main = 'com.asakusafw.compiler.bootstrap.AllBatchCompilerDriver'
            classpath = project.sourceSets.main.runtimeClasspath + project.configurations.provided
            maxHeapSize = project.asakusafw.maxHeapSize
            enableAssertions = true
            jvmArgs = [
                    "-Dlogback.configurationFile=${project.asakusafw.logbackConf}",
                    "-Dcom.asakusafw.compiler.options=${project.asakusafw.compiler.compilerOptions}",
                    "-Dcom.asakusafw.batchapp.build.timestamp=${timestamp}",
                    "-Dcom.asakusafw.batchapp.build.java.version=${javaVersion}",
                    "-Dcom.asakusafw.framework.version=${project.asakusafw.asakusafwVersion}",
                    "-Dcom.asakusafw.batchapp.project.version=${project.version}",
            ]
            args = [
                    '-output',
                    project.asakusafw.compiler.compiledSourceDirectory,
                    '-package',
                    project.asakusafw.compiler.compiledSourcePackage,
                    '-compilerwork',
                    project.asakusafw.compiler.compilerWorkDirectory,
                    '-hadoopwork',
                    project.asakusafw.compiler.hadoopWorkDirectory,
                    '-link',
                    project.sourceSets.main.output.classesDir,
                    '-scanpath',
                    project.sourceSets.main.output.classesDir,
                    '-skiperror',
            ]
            if (compilerPluginPath.size()) {
                args += [
                        '-plugin',
                        compilerPluginPath
                ]
            }
        }
    }
}
