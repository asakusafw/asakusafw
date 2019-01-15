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
package com.asakusafw.gradle.plugins.internal

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.file.SourceDirectorySetFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.SourceSet

/**
 * An internal plug-in for providing Asakusa SDK common operations.
 * This class is only for internal use.
 * @since 0.8.0
 */
class AsakusaSdk implements Plugin<Project> {

    /**
     * The environment variable of Asakusa framework installation path.
     */
    public static final String ENV_HOME = 'ASAKUSA_HOME'

    private Project project

    private File frameworkHome

    @Override
    void apply(Project project) {
        this.project = project
        this.frameworkHome = [System.getenv(ENV_HOME)].findAll { it }.collect { project.file(it) }.find()
    }

    private static AsakusaSdk getInstance(Project project) {
        project.apply plugin: AsakusaSdk
        return project.plugins.getPlugin(AsakusaSdk)
    }

    static void setFrameworkInstallationPath(Project project, File path) {
        getInstance(project).frameworkHome = path
    }

    static File getFrameworkInstallationPath(Project project, boolean failOnUndefined = false) {
        File result = getInstance(project).frameworkHome
        if (result == null && failOnUndefined) {
            throw new IllegalStateException("'${ENV_HOME}' is not defined")
        }
        return result
    }

    static boolean isFrameworkInstalled(Project project) {
        File home = getInstance(project).frameworkHome
        return home != null && home.exists()
    }

    static void checkFrameworkInstalled(Project project) {
        File home = getInstance(project).frameworkHome
        if (home == null) {
            throw new IllegalStateException("Environment variable '${ENV_HOME}' is not defined")
        } else if (home.exists() == false) {
            throw new IllegalStateException("Environment variable '${ENV_HOME}' is not valid: ${home}")
        }
    }

    static File getFrameworkFile(Project project, String relativePath) {
        File home = getInstance(project).frameworkHome
        if (home != null) {
            return new File(home, relativePath)
        }
        return null
    }

    static SourceDirectorySet createSourceDirectorySet(Project project, SourceSet parent, String name, String displayName) {
        AsakusaSdk instance = getInstance(project)
        assert parent instanceof ExtensionAware
        ExtensionContainer extensions = parent.extensions
        // currently, project.sourceSets.main.* is not ExtensionAware
        SourceDirectorySet extension = instance.newSourceDirectorySet(name, displayName)
        extensions.add(name, extension)
        return extension
    }

    private SourceDirectorySet newSourceDirectorySet(String name, String displayName) {
        if (PluginUtils.compareGradleVersion('2.12') >= 0) {
            // SourceDirectorySetFactory has been introduced since 2.12
            SourceDirectorySetFactory factory = project.services.get(SourceDirectorySetFactory)
            return factory.create(name, displayName)
        } else {
            // In older versions of Gradle, we must directly create DefaultSourceDirectorySet
            FileResolver fileResolver = project.services.get(FileResolver)
            return new DefaultSourceDirectorySet(name, displayName, fileResolver)
        }
    }
}
