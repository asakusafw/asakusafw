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

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.util.ConfigureUtil

import com.asakusafw.gradle.plugins.AsakusafwOrganizerProfile

/**
 * An abstract implementation of processing {@link AsakusafwOrganizerProfile}.
 * @since 0.7.4
 */
abstract class AbstractOrganizer {

    /**
     * The current project.
     */
    final Project project

    /**
     * The target profile.
     */
    final AsakusafwOrganizerProfile profile

    /**
     * Creates a new instance.
     * @param project the current project
     * @param profile the target profile
     */
    AbstractOrganizer(Project project, AsakusafwOrganizerProfile profile) {
        this.project = project
        this.profile = profile
    }

    /**
     * Configures the target profile.
     */
    abstract void configureProfile()

    /**
     * Returns the profile name.
     * @return the profile name
     */
    String getName() {
        return profile.name
    }

    /**
     * Adds new configurations into the current project for the profile.
     * @param prefix the common configuration name prefix
     * @param confMap {@code name => description} map
     */
    protected void createConfigurations(String prefix, Map<String, String> confMap) {
        confMap.each { String name, String desc ->
            createConfiguration(prefix + name) {
                description desc
                visible false
            }
        }
    }

    /**
     * Adds dependencies into the current project for the profile.
     * @param prefix the common configuration name prefix
     * @param depsMap {@code configuration name => dependencies} map:
     *     {@code dependencies} can be a dependency notation or its collection
     */
    protected void createDependencies(String prefix, Map<String, Object> depsMap) {
        depsMap.each { String name, Object value ->
            if (value instanceof Collection<?> || value instanceof Object[]) {
                value.each { Object notation ->
                    createDependency prefix + name, notation
                }
            } else if (value != null) {
                createDependency prefix + name, value
            }
        }
    }

    /**
     * Adds a new task for attaching components into the assembly for the profile.
     * @param prefix the common task name prefix
     * @param actionMap {@code task name => task action} map
     */
    protected void createAttachComponentTasks(String prefix, Map<String, Closure<?>> actionMap) {
        actionMap.each { String name, Closure<?> action ->
            createTask(prefix + name) {
                doLast {
                    ConfigureUtil.configure action, profile.components
                }
            }
        }
    }

    /**
     * Returns whether the target task is for the profile or not.
     * @param task the target task
     * @return {@code true} if the target task is for the profile, otherwise {@code false}
     */
    protected boolean isProfileTask(Task task) {
        if (task.hasProperty('profileName')) {
            return task.profileName == profile.name
        }
        return false
    }

    /**
     * Returns the qualified name for the profile.
     * @param name the original name
     * @return the qualified name for the profile
     */
    protected String qualify(String name) {
        return "${name}_${profile.name}"
    }

    /**
     * Adds a new configuration for the profile.
     * @param name the configuration name
     * @param configurator the callback for customizing the target configuration
     * @return the created configuration
     */
    protected Configuration createConfiguration(String name, Closure<?> configurator) {
        return project.configurations.create(qualify(name), configurator)
    }

    /**
     * Adds a new dependency for the profile.
     * @param configurationName the target configuration name
     * @param notation the dependency notation
     * @return the created dependency object
     */
    protected Dependency createDependency(String configurationName, Object notation) {
        return project.dependencies.add(qualify(configurationName), notation)
    }

    /**
     * Creates a new task for the profile.
     * @param name the task name
     * @param parent the super class of the task
     * @param configurator the callback for configuring the target task
     * @return the created task
     */
    protected Task createTask(String name, Class<? extends Task> parent, Closure<?> configurator) {
        project.tasks.create(qualify(name), parent, configurator)
        Task task = task(name)
        task.ext.profileName = profile.name
        return task
    }

    /**
     * Creates a new task for the profile.
     * @param name the task name
     * @return the created task
     */
    protected Task createTask(String name) {
        project.tasks.create(qualify(name))
        Task task = task(name)
        task.ext.profileName = profile.name
        return task
    }

    /**
     * Creates a new task for the profile.
     * @param name the task name
     * @param configurator the callback for configuring the target task
     * @return the created task
     */
    protected Task createTask(String name, Closure<?> configurator) {
        project.tasks.create(qualify(name), configurator)
        Task task = task(name)
        task.ext.profileName = profile.name
        return task
    }

    /**
     * Returns the task name for the profile.
     * @param name the bare task name
     * @return the corresponded profile task name
     */
    String taskName(String name) {
        return qualify(name)
    }

    /**
     * Returns the task for the profile.
     * @param name the bare task name
     * @return the corresponded profile task
     */
    Task task(String name) {
        return project.tasks[qualify(name)]
    }

    /**
     * Returns the configuration for the profile.
     * @param name the bare task name
     * @return the corresponded profile configuration
     */
    Configuration configuration(String name) {
        return project.configurations[qualify(name)]
    }
}
