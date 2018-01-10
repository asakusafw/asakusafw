/**
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
package com.asakusafw.operation.tools.directio.file;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.operation.tools.directio.BasePath;
import com.asakusafw.operation.tools.directio.DataSourceInfo;
import com.asakusafw.operation.tools.directio.DirectIoPath;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.FilePattern;
import com.asakusafw.runtime.directio.ResourceInfo;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.asakusafw.utils.jcommander.CommandExecutionException;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for printing file list.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "list",
        commandDescriptionKey = "command.file-list",
        resourceBundle = "com.asakusafw.operation.tools.directio.jcommander"
)
public class FileListCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(FileListCommand.class);

    private static final FilePattern ALL = FilePattern.compile("**");

    @ParametersDelegate
    final HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    final VerboseParameter verboseParameter = new VerboseParameter();

    @ParametersDelegate
    final OutputParameter outputParameter = new OutputParameter();

    @ParametersDelegate
    final DataSourceParameter dataSourceParameter = new DataSourceParameter();

    @Parameter(
            description = "[directio-path..]",
            required = false
    )
    List<String> paths = new ArrayList<>();

    @Override
    public void run() {
        LOG.debug("starting {}", getClass().getSimpleName());

        List<ResourceInfo> entries = list().stream()
                .sorted(Comparator.comparing(ResourceInfo::getPath))
                .collect(Collectors.toList());

        try (PrintWriter writer = outputParameter.open()) {
            if (verboseParameter.isRequired()) {
                writer.printf("total %,d%n", entries.size());
                entries.forEach(it -> {
                    writer.printf("%s%n", it.getPath());
                    writer.printf("    data source: %s%n", it.getId());
                    writer.printf("      directory: %s%n", it.isDirectory());
                });
            } else {
                entries.forEach(it -> writer.println(it.getPath()));
            }
        }
    }

    private List<ResourceInfo> list() {
        if (paths.isEmpty()) {
            List<DataSourceInfo> all = dataSourceParameter.getDataSourceInfo()
                    .map(Collections::singletonList)
                    .orElseGet(dataSourceParameter::getAllDataSourceInfo);
            if (all.isEmpty()) {
                throw new CommandConfigurationException("there are no available Direct I/O data sources");
            }
            return all.stream()
                    .map(info -> new DirectIoPath(info, info.getPath(), ALL))
                    .flatMap(it -> list(it).stream())
                    .collect(Collectors.toList());
        } else {
            return paths.stream()
                    .flatMap(path -> list(dataSourceParameter.resolve(path)).stream())
                    .collect(Collectors.toList());
        }
    }

    static List<ResourceInfo> list(DirectIoPath path) {
        LOG.debug("listing: {} ({})", path, path.getBarePath());
        try {
            if (path.isComponentRoot()) {
                return Collections.singletonList(new ResourceInfo(
                        path.getSource().getId(),
                        path.getSource().getEntity().path(path.getComponentPath().getPathString()),
                        true));
            } else if (path.getResourcePattern().isPresent()) {
                return path.getSource().getEntity().list(
                        path.getComponentPath().getPathString(),
                        path.getResourcePattern().get(),
                        new Counter());
            } else {
                return path.getSource().getEntity().list(
                        BasePath.EMPTY.getPathString(),
                        path.getComponentPath().asFilePattern(),
                        new Counter());
            }
        } catch (IOException | InterruptedException e) {
            throw new CommandExecutionException(MessageFormat.format(
                    "error occurred while resolving path: {0} ({1})",
                    path,
                    path.getBarePath()), e);
        }
    }
}
