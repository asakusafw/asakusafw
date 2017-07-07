/*
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.gradle.plugins.internal

import org.gradle.api.InvalidUserDataException
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.CompileOptions
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc

import com.asakusafw.gradle.plugins.AsakusaTestkit
import com.asakusafw.gradle.plugins.AsakusafwBaseExtension
import com.asakusafw.gradle.plugins.AsakusafwBasePlugin
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention
import com.asakusafw.gradle.plugins.AsakusafwSdkExtension
import com.asakusafw.gradle.plugins.EclipsePluginEnhancement
import com.asakusafw.gradle.plugins.IdeaPluginEnhancement
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.CompilerConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.CoreConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.DmdlConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.JavacConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.ModelgenConfiguration
import com.asakusafw.gradle.plugins.AsakusafwPluginConvention.TestToolsConfiguration
import com.asakusafw.gradle.tasks.CompileDmdlTask
import com.asakusafw.gradle.tasks.GenerateHiveDdlTask
import com.asakusafw.gradle.tasks.GenerateTestbookTask
import com.asakusafw.gradle.tasks.RunBatchappTask
import com.asakusafw.gradle.tasks.TestToolTask
import com.asakusafw.gradle.tasks.internal.AbstractTestToolTask
import com.asakusafw.gradle.tasks.internal.ResolutionUtils

/**
 * A Gradle plug-in for application development project using Asakusa SDK.
 * @since 0.8.0
 * @version 0.9.0
 */
class AsakusaSdkPlugin implements Plugin<Project> {

    /**
     * The plug-in ID.
     */
    public static final String ID = 'asakusa-sdk'

    /**
     * The build group name.
     */
    public static final String ASAKUSAFW_BUILD_GROUP = 'Asakusa Framework Build'

    private Project project

    private AsakusafwPluginConvention extension

    @Override
    void apply(Project project) {
        this.project = project
        this.extension = project.extensions.create('asakusafw', AsakusafwPluginConvention)

        project.apply plugin: JavaPlugin
        project.apply plugin: AsakusafwBasePlugin

        configureProject()
        configureJavaPlugin()
        defineAsakusaTasks()
        applySubPlugins()
    }

    private void configureProject() {
        configureExtentionProperties()
        configureConfigurations()
        configureDependencies()
        configureClasspath()
        configureSourceSets()
    }

    private void configureExtentionProperties() {
        AsakusafwBaseExtension base = AsakusafwBasePlugin.get(project)
        extension.core = extension.extensions.create('core', CoreConfiguration)
        extension.sdk = extension.extensions.create('sdk', AsakusafwSdkExtension)
        extension.dmdl = extension.extensions.create('dmdl', DmdlConfiguration)
        extension.modelgen = extension.extensions.create('modelgen', ModelgenConfiguration)
        extension.javac = extension.extensions.create('javac', JavacConfiguration)
        extension.compiler = extension.extensions.create('compiler', CompilerConfiguration)
        extension.testtools = extension.extensions.create('testtools', TestToolsConfiguration)
        extension.conventionMapping.with {
            asakusafwVersion = {
                if (base.frameworkVersion == null) {
                    throw new InvalidUserDataException('Asakusa Framework core libraries version is not defined')
                }
                return base.frameworkVersion
            }
            maxHeapSize = { '1024m' }
            logbackConf = { (String) "src/${project.sourceSets.test.name}/resources/logback-test.xml" }
            basePackage = {
                if (project.group == null || project.group == '') {
                    throw new InvalidUserDataException('"asakusafw.basePackage" must be specified')
                }
                return project.group
            }
        }
        extension.sdk.with {
            core true
            dmdl true
            operator true
            testing true
            testkit true
            directio true
            windgate true
            hive false
            incubating false
        }
        extension.dmdl.conventionMapping.with {
            dmdlEncoding = { 'UTF-8' }
            dmdlSourceDirectory = { (String) "src/${project.sourceSets.main.name}/dmdl" }
        }
        extension.modelgen.conventionMapping.with {
            modelgenSourcePackage = { (String) "${extension.basePackage}.modelgen" }
            modelgenSourceDirectory = { (String) "${project.buildDir}/generated-sources/modelgen" }
        }
        extension.javac.conventionMapping.with {
            annotationSourceDirectory = { (String) "${project.buildDir}/generated-sources/annotations" }
            sourceEncoding = { 'UTF-8' }
            sourceCompatibility = { JavaVersion.toVersion(base.javaVersion) }
            targetCompatibility = { JavaVersion.toVersion(base.javaVersion) }
        }
        extension.compiler.conventionMapping.with {
            enabled = { true }
            compiledSourcePackage = { (String) "${extension.basePackage}.batchapp" }
            compiledSourceDirectory = { (String) "${project.buildDir}/batchc" }
            compilerOptions = {[
                String.format("XjavaVersion=%s", JavaVersion.toVersion(extension.javac.targetCompatibility))
            ]}
            compilerWorkDirectory = { null }
            hadoopWorkDirectory = { 'target/hadoopwork/${execution_id}' }
        }
        extension.testtools.conventionMapping.with {
            testDataSheetFormat = { 'ALL' }
            testDataSheetDirectory = { (String) "${project.buildDir}/excel" }
        }
        PluginUtils.deprecateAsakusafwVersion project, 'asakusafw', extension
        PluginUtils.injectVersionProperty(extension.core, { base.frameworkVersion })
        extension.metaClass.toStringDelegate = { -> "asakusafw { ... }" }
    }

    private void configureConfigurations() {
        def provided = project.configurations.create('provided')
        provided.description = '''Emulating Maven's provided scope.'''

        def embedded = project.configurations.create('embedded')
        embedded.description = 'Project embedded libraries.'

        def asakusaDmdlCompiler = project.configurations.create('asakusaDmdlCompiler')
        asakusaDmdlCompiler.description = 'Asakusa DMDL compiler.'
        asakusaDmdlCompiler.extendsFrom project.configurations.compile

        def asakusaHiveCli = project.configurations.create('asakusaHiveCli')
        asakusaHiveCli.description = 'Asakusa Hive CLI libraries.'
        asakusaHiveCli.extendsFrom project.configurations.compile
    }

    private void configureDependencies() {
        PluginUtils.afterEvaluate(project) {
            AsakusafwBaseExtension base = AsakusafwBasePlugin.get(project)
            project.dependencies {
                embedded project.sourceSets.main.libs

                compile group: 'org.slf4j', name: 'jcl-over-slf4j', version: base.slf4jVersion
                compile group: 'ch.qos.logback', name: 'logback-classic', version: base.logbackVersion
                asakusaHiveCli group: 'com.asakusafw', name: 'asakusa-hive-cli', version: base.frameworkVersion
            }
        }
    }

    private void configureClasspath() {
        PluginUtils.afterEvaluate(project) {
            AsakusafwBaseExtension base = AsakusafwBasePlugin.get(project)
            AsakusafwSdkExtension features = extension.sdk
            project.dependencies {
                if (features.core) {
                    compile "com.asakusafw.sdk:asakusa-sdk-app-core:${base.frameworkVersion}"
                    if (features.operator) {
                        if (features.incubating) {
                            compile "com.asakusafw.operator:asakusa-operator-all:${base.frameworkVersion}"
                        } else {
                            compile "com.asakusafw.mapreduce.compiler:asakusa-mapreduce-compiler-operator:${base.frameworkVersion}"
                        }
                    }
                    if (features.directio) {
                        compile "com.asakusafw.sdk:asakusa-sdk-app-directio:${base.frameworkVersion}"
                    }
                    if (features.windgate) {
                        compile "com.asakusafw.sdk:asakusa-sdk-app-windgate:${base.frameworkVersion}"
                    }
                    if (features.hive) {
                        compile "com.asakusafw.sdk:asakusa-sdk-app-hive:${base.frameworkVersion}"
                    }
                }
                if (features.testing) {
                    // For eclipse classpath
                    project.configurations.compile.exclude group: 'asm', module: 'asm'
                    project.configurations.testCompile.exclude group: 'asm', module: 'asm'
                    testCompile "com.asakusafw.sdk:asakusa-sdk-test-core:${base.frameworkVersion}"
                    if (features.directio) {
                        testCompile "com.asakusafw.sdk:asakusa-sdk-test-directio:${base.frameworkVersion}"
                    }
                    if (features.windgate) {
                        testCompile "com.asakusafw.sdk:asakusa-sdk-test-windgate:${base.frameworkVersion}"
                    }
                    if (features.testkit) {
                        AsakusaTestkit found = findTestkit(features.testkit, features.availableTestkits)?.apply(project)
                    }
                }
                if (features.dmdl) {
                    asakusaDmdlCompiler "com.asakusafw.sdk:asakusa-sdk-dmdl-core:${base.frameworkVersion}"
                    if (features.directio) {
                        asakusaDmdlCompiler "com.asakusafw.sdk:asakusa-sdk-dmdl-directio:${base.frameworkVersion}"
                    }
                    if (features.windgate) {
                        asakusaDmdlCompiler "com.asakusafw.sdk:asakusa-sdk-dmdl-windgate:${base.frameworkVersion}"
                    }
                    if (features.hive) {
                        asakusaDmdlCompiler "com.asakusafw.sdk:asakusa-sdk-dmdl-hive:${base.frameworkVersion}"
                    }
                }
            }
        }
    }

    private AsakusaTestkit findTestkit(Object value, Set<AsakusaTestkit> availables) {
        if (value instanceof AsakusaTestkit) {
            return (AsakusaTestkit) value
        } else if (value instanceof String || value instanceof GString) {
            AsakusaTestkit kit = availables.find { it.name == value }
            if (kit) {
                return kit
            } else {
                throw new InvalidUserDataException("testkit \"${value}\" is not found")
            }
        } else if (value) {
            AsakusaTestkit found = null
            for (AsakusaTestkit k : availables) {
                if (k.priority >= 0 && (found == null || found.priority < k.priority)) {
                    found = k
                }
            }
            return found
        }
    }

    private void configureSourceSets() {
        AsakusafwBaseExtension base = AsakusafwBasePlugin.get(project)
        SourceSet container = project.sourceSets.main

        // Application Libraries
        SourceDirectorySet libs = AsakusaSdk.createSourceDirectorySet(project, container, 'libs',
            'Application libraries')
        libs.filter.include '*.jar'
        libs.srcDirs { base.embeddedLibsDirectory }

        // DMDL source set
        SourceDirectorySet dmdl = AsakusaSdk.createSourceDirectorySet(project, container, 'dmdl',
            'DMDL scripts')
        dmdl.filter.include '**/*.dmdl'
        dmdl.srcDirs { extension.dmdl.dmdlSourceDirectory }

        // Annotation processors
        SourceDirectorySet annotations = AsakusaSdk.createSourceDirectorySet(project, container,'annotations',
            'Java annotation processor results')
        annotations.srcDirs { extension.javac.annotationSourceDirectory }

        PluginUtils.afterEvaluate(project) {
            if (extension.sdk.dmdl) {
                container.java.srcDirs { extension.modelgen.modelgenSourceDirectory }
            }
            if (extension.sdk.operator) {
                container.allJava.source annotations
                container.allSource.source annotations
                // Note: Don't add generated directory into container.output.dirs for eclipse plug-in
            }
        }
    }

    private void configureJavaPlugin() {
        // NOTE: Must configure project.sourceSets first for organizing *.compileClasspath/runtimeClasspath
        configureJavaSourceSets()
        configureJavaProjectProperties()
        configureJavaTasks()
    }

    private void configureJavaSourceSets() {
        // FIXME This REDEFINES compileClasspath/runtimeClasspath, please change it to modify FileCollections
        project.sourceSets {
            main.compileClasspath += project.configurations.provided
            test.compileClasspath += project.configurations.provided
            test.runtimeClasspath += project.configurations.provided
            main.compileClasspath += project.configurations.embedded
            test.compileClasspath += project.configurations.embedded
            test.runtimeClasspath += project.configurations.embedded
        }
    }

    private void configureJavaProjectProperties() {
        PluginUtils.afterEvaluate(project) {
            project.sourceCompatibility = extension.javac.sourceCompatibility
            project.targetCompatibility = extension.javac.targetCompatibility
        }
    }

    private void configureJavaTasks() {
        configureJavaCompileTask()
        configureJavadocTask()
    }

    private void configureJavaCompileTask() {
        PluginUtils.afterEvaluate(project) {
            [project.tasks.compileJava, project.tasks.compileTestJava].each { JavaCompile task ->
                task.options.encoding = extension.javac.sourceEncoding
            }
            Set<File> annotations = project.sourceSets.main.annotations.getSrcDirs()
            if (extension.sdk.operator && annotations.size() >= 1) {
                if (annotations.size() >= 2) {
                    throw new InvalidUserDataException("sourceSets.main.annotations has only upto 1 directory: ${annotations}")
                }
                File directory = annotations.iterator().next()
                project.tasks.compileJava.inputs.property 'annotationSourceDirectory', directory.absolutePath

                CompileOptions opts = project.tasks.compileJava.options
                opts.compilerArgs.addAll(['-s', directory.absolutePath])
                opts.compilerArgs.addAll(['-Xmaxerrs', '10000'])
                ResolutionUtils.resolveToStringMap(extension.javac.processorOptions).each { String k, String v ->
                    opts.compilerArgs.add("-A${k}=${v}")
                }
            }
        }
    }

    private void configureJavadocTask() {
        project.tasks.javadoc { Javadoc task ->
            // XXX Must reassign compileClasspath because sourceSets.main.compileClasspath is REDEFINED.
            task.classpath = project.sourceSets.main.compileClasspath
            if (task.options.hasProperty('docEncoding')) {
                task.options.docEncoding = 'UTF-8'
            }
            if (task.options.hasProperty('charSet')) {
                task.options.charSet = 'UTF-8'
            }
            task.dependsOn project.tasks.compileJava
        }
        PluginUtils.afterEvaluate(project) {
            project.tasks.javadoc { Javadoc task ->
                task.options.encoding = extension.javac.sourceEncoding
                task.options.source = String.valueOf(extension.javac.sourceCompatibility)
            }
        }
    }

    private void defineAsakusaTasks() {
        extendVersionsTask()
        defineCompileDMDLTask()
        defineCompileBatchappTask()
        defineJarBatchappTask()
        extendCompileJavaTask()
        defineGenerateTestbookTask()
        defineGenerateHiveDdlTask()
        configureTestToolTasks()
    }

    private void extendVersionsTask() {
        project.tasks.getByName(AsakusafwBasePlugin.TASK_VERSIONS).doLast {
            AsakusafwBaseExtension base = AsakusafwBasePlugin.get(project)
            logger.lifecycle "Asakusa SDK: ${base.frameworkVersion ?: 'INVALID'}"
            logger.lifecycle "JVM: ${extension.javac.targetCompatibility}"
        }
    }

    private void defineCompileDMDLTask() {
        project.task('compileDMDL', type: CompileDmdlTask) {
            group ASAKUSAFW_BUILD_GROUP
            description 'Compiles Asakusa DMDL scripts.'
            launcherClasspath << project.configurations.asakusaToolLauncher
            sourcepath << { project.sourceSets.main.dmdl }
            toolClasspath << project.configurations.asakusaDmdlCompiler
            conventionMapping.with {
                logbackConf = { this.findLogbackConf() }
                maxHeapSize = { extension.maxHeapSize }
                packageName = { extension.modelgen.modelgenSourcePackage }
                sourceEncoding = { extension.dmdl.dmdlEncoding }
                targetEncoding = { extension.javac.sourceEncoding }
                outputDirectory = { project.file(extension.modelgen.modelgenSourceDirectory) }
            }
        }
    }

    private void defineCompileBatchappTask() {
        project.tasks.create('compileBatchapp') { Task task ->
            task.group ASAKUSAFW_BUILD_GROUP
            task.description 'Compiles Asakusa DSL source files.'

            // sub plug-ins must add '*CompileBatchapps' as a dependency of this task
        }
    }

    private void defineJarBatchappTask() {
        project.tasks.create('jarBatchapp', Jar) { Jar task ->
            task.group ASAKUSAFW_BUILD_GROUP
            task.description 'Assembles Asakusa batch applications into JAR file.'
            task.dependsOn 'compileBatchapp'

            task.destinationDir project.buildDir
            task.appendix 'batchapps'

            // sub plug-ins must add outputs of '*CompileBatchapps' as input of this task
        }
    }

    private extendCompileJavaTask() {
        project.tasks.compileJava.doFirst {
            project.delete(extension.javac.annotationSourceDirectory)
            project.mkdir(extension.javac.annotationSourceDirectory)
        }
        project.tasks.compileJava.dependsOn(project.tasks.compileDMDL)
    }

    private defineGenerateTestbookTask() {
        project.task('generateTestbook', type: GenerateTestbookTask) {
            group ASAKUSAFW_BUILD_GROUP
            description 'Generates Asakusa test template Excel books.'
            launcherClasspath << project.configurations.asakusaToolLauncher
            sourcepath << project.sourceSets.main.dmdl
            toolClasspath << project.configurations.asakusaDmdlCompiler
            conventionMapping.with {
                logbackConf = { this.findLogbackConf() }
                maxHeapSize = { extension.maxHeapSize }
                sourceEncoding = { extension.dmdl.dmdlEncoding }
                outputSheetFormat = { extension.testtools.testDataSheetFormat }
                outputDirectory = { project.file(extension.testtools.testDataSheetDirectory) }
            }
        }
    }

    private void defineGenerateHiveDdlTask() {
        project.tasks.create('generateHiveDDL', GenerateHiveDdlTask) { GenerateHiveDdlTask task ->
            task.group ASAKUSAFW_BUILD_GROUP
            task.description 'Generates a Hive DDL file [Experimental].'
            task.toolClasspath += project.configurations.asakusaHiveCli
            task.toolClasspath += project.sourceSets.main.compileClasspath
            task.sourcepath = PluginUtils.getClassesDirs(project, project.sourceSets.main.output)
            task.conventionMapping.with {
                logbackConf = { this.findLogbackConf() }
                maxHeapSize = { extension.maxHeapSize }
                outputFile = { project.file("${project.buildDir}/hive-ddl/${project.name}.sql") }
            }
            task.dependsOn project.tasks.compileJava
        }
    }

    private void configureTestToolTasks() {
        project.tasks.withType(RunBatchappTask) { AbstractTestToolTask task ->
            task.toolClasspath = project.files({ project.sourceSets.test.runtimeClasspath })
        }
        project.tasks.withType(TestToolTask) { AbstractTestToolTask task ->
            task.toolClasspath = project.files({ project.sourceSets.test.runtimeClasspath })
        }
    }

    private void applySubPlugins() {
        new EclipsePluginEnhancement().apply(project)
        new IdeaPluginEnhancement().apply(project)
    }

    // utilities

    private static AsakusaSdkPlugin getInstance(Project project) {
        AsakusaSdkPlugin result = project.plugins.getPlugin(AsakusaSdkPlugin)
        if (result == null) {
            throw new IllegalStateException('AsakusaSdkPlugin has not been applied')
        }
        return result
    }

    /**
     * Applies this plug-in and returns the extension object for the project.
     * @param project the target project
     * @return the corresponded extension
     */
    static AsakusafwPluginConvention get(Project project) {
        project.apply plugin: AsakusaSdkPlugin
        return getInstance(project).extension
    }

    private File findLogbackConf() {
        if (extension.logbackConf) {
            return project.file(extension.logbackConf)
        } else {
            return null
        }
    }
}
