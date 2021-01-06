/*
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.util.ConfigureUtil

import com.asakusafw.gradle.assembly.AsakusafwAssembly
import com.asakusafw.gradle.assembly.AssemblyHandler
import com.asakusafw.gradle.tasks.internal.ResolutionUtils

/**
 * Gathers {@link AsakusafwAssembly} contents into a directory.
 * @since 0.7.0
 */
class GatherAssemblyTask extends DefaultTask {

    /**
     * The source assemblies information.
     */
    final List<AsakusafwAssembly> assemblies = new ArrayList<>()

    /**
     * The destination base directory.
     */
    @OutputDirectory
    File destination

    /**
     * Returns the input files for this task.
     * @return the input files
     */
    @SkipWhenEmpty
    @InputFiles
    Set<File> getAssemblyInputs() {
        if (getAssemblies().isEmpty()) {
            return Collections.emptySet()
        }
        def results = getAssemblies()*.handlers.flatten().collect { AssemblyHandler handler ->
            project.files(handler.sourceFiles + handler.sourceArchives).collect { File f ->
                if (f.isDirectory()) {
                    return project.fileTree(f).files
                } else if (f.isFile()) {
                    return [f]
                } else {
                    return []
                }
            }
        }.flatten().toSet()
        return results
    }

    /**
     * Returns the digest information for this assembly.
     * @return the digest information
     */
    @Input
    Object getInputDigest() {
        if (getAssemblies().isEmpty()) {
            return Collections.emptyMap()
        }
        def results = getAssemblies()*.handlers.flatten().collect { AssemblyHandler handler ->
            String key = ResolutionUtils.resolveToString(handler.target)
            return \
                project.files(handler.sourceFiles).collect { File file -> "${key}:file@${file}" as String } +
                project.files(handler.sourceArchives).collect { File file -> "${key}:archive@${file}" as String }
        }.flatten().toSet()
        return results
    }

    /**
     * Performs this task.
     */
    @TaskAction
    void perform() {
        File dest = getDestination()
        if (dest.exists()) {
            logger.info "Cleanup gather directory: ${dest}"
            project.delete dest
        }
        project.copy { CopySpec root ->
            logger.info "Gether into: ${dest}"
            root.into dest
            getAssemblies().each { AsakusafwAssembly assembly ->
                logger.info "Applying definition: ${assembly}"
                assembly.handlers.flatten().each { AssemblyHandler handler ->
                    String target = ResolutionUtils.resolveToString(handler.target)
                    root.into(handler.target) { CopySpec child ->
                        project.files(handler.sourceFiles).each { File file ->
                            logger.info "Copy: ${target} <- ${file}"
                            child.from file
                        }
                        project.files(handler.sourceArchives).each { File file ->
                            logger.info "Extract: ${target} <- ${file}"
                            child.from project.zipTree(file)
                        }
                        assembly.extraOperations.each { Closure<?> cl ->
                            ConfigureUtil.configure cl, child
                        }
                        handler.extraOperations.each { Closure<?> cl ->
                            ConfigureUtil.configure cl, child
                        }
                    }
                }
            }
        }
    }
}
