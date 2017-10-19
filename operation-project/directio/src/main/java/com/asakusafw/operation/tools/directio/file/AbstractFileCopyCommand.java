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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
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
import com.beust.jcommander.ParametersDelegate;

/**
 * An abstract implementation of command for copying/moving Direct I/O resources.
 * @since 0.10.0
 */
public abstract class AbstractFileCopyCommand implements Runnable {

    static final Logger LOG = LoggerFactory.getLogger(AbstractFileCopyCommand.class);

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
            description = "source-directio-path.. destination-directio-path",
            required = false)
    List<String> paths = new ArrayList<>();

    @Parameter(
            names = { "-w", "--overwrite" },
            description = "Overwrite destination files.",
            required = false)
    boolean overwrite = false;

    abstract Op getOp();

    @Override
    public void run() {
        LOG.debug("starting {}", getClass().getSimpleName());

        if (paths.size() < 2) {
            throw new CommandConfigurationException("source and destination files must be specified");
        }
        List<DirectIoPath> sources = getSources();
        LOG.debug("source: {}", sources);

        Path destination = getDestination();
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

        validate(files, destination);
        Optional<FileStatus> stat = stat(destination);

        if (stat.filter(it -> it.isDirectory()).isPresent()) {
            copyOnto(files, destination);
        } else if (stat.filter(it -> it.isDirectory() == false).isPresent() && overwrite == false) {
            throw new CommandConfigurationException(MessageFormat.format(
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
                copyTo(files.get(0), destination);
            } else {
                throw new CommandConfigurationException(MessageFormat.format(
                        "destination directory does not exist: {0}",
                        parent));
            }
        }
    }

    private void validate(List<ResourceInfo> files, Path destination) {
        Set<Path> ancestors = new HashSet<>();
        for (Path path = qualify(destination); path != null; path = path.getParent()) {
            ancestors.add(path);
        }
        for (ResourceInfo file : files) {
            Path source = qualify(asHadoopPath(file.getPath()));
            LOG.debug("validate: {} -> {}", source, destination);
            if (ancestors.contains(source)) {
                throw new CommandConfigurationException(MessageFormat.format(
                        "cannot copy directory into its sub-directories: {0} -> {1}",
                        source, destination));
            }
        }
    }

    private Path qualify(Path path) {
        FileSystem fs = dataSourceParameter.getHadoopFileSystem(path);
        return fs.makeQualified(path);
    }

    private Optional<FileStatus> stat(Path path) {
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

    private void copyOnto(List<ResourceInfo> sources, Path destination) {
        sources.stream()
                .filter(it -> isRecursive() || it.isDirectory() == false)
                .collect(Collectors.groupingBy(it -> asHadoopPath(it.getPath()).getName()))
                .forEach((k, v) -> {
                    Path dst = resolve(destination, k);
                    if (v.size() >= 2) {
                        throw new CommandConfigurationException(MessageFormat.format(
                                "conflict destination file \"{0}\": {1}",
                                dst,
                                v.stream()
                                        .map(String::valueOf)
                                        .collect(Collectors.joining(", "))));
                    }
                    ResourceInfo src = v.get(0);
                    if (overwrite == false && stat(dst).isPresent()) {
                        throw new CommandConfigurationException(MessageFormat.format(
                                "destination file already exists: {0} ({1})",
                                dst,
                                src));
                    }
                });
        try (PrintWriter writer = outputParameter.open()) {
            executorParameter.execute(sources.stream()
                    .map(source -> {
                        Path src = asHadoopPath(source.getPath());
                        Path dst = resolve(destination, src.getName());
                        return new Copy(
                                writer,
                                dataSourceParameter.getHadoopFileSystem(src), src,
                                dataSourceParameter.getHadoopFileSystem(dst), dst);
                    })
                    .collect(Collectors.toList()));
        }
    }

    private void copyTo(ResourceInfo source, Path destination) {
        try (PrintWriter writer = outputParameter.open()) {
            org.apache.hadoop.fs.Path src = asHadoopPath(source.getPath());
            Path dst = destination;
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

    private Path getDestination() {
        return dataSourceParameter.resolveAsHadoopPath(paths.get(paths.size() - 1));
    }

    boolean isRecursive() {
        return getOp() != Op.COPY_THIN;
    }

    boolean isMove() {
        return getOp() == Op.MOVE;
    }

    Configuration getConf() {
        return dataSourceParameter.getConfiguration();
    }

    enum Op {

        COPY_THIN,

        COPY_RECURSIVE,

        MOVE,
    }

    private class Copy implements Task {

        private final PrintWriter writer;

        private final org.apache.hadoop.fs.FileSystem srcFs;

        private final org.apache.hadoop.fs.FileSystem dstFs;

        private final Path source;

        private final Path destination;

        Copy(PrintWriter writer,
                FileSystem srcFs, Path source,
                FileSystem dstFs, Path destination) {
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
                    if (isRecursive()) {
                        if (dstFs.isFile(destination)) {
                            throw new IOException(MessageFormat.format(
                                    "cannot overwrite file by directory: {0} -> {1}",
                                    source, destination));
                        }
                        dstFs.mkdirs(destination);
                        verboseParameter.printf(writer, "copy directory: %s -> %s%n", source, destination);
                        Arrays.stream(srcFs.listStatus(source))
                                .map(s -> {
                                    Path src = s.getPath();
                                    Path dst = resolve(destination, src.getName());
                                    return context.submit(new Copy(writer, srcFs, src, dstFs, dst));
                                })
                                .collect(Collectors.toList())
                                .forEach(Task.Wait::forDone);
                        if (isMove()) {
                            srcFs.delete(source, true);
                        }
                    } else {
                        LOG.warn("skip directory: {}", source);
                    }
                } else {
                    if (dstFs.isDirectory(destination)) {
                        throw new IOException(MessageFormat.format(
                                "cannot overwrite directory by file: {0} -> {1}",
                                source, destination));
                    }
                    FileUtil.copy(srcFs, source, dstFs, destination, isMove(), getConf());
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
