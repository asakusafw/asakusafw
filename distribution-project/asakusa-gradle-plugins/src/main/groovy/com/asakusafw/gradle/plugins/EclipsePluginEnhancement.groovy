package com.asakusafw.gradle.plugins

import groovy.xml.MarkupBuilder
import org.gradle.api.*

class EclipsePluginEnhancement {

    private Project project
    private AntBuilder ant

    void apply(Project project) {
        this.project = project
        this.ant = project.ant

        project.gradle.taskGraph.whenReady { taskGraph ->
            if (!project.plugins.hasPlugin('eclipse'))
             return

            configureProject()
            configureEclipsePlugin()
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
        extendEclipseClasspath()
        extendEclipseJdtConfiguration()
        extendEclipseJdtTask()
        extendCleanEclipseJdtTask()
    }

    private void extendEclipseClasspath() {
        project.eclipse.classpath {
            file.whenMerged { classpath ->
                classpath.entries.findAll { it.path.contains('org.eclipse.jdt.launching.JRE_CONTAINER') }.each {
                    it.path = 'org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6'
                }
                classpath.entries.unique()
            }
            plusConfigurations += project.configurations.provided
            noExportConfigurations += project.configurations.provided
            plusConfigurations += project.configurations.embedded
            noExportConfigurations += project.configurations.embedded
        }
    }

    private void extendEclipseJdtConfiguration() {
        project.eclipse.jdt.file.withProperties { props ->
            props.setProperty('encoding//src/main/java', project.compileJava.options.encoding)
            props.setProperty('encoding//src/main/resources', project.compileJava.options.encoding)
            props.setProperty('encoding//src/test/java', project.compileTestJava.options.encoding)
            props.setProperty('encoding//src/test/resources', project.compileTestJava.options.encoding)
            props.setProperty('org.eclipse.jdt.core.compiler.processAnnotations', 'enabled')
        }
    }

    private void extendEclipseJdtTask() {
        project.eclipseJdt.doLast {
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
            project.asakusafw.conventionProperties.each { key, value ->
                if (key.endsWith('Directory')) {
                    value = project.relativePath(value).replace('\\', '/')
                }
                out.writeLine("${key}=${value}")
            }
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

