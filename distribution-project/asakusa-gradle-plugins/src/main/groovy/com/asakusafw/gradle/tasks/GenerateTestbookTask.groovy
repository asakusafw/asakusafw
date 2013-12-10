package com.asakusafw.gradle.tasks

import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

class GenerateTestbookTask extends SourceTask {

    @TaskAction
    def generateTestbook() {
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
}
