/**
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
package com.asakusafw.info.cli.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.asakusafw.utils.jcommander.common.LocalPath;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Provides the batch applications directory.
 * @since 0.10.0
 */
@Parameters(resourceBundle = "com.asakusafw.info.cli.jcommander")
public class ApplicationBaseDirectoryParameter {

    static final Logger LOG = LoggerFactory.getLogger(ApplicationBaseDirectoryParameter.class);

    static final String ENV_ASAKUSA_HOME = "ASAKUSA_HOME";

    static final String ASAKUSA_BATCHAPPS_HOME;
    static {
        ASAKUSA_BATCHAPPS_HOME = Stream.of(
            Optional.ofNullable(System.getenv("ASAKUSA_BATCHAPPS_HOME"))
                .map(it -> it.trim())
                .filter(it -> it.isEmpty() == false)
                .map(Paths::get),
            Optional.ofNullable(System.getenv(ENV_ASAKUSA_HOME))
                .map(it -> it.trim())
                .filter(it -> it.isEmpty() == false)
                .map(Paths::get)
                .map(it -> it.resolve("batchapps")))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(Files::isDirectory)
            .map(Path::toString)
            .findFirst()
            .orElse(null);
    }

    static final String OPT_BATCHAPPS = "--batchapps";

    static final String PATH_BATCH_INFO = "etc/batch-info.json";

    /**
     * The path.
     */
    @Parameter(
            names = { "-B", OPT_BATCHAPPS },
            descriptionKey = "parameter.batchapps",
            required = false
    )
    public String path = ASAKUSA_BATCHAPPS_HOME;

    /**
     * Returns the path.
     * @return the path, or {@code empty} if the re is not available batch application base directory
     */
    public Optional<Path> findPath() {
        if (path == null) {
            return Optional.empty();
        }
        return Optional.of(path)
                .map(LocalPath::of)
                .filter(Files::isDirectory);
    }

    /**
     * Returns the path.
     * @return the path
     * @throws CommandConfigurationException if there is no available batch application base directory
     */
    public Path getPath() {
        if (path == null) {
            throw new CommandConfigurationException(MessageFormat.format(
                    "please set environment variable \"{0}\", or specify {1} /path/to/batchapps to command line",
                    ENV_ASAKUSA_HOME,
                    OPT_BATCHAPPS));
        }
        Path result = LocalPath.of(path);
        if (Files.isDirectory(result) == false) {
            throw new CommandConfigurationException(MessageFormat.format(
                    "batch applications directory is not found: {0}",
                    result));
        }
        return result;
    }

    /**
     * Returns the available entries in the batch applications directory.
     * @return the available entries
     */
    public List<Path> getEntries() {
        return getEntries(getPath());
    }

    /**
     * Returns the available entries in the batch application base directory.
     * @param applicationBaseDir the batch application base directory
     * @return the available entries
     */
    public static List<Path> getEntries(Path applicationBaseDir) {
        try {
            if (Files.isDirectory(applicationBaseDir) == false) {
                return Collections.emptyList();
            }
            return Files.list(applicationBaseDir)
                    .filter(Files::isDirectory)
                    .filter(it -> findInfo(it).isPresent())
                    .sorted(Comparator.comparing(ApplicationBaseDirectoryParameter::getName))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.warn("cannot list entries of directory: {}", applicationBaseDir, e);
            return Collections.emptyList();
        }
    }

    /**
     * Returns the information file path in the given batch application directory.
     * @param applicationDir the target application directory
     * @return the path, or {@code empty} if it is not found
     */
    public static Optional<Path> findInfo(Path applicationDir) {
        return Optional.ofNullable(applicationDir)
                .filter(Files::isDirectory)
                .map(it -> it.resolve(PATH_BATCH_INFO))
                .filter(Files::isRegularFile);
    }

    private static String getName(Path path) {
        return Optional.ofNullable(path.getFileName())
                .map(Path::toString)
                .orElseThrow(() -> new IllegalStateException(path.toString()));
    }
}
