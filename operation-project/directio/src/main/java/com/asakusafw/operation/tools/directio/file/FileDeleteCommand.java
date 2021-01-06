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
package com.asakusafw.operation.tools.directio.file;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.operation.tools.directio.BasePath;
import com.asakusafw.operation.tools.directio.DirectIoPath;
import com.asakusafw.operation.tools.directio.common.ExecutorParameter;
import com.asakusafw.operation.tools.directio.common.Task;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.asakusafw.utils.jcommander.CommandExecutionException;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for removing files.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "delete",
        commandDescriptionKey = "command.file-delete",
        resourceBundle = "com.asakusafw.operation.tools.directio.jcommander"
)
public class FileDeleteCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(FileDeleteCommand.class);

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
            required = false
    )
    List<String> paths = new ArrayList<>();

    @Parameter(
            names = { "-r", "--recursive" },
            descriptionKey = "parameter.recursive-delete",
            required = false)
    boolean recursive = false;

    @Override
    public void run() {
        LOG.debug("starting {}", getClass().getSimpleName());

        if (paths.isEmpty()) {
            throw new CommandConfigurationException("no target files are specified");
        }
        List<DirectIoPath> dpaths = paths.stream()
                .map(dataSourceParameter::resolve)
                .peek(it -> {
                    if (it.isComponentRoot()) {
                        throw new CommandConfigurationException(MessageFormat.format(
                                "cannot delete data source root \"{0}\"",
                                it));
                    }
                })
                .peek(it -> {
                    if (FileListCommand.list(it).isEmpty()) {
                        throw new CommandConfigurationException(MessageFormat.format(
                                "there are no resources to delete: {0}",
                                it));
                    }
                })
                .collect(Collectors.toList());

        try (PrintWriter writer = outputParameter.open()) {
            executorParameter.execute(dpaths.stream()
                    .map(dpath -> (Task) context -> delete(writer, dpath))
                    .collect(Collectors.toList()));
        }
    }

    private void delete(PrintWriter writer, DirectIoPath dpath) {
        LOG.debug("delete: {} (recursive={})", dpath, recursive);
        if (verboseParameter.isRequired()) {
            FileListCommand.list(dpath).forEach(info -> {
                if (recursive || info.isDirectory() == false) {
                    writer.printf("delete: %s%n", info.getPath());
                } else {
                    writer.printf("skip: %s%n", info.getPath());
                }
            });
        }
        long count = delete(dpath);
        if (count == 0) {
            LOG.warn("cannot delete any files: {}", dpath);
        }
    }

    private long delete(DirectIoPath dpath) {
        try {
            Counter counter = new Counter();
            if (dpath.getResourcePattern().isPresent()) {
                dpath.getSource().getEntity().delete(
                        dpath.getComponentPath().getPathString(),
                        dpath.getResourcePattern().get(),
                        recursive,
                        counter);
            } else {
                dpath.getSource().getEntity().delete(
                        BasePath.EMPTY.getPathString(),
                        dpath.getComponentPath().asFilePattern(),
                        recursive,
                        counter);
            }
            return counter.get();
        } catch (IOException | InterruptedException e) {
            throw new CommandExecutionException(MessageFormat.format(
                    "error occurred while deleting file: {0}",
                    dpath.getBarePath()), e);
        }
    }
}
