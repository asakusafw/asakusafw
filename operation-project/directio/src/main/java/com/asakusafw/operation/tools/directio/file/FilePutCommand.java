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

import java.io.FileNotFoundException;
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
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.operation.tools.directio.common.ExecutorParameter;
import com.asakusafw.operation.tools.directio.common.Task;
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
        commandNames = "put",
        commandDescription = "Copies local files onto Direct I/O data source."
)
public class FilePutCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(FilePutCommand.class);

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
            description = "local-path.. directio-path",
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
        List<java.nio.file.Path> sources = getSources();
        LOG.debug("source: {}", sources);

        org.apache.hadoop.fs.Path destination = getDestination();
        LOG.debug("destination: {}", destination);

        Optional<org.apache.hadoop.fs.FileStatus> stat = stat(destination);
        if (stat.filter(it -> it.isDirectory()).isPresent()) {
            copyOnto(sources, destination);
        } else if (stat.filter(it -> it.isDirectory() == false).isPresent() && overwrite == false) {
            throw new CommandExecutionException(MessageFormat.format(
                    "destination file already exists: {0}",
                    destination));
        } else {
            Path parent = Optional.ofNullable(destination.getParent())
                    .orElseThrow(() -> new IllegalStateException(destination.toString()));
            if (stat(parent).filter(it -> it.isDirectory()).isPresent()) {
                if (sources.size() >= 2) {
                    throw new CommandConfigurationException(MessageFormat.format(
                            "copy source is ambiguous: {0}",
                            sources.stream()
                                    .map(String::valueOf)
                                    .collect(Collectors.joining(", "))));
                }
                copyTo(sources.get(0), destination);
            } else {
                throw new CommandConfigurationException(MessageFormat.format(
                        "destination directory does not exist: {0}",
                        parent));
            }
        }
    }

    private Optional<org.apache.hadoop.fs.FileStatus> stat(org.apache.hadoop.fs.Path path) {
        try {
            return Optional.of(dataSourceParameter.getHadoopFileSystem(path).getFileStatus(path));
        } catch (FileNotFoundException e) {
            LOG.trace("not found: {}", path, e);
            return Optional.empty();
        } catch (IOException e) {
            throw new CommandConfigurationException(MessageFormat.format(
                    "error occurred while resolving Hadoop path: {0}",
                    path), e);
        }
    }

    private void copyOnto(List<java.nio.file.Path> sources, org.apache.hadoop.fs.Path destination) {
        sources.stream()
                .filter(it -> recursive || Files.isDirectory(it) == false)
                .collect(Collectors.groupingBy(it -> Optional.ofNullable(it.getFileName())
                        .map(String::valueOf)
                        .orElseThrow(() -> new IllegalStateException(it.toString()))))
                .forEach((k, v) -> {
                    org.apache.hadoop.fs.Path dst = resolve(destination, k);
                    if (v.size() >= 2) {
                        throw new CommandExecutionException(MessageFormat.format(
                                "conflict destination file \"{0}\": {1}",
                                dst,
                                v.stream()
                                        .map(String::valueOf)
                                        .collect(Collectors.joining(", "))));
                    }
                    java.nio.file.Path src = v.get(0);
                    if (overwrite == false && stat(dst).isPresent()) {
                        throw new CommandExecutionException(MessageFormat.format(
                                "destination file already exists: {0} ({1})",
                                dst,
                                src));
                    }
                });
        try (PrintWriter writer = outputParameter.open()) {
            executorParameter.execute(sources.stream()
                    .map(source -> {
                        org.apache.hadoop.fs.Path src = asHadoopPath(source);
                        org.apache.hadoop.fs.Path dst = resolve(destination, src.getName());
                        return new Copy(
                                writer,
                                dataSourceParameter.getHadoopFileSystem(src), src,
                                dataSourceParameter.getHadoopFileSystem(dst), dst);
                    })
                    .collect(Collectors.toList()));
        }
    }

    private void copyTo(java.nio.file.Path source, org.apache.hadoop.fs.Path destination) {
        try (PrintWriter writer = outputParameter.open()) {
            org.apache.hadoop.fs.Path src = asHadoopPath(source);
            org.apache.hadoop.fs.Path dst = destination;
            executorParameter.execute(new Copy(
                    writer,
                    dataSourceParameter.getHadoopFileSystem(src), src,
                    dataSourceParameter.getHadoopFileSystem(dst), dst));
        }
    }

    private List<java.nio.file.Path> getSources() {
        return paths.subList(0, paths.size() - 1).stream()
                .map(localPathParameter::resolve)
                .collect(Collectors.toList());
    }

    private org.apache.hadoop.fs.Path getDestination() {
        return dataSourceParameter.resolveAsHadoopPath(paths.get(paths.size() - 1));
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
                    dstFs.copyFromLocalFile(false, true, source, destination);
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
