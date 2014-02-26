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

import org.gradle.api.InvalidUserDataException
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar

import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.CompilerConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.DmdlConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.JavacConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.ModelgenConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.TestToolsConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.ThunderGateConfiguration
import com.asakusafw.gradle.tasks.CompileBatchappTask
import com.asakusafw.gradle.tasks.CompileDmdlTask
import com.asakusafw.gradle.tasks.GenerateTestbookTask
import com.asakusafw.gradle.tasks.GenerateThunderGateDataModelTask

/**
 * Gradle plugin for building application component blocks.
 */
class AsakusafwPlugin implements Plugin<Project> {

    public static final String ASAKUSAFW_BUILD_GROUP = 'Asakusa Framework Build'

    private final FileResolver fileResolver

    private Project project

    private File frameworkHome

    @Inject
    public AsakusafwPlugin(FileResolver fileResolver) {
        this.fileResolver = fileResolver
    }

    void apply(Project project) {
        this.project = project
        this.frameworkHome = System.env['ASAKUSA_HOME'] == null ? null : new File(System.env['ASAKUSA_HOME'])

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
        AsakusafwPluginConvention convention = project.extensions.create('asakusafw', AsakusafwPluginConvention)
        convention.dmdl = convention.extensions.create('dmdl', DmdlConfiguration)
        convention.modelgen = convention.extensions.create('modelgen', ModelgenConfiguration)
        convention.javac = convention.extensions.create('javac', JavacConfiguration)
        convention.compiler = convention.extensions.create('compiler', CompilerConfiguration)
        convention.testtools = convention.extensions.create('testtools', TestToolsConfiguration)
        convention.thundergate = convention.extensions.create('thundergate', ThunderGateConfiguration)
        convention.conventionMapping.with {
            asakusafwVersion = { throw new InvalidUserDataException('"asakusafw.asakusafwVersion" must be set') }
            maxHeapSize = { '1024m' }
            logbackConf = { (String) "src/${project.sourceSets.test.name}/resources/logback-test.xml" }
            basePackage = {
                if (project.group == null || project.group == '') {
                    throw new InvalidUserDataException('"asakusafw.basePackage" must be specified')
                }
                return project.group
            }
        }
        convention.dmdl.conventionMapping.with {
            dmdlEncoding = { 'UTF-8' }
            dmdlSourceDirectory = { (String) "src/${project.sourceSets.main.name}/dmdl" }
        }
        convention.modelgen.conventionMapping.with {
            modelgenSourcePackage = { (String) "${project.asakusafw.basePackage}.modelgen" }
            modelgenSourceDirectory = { (String) "${project.buildDir}/generated-sources/modelgen" }
        }
        convention.javac.conventionMapping.with {
            annotationSourceDirectory = { (String) "${project.buildDir}/generated-sources/annotations" }
            sourceEncoding = { 'UTF-8' }
            sourceCompatibility = { JavaVersion.VERSION_1_6 }
            targetCompatibility = { JavaVersion.VERSION_1_6 }
        }
        convention.compiler.conventionMapping.with {
            compiledSourcePackage = { (String) "${project.asakusafw.basePackage}.batchapp" }
            compiledSourceDirectory = { (String) "${project.buildDir}/batchc" }
            compilerOptions = { '' }
            compilerWorkDirectory = { (String) "${project.buildDir}/batchcwork" }
            hadoopWorkDirectory = { 'target/hadoopwork/${execution_id}' }
        }
        convention.testtools.conventionMapping.with {
            testDataSheetFormat = { 'ALL' }
            testDataSheetDirectory = { (String) "${project.buildDir}/excel" }
        }
        convention.thundergate.conventionMapping.with {
            target = { null }
            ddlEncoding = { null }
            ddlSourceDirectory = { (String) "src/${project.sourceSets.main.name}/sql/modelgen" }
            includes = { null }
            excludes = { null }
            dmdlOutputDirectory = { (String) "${project.buildDir}/thundergate/dmdl" }
            ddlOutputDirectory = { (String) "${project.buildDir}/thundergate/sql" }
            sidColumn = { 'SID' }
            timestampColumn = { 'UPDT_DATETIME' }
            deleteColumn = { 'DELETE_FLAG' }
            deleteValue = { '"1"' }
        }
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
                embedded project.sourceSets.main.libs
                compile group: 'org.slf4j', name: 'jcl-over-slf4j', version: project.asakusafwInternal.dep.slf4jVersion
                compile group: 'ch.qos.logback', name: 'logback-classic', version: project.asakusafwInternal.dep.logbackVersion
            }
        }
    }

    private void configureSourceSets() {
        SourceSet container = project.sourceSets.main

        // Application Libraries
        SourceDirectorySet libs = createSourceDirectorySet(container, 'libs', 'Application libraries')
        libs.filter.include '*.jar'
        libs.srcDirs { project.asakusafwInternal.dep.embeddedLibsDirectory }

        // DMDL source set
        SourceDirectorySet dmdl = createSourceDirectorySet(container, 'dmdl', 'DMDL scripts')
        dmdl.filter.include '**/*.dmdl'
        dmdl.srcDirs { project.asakusafw.dmdl.dmdlSourceDirectory }
        container.java.srcDirs { project.asakusafw.modelgen.modelgenSourceDirectory }

        // Annotation processors
        SourceDirectorySet annotations = createSourceDirectorySet(container, 'annotations', 'Java annotation processor results')
        annotations.srcDirs { project.asakusafw.javac.annotationSourceDirectory }
        container.allJava.source annotations
        container.allSource.source annotations
        // Note: Don't add generated directory into container.output.dirs for eclipse plug-in

        // ThunderGate DDL source set
        SourceDirectorySet sql = createSourceDirectorySet(container, 'thundergateDdl', 'ThunderGate DDL scripts')
        sql.filter.include '**/*.sql'
        sql.srcDirs { project.asakusafw.thundergate.ddlSourceDirectory }
        // Note: the generated DMDL source files will be added later only if there are actually required
    }

    private SourceDirectorySet createSourceDirectorySet(SourceSet parent, String name, String displayName) {
        assert parent instanceof ExtensionAware
        ExtensionContainer extensions = parent.extensions
        // currently, project.sourceSets.main.* is not ExtensionAware
        SourceDirectorySet extension = new DefaultSourceDirectorySet(name, displayName, fileResolver)
        extensions.add(name, extension)
        return extension
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
            Set<File> annotations = project.sourceSets.main.annotations.getSrcDirs()
            if (annotations.size() >= 1) {
                if (annotations.size() >= 2) {
                    throw new InvalidUserDataException("sourceSets.main.annotations has only upto 1 directory: ${annotations}")
                }
                File directory = annotations.iterator().next()
                project.tasks.compileJava.options.compilerArgs += ['-s', directory.absolutePath, '-Xmaxerrs', '10000']
                project.tasks.compileJava.inputs.property 'annotationSourceDirectory', directory.absolutePath
            }
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
        defineGenerateThunderGateDataModelTask()
    }

    private void defineCompileDMDLTask() {
        project.task('compileDMDL', type: CompileDmdlTask) {
            group ASAKUSAFW_BUILD_GROUP
            description 'Compiles the DMDL scripts with DMDL Compiler.'
            sourcepath << project.sourceSets.main.dmdl
            toolClasspath << project.sourceSets.main.compileClasspath
            if (isFrameworkInstalled()) {
                pluginClasspath << project.fileTree(dir: getFrameworkFile('dmdl/plugin'), include: '**/*.jar')
            }
            conventionMapping.with {
                logbackConf = { this.findLogbackConf() }
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
            toolClasspath << project.sourceSets.main.compileClasspath
            toolClasspath << project.sourceSets.main.output
            if (isFrameworkInstalled()) {
                pluginClasspath << project.fileTree(dir: getFrameworkFile('compiler/plugin'), include: '**/*.jar')
            }
            conventionMapping.with {
                logbackConf = { this.findLogbackConf() }
                maxHeapSize = { project.asakusafw.maxHeapSize }
                frameworkVersion = { project.asakusafw.asakusafwVersion }
                packageName = { project.asakusafw.compiler.compiledSourcePackage }
                compilerOptions = { project.asakusafw.compiler.compilerOptions ?: '' }
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
            sourcepath << project.sourceSets.main.dmdl
            toolClasspath << project.sourceSets.main.compileClasspath
            if (isFrameworkInstalled()) {
                pluginClasspath << project.fileTree(dir: getFrameworkFile('dmdl/plugin'), include: '**/*.jar')
            }
            conventionMapping.with {
                logbackConf = { this.findLogbackConf() }
                maxHeapSize = { project.asakusafw.maxHeapSize }
                sourceEncoding = { project.asakusafw.dmdl.dmdlEncoding }
                outputSheetFormat = { project.asakusafw.testtools.testDataSheetFormat }
                outputDirectory = { project.file(project.asakusafw.testtools.testDataSheetDirectory) }
            }
        }
    }

    private void defineGenerateThunderGateDataModelTask() {
        def thundergate = project.asakusafw.thundergate
        def task = project.task('generateThunderGateDataModel', type: GenerateThunderGateDataModelTask) {
            group ASAKUSAFW_BUILD_GROUP
            description 'Executes DDLs and generates ThunderGate data models.'
            sourcepath << project.sourceSets.main.thundergateDdl
            toolClasspath << project.sourceSets.main.compileClasspath
            systemDdlFiles << { getFrameworkFile('bulkloader/sql/create_table.sql') }
            systemDdlFiles << { getFrameworkFile('bulkloader/sql/insert_import_table_lock.sql') }
            conventionMapping.with {
                logbackConf = { this.findLogbackConf() }
                maxHeapSize = { project.asakusafw.maxHeapSize }
                jdbcConfiguration = { getFrameworkFile("bulkloader/conf/${thundergate.target}-jdbc.properties") }
                ddlEncoding = { thundergate.ddlEncoding }
                includePattern = { thundergate.includes }
                excludePattern = { thundergate.excludes }
                dmdlOutputDirectory = { project.file(thundergate.dmdlOutputDirectory) }
                dmdlOutputEncoding = { project.asakusafw.dmdl.dmdlEncoding }
                recordLockDdlOutput = { new File(project.file(thundergate.ddlOutputDirectory), 'record-lock-ddl.sql') }
                sidColumnName = { thundergate.sidColumn }
                timestampColumnName = { thundergate.timestampColumn }
                deleteFlagColumnName = { thundergate.deleteColumn }
                deleteFlagColumnValue = { thundergate.deleteValue }
            }
            onlyIf { thundergate.target != null }
            doFirst { checkFrameworkInstalled() }
        }
        project.afterEvaluate {
            if (project.asakusafw.thundergate.target == null) {
                project.logger.info('Disables task: {}', task.name)
            } else {
                project.logger.info('Enables task: {} (using {})', task.name, task.name)
                project.tasks.compileDMDL.dependsOn task
                project.sourceSets.main.dmdl.srcDirs { thundergate.dmdlOutputDirectory }
            }
        }
    }

    protected File findLogbackConf() {
        if (project.asakusafw.logbackConf) {
            return project.file(project.asakusafw.logbackConf)
        } else {
            return null
        }
    }

    /**
     * Sets the Asakusa Framework home path (internal use only).
     * @param path the home path
     */
    protected void setFrameworkHome(File path) {
        this.frameworkHome = path
    }

    protected boolean isFrameworkInstalled() {
        return frameworkHome != null && frameworkHome.exists()
    }

    protected def checkFrameworkInstalled() {
        if (isFrameworkInstalled()) {
            return true
        }
        if (frameworkHome == null) {
            throw new IllegalStateException('Environment variable "ASAKUSA_HOME" is not defined')
        }
        throw new IllegalStateException("Environment variable 'ASAKUSA_HOME' is not valid: ${this.frameworkHome}")
    }

    protected File getFrameworkFile(String relativePath) {
        if (frameworkHome != null) {
            return new File(frameworkHome, relativePath)
        }
        return null
    }

    private void applySubPlugins() {
        new EclipsePluginEnhancement().apply(project)
    }

}

