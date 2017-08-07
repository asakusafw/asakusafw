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
package com.asakusafw.info.cli.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Optional;

import com.asakusafw.info.BatchInfo;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides the batch information.
 * @since 0.10.0
 */
public class BatchInfoParameter {

    /**
     * The batch application base directory.
     */
    @ParametersDelegate
    public ApplicationBaseDirectoryParameter batchappsParameter = new ApplicationBaseDirectoryParameter();

    /**
     * The information file location - must be one of batch ID, batch directory, or information file path.
     */
    @Parameter(
            description = "batch-id",
            required = true
    )
    public String location;

    /**
     * Returns the information file path.
     * @return the information file path
     * @throws CommandConfigurationException if it is not found
     */
    public Path getPath() {
        Path base = batchappsParameter.findPath().orElse(null);
        return findInfo(base, location)
                .orElseThrow(() -> new CommandConfigurationException(MessageFormat.format(
                        "batch application \"{0}\" is not found",
                        location)));
    }

    /**
     * Loads the given information file.
     * @return the loaded information
     * @throws CommandConfigurationException if error occurred while loading the information file
     */
    public BatchInfo load() {
        return load(getPath());
    }

    /**
     * Returns the DSL information file path.
     * @param applicationBaseDir the batch application base directory (nullable)
     * @param location the file location - must be one of batch ID, batch directory, or information file path
     * @return the information file path, or {@code empty} if it is not found
     */
    public static Optional<Path> findInfo(Path applicationBaseDir, String location) {
        Path path = Paths.get(location);
        // just batch-info.json
        if (Files.isRegularFile(path)) {
            return Optional.of(path);
        }
        // may be a batch directory
        if (Files.isDirectory(path)) {
            Optional<Path> info = ApplicationBaseDirectoryParameter.findInfo(path);
            if (info.isPresent()) {
                return info;
            }
        }
        // may be a batch ID
        if (path.isAbsolute() == false && path.getNameCount() == 1 && applicationBaseDir != null) {
            Path applicationDir = applicationBaseDir.resolve(path.toString());
            Optional<Path> info = ApplicationBaseDirectoryParameter.findInfo(applicationDir);
            if (info.isPresent()) {
                return info;
            }
        }
        // not found
        return Optional.empty();
    }

    /**
     * Loads the given information file.
     * @param path the target information file path
     * @return the loaded information
     * @throws CommandConfigurationException if error occurred while loading the information file
     */
    public static BatchInfo load(Path path) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(path.toFile(), BatchInfo.class);
        } catch (IOException e) {
            throw new CommandConfigurationException(MessageFormat.format(
                    "failed to load DSL information file: {0}",
                    path), e);
        }
    }
}
