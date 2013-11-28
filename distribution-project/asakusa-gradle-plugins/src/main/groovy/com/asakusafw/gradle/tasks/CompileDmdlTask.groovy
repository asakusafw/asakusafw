package com.asakusafw.gradle.tasks

import org.gradle.api.tasks.*

class CompileDmdlTask extends SourceTask {

    @TaskAction
    def compileDmdl() {
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
            if (dmdlPluginPath.size()) {
                args += [
                        '-plugin',
                        dmdlPluginPath
                ]
            }
        }
    }
}
