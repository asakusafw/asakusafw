/*
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.gradle.assembly

import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.Buildable
import org.gradle.api.Task
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.file.FileTreeElement
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.TaskDependency
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.ConfigureUtil

/**
 * Handles assembly definitions.
 * @since 0.7.0
 * @version 0.7.1
 */
class AssemblyHandler implements Buildable {

    /**
     * The fragment target path.
     * This will be resolved as a string.
     */
    final Object target

    /**
     * Source files and directories.
     * This will be resolved using {@code project.files(...)}.
     */
    final List<Object> sourceFiles = new ArrayList<>()

    /**
     * Source archives in ZIP format.
     * This will be resolved using {@code project.files(...)}.
     */
    final List<Object> sourceArchives = new ArrayList<>()

    /**
     * The extra operations which are only available for this handler.
     */
    final LinkedList<Closure<?>> extraOperations = new LinkedList<>()

    /**
     * Creates a new instance.
     * @param target the target path (relative from assembly root)
     */
    AssemblyHandler(Object target) {
        this.target = target
    }

    /**
     * Adds source files or directories into this assembly definition.
     * @param files source files or directories
     * @return this
     */
    AssemblyHandler from(Object... files) {
        sourceFiles.addAll files
        return this
    }

    /**
     * Adds source files or directories into this assembly definition.
     * @param files source files or directories
     * @return this
     */
    AssemblyHandler put(Object... files) {
        return from(files)
    }

    /**
     * Adds source archive files (in ZIP) into this assembly definition.
     * @param archives archive files
     * @return this
     */
    AssemblyHandler extract(Object... archives) {
        sourceArchives.addAll archives
        return this
    }

    /**
     * Adds extra operations for this assembly definition.
     * The closure will receive {@link CopySpec} object for building the target assembly.
     * @param operation the extra operation
     * @return this
     */
    AssemblyHandler process(Closure<?> operation) {
        extraOperations.add operation
        return this
    }

    /**
     * Adds text replacement operation for this assembly definition.
     * This is equivalent to <code>replace(replacement, '*')</code>
     * This method is equivalent to <code>process { it.filter(ReplaceTokens, tokens: replacement) }</code>
     * @param replacement the replacement map
     * @return this
     */
    AssemblyHandler replace(Map<?, ?> replacement) {
        return replace(replacement, '*')
    }

    /**
     * Adds text replacement operation for this assembly definition.
     * This uses {@code ReplaceTokens} filter, and {@code patterns} are qualified with {@code '&#42;&#42;/'}.
     * @param replacement the replacement map
     * @param patterns target file path patterns
     * @return this
     */
    AssemblyHandler replace(Map<?, ?> replacement, String... patterns) {
        Spec<FileTreeElement> filter = new PatternSet().include(patterns.collect {
            return it.startsWith('**/') ? it : '**/' + it
        }).asSpec
        return process { CopySpec spec ->
            spec.eachFile { FileCopyDetails details ->
                if (filter.isSatisfiedBy(details)) {
                    details.filter(ReplaceTokens, tokens: replacement)
                }
            }
        }
    }

    @Override
    TaskDependency getBuildDependencies() {
        return { Task task ->
            task.project.files(sourceFiles, sourceArchives).buildDependencies.getDependencies(task)
        } as TaskDependency
    }

    /**
     * Configures this assembly definition.
     * @param configurator the configurator closure
     * @return this
     */
    AssemblyHandler configure(Closure<?> configurator) {
        return ConfigureUtil.configure(configurator, this)
    }
}
