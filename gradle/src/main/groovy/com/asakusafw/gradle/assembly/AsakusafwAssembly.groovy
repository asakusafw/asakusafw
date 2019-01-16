/*
 * Copyright 2011-2019 Asakusa Framework Team.
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

import org.gradle.api.Buildable
import org.gradle.api.Task
import org.gradle.api.tasks.TaskDependency

/**
 * Assembly definitions for organizing Asakusa Framework distribution packages.
 * @since 0.7.0
 * @version 0.7.1
 */
class AsakusafwAssembly implements Buildable {

    /**
     * The assembly name.
     */
    final String name

    /**
     * The assembly definition fragments.
     */
    final List<AssemblyHandler> handlers = new ArrayList<>()

    /**
     * The common extra operations which are available for all handlers in this assembly.
     */
    final LinkedList<Closure<?>> extraOperations = new LinkedList<>()

    /**
     * Creates a new instance.
     * @param project the target project
     * @param name the assembly name
     */
    AsakusafwAssembly(String name) {
        this.name = name
    }

    /**
     * Adds extra operations for all assembly definitions.
     * The closure will receive {@link CopySpec} object for building the target assembly.
     * @param operation the extra operation
     * @return this
     */
    AsakusafwAssembly process(Closure<?> operation) {
        extraOperations.add operation
        return this
    }

    /**
     * Adds an assembly definition.
     * @param target the target directory path (relative from assembly package root)
     * @return the created assembly definition
     */
    AssemblyHandler into(Object target) {
        AssemblyHandler handler = new AssemblyHandler(target)
        handlers.add(handler)
        return handler
    }

    /**
     * Adds an assembly definition and configures it.
     * @param target the target directory path (relative from assembly package root)
     * @param configurator a closure that configures the target assembly definition
     * @return the created assembly definition
     */
    AssemblyHandler into(Object target, Closure<?> configurator) {
        return into(target).configure(configurator)
    }

    @Override
    TaskDependency getBuildDependencies() {
        return { Task task ->
            handlers.collect { AssemblyHandler handler ->
                handler.buildDependencies.getDependencies(task)
            }.flatten().toSet()
        } as TaskDependency
    }

    @Override
    String toString() {
        return "Assembly[${name}]"
    }
}
