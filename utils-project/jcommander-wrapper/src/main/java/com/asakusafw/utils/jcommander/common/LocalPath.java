/**
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
package com.asakusafw.utils.jcommander.common;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.utils.jcommander.CommandConfigurationException;

/**
 * Utilities about local path.
 * @since 0.10.0
 */
public final class LocalPath {

    static final Logger LOG = LoggerFactory.getLogger(LocalPath.class);

    static final String KEY_WORKING_DIRECTORY = "cli.cwd"; //$NON-NLS-1$

    static final String ENV_WORKING_DIRECTORY = "CALLER_CWD"; //$NON-NLS-1$

    private static final Path WORKING_DIRECTORY;

    static {
        WORKING_DIRECTORY = Optional.ofNullable(
                System.getProperty(KEY_WORKING_DIRECTORY, System.getenv(ENV_WORKING_DIRECTORY)))
                .filter(it -> it.isEmpty() == false)
                .map(Paths::get)
                .orElse(null);
    }

    private LocalPath() {
        return;
    }

    /**
     * Returns the custom working directory.
     * @return the custom working directory, or {@code empty} if it is not defined
     */
    public static Optional<Path> findWorkingDirectory() {
        return Optional.ofNullable(WORKING_DIRECTORY);
    }

    /**
     * Returns a local path.
     * This is an alias of {@code #of(String, Path) of(path, Paths.get("."))}.
     * @param path the path string
     * @return the local path
     * @throws CommandConfigurationException if the path cannot be resolved
     */
    public static Path of(String path) {
        return of(path, Paths.get(".")); //$NON-NLS-1$
    }

    /**
     * Returns a local path.
     * @param path the path string
     * @param defaultWorkingDirectory the default working directory (nullable)
     * @return the local path
     * @throws CommandConfigurationException if the path cannot be resolved
     */
    public static Path of(String path, Path defaultWorkingDirectory) {
        Path candidate = Paths.get(path);
        if (candidate.isAbsolute()) {
            return candidate;
        } else if (WORKING_DIRECTORY != null) {
            if (WORKING_DIRECTORY.isAbsolute() == false) {
                throw new CommandConfigurationException(MessageFormat.format(
                        "custom working dierctory path must be absolute: {0}",
                        WORKING_DIRECTORY));
            }
            Path result = WORKING_DIRECTORY.resolve(path);
            LOG.debug("resolve local path: {} -> {}", path, result); //$NON-NLS-1$
            return result;
        } else if (defaultWorkingDirectory != null) {
            return defaultWorkingDirectory.resolve(path).toAbsolutePath();
        } else {
            throw new CommandConfigurationException(MessageFormat.format(
                    "local file path must be absolute: {0}",
                    path));
        }
    }
}
