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

import static com.asakusafw.operation.tools.directio.file.Util.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.operation.tools.directio.DirectIoPath;
import com.asakusafw.operation.tools.directio.common.ExecutorParameter;
import com.asakusafw.operation.tools.directio.common.Task;
import com.asakusafw.runtime.directio.ResourceInfo;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceCore;
import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.asakusafw.utils.jcommander.CommandExecutionException;
import com.asakusafw.utils.jcommander.common.HelpParameter;
import com.asakusafw.utils.jcommander.common.OutputParameter;
import com.asakusafw.utils.jcommander.common.VerboseParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * A command for copying Direct I/O resources.
 * @since 0.10.0
 */
@Parameters(
        commandNames = "get",
        commandDescription = "Copies Direct I/O resources onto local file system."
)
public class FileGetCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(FileGetCommand.class);

    @ParametersDelegate
    final HelpParameter helpParameter = new HelpParameter();

    @ParametersDelegate
    final VerboseParameter verboseParameter = new VerboseParameter();

    @ParametersDelegate
    final OutputParameter outputParameter = new OutputParameter();

    @ParametersDelegate
    final DataSourceParameter dataSourceParameter = new DataSourceParameter();

    @ParametersDelegate
    final LocalPathParameter localPathParameter = new LocalPathParameter();

    @ParametersDelegate
    final ExecutorParameter executorParameter = new ExecutorParameter();

    @Parameter(
            description = "directio-path.. local-path",
            required = false)
    List<String> paths = new ArrayList<>();

    @Parameter(
            names = { "-r", "--recursive" },
            description = "Copy directories recursively.",
            required = false)
    boolean recursive = false;

    @Parameter(
            names = { "-w", "--overwrite" },
            description = "Overwrite destination files.",
            required = false)
    boolean overwrite = false;

    @Override
    public void run() {
        LOG.debug("starting {}", getClass().getSimpleName());

        if (paths.size() < 2) {
            throw new CommandConfigurationException("source and destination files must be specified");
        }
        List<DirectIoPath> sources = getSources();
        LOG.debug("source: {}", sources);

        java.nio.file.Path destination = getDestination();
        LOG.debug("destination: {}", destination);

        List<ResourceInfo> files = sources.stream()
                .flatMap(it -> {
                    List<ResourceInfo> list = FileListCommand.list(it);
                    if (list.isEmpty()) {
                        throw new CommandConfigurationException(MessageFormat.format(
                                "there are no files to copy: {0}",
                                it));
                    }
                    return list.stream();
                })
                .collect(Collectors.toList());

        if (Files.isDirectory(destination)) {
            copyOnto(files, destination);
        } else if (Optional.ofNullable(destination.getParent())
                .filter(Files::isDirectory)
                .isPresent()) {
            if (files.size() != 1) {
                throw new CommandConfigurationException(MessageFormat.format(
                        "copy souce is ambiguous: {0}",
                        files.stream()
                                .map(ResourceInfo::getPath)
                                .collect(Collectors.joining(", "))));
            }
            copyTo(files.get(0), destination);
        } else {
            throw new CommandConfigurationException(MessageFormat.format(
                    "destination directory does not exist: {0}",
                    destination.getParent()));
        }
    }

    private void copyOnto(List<ResourceInfo> sources, java.nio.file.Path destination) {
        sources.stream()
                .filter(it -> recursive || it.isDirectory() == false)
                .collect(Collectors.groupingBy(it -> asHadoopPath(it.getPath()).getName()))
                .forEach((k, v) -> {
                    java.nio.file.Path dst = destination.resolve(k);
                    if (v.size() >= 2) {
                        throw new CommandExecutionException(MessageFormat.format(
                                "conflict destination file \"{0}\": {1}",
                                dst,
                                v.stream()
                                        .map(ResourceInfo::getPath)
                                        .collect(Collectors.joining(", "))));
                    }
                    ResourceInfo src = v.get(0);
                    if (overwrite == false && Files.exists(dst)) {
                        throw new CommandExecutionException(MessageFormat.format(
                                "destination file already exists: {0} ({1})",
                                dst,
                                src.getPath()));
                    }
                });
        try (PrintWriter writer = outputParameter.open()) {
            executorParameter.execute(sources.stream()
                    .map(info -> {
                        org.apache.hadoop.fs.Path src = asHadoopPath(info.getPath());
                        org.apache.hadoop.fs.Path dst = asHadoopPath(destination.resolve(src.getName()));
                        return new Copy(
                                writer,
                                dataSourceParameter.getHadoopFileSystem(src), src,
                                dataSourceParameter.getHadoopFileSystem(dst), dst);
                    })
                    .collect(Collectors.toList()));
        }
    }

    private void copyTo(ResourceInfo source, java.nio.file.Path destination) {
        assert Files.isDirectory(destination) == false;
        try (PrintWriter writer = outputParameter.open()) {
            org.apache.hadoop.fs.Path src = asHadoopPath(source.getPath());
            org.apache.hadoop.fs.Path dst = asHadoopPath(destination);
            executorParameter.execute(new Copy(
                    writer,
                    dataSourceParameter.getHadoopFileSystem(src), src,
                    dataSourceParameter.getHadoopFileSystem(dst), dst));
        }
    }

    private List<DirectIoPath> getSources() {
        List<DirectIoPath> dpaths = paths.subList(0, paths.size() - 1).stream()
                .map(dataSourceParameter::resolve)
                .peek(it -> {
                    if (it.isComponentRoot()) {
                        throw new CommandConfigurationException(MessageFormat.format(
                                "cannot copy data source root \"{0}\"",
                                it));
                    }
                    if (it.getSource().getEntity().findProperty(HadoopDataSourceCore.class).isPresent() == false) {
                        throw new CommandConfigurationException(MessageFormat.format(
                                "unsupported data source \"{0}\" (type: {1}): {2}",
                                it.getSource().getId(),
                                it.getSource().getEntity().getClass().getName(),
                                it));
                    }
                })
                .collect(Collectors.toList());
        return dpaths;
    }

    private java.nio.file.Path getDestination() {
        java.nio.file.Path destination = localPathParameter.resolve(paths.get(paths.size() - 1));
        if (overwrite == false && Files.isRegularFile(destination)) {
            throw new CommandExecutionException(MessageFormat.format(
                    "destination file already exists: {0}",
                    destination));
        }
        return destination;
    }

    private class Copy implements Task {

        private final PrintWriter writer;

        private final org.apache.hadoop.fs.FileSystem srcFs;

        private final org.apache.hadoop.fs.FileSystem dstFs;

        private final org.apache.hadoop.fs.Path source;

        private final org.apache.hadoop.fs.Path destination;

        Copy(PrintWriter writer,
                FileSystem srcFs, org.apache.hadoop.fs.Path source,
                FileSystem dstFs, org.apache.hadoop.fs.Path destination) {
            this.writer = writer;
            this.srcFs = srcFs;
            this.dstFs = dstFs;
            this.source = source;
            this.destination = destination;
        }

        @Override
        public void execute(Context context) {
            try {
                FileStatus stat = srcFs.getFileStatus(source);
                LOG.debug("process: {} (dir={})", stat.getPath(), stat.isDirectory());
                if (stat.isDirectory()) {
                    if (recursive) {
                        if (dstFs.isFile(destination)) {
                            throw new IOException(MessageFormat.format(
                                    "cannot overwrite file by directory: {0} -> {1}",
                                    source, destination));
                        }
                        dstFs.mkdirs(destination);
                        verboseParameter.printf(writer, "copy directory: %s -> %s%n", source, destination);
                        Arrays.stream(srcFs.listStatus(source))
                                .map(s -> {
                                    org.apache.hadoop.fs.Path src = s.getPath();
                                    org.apache.hadoop.fs.Path dst = resolve(destination, src.getName());
                                    return context.submit(new Copy(writer, srcFs, src, dstFs, dst));
                                })
                                .collect(Collectors.toList())
                                .forEach(Task.Wait::forDone);
                    } else {
                        LOG.warn("skip directory: {}", source);
                    }
                } else {
                    if (dstFs.isDirectory(destination)) {
                        throw new IOException(MessageFormat.format(
                                "cannot overwrite directory by file: {0} -> {1}",
                                source, destination));
                    }
                    srcFs.copyToLocalFile(false, source, destination, true);
                    verboseParameter.printf(writer, "copy file: %s -> %s%n", source, destination);
                }
            } catch (IOException e) {
                throw new CommandExecutionException(MessageFormat.format(
                        "cannot copy resource: {0} -> {1}",
                        source, destination), e);
            }
        }
    }
}
