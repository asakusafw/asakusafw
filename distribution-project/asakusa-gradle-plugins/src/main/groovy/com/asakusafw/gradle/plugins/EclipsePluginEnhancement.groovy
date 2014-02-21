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

import groovy.xml.MarkupBuilder
import org.gradle.api.*

/**
 * Enhancements Gradle Eclipse Plugin for Asakusa Framework.
 */
class EclipsePluginEnhancement {

    private Project project
    private AntBuilder ant

    void apply(Project project) {
        this.project = project
        this.ant = project.ant

        project.afterEvaluate {
            if (project.plugins.hasPlugin('eclipse')) {
                configureProject()
                configureEclipsePlugin()
            }
        }
    }

    private void configureProject() {
        configureConfigurations()
        configureDependencies()
    }

    private void configureConfigurations() {
        def compileOperator = project.configurations.create('compileOperator')

        compileOperator.description = 'Libraries for compiling Operator DSL on Java Annotation Processor'
    }

    private void configureDependencies() {
        project.dependencies {
            compileOperator "com.asakusafw:asakusa-runtime:${project.asakusafw.asakusafwVersion}"
            compileOperator "com.asakusafw:asakusa-dsl-vocabulary:${project.asakusafw.asakusafwVersion}"
            compileOperator "com.asakusafw:ashigel-compiler:${project.asakusafw.asakusafwVersion}"
            compileOperator "com.asakusafw:java-dom:${project.asakusafw.asakusafwVersion}"
            compileOperator "com.asakusafw:javadoc-parser:${project.asakusafw.asakusafwVersion}"
            compileOperator "com.asakusafw:jsr269-bridge:${project.asakusafw.asakusafwVersion}"
            compileOperator "com.asakusafw:collections:${project.asakusafw.asakusafwVersion}"
            compileOperator "com.asakusafw:simple-graph:${project.asakusafw.asakusafwVersion}"
            compileOperator "commons-io:commons-io:${project.asakusafwInternal.dep.commonsIoVersion}"
            compileOperator "commons-lang:commons-lang:${project.asakusafwInternal.dep.commonsLangVersion}"
            compileOperator "ch.qos.logback:logback-classic:${project.asakusafwInternal.dep.logbackVersion}"
            compileOperator "ch.qos.logback:logback-core:${project.asakusafwInternal.dep.logbackVersion}"
            compileOperator "org.slf4j:slf4j-api:${project.asakusafwInternal.dep.slf4jVersion}"
        }
    }

    private void configureEclipsePlugin() {
        extendEclipseProjectTask()
        extendEclipseClasspath()
        extendEclipseJdtConfiguration()
        extendEclipseJdtTask()
        extendCleanEclipseProjectTask()
        extendCleanEclipseJdtTask()
    }

    private void extendEclipseProjectTask() {
        project.tasks.eclipseProject.doLast {
            generateResourcePref()
        }
    }

    private void generateResourcePref() {
        project.mkdir('.settings')
        project.file('.settings/org.eclipse.core.resources.prefs').text = """\
            |eclipse.preferences.version=1
            |encoding/<project>=UTF-8
            |""" .stripMargin()
    }

    private void extendEclipseClasspath() {
        project.eclipse.classpath {
            file {
                whenMerged { classpath ->
                    classpath.entries.findAll { it.path.contains('org.eclipse.jdt.launching.JRE_CONTAINER') }.each {
                        it.path = "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-${project.asakusafw.javac.sourceCompatibility}"
                    }
                    classpath.entries.unique()
                }
                withXml { provider ->
                    def xml = provider.asNode()
                    def target = project.relativePath(project.asakusafw.javac.annotationSourceDirectory).replace('¥¥', '/')
                    if (xml.children().find { it.attributes().kind == 'src' && it.attributes().path == target } == null) {
                        xml.appendNode 'classpathentry', [kind: 'src', path: target]
                    }
                }
            }
            plusConfigurations += project.configurations.provided
            noExportConfigurations += project.configurations.provided
            plusConfigurations += project.configurations.embedded
            noExportConfigurations += project.configurations.embedded
        }
        project.eclipseClasspath.doFirst {
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

    private void extendEclipseJdtConfiguration() {
        project.eclipse.jdt.file.withProperties { props ->
            props.setProperty('org.eclipse.jdt.core.compiler.processAnnotations', 'enabled')
        }
    }

    private void extendEclipseJdtTask() {
        project.tasks.eclipseJdt.doLast {
            generateFactorypath()
            generateAptPref()
            generateAsakusafwProjectPref()
        }
    }

    private void generateFactorypath() {
        project.file('.factorypath').withWriter {
            new MarkupBuilder(it).'factorypath' {
                project.configurations.compileOperator.files.each { dep ->
                    'factorypathentry' kind: 'EXTJAR', id: dep.absolutePath, enabled: true, runInBatchMode: false
                }
            }
        }
    }

    private void generateAptPref() {
        project.file('.settings/org.eclipse.jdt.apt.core.prefs').text = """\
            |eclipse.preferences.version=1
            |org.eclipse.jdt.apt.aptEnabled=true
            |org.eclipse.jdt.apt.genSrcDir=${project.relativePath(project.asakusafw.javac.annotationSourceDirectory).replace('\\', '/')}
            |org.eclipse.jdt.apt.reconcileEnabled=true
            |""" .stripMargin()
    }

    private void generateAsakusafwProjectPref() {
        project.file('.settings/com.asakusafw.asakusafw.prefs').withWriter('UTF-8') { out ->
            out.writeLine('eclipse.preferences.version=1')
            project.asakusafw.conventionProperties.each { key, value ->
                if (key.endsWith('Directory')) {
                    value = project.relativePath(value).replace('\\', '/')
                }
                out.writeLine("${key}=${value}")
            }
        }
    }

    private void extendCleanEclipseProjectTask() {
        project.cleanEclipseProject.doLast {
            project.delete(project.file('.settings/org.eclipse.core.resources.prefs'))
        }
    }

    private void extendCleanEclipseJdtTask() {
        project.cleanEclipseJdt.doLast {
            project.delete(project.file('.factorypath'))
            project.delete(project.file('.settings/org.eclipse.jdt.apt.core.prefs'))
            project.delete(project.file('.settings/com.asakusafw.asakusafw.prefs'))
        }
    }
}

