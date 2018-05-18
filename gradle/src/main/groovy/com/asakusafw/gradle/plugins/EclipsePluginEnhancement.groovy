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
package com.asakusafw.gradle.plugins

import groovy.xml.MarkupBuilder

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.plugins.ide.eclipse.EclipsePlugin

import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.JavacConfiguration
import com.asakusafw.gradle.plugins.internal.PluginUtils
import com.asakusafw.gradle.tasks.internal.ResolutionUtils

/**
 * Gradle Eclipse plugin enhancements for Asakusa Framework.
 */
class EclipsePluginEnhancement {

    private static final String PREFIX_SETTINGS = '.settings/com.asakusafw.'

    private static final String SETTINGS_PROJECT = PREFIX_SETTINGS + 'asakusafw.prefs'

    private static final String SETTINGS_DMDL = PREFIX_SETTINGS + 'dmdl.prefs'

    private static final String PREFIX_CLASSPATH = 'classpath.'

    private Project project

    void apply(Project project) {
        this.project = project
        PluginUtils.afterPluginEnabled(project, EclipsePlugin) {
            configureProject()
            configureEclipsePlugin()
        }
    }

    private void configureProject() {
        configureConfigurations()
        configureDependencies()
    }

    private void configureConfigurations() {
        project.configurations {
            eclipseAnnotationProcessor {
                description 'Libraries for compiling Operator DSL on Java Annotation Processor'
            }
        }
    }

    private void configureDependencies() {
        PluginUtils.afterEvaluate(project) {
            AsakusafwBaseExtension base = AsakusafwBasePlugin.get(project)
            AsakusafwPluginConvention sdk =  project.asakusafw
            if (sdk.sdk.operator) {
                project.dependencies {
                    eclipseAnnotationProcessor "com.asakusafw.operator:asakusa-operator-all:${base.frameworkVersion}:lib@jar"
                }
            }
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
        preferences('.settings/org.eclipse.core.resources.prefs') { Properties props ->
            props.setProperty('encoding/<project>', 'UTF-8')
        }
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
            }
            plusConfigurations += [project.configurations.provided, project.configurations.embedded]
            if (PluginUtils.compareGradleVersion('2.5-rc-1') < 0) {
                noExportConfigurations += [project.configurations.provided, project.configurations.embedded]
            }
        }
        PluginUtils.afterEvaluate(project) {
            AsakusafwPluginConvention sdk =  project.asakusafw
            if (sdk.sdk.dmdl) {
                project.tasks.eclipseClasspath {
                    shouldRunAfter(project.tasks.compileDMDL)
                    doFirst {
                        if (!project.file(sdk.modelgen.modelgenSourceDirectory).exists()) {
                            project.mkdir(sdk.modelgen.modelgenSourceDirectory)
                        }
                    }
                }
            }
            if (sdk.sdk.operator) {
                project.tasks.eclipseClasspath {
                    doFirst {
                        if (!project.file(sdk.javac.annotationSourceDirectory).exists()) {
                            project.mkdir(sdk.javac.annotationSourceDirectory)
                        }
                    }
                }
            }
        }
    }

    private void extendEclipseJdtConfiguration() {
        PluginUtils.afterEvaluate(project) {
            AsakusafwPluginConvention sdk =  project.asakusafw
            if (sdk.sdk.operator) {
                project.eclipse.jdt.file.withProperties { Properties props ->
                    props.setProperty('org.eclipse.jdt.core.compiler.processAnnotations', 'enabled')
                }
            }
        }
    }

    private void extendEclipseJdtTask() {
        project.tasks.eclipseJdt { Task t ->
            t.outputs.files SETTINGS_PROJECT
            t.outputs.files SETTINGS_DMDL
            t.doLast {
                generateAsakusafwProjectPref()
                generateAsakusafwDmdlPref()
            }
        }
        PluginUtils.afterEvaluate(project) {
            AsakusafwPluginConvention sdk =  project.asakusafw
            if (sdk.sdk.operator) {
                project.tasks.eclipseJdt.doLast {
                    generateFactorypath()
                    generateAptPref()
                }
            }
        }
    }

    private void generateFactorypath() {
        project.file('.factorypath').withWriter {
            new MarkupBuilder(it).'factorypath' {
                project.configurations.eclipseAnnotationProcessor.files.each { File dep ->
                    'factorypathentry' kind: 'EXTJAR', id: dep.absolutePath, enabled: true, runInBatchMode: false
                }
            }
        }
    }

    private void generateAptPref() {
        JavacConfiguration javac = project.asakusafw.javac
        preferences('.settings/org.eclipse.jdt.apt.core.prefs') { Properties props ->
            props.setProperty('org.eclipse.jdt.apt.aptEnabled', 'true')
            props.setProperty('org.eclipse.jdt.apt.genSrcDir', relativePath(javac.annotationSourceDirectory))
            props.setProperty('org.eclipse.jdt.apt.reconcileEnabled', 'true')
            ResolutionUtils.resolveToStringMap(javac.processorOptions).each { String k, String v ->
                props.setProperty("org.eclipse.jdt.apt.processorOptions/${k}", v)
            }
        }
    }

    private void generateAsakusafwProjectPref() {
        preferences(SETTINGS_PROJECT) { Properties props ->
            project.asakusafw.conventionProperties.each { key, value ->
                if (key.endsWith('File') || key.endsWith('Directory') || key.endsWith('Dir')) {
                    value = (value == null || value.isEmpty()) ? '' : relativePath(value)
                }
                props.setProperty(key, value)
            }
        }
    }

    private void generateAsakusafwDmdlPref() {
        preferences(SETTINGS_DMDL) { Properties props ->
            FileCollection classpath = project.configurations.asakusaDmdlCompiler - project.configurations.compile
            generateClasspathList(props, classpath)
        }
    }

    private void generateClasspathList(Properties target, Iterable<File> files) {
        for (Iterator<Map.Entry<?, ?>> iter = target.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<?, ?> entry = iter.next()
            def key = entry.getKey()
            if (key instanceof String && key.startsWith(PREFIX_CLASSPATH)) {
                iter.remove()
            }
        }
        int index = 0
        for (File file : files) {
            String key = PREFIX_CLASSPATH + index++
            target.setProperty(key, file.absolutePath)
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

    private String relativePath(Object path) {
        return project.relativePath(path).replace('\\', '/')
    }

    private void preferences(Object path, Closure<?> closure) {
        File file = project.file(path)
        Properties properties = new Properties()
        if (file.exists()) {
            file.withInputStream { stream ->
                properties.load(stream)
            }
        } else {
            file.parentFile.mkdirs()
        }
        closure.call(properties)
        properties.setProperty('eclipse.preferences.version', '1')
        file.withOutputStream { stream ->
            properties.store(stream, null)
        }
    }
}

