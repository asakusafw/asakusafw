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
package com.asakusafw.gradle.plugins

import org.gradle.api.Project

/**
 * Gradle IntelliJ IDEA plugin enhancements for Asakusa Framework.
 */
class IdeaPluginEnhancement {

    private Project project

    void apply(Project project) {
        this.project = project
        afterPluginEnabled('idea') {
            configureIdeaPlugin()
        }
    }

    private void afterPluginEnabled(String pluginId, Closure<?> closure) {
        project.plugins.matching({ it == project.plugins.findPlugin(pluginId) }).all {
            // without delegate to the found plugin
            closure.call()
        }
    }

    private void configureIdeaPlugin() {
        extendIdeaProjectTask()
        extendIdeaModuleTask()
    }

    private void extendIdeaProjectTask() {
        project.idea.project {
            jdkName = project.asakusafw.javac.sourceCompatibility
            languageLevel = project.asakusafw.javac.sourceCompatibility

            ipr.withXml { projectXml ->
                projectXml.asNode().component.find {
                    it.@name == 'CompilerConfiguration'
                }.annotationProcessing[0].replaceNode {
                    annotationProcessing {
                        profile(default: true, name: 'Default', enabled: true) {
                            sourceOutputDir name: project.relativePath("${project.asakusafw.javac.annotationSourceDirectory}")
                            sourceTestOutputDir name: project.relativePath("${project.asakusafw.javac.annotationSourceDirectory}" + "-test")
                            outputRelativeToContentRoot value: true
                            processorPath useClasspath: true
                        }
                    }
                }
            }
        }
    }

    private void extendIdeaModuleTask() {
        project.idea.module {
            scopes.COMPILE.plus += [project.configurations.embedded]
            scopes.PROVIDED.plus += [project.configurations.provided]

            sourceDirs += [
                project.file(project.asakusafw.modelgen.modelgenSourceDirectory),
                project.file(project.asakusafw.javac.annotationSourceDirectory)
            ]
            excludeDirs = [
                project.file('.gradle')
            ]
        }
        project.ideaModule.doFirst {
            makeGeneratedSourceDir()
        }
    }

    private void makeGeneratedSourceDir() {
        if (!project.file(project.asakusafw.modelgen.modelgenSourceDirectory).exists()) {
            project.mkdir(project.asakusafw.modelgen.modelgenSourceDirectory)
        }
        if (!project.file(project.asakusafw.javac.annotationSourceDirectory).exists()) {
            project.mkdir(project.asakusafw.javac.annotationSourceDirectory)
        }
    }
}
