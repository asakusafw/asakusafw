/**
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
package com.asakusafw.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.utils.gradle.BaseProject;
import com.asakusafw.utils.gradle.Bundle;
import com.asakusafw.utils.gradle.TryConsumer;

/**
 * Utilities for testing on Asakusa projects.
 * @since 0.9.2
 * @see AsakusaProjectProvider
 */
public class AsakusaProject extends BaseProject<AsakusaProject> {

    static final Logger LOG = LoggerFactory.getLogger(AsakusaProject.class);

    private final Supplier<? extends Path> temporary;

    private final Path projectDirectory;

    private final Path frameworkDirectory;

    AsakusaProject(Path projectDirectory, Path frameworkDirectory, Supplier<? extends Path> temporary) {
        this.temporary = temporary;
        this.projectDirectory = projectDirectory;
        this.frameworkDirectory = frameworkDirectory;
    }

    @Override
    protected Path getDirectory() {
        return projectDirectory;
    }

    /**
     * Adds a bundle into the project.
     * @param key the system property key, which will pass to Gradle to obtain the bundle path
     * @return the bundle
     */
    public Bundle addBundle(String key) {
        return addBundle(key, temporary.get());
    }

    /**
     * Adds a bundle into the project.
     * @param key the system property key, which will pass to Gradle to obtain the bundle path
     * @param directory the directory
     * @return the bundle
     */
    public Bundle addBundle(String key, Path directory) {
        Path path = directory.toAbsolutePath();
        withProperty(key, path.toString());
        return getBundle(path);
    }

    /**
     * Returns the Asakusa Framework installation bundle.
     * @return the Asakusa Framework bundle
     */
    public Bundle getFramework() {
        if (Files.isDirectory(frameworkDirectory) == false) {
            throw new IllegalStateException("Asakusa Framework is not installed yet");
        }
        return getBundle(frameworkDirectory);
    }

    /**
     * Returns the Asakusa Framework installation bundle.
     * @param configurator the bundle configurator
     * @return this
     */
    public AsakusaProject withFramework(TryConsumer<? super Bundle, IOException> configurator) {
        return configure(getFramework(), configurator);
    }
}
