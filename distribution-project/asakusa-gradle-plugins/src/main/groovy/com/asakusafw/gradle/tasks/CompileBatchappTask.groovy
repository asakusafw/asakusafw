package com.asakusafw.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class CompileBatchappTask extends DefaultTask {

    @TaskAction
    def compileBatchapp() {
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
            if (compilerPluginPath.size()) {
                args += [
                        '-plugin',
                        compilerPluginPath
                ]
            }
        }
    }
}
