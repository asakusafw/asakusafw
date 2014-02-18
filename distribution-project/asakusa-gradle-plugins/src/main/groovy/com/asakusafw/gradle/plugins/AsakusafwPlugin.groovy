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

import javax.inject.Inject

import org.gradle.api.*
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.plugins.*
import org.gradle.api.tasks.bundling.*

import com.asakusafw.gradle.plugins.internal.AsakusaDmdlSourceDelegateImpl
import com.asakusafw.gradle.tasks.*

/**
 * Gradle plugin for building application component blocks.
 */
class AsakusafwPlugin implements Plugin<Project> {

    public static final String ASAKUSAFW_BUILD_GROUP = 'Asakusa Framework Build'

    private final FileResolver fileResolver

    private Project project

    private AntBuilder ant

    @Inject
    public AsakusafwPlugin(FileResolver fileResolver) {
        this.fileResolver = fileResolver
    }

    void apply(Project project) {
        this.project = project
        this.ant = project.ant

        project.plugins.apply(JavaPlugin.class)
        project.plugins.apply(AsakusafwBasePlugin.class)

        configureProject()
        configureJavaPlugin()
        defineAsakusaTasks()
        applySubPlugins()
    }

    private void configureProject() {
        configureExtentionProperties()
        configureConfigurations()
        configureDependencies()
        configureSourceSets()
    }

    private void configureExtentionProperties() {
        project.extensions.create('asakusafw', AsakusafwPluginConvention, project)
    }

    private void configureConfigurations() {
        def provided = project.configurations.create('provided')
        provided.description = 'Emulating Maven’s provided scope'

        def embedded = project.configurations.create('embedded')
        embedded.description = 'Project embedded libraries'
    }

    private void configureDependencies() {
        project.afterEvaluate {
            project.dependencies {
                embedded project.fileTree(dir: project.asakusafwInternal.dep.embeddedLibsDirectory, include: '*.jar')
                compile group: 'org.slf4j', name: 'jcl-over-slf4j', version: project.asakusafwInternal.dep.slf4jVersion
                compile group: 'ch.qos.logback', name: 'logback-classic', version: project.asakusafwInternal.dep.logbackVersion
            }
        }
    }

    private void configureSourceSets() {
        def container = project.sourceSets.main
        def extension = new AsakusaDmdlSourceDelegateImpl(fileResolver)
        container.convention.plugins.dmdl = extension

        def dmdl = extension.dmdl
        dmdl.srcDirs { project.asakusafw.dmdl.dmdlSourceDirectory }
        container.java.srcDirs { project.asakusafw.modelgen.modelgenSourceDirectory }
    }

    private void configureJavaPlugin() {
        configureJavaProjectProperties()
        configureJavaTaskProperties()
        configureJavaSourceSets()
    }

    private void configureJavaProjectProperties() {
        project.afterEvaluate {
            project.sourceCompatibility = project.asakusafw.javac.sourceCompatibility
            project.targetCompatibility = project.asakusafw.javac.targetCompatibility
        }
    }

    private void configureJavaTaskProperties() {
        project.afterEvaluate {
            [project.tasks.compileJava, project.tasks.compileTestJava].each {
                it.options.encoding = project.asakusafw.javac.sourceEncoding
            }
            project.tasks.compileJava.options.compilerArgs += ['-s', project.asakusafw.javac.annotationSourceDirectory, '-Xmaxerrs', '10000']
            project.tasks.compileJava.inputs.property 'annotationSourceDirectory', project.asakusafw.javac.annotationSourceDirectory
        }
    }

    private void configureJavaSourceSets() {
        project.sourceSets {
            main.compileClasspath += project.configurations.provided
            test.compileClasspath += project.configurations.provided
            test.runtimeClasspath += project.configurations.provided
            main.compileClasspath += project.configurations.embedded
            test.compileClasspath += project.configurations.embedded
            test.runtimeClasspath += project.configurations.embedded
        }
    }

    private void defineAsakusaTasks() {
        defineCompileDMDLTask()
        extendCompileJavaTask()
        defineCompileBatchappTask()
        defineJarBatchappTask()
        extendAssembleTask()
        defineGenerateTestbookTask()
    }

    private void defineCompileDMDLTask() {
        project.task('compileDMDL', type: CompileDmdlTask) {
            group ASAKUSAFW_BUILD_GROUP
            description 'Compiles the DMDL scripts with DMDL Compiler.'
            sourcepath << { project.sourceSets.main.dmdl }
            toolClasspath << { project.sourceSets.main.compileClasspath }
            if (System.env['ASAKUSA_HOME'] != null) {
                pluginClasspath << { project.fileTree(dir: "${System.env.ASAKUSA_HOME}/dmdl/plugin", include: '**/*.jar') }
            }
            conventionMapping.with {
                logbackConf = {
                    if (project.asakusafw.logbackConf) {
                        return project.file(project.asakusafw.logbackConf)
                    } else {
                        return null
                    }
                }
                maxHeapSize = { project.asakusafw.maxHeapSize }
                packageName = { project.asakusafw.modelgen.modelgenSourcePackage }
                sourceEncoding = { project.asakusafw.dmdl.dmdlEncoding }
                targetEncoding = { project.asakusafw.javac.sourceEncoding }
                outputDirectory = { project.file(project.asakusafw.modelgen.modelgenSourceDirectory) }
            }
        }
    }

    private extendCompileJavaTask() {
        project.tasks.compileJava.doFirst {
            project.delete(project.asakusafw.javac.annotationSourceDirectory)
            project.mkdir(project.asakusafw.javac.annotationSourceDirectory)
        }
        project.tasks.compileJava.dependsOn(project.tasks.compileDMDL)
    }

    private defineCompileBatchappTask() {
        project.task('compileBatchapp', type: CompileBatchappTask, dependsOn: ['compileJava', 'processResources']) {
            group ASAKUSAFW_BUILD_GROUP
            description 'Compiles the Asakusa DSL java source with Asakusa DSL Compiler.'
            sourcepath << { project.sourceSets.main.output.classesDir }
            toolClasspath << { project.sourceSets.main.compileClasspath }
            if (System.env['ASAKUSA_HOME'] != null) {
                pluginClasspath << { project.fileTree(dir: "${System.env.ASAKUSA_HOME}/compiler/plugin", include: '**/*.jar') }
            }
            conventionMapping.with {
                logbackConf = {
                    if (project.asakusafw.logbackConf) {
                        return project.file(project.asakusafw.logbackConf)
                    } else {
                        return null
                    }
                }
                maxHeapSize = { project.asakusafw.maxHeapSize }
                frameworkVersion = { project.asakusafw.asakusafwVersion }
                packageName = { project.asakusafw.compiler.compiledSourcePackage }
                compilerOptions = { project.asakusafw.compiler.compilerOptions }
                workingDirectory = { project.file(project.asakusafw.compiler.compilerWorkDirectory) }
                hadoopWorkingDirectory = { project.asakusafw.compiler.hadoopWorkDirectory }
                outputDirectory = { project.file(project.asakusafw.compiler.compiledSourceDirectory) }
            }
        }
    }

    private defineJarBatchappTask() {
        project.task('jarBatchapp', type: Jar, dependsOn: 'compileBatchapp') {
            group ASAKUSAFW_BUILD_GROUP
            description 'Assembles a jar archive containing compiled batch applications.'
            from { project.asakusafw.compiler.compiledSourceDirectory }
            destinationDir project.buildDir
            appendix 'batchapps'
        }
    }

    private extendAssembleTask() {
        project.tasks.assemble.dependsOn(project.jarBatchapp)
    }

    private defineGenerateTestbookTask() {
        project.task('generateTestbook', type: GenerateTestbookTask) {
            group ASAKUSAFW_BUILD_GROUP
            description 'Generates the template Excel books for TestDriver.'
            sourcepath << { project.sourceSets.main.dmdl }
            toolClasspath << { project.sourceSets.main.compileClasspath }
            if (System.env['ASAKUSA_HOME'] != null) {
                pluginClasspath << { project.fileTree(dir: "${System.env.ASAKUSA_HOME}/dmdl/plugin", include: '**/*.jar') }
            }
            conventionMapping.with {
                logbackConf = {
                    if (project.asakusafw.logbackConf) {
                        return project.file(project.asakusafw.logbackConf)
                    } else {
                        return null
                    }
                }
                maxHeapSize = { project.asakusafw.maxHeapSize }
                sourceEncoding = { project.asakusafw.dmdl.dmdlEncoding }
                outputSheetFormat = { project.asakusafw.testtools.testDataSheetFormat }
                outputDirectory = { project.file(project.asakusafw.testtools.testDataSheetDirectory) }
            }
        }
    }

    private void applySubPlugins() {
        new EclipsePluginEnhancement().apply(project)
    }

}

