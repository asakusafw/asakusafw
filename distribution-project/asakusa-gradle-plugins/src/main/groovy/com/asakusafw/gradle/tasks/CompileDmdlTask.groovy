/*
 * Copyright 2011-2013 Asakusa Framework Team.
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

import org.gradle.api.tasks.*

/**
 * Gradle Task for DMDL Compile.
 */
class CompileDmdlTask extends SourceTask {

    /**
     * Task Action of this task.
     */
    @TaskAction
    def compileDmdl() {
        def dmdlPluginPath = ant.path {
            fileset(dir:"${System.env.ASAKUSA_HOME}/dmdl/plugin", includes: '**/*.jar', erroronmissingdir: false)
        }
        project.javaexec {
            main = 'com.asakusafw.dmdl.java.Main'
            classpath = project.sourceSets.main.compileClasspath
            maxHeapSize = project.asakusafw.maxHeapSize
            jvmArgs = [
                    "-Dlogback.configurationFile=${project.asakusafw.logbackConf}"
            ]
            args = [
                    '-output',
                    project.asakusafw.modelgen.modelgenSourceDirectory,
                    '-package',
                    project.asakusafw.modelgen.modelgenSourcePackage,
                    '-source',
                    project.asakusafw.dmdl.dmdlSourceDirectory,
                    '-sourceencoding',
                    project.asakusafw.dmdl.dmdlEncoding,
                    '-targetencoding',
                    project.asakusafw.javac.sourceEncoding
            ]
            if (dmdlPluginPath.size()) {
                args += [
                        '-plugin',
                        dmdlPluginPath
                ]
            }
        }
    }
}
