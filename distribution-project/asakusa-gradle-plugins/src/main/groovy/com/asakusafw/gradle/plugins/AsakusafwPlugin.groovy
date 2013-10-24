package com.asakusafw.gradle.plugins

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.bundling.*

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
            it.options.compilerArgs = ['-s', project.asakusafw.javac.annotationSourceDirectory, '-Xmaxerrs', '10000', '-XprintRounds']
            it.options.encoding = project.asakusafw.javac.sourceEncoding
            }
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
        def compileDMDL = project.task('compileDMDL') << {
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
                if (dmdlPluginPath) {
                    args += [
                        '-plugin',
                        dmdlPluginPath
                    ]
                }
            }
        }
        compileDMDL.setGroup(ASAKUSAFW_BUILD_GROUP)
        compileDMDL.setDescription('Compiles the DMDL scripts with DMDL Compiler.')
    }

    private extendCompileJavaTask() {
        project.compileJava.doFirst {
            project.delete(project.asakusafw.javac.annotationSourceDirectory)
            project.mkdir(project.asakusafw.javac.annotationSourceDirectory)
        }
        project.compileJava.dependsOn(project.compileDMDL)
    }

    private defineCompileBatchappTask() {
        def compileBatchapp = project.task('compileBatchapp') << {
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
                if (compilerPluginPath) {
                    args += [
                        '-plugin',
                        compilerPluginPath
                    ]
                }
            }
        }
        compileBatchapp.setGroup(ASAKUSAFW_BUILD_GROUP)
        compileBatchapp.setDescription('Compiles the Asakusa DSL java source with Asakusa DSL Compiler.')
        compileBatchapp.dependsOn(project.classes)
    }

    private defineJarBatchappTask() {
        def jarBatchapp = project.task('jarBatchapp', type: Jar) {
            from project.asakusafw.compiler.compiledSourceDirectory
            destinationDir project.buildDir
            appendix 'batchapps'
        }
        jarBatchapp.setGroup(ASAKUSAFW_BUILD_GROUP)
        jarBatchapp.setDescription('Assembles a jar archive containing compiled batch applications.')
        jarBatchapp.dependsOn(project.compileBatchapp)
    }

    private extendAssembleTask() {
        project.assemble.dependsOn(project.jarBatchapp)
    }

    private defineGenerateTestbookTask() {
        def generateTestbook = project.task('generateTestbook') << {
            project.javaexec {
                main = 'com.asakusafw.testdata.generator.excel.Main'
                classpath = project.sourceSets.main.compileClasspath
                maxHeapSize = project.asakusafw.maxHeapSize
                jvmArgs = [
                    "-Dlogback.configurationFile=${project.asakusafw.logbackConf}"
                ]
                args = [
                    '-format',
                    project.asakusafw.testtools.testDataSheetFormat,
                    '-output',
                    project.asakusafw.testtools.testDataSheetDirectory,
                    '-source',
                    project.asakusafw.dmdl.dmdlSourceDirectory,
                    '-encoding',
                    project.asakusafw.dmdl.dmdlEncoding,
                ]
            }
        }
        generateTestbook.setGroup(ASAKUSAFW_BUILD_TOOL_GROUP)
        generateTestbook.setDescription('Generates the template Excel books for TestDriver.')
    }

    private void applySubPlugins() {
        new EclipsePluginEnhancement().apply(project)
    }

}

