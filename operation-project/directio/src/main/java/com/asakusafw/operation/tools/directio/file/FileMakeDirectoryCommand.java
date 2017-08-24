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

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.operation.tools.directio.common.ExecutorParameter;
import com.asakusafw.operation.tools.directio.common.Task;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.asakusafw.utils.jcommander.CommandException;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for creating directories on Direct I/O.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "mkdir",
        commandDescription = "Creates directories on Direct I/O data source."
)
public class FileMakeDirectoryCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(FileMakeDirectoryCommand.class);

    @ParametersDelegate
    final HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    final VerboseParameter verboseParameter = new VerboseParameter();

    @ParametersDelegate
    final OutputParameter outputParameter = new OutputParameter();

    @ParametersDelegate
    final DataSourceParameter dataSourceParameter = new DataSourceParameter();

    @ParametersDelegate
    final ExecutorParameter executorParameter = new ExecutorParameter();

    @Parameter(
            description = "directio-path..",
            required = false)
    List<String> paths = new ArrayList<>();

    @Override
    public void run() {
        LOG.debug("starting {}", getClass().getSimpleName());

        if (paths.isEmpty()) {
            throw new CommandConfigurationException("no target paths are specified");
        }
        List<Path> hpaths = paths.stream()
                .map(dataSourceParameter::resolveAsHadoopPath)
                .collect(Collectors.toList());
        try (PrintWriter writer = outputParameter.open()) {
            executorParameter.execute(hpaths.stream()
                    .map(path -> (Task) context -> mkdir(writer, path))
                    .collect(Collectors.toList()));
        }
    }

    private void mkdir(PrintWriter writer, Path path) {
        LOG.debug("mkdir: {}", path);
        try {
            FileSystem fs = path.getFileSystem(dataSourceParameter.getConfiguration());
            if (stat(fs, path)
                    .filter(FileStatus::isDirectory)
                    .isPresent()) {
                verboseParameter.printf(writer, "already exists: %s%n", path);
            } else {
                if (fs.mkdirs(path) == false && stat(fs, path)
                        .filter(s -> s.isDirectory() == false)
                        .isPresent() == false) {
                    throw new CommandException(MessageFormat.format(
                            "cannot create directory: {0}",
                            path));
                }
                verboseParameter.printf(writer, "create directory: %s%n", path);
            }
        } catch (IOException e) {
            throw new CommandException(MessageFormat.format(
                    "error occurred while creating directory: {0}",
                    path), e);
        }
    }

    static Optional<FileStatus> stat(FileSystem fs, Path path) {
        try {
            return Optional.of(fs.getFileStatus(path));
        } catch (IOException e) {
            LOG.trace("exception: {}", path, e);
            return Optional.empty();
        }
    }
}
