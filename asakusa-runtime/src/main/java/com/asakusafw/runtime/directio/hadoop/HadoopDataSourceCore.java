/**
 * Copyright 2012 Asakusa Framework Team.
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
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.OutputAttemptContext;
import com.asakusafw.runtime.directio.OutputTransactionContext;
import com.asakusafw.runtime.directio.SearchPattern;
import com.asakusafw.runtime.directio.util.CountInputStream;
import com.asakusafw.runtime.directio.util.CountOutputStream;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * An implementation of {@link DirectDataSource} using {@link FileSystem}.
 * @since 0.2.5
 */
class HadoopDataSourceCore implements DirectDataSource {

    static final Log LOG = LogFactory.getLog(HadoopDataSourceCore.class);

    private static final String ATTEMPT_AREA = "attempts";

    private static final String STAGING_AREA = "staging";

    private final HadoopDataSourceProfile profile;

    HadoopDataSourceCore(HadoopDataSourceProfile profile) {
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
            SearchPattern resourcePattern) throws IOException, InterruptedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Start finding input (id={0}, path={1}, resourcePattern={2})",
                    profile.getId(),
                    basePath,
                    resourcePattern));
        }
        BinaryStreamFormat<T> sformat = validate(format);
        HadoopDataSourceProfile p = profile;
        FileSystem fs = p.getFileSystem();
        Path root = p.getFileSystemPath();
        Path base = append(root, basePath);
        List<FileStatus> stats = HadoopDataSourceUtil.search(fs, base, resourcePattern);
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
            if (stat.isDir() == false) {
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
        BinaryStreamFormat<T> sformat = validate(format);
        FileSystem fs = profile.getFileSystem();
        FSDataInputStream stream = fs.open(new Path(fragment.getPath()));
        boolean succeed = false;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Process opening input [stream opened] (id={0}, path={1}, offset={2}, size={3})",
                        profile.getId(),
                        fragment.getPath(),
                        fragment.getOffset(),
                        fragment.getSize()));
            }
            long offset = fragment.getOffset();
            if (offset != 0) {
                stream.seek(offset);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Process opening input [sought to offset] (id={0}, path={1}, offset={2}, size={3})",
                            profile.getId(),
                            fragment.getPath(),
                            fragment.getOffset(),
                            fragment.getSize()));
                }
            }
            CountInputStream cstream;
            if (LOG.isDebugEnabled()) {
                final HadoopDataSourceProfile p = profile;
                final DirectInputFragment f = fragment;
                cstream = new CountInputStream(stream, counter) {
                    @Override
                    public void close() throws IOException {
                        LOG.debug(MessageFormat.format(
                                "Start closing input (id={0}, path={1}, offset={2}, size={3})",
                                p.getId(),
                                f.getPath(),
                                f.getOffset(),
                                f.getSize()));
                        super.close();
                        LOG.debug(MessageFormat.format(
                                "Finish closing input (id={0}, path={1}, offset={2}, size={3})",
                                p.getId(),
                                f.getPath(),
                                f.getOffset(),
                                f.getSize()));
                    }
                };
            } else {
                cstream = new CountInputStream(stream, counter);
            }
            ModelInput<T> input =
                sformat.createInput(dataType, fragment.getPath(), cstream, offset, fragment.getSize());
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Finish opening input (id={0}, path={1}, offset={2}, size={3})",
                        profile.getId(),
                        fragment.getPath(),
                        fragment.getOffset(),
                        fragment.getSize()));
            }
            succeed = true;
            return input;
        } finally {
            if (succeed == false) {
                try {
                    stream.close();
                } catch (IOException e) {
                    LOG.warn(MessageFormat.format(
                            "Failed to close input (path={1}, offset={2}, size={3})",
                            fragment.getPath(),
                            fragment.getOffset(),
                            fragment.getSize()), e);
                }
            }
        }
    }

    @Override
    public <T> ModelOutput<T> openOutput(
            OutputAttemptContext context,
            Class<? extends T> dataType,
            DataFormat<T> format,
            String basePath,
            String resourcePath,
            Counter counter) throws IOException, InterruptedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Start opening output (id={0}, path={1}, resource={2})",
                    profile.getId(),
                    basePath,
                    resourcePath));
        }
        BinaryStreamFormat<T> sformat = validate(format);
        FileSystem fs = profile.getFileSystem();
        Path attempt = getAttemptOutput(context);
        Path file = append(append(attempt, basePath), resourcePath);

        FSDataOutputStream stream = fs.create(file);
        boolean succeed = false;
        try {
            CountOutputStream cstream = new CountOutputStream(stream, counter);
            if (LOG.isDebugEnabled()) {
                final HadoopDataSourceProfile p = profile;
                final Path f = file;
                cstream = new CountOutputStream(stream, counter) {
                    @Override
                    public void close() throws IOException {
                        LOG.debug(MessageFormat.format(
                                "Start closing output (id={0}, file={1})",
                                p.getId(),
                                f));
                        super.close();
                        LOG.debug(MessageFormat.format(
                                "Finish closing output (id={0}, file={1})",
                                p.getId(),
                                f));
                    }
                };
            } else {
                cstream = new CountOutputStream(stream, counter);
            }
            ModelOutput<T> output = sformat.createOutput(dataType, attempt.toString(), cstream);
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Finish opening output (id={0}, path={1}, resource={2}, file={3})",
                        profile.getId(),
                        basePath,
                        resourcePath,
                        file));
            }
            succeed = true;
            return output;
        } finally {
            if (succeed == false) {
                try {
                    stream.close();
                } catch (IOException e) {
                    LOG.warn(MessageFormat.format(
                            "Failed to close output (path={0})",
                            attempt), e);
                }
            }
        }
    }

    private <T> BinaryStreamFormat<T> validate(DataFormat<T> format) throws IOException {
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
    public boolean delete(
            String basePath,
            SearchPattern resourcePattern) throws IOException, InterruptedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Start deleting files (id={0}, path={1}, resource={2})",
                    profile.getId(),
                    basePath,
                    resourcePattern));
        }
        HadoopDataSourceProfile p = profile;
        FileSystem fs = p.getFileSystem();
        Path root = p.getFileSystemPath();
        Path base = append(root, basePath);
        List<FileStatus> stats = HadoopDataSourceUtil.search(fs, base, resourcePattern);
        List<FileStatus> targets = HadoopDataSourceUtil.onlyMinimalCovered(stats);
        Path temporary = p.getTemporaryFileSystemPath();

        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Process deleting files (id={0}, path={1}, resource={2}, files={3})",
                    profile.getId(),
                    basePath,
                    resourcePattern,
                    stats.size()));
        }
        boolean succeed = true;
        for (FileStatus stat : targets) {
            if (isIn(stat, temporary)) {
                continue;
            }
            if (LOG.isTraceEnabled()) {
                LOG.trace(MessageFormat.format(
                        "Deleting file (id={0}, path={1})",
                        profile.getId(),
                        stat.getPath()));
            }
            succeed &= fs.delete(stat.getPath(), true);
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

    private Path append(Path parent, String child) {
        assert parent != null;
        assert child != null;
        return child.isEmpty() ? parent : new Path(parent, child);
    }

    @Override
    public void setupAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException {
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

    @Override
    public void commitAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException {
        Path attempt = getAttemptOutput(context);
        Path target;
        if (profile.isStagingRequired()) {
            target = getStagingOutput(context.getTransactionContext());
        } else {
            target = profile.getFileSystemPath();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Commit attempt area (id={0}, path={1}, staging={2})",
                    profile.getId(),
                    attempt,
                    profile.isStagingRequired()));
        }
        FileSystem fs = profile.getFileSystem();
        HadoopDataSourceUtil.move(fs, attempt, target);
    }

    @Override
    public void cleanupAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException {
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

    @Override
    public void setupTransactionOutput(OutputTransactionContext context) throws IOException, InterruptedException {
        if (profile.isStagingRequired()) {
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
        if (profile.isStagingRequired()) {
            FileSystem fs = profile.getFileSystem();
            Path staging = getStagingOutput(context);
            Path target = profile.getFileSystemPath();
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Commit staging area (id={0}, path={1})",
                        profile.getId(),
                        staging));
            }
            HadoopDataSourceUtil.move(fs, staging, target);
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
            fs.delete(path, true);
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
}
