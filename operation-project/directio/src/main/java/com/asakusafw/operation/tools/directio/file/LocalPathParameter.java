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
package com.asakusafw.operation.tools.directio.file;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.beust.jcommander.Parameter;

/**
 * Handles parameters about local file system path.
 * @since 0.10.0
 */
public class LocalPathParameter {

    static final Logger LOG = LoggerFactory.getLogger(LocalPathParameter.class);

    static final String KEY_WORKING_DIRECTORY = "cli.cwd";

    static final String ENV_WORKING_DIRECTORY = "CALLER_CWD";

    @Parameter(
            names = { "--working-directory" },
            description = "",
            required = false,
            hidden = true)
    Path workingDirectory = Optional.ofNullable(
            System.getProperty(KEY_WORKING_DIRECTORY, System.getenv(ENV_WORKING_DIRECTORY)))
            .filter(it -> it.isEmpty() == false)
            .map(Paths::get)
            .orElse(null);

    /**
     * Resolves the given path.
     * @param path the path string
     * @return the corresponded local file path
     */
    public Path resolve(String path) {
        Path candidate = Paths.get(path);
        if (candidate.isAbsolute()) {
            return candidate;
        } else if (workingDirectory != null) {
            if (workingDirectory.isAbsolute() == false) {
                throw new CommandConfigurationException(MessageFormat.format(
                        "custom working dierctory path must be absolute: {0}",
                        workingDirectory));
            }
            Path result = workingDirectory.resolve(path);
            LOG.debug("resolve local path: {} -> {}", path, result);
            return result;
        } else {
            throw new CommandConfigurationException(MessageFormat.format(
                    "local file path must be absolute: {0}",
                    path));
        }
    }
}
