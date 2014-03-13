/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio.hadoop;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.asakusafw.runtime.compatibility.FileSystemCompatibility;
import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.FilePattern;
import com.asakusafw.runtime.directio.FragmentableDataFormat;
import com.asakusafw.runtime.directio.OutputAttemptContext;
import com.asakusafw.runtime.directio.OutputTransactionContext;
import com.asakusafw.runtime.directio.ResourceInfo;
import com.asakusafw.runtime.directio.ResourcePattern;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * An implementation of {@link DirectDataSource} using {@link FileSystem}.
 * @since 0.2.5
 * @version 0.2.6
 */
public class HadoopDataSourceCore implements DirectDataSource {

    static final Log LOG = LogFactory.getLog(HadoopDataSourceCore.class);

    private static final String ATTEMPT_AREA = "attempts";

    private static final String STAGING_AREA = "staging";

    private final HadoopDataSourceProfile profile;

    /**
     * Creates a new instance.
     * @param profile profile of target data source
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public HadoopDataSourceCore(HadoopDataSourceProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        this.profile = profile;
    }

    @Override
    public <T> List<DirectInputFragment> findInputFragments(
            Class<? extends T> dataType,
            DataFormat<T> format,
            String basePath,
            ResourcePattern resourcePattern) throws IOException, InterruptedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Start finding input (id={0}, path={1}, resourcePattern={2})",
                    profile.getId(),
                    basePath,
                    resourcePattern));
        }
        FilePattern pattern = validate(resourcePattern);
        FragmentableDataFormat<T> sformat = validateFragmentable(format);
        HadoopDataSourceProfile p = profile;
        FileSystem fs = p.getFileSystem();
        Path root = p.getFileSystemPath();
        Path base = append(root, basePath);
        List<FileStatus> stats = HadoopDataSourceUtil.search(fs, base, pattern);
        stats = filesOnly(stats);

        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Process finding input (id={0}, path={1}, resource={2}, files={3})",
                    profile.getId(),
                    basePath,
                    resourcePattern,
                    stats.size()));
        }
        if (LOG.isTraceEnabled()) {
            for (FileStatus stat : stats) {
                LOG.trace(MessageFormat.format(
                        "Input found (path={0}, length={1})",
                        stat.getPath(),
                        stat.getLen()));
            }
        }
        long minSize = p.getMinimumFragmentSize(sformat);
        long prefSize = p.getPreferredFragmentSize(sformat);
        boolean combineBlocks = p.isCombineBlocks();
        boolean splitBlocks = p.isSplitBlocks();
        Path temporary = p.getTemporaryFileSystemPath();
        FragmentComputer optimizer = new FragmentComputer(minSize, prefSize, combineBlocks, splitBlocks);
        List<DirectInputFragment> results = new ArrayList<DirectInputFragment>();
        for (FileStatus stat : stats) {
            if (isIn(stat, temporary)) {
                continue;
            }
            String path = stat.getPath().toString();
            long fileSize = stat.getLen();
            List<BlockInfo> blocks = toBlocks(stat);
            if (LOG.isTraceEnabled()) {
                for (BlockInfo block : blocks) {
                    LOG.trace(MessageFormat.format(
                            "Original BlockInfo (path={0}, start={1}, end={2}, hosts={3})",
                            path,
                            block.start,
                            block.end,
                            block.hosts == null ? null : Arrays.toString(block.hosts)));
                }
            }
            List<DirectInputFragment> fragments = optimizer.computeFragments(path, fileSize, blocks);
            if (LOG.isTraceEnabled()) {
                for (DirectInputFragment fragment : fragments) {
                    LOG.trace(MessageFormat.format(
                            "Fragment found (path={0}, offset={1}, size={2}, owners={3})",
                            fragment.getPath(),
                            fragment.getOffset(),
                            fragment.getSize(),
                            fragment.getOwnerNodeNames()));
                }
            }
            results.addAll(fragments);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Finish finding input (id={0}, path={1}, resource={2}, fragments={3})",
                    profile.getId(),
                    basePath,
                    resourcePattern,
                    results.size()));
        }
        return results;
    }

    private boolean isIn(FileStatus stat, Path temporary) {
        assert stat != null;
        assert temporary != null;
        Path path = stat.getPath();
        if (path.equals(temporary) || HadoopDataSourceUtil.contains(temporary, path)) {
            return true;
        }
        return false;
    }

    private List<BlockInfo> toBlocks(FileStatus stat) throws IOException {
        BlockLocation[] locations = profile.getFileSystem().getFileBlockLocations(stat, 0, stat.getLen());
        List<BlockInfo> results = new ArrayList<BlockInfo>();
        for (BlockLocation location : locations) {
            long length = location.getLength();
            long start = location.getOffset();
            results.add(new BlockInfo(start, start + length, location.getHosts()));
        }
        return results;
    }

    private List<FileStatus> filesOnly(List<FileStatus> stats) {
        List<FileStatus> results = new ArrayList<FileStatus>();
        for (FileStatus stat : stats) {
            if (FileSystemCompatibility.isDirectory(stat) == false) {
                results.add(stat);
            }
        }
        return results;
    }

    @Override
    public <T> ModelInput<T> openInput(
            Class<? extends T> dataType,
            DataFormat<T> format,
            DirectInputFragment fragment,
            Counter counter) throws IOException, InterruptedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Start opening input (id={0}, path={1}, offset={2}, size={3})",
                    profile.getId(),
                    fragment.getPath(),
                    fragment.getOffset(),
                    fragment.getSize()));
        }
        HadoopFileFormat<T> fileFormat = convertFormat(format);
        ModelInput<T> input = fileFormat.createInput(
                dataType,
                profile.getFileSystem(),
                new Path(fragment.getPath()),
                fragment.getOffset(),
                fragment.getSize(),
                counter);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Finish opening input (id={0}, path={1}, offset={2}, size={3})",
                    profile.getId(),
                    fragment.getPath(),
                    fragment.getOffset(),
                    fragment.getSize()));
        }
        return input;
    }

    @Override
    public <T> ModelOutput<T> openOutput(
            OutputAttemptContext context,
            Class<? extends T> dataType,
            DataFormat<T> format,
            String basePath,
            String resourcePath,
            Counter counter) throws IOException, InterruptedException {
        FileSystem fs;
        Path attempt;
        if (isLocalAttemptOutput()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Start opening output (id={0}, path={1}, resource={2}, streaming={3})",
                        profile.getId(),
                        basePath,
                        resourcePath,
                        true));
            }
            fs = profile.getLocalFileSystem();
            attempt = getLocalAttemptOutput(context);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Start opening output (id={0}, path={1}, resource={2}, streaming={3})",
                        profile.getId(),
                        basePath,
                        resourcePath,
                        false));
            }
            fs = profile.getFileSystem();
            attempt = getAttemptOutput(context);
        }
        Path file = append(append(attempt, basePath), resourcePath);
        HadoopFileFormat<T> fileFormat = convertFormat(format);
        ModelOutput<T> output = fileFormat.createOutput(dataType, fs, file, counter);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Finish opening output (id={0}, path={1}, resource={2}, file={3})",
                    profile.getId(),
                    basePath,
                    resourcePath,
                    file));
        }
        return output;
    }

    boolean isLocalAttemptOutput() {
        return profile.isOutputStreaming() == false
                && HadoopDataSourceUtil.isLocalAttemptOutputDefined(profile.getLocalFileSystem());
    }

    private FilePattern validate(ResourcePattern pattern) throws IOException {
        assert pattern != null;
        if ((pattern instanceof FilePattern) == false) {
            throw new IOException(MessageFormat.format(
                    "{2} must be a subtype of {1} (path={0})",
                    profile.getContextPath(),
                    FilePattern.class.getName(),
                    pattern.getClass().getName()));
        }
        return (FilePattern) pattern;
    }

    private <T> FragmentableDataFormat<T> validateFragmentable(DataFormat<T> format) throws IOException {
        assert format != null;
        if ((format instanceof FragmentableDataFormat<?>) == false) {
            throw new IOException(MessageFormat.format(
                    "{2} must be a subtype of {1} (path={0})",
                    profile.getContextPath(),
                    FragmentableDataFormat.class.getName(),
                    format.getClass().getName()));
        }
        return (FragmentableDataFormat<T>) format;
    }

    private <T> HadoopFileFormat<T> convertFormat(DataFormat<T> format) throws IOException {
        assert format != null;
        if (format instanceof HadoopFileFormat<?>) {
            return (HadoopFileFormat<T>) format;
        } else {
            return new HadoopFileFormatAdapter<T>(validateStream(format), profile.getFileSystem().getConf());
        }
    }

    private <T> BinaryStreamFormat<T> validateStream(DataFormat<T> format) throws IOException {
        assert format != null;
        if ((format instanceof BinaryStreamFormat<?>) == false) {
            throw new IOException(MessageFormat.format(
                    "{2} must be a subtype of {1} (path={0})",
                    profile.getContextPath(),
                    BinaryStreamFormat.class.getName(),
                    format.getClass().getName()));
        }
        return (BinaryStreamFormat<T>) format;
    }

    @Override
    public List<ResourceInfo> list(
            String basePath,
            ResourcePattern resourcePattern,
            Counter counter) throws IOException, InterruptedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Start listing files (id={0}, path={1}, resource={2})",
                    profile.getId(),
                    basePath,
                    resourcePattern));
        }
        FilePattern pattern = validate(resourcePattern);
        HadoopDataSourceProfile p = profile;
        FileSystem fs = p.getFileSystem();
        Path root = p.getFileSystemPath();
        Path base = append(root, basePath);
        Path temporary = p.getTemporaryFileSystemPath();
        List<FileStatus> stats = HadoopDataSourceUtil.search(fs, base, pattern);
        stats = normalize(stats, root, temporary);

        List<ResourceInfo> results = new ArrayList<ResourceInfo>();
        for (FileStatus stat : stats) {
            counter.add(1);
            ResourceInfo resource = new ResourceInfo(
                    profile.getId(),
                    stat.getPath().toString(),
                    FileSystemCompatibility.isDirectory(stat));
            results.add(resource);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Finish listing files (id={0}, path={1}, resource={2}, count={3})",
                    profile.getId(),
                    basePath,
                    resourcePattern,
                    results.size()));
        }
        return results;
    }

    @Override
    public boolean delete(
            String basePath,
            ResourcePattern resourcePattern,
            boolean recursive,
            Counter counter) throws IOException, InterruptedException {
        assert basePath.startsWith("/") == false;
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Start deleting files (id={0}, path={1}, resource={2}, recursive={3})",
                    profile.getId(),
                    basePath,
                    resourcePattern,
                    recursive));
        }
        FilePattern pattern = validate(resourcePattern);
        HadoopDataSourceProfile p = profile;
        FileSystem fs = p.getFileSystem();
        Path root = p.getFileSystemPath();
        Path base = append(root, basePath);
        List<FileStatus> stats = HadoopDataSourceUtil.search(fs, base, pattern);
        Path temporary = p.getTemporaryFileSystemPath();
        stats = normalize(stats, root, temporary);
        if (recursive) {
            stats = HadoopDataSourceUtil.onlyMinimalCovered(stats);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Process deleting files (id={0}, path={1}, resource={2}, files={3})",
                    profile.getId(),
                    basePath,
                    resourcePattern,
                    stats.size()));
        }
        boolean succeed = true;
        for (FileStatus stat : stats) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(MessageFormat.format(
                        "Deleting file (id={0}, path={1}, recursive={2})",
                        profile.getId(),
                        stat.getPath(),
                        recursive));
            }
            if (recursive == false && FileSystemCompatibility.isDirectory(stat)) {
                LOG.info(MessageFormat.format(
                        "Skip deleting directory (id={0}, path={1})",
                        profile.getId(),
                        stat.getPath()));
            } else {
                counter.add(1);
                succeed &= fs.delete(stat.getPath(), recursive);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Finish deleting files (id={0}, path={1}, resource={2}, files={3})",
                    profile.getId(),
                    basePath,
                    resourcePattern,
                    stats.size()));
        }
        return succeed;
    }

    private List<FileStatus> normalize(List<FileStatus> stats, Path root, Path temporary) {
        assert stats != null;
        assert root != null;
        assert temporary != null;
        List<FileStatus> results = new ArrayList<FileStatus>();
        for (FileStatus stat : stats) {
            if (root.equals(stat.getPath()) == false && isIn(stat, temporary) == false) {
                results.add(stat);
            }
        }
        return results;
    }

    private Path append(Path parent, String child) {
        assert parent != null;
        assert child != null;
        return child.isEmpty() ? parent : new Path(parent, child);
    }

    @Override
    public void setupAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException {
        if (profile.isOutputStreaming() == false && isLocalAttemptOutput() == false) {
            LOG.warn(MessageFormat.format(
                    "Streaming output is disabled but the local temporary directory ({1}) is not defined (id={0})",
                    profile.getId(),
                    HadoopDataSourceUtil.KEY_LOCAL_TEMPDIR));
        }
        if (isLocalAttemptOutput()) {
            FileSystem fs = profile.getLocalFileSystem();
            Path attempt = getLocalAttemptOutput(context);
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Create local attempt area (id={0}, path={1})",
                        profile.getId(),
                        attempt));
            }
            fs.mkdirs(attempt);
        } else {
            FileSystem fs = profile.getFileSystem();
            Path attempt = getAttemptOutput(context);
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Create attempt area (id={0}, path={1})",
                        profile.getId(),
                        attempt));
            }
            fs.mkdirs(attempt);
        }
    }

    @Override
    public void commitAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException {
        Path target;
        if (profile.isOutputStaging()) {
            target = getStagingOutput(context.getTransactionContext());
        } else {
            target = profile.getFileSystemPath();
        }
        if (isLocalAttemptOutput()) {
            Path attempt = getLocalAttemptOutput(context);
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Commit local attempt area (id={0}, path={1}, staging={2})",
                        profile.getId(),
                        attempt,
                        profile.isOutputStaging()));
            }
            HadoopDataSourceUtil.moveFromLocal(
                    context.getCounter(), profile.getLocalFileSystem(), profile.getFileSystem(), attempt, target);
        } else {
            Path attempt = getAttemptOutput(context);
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Commit attempt area (id={0}, path={1}, staging={2})",
                        profile.getId(),
                        attempt,
                        profile.isOutputStaging()));
            }
            HadoopDataSourceUtil.move(context.getCounter(), profile.getFileSystem(), attempt, target);
        }
    }

    @Override
    public void cleanupAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException {
        if (isLocalAttemptOutput()) {
            Path attempt = getLocalAttemptOutput(context);
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Delete local attempt area (id={0}, path={1})",
                        profile.getId(),
                        attempt));
            }
            FileSystem fs = profile.getLocalFileSystem();
            fs.delete(attempt, true);
        } else {
            Path attempt = getAttemptOutput(context);
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Delete attempt area (id={0}, path={1})",
                        profile.getId(),
                        attempt));
            }
            FileSystem fs = profile.getFileSystem();
            fs.delete(attempt, true);
        }
    }

    @Override
    public void setupTransactionOutput(OutputTransactionContext context) throws IOException, InterruptedException {
        if (profile.isOutputStaging()) {
            FileSystem fs = profile.getFileSystem();
            Path staging = getStagingOutput(context);
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Create staging area (id={0}, path={1})",
                        profile.getId(),
                        staging));
            }
            fs.mkdirs(staging);
        }
    }

    @Override
    public void commitTransactionOutput(OutputTransactionContext context) throws IOException, InterruptedException {
        if (profile.isOutputStaging()) {
            FileSystem fs = profile.getFileSystem();
            Path staging = getStagingOutput(context);
            Path target = profile.getFileSystemPath();
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Commit staging area (id={0}, path={1})",
                        profile.getId(),
                        staging));
            }
            HadoopDataSourceUtil.move(context.getCounter(), fs, staging, target);
        }
    }

    @Override
    public void cleanupTransactionOutput(OutputTransactionContext context) throws IOException, InterruptedException {
        FileSystem fs = profile.getFileSystem();
        Path path = getTemporaryOutput(context);
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Delete temporary area (id={0}, path={1})",
                    profile.getId(),
                    path));
        }
        try {
            if (fs.delete(path, true) == false) {
                LOG.warn(MessageFormat.format(
                        "Failed to delete temporary area (id={0}, path={0})",
                        profile.getId(),
                        path));
            }
        } catch (FileNotFoundException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Temporary area is not found (may be not used): {0}",
                        path));
            }
        }
    }

    private Path getTemporaryOutput(OutputTransactionContext context) {
        assert context != null;
        Path tempRoot = profile.getTemporaryFileSystemPath();
        String suffix = String.format("%s-%s",
                context.getTransactionId(),
                context.getOutputId());
        return append(tempRoot, suffix);
    }

    Path getStagingOutput(OutputTransactionContext context) {
        assert context != null;
        Path tempPath = getTemporaryOutput(context);
        String suffix = STAGING_AREA;
        return append(tempPath, suffix);
    }

    Path getAttemptOutput(OutputAttemptContext context) {
        assert context != null;
        Path tempPath = getTemporaryOutput(context.getTransactionContext());
        String suffix = String.format("%s/%s",
                ATTEMPT_AREA,
                context.getAttemptId());
        return append(tempPath, suffix);
    }

    Path getLocalAttemptOutput(OutputAttemptContext context) throws IOException {
        assert context != null;
        Path tempPath = HadoopDataSourceUtil.getLocalTemporaryDirectory(profile.getLocalFileSystem());
        String suffix = String.format("%s-%s-%s",
                context.getTransactionId(),
                context.getAttemptId(),
                context.getOutputId());
        return append(tempPath, suffix);
    }
}
