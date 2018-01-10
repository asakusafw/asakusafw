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
package com.asakusafw.gradle.plugins

import java.util.concurrent.Callable

import org.gradle.api.Buildable
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskDependency

/**
 * A test utilities for Asakusa organizers.
 */
abstract class OrganizerTestRoot {

    /**
     * Returns the current target project.
     * @return the target project
     */
    abstract Project getProject()

    String pname(AsakusafwOrganizerProfile profile, String name) {
        MockOrganizer organizer = new MockOrganizer(project, profile)
        assert organizer.taskName(name) != name
        return organizer.taskName(name)
    }

    Task ptask(AsakusafwOrganizerProfile profile, String name) {
        MockOrganizer organizer = new MockOrganizer(project, profile)
        assert organizer.taskName(name) != name
        return organizer.task(name)
    }

    void checkDependencies(String name) {
        assert dependencies(project.tasks.getByName(name)).containsAll(profileTasks(name))
    }

    Set<String> profileTasks(String name) {
        AsakusafwOrganizerPluginConvention convention = project.asakusafwOrganizer
        return convention.profiles.collect { AsakusafwOrganizerProfile profile ->
            return new MockOrganizer(project, profile).taskName(name)
        }.toSet()
    }

    Set<String> dependencies(Task task) {
        return task.getDependsOn().collect { toTaskNames(task, it) }.flatten().toSet()
    }

    Collection<String> toTaskNames(Task origin, Object value) {
        if (value instanceof Task) {
            return [ value.name ]
        } else if (value instanceof Callable<?>) {
            return toTaskNames(origin, value.call() ?: [])
        } else if (value instanceof TaskDependency) {
            return value.getDependencies(origin).collect { it.name }
        } else if (value instanceof Buildable) {
            return toTaskNames(origin, value.buildDependencies)
        } else if (value instanceof Collection<?> || value instanceof Object[]) {
            return value.collect { toTaskNames(origin, it) }.flatten()
        } else {
            return [ String.valueOf(value) ]
        }
    }
}
