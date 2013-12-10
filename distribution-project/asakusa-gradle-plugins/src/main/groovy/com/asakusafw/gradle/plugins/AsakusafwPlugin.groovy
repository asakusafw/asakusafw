package com.asakusafw.gradle.plugins

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.bundling.*

import com.asakusafw.gradle.tasks.*

class AsakusafwPlugin implements Plugin<Project> {

    public static final String ASAKUSAFW_BUILD_GROUP = 'Asakusa Framework Build'
    public static final String ASAKUSAFW_BUILD_TOOL_GROUP = 'Asakusa Framework Build Tool'

    private Project project
    private AntBuilder ant

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
            [project.compileJava, project.compileTestJava].each {
                it.options.encoding = project.asakusafw.javac.sourceEncoding
            }
            project.compileJava.options.compilerArgs = ['-s', project.asakusafw.javac.annotationSourceDirectory, '-Xmaxerrs', '10000', '-XprintRounds']

        }
    }

    private void configureJavaSourceSets() {
        project.afterEvaluate {
            project.sourceSets {
                main.java.srcDirs += [project.asakusafw.javac.annotationSourceDirectory, project.asakusafw.modelgen.modelgenSourceDirectory]

                main.compileClasspath += project.configurations.provided
                test.compileClasspath += project.configurations.provided
                test.runtimeClasspath += project.configurations.provided
                main.compileClasspath += project.configurations.embedded
                test.compileClasspath += project.configurations.embedded
                test.runtimeClasspath += project.configurations.embedded
            }
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
            source project.asakusafw.dmdl.dmdlSourceDirectory
            outputs.dir project.asakusafw.modelgen.modelgenSourceDirectory
        }
    }

    private extendCompileJavaTask() {
        project.compileJava.doFirst {
            project.delete(project.asakusafw.javac.annotationSourceDirectory)
            project.mkdir(project.asakusafw.javac.annotationSourceDirectory)
        }
        project.compileJava.dependsOn(project.compileDMDL)
    }

    private defineCompileBatchappTask() {
        project.task('compileBatchapp', type: CompileBatchappTask, dependsOn: ['compileJava', 'processResources']) {
            group ASAKUSAFW_BUILD_GROUP
            description 'Compiles the Asakusa DSL java source with Asakusa DSL Compiler.'
            onlyIf { dependsOnTaskDidWork() }
        }
    }

    private defineJarBatchappTask() {
        project.task('jarBatchapp', type: Jar, dependsOn: 'compileBatchapp') {
            group ASAKUSAFW_BUILD_GROUP
            description 'Assembles a jar archive containing compiled batch applications.'
            from project.asakusafw.compiler.compiledSourceDirectory
            destinationDir project.buildDir
            appendix 'batchapps'
            onlyIf { dependsOnTaskDidWork() }
        }
    }

    private extendAssembleTask() {
        project.assemble.dependsOn(project.jarBatchapp)
    }

    private defineGenerateTestbookTask() {
        project.task('generateTestbook', type: GenerateTestbookTask) {
            group ASAKUSAFW_BUILD_GROUP
            description 'Generates the template Excel books for TestDriver.'
            source project.asakusafw.dmdl.dmdlSourceDirectory
            outputs.dir project.asakusafw.testtools.testDataSheetDirectory
        }
    }

    private void applySubPlugins() {
        new EclipsePluginEnhancement().apply(project)
    }

}

