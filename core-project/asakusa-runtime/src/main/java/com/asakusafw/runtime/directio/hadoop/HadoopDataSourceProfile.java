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
package com.asakusafw.runtime.directio.hadoop;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;

import com.asakusafw.runtime.directio.DirectDataSourceProfile;
import com.asakusafw.runtime.directio.FragmentableDataFormat;

/**
 * A structured profile for {@link HadoopDataSource}.
 * @since 0.2.5
 * @version 0.9.0
 */
public class HadoopDataSourceProfile {

    static final Log LOG = LogFactory.getLog(HadoopDataSourceProfile.class);

    // fs.staging

    private static final String ROOT_REPRESENTATION = "/"; //$NON-NLS-1$

    /**
     * The property key name for {@link #getFileSystemPath()}.
     * Default is {@link FileSystem#getWorkingDirectory()}.
     */
    public static final String KEY_PATH = "fs.path"; //$NON-NLS-1$

    /**
     * The property key name for {@link #getTemporaryFileSystemPath()}.
     */
    public static final String KEY_TEMP = "fs.tempdir"; //$NON-NLS-1$

    /**
     * The property key name for {@link #isOutputStaging()}.
     */
    public static final String KEY_OUTPUT_STAGING = "output.staging"; //$NON-NLS-1$

    /**
     * The property key name for {@link #isOutputStreaming()}.
     */
    public static final String KEY_OUTPUT_STREAMING = "output.streaming"; //$NON-NLS-1$

    /**
     * The property key name for {@link #getMinimumFragmentSize(FragmentableDataFormat)}.
     */
    public static final String KEY_MIN_FRAGMENT = "fragment.min"; //$NON-NLS-1$

    /**
     * The property key name for {@link #getPreferredFragmentSize(FragmentableDataFormat)}.
     */
    public static final String KEY_PREF_FRAGMENT = "fragment.pref"; //$NON-NLS-1$

    /**
     * The property key name for {@link #isSplitBlocks()}.
     */
    public static final String KEY_SPLIT_BLOCKS = "block.split"; //$NON-NLS-1$

    /**
     * The property key name for {@link #isCombineBlocks()}.
     */
    public static final String KEY_COMBINE_BLOCKS = "block.combine"; //$NON-NLS-1$

    /**
     * The property key name for {@link #getKeepAliveInterval()}.
     * @since 0.2.6
     */
    public static final String KEY_KEEPALIVE_INTERVAL = "keepalive.interval"; //$NON-NLS-1$

    /**
     * The property key name of number of threads for moving files in roll-forward operation.
     * @since 0.9.0
     */
    public static final String KEY_ROLLFORWARD_THREADS = "threads.commit"; //$NON-NLS-1$

    private static final String DEFAULT_TEMP_SUFFIX = "_directio_temp"; //$NON-NLS-1$

    private static final boolean DEFAULT_OUTPUT_STAGING = true;

    private static final boolean DEFAULT_OUTPUT_STREAMING = true;

    private static final long DEFAULT_MIN_FRAGMENT = 16 * 1024 * 1024;

    private static final long DEFAULT_PREF_FRAGMENT = 64 * 1024 * 1024;

    private static final boolean DEFAULT_SPLIT_BLOCKS = true;

    private static final boolean DEFAULT_COMBINE_BLOCKS = true;

    private static final long DEFAULT_KEEPALIVE_INTERVAL = 0;

    private static final int DEFAULT_ROLLFORWARD_THREADS = 1;

    private final String id;

    private final String contextPath;

    private final Path fileSystemPath;

    private final Path temporaryPath;

    private boolean outputStaging = DEFAULT_OUTPUT_STAGING;

    private boolean outputStreaming = DEFAULT_OUTPUT_STREAMING;

    private long minimumFragmentSize = DEFAULT_MIN_FRAGMENT;

    private long preferredFragmentSize = DEFAULT_PREF_FRAGMENT;

    private boolean splitBlocks = DEFAULT_SPLIT_BLOCKS;

    private boolean combineBlocks = DEFAULT_COMBINE_BLOCKS;

    private long keepAliveInterval = DEFAULT_KEEPALIVE_INTERVAL;

    private int rollforwardThreads = DEFAULT_ROLLFORWARD_THREADS;

    private final FileSystem fileSystem;

    private final LocalFileSystem localFileSystem;

    /**
     * the Hadoop configuration key of whether or not use the <em>minimum</em> value between
     * {@link #getMinimumFragmentSize()} and {@link FragmentableDataFormat#getMinimumFragmentSize()}.
     * After the <a href="https://github.com/asakusafw/asakusafw/pull/835"> issue </a> was fixed,
     * we use the <em>maximum</em> value of them.
     * @since 0.10.3
     * @see <a href="https://github.com/asakusafw/asakusafw/pull/835"> GitHub issue page </a>
     */
    public static final String KEY_LEGACY_FRAGMENT_MIN = HadoopDataSourceUtil.PREFIX + "COMPAT835"; //$NON-NLS-1$

    private final boolean legacyFragmentMin;

    /**
     * Creates a new instance.
     * @param conf the current configuration
     * @param id the ID of this datasource
     * @param contextPath the logical context path
     * @param fileSystemPath the mapping target path
     * @param temporaryPath the temporary root path
     * @throws IOException if failed to create profile
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public HadoopDataSourceProfile(
            Configuration conf,
            String id,
            String contextPath,
            Path fileSystemPath,
            Path temporaryPath) throws IOException {
        this.id = id;
        this.contextPath = contextPath;
        this.fileSystemPath = fileSystemPath;
        this.temporaryPath = temporaryPath;
        this.fileSystem = fileSystemPath.getFileSystem(conf);
        this.localFileSystem = FileSystem.getLocal(conf);
        this.legacyFragmentMin = conf.getBoolean(KEY_LEGACY_FRAGMENT_MIN, false);
    }

    /**
     * Return the ID of this datasource.
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the logical context path.
     * @return the logical context path
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Returns the mapping target path.
     * @return the mapping target path
     */
    public Path getFileSystemPath() {
        return fileSystemPath;
    }

    /**
     * Returns the temporary root path.
     * @return the temporary root path
     */
    public Path getTemporaryFileSystemPath() {
        return temporaryPath;
    }

    /**
     * Returns the file system for the this datastore.
     * @return the file system object
     */
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    /**
     * Returns the local file system for the this datastore.
     * @return the local file system object
     */
    public LocalFileSystem getLocalFileSystem() {
        return localFileSystem;
    }

    /**
     * Returns the minimum fragment size.
     * @param format target format
     * @return the minimum fragment size, or {@code < 0} if fragmentation is restricted
     * @throws IOException if failed to compute size by I/O error
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public long getMinimumFragmentSize(FragmentableDataFormat<?> format) throws IOException, InterruptedException {
        if (format == null) {
            throw new IllegalArgumentException("format must not be null"); //$NON-NLS-1$
        }
        long formatMin = format.getMinimumFragmentSize();
        if (formatMin < 0 || minimumFragmentSize < 0) {
            return -1;
        }
        if (!legacyFragmentMin) {
            return Math.max(formatMin, minimumFragmentSize);
        } else {
            return Math.min(formatMin, minimumFragmentSize);
        }
    }

    /**
     * Returns the minimum fragment size.
     * @return the minimum fragment size, or {@code < 0} if fragmentation is restricted
     */
    public long getMinimumFragmentSize() {
        return minimumFragmentSize < 0 ? -1 : minimumFragmentSize;
    }

    /**
     * Configures the minimum fragment size in bytes.
     * @param size the size, or {@code <= 0} to restrict fragmentation
     */
    public void setMinimumFragmentSize(long size) {
        if (size <= 0) {
            this.minimumFragmentSize = -1;
        }
        this.minimumFragmentSize = size;
    }

    /**
     * Returns the preferred fragment size.
     * @param format target format
     * @return the preferred fragment size
     * @throws IOException if failed to compute size by I/O error
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public long getPreferredFragmentSize(FragmentableDataFormat<?> format) throws IOException, InterruptedException {
        if (format == null) {
            throw new IllegalArgumentException("format must not be null"); //$NON-NLS-1$
        }
        long min = getMinimumFragmentSize(format);
        if (min < 0) {
            return -1;
        }
        long formatPref = format.getPreferredFragmentSize();
        if (formatPref > 0) {
            return Math.max(formatPref, min);
        }
        return Math.max(preferredFragmentSize, min);
    }

    /**
     * Returns the preferred fragment size.
     * @return the preferred fragment size
     */
    public long getPreferredFragmentSize() {
        long min = getMinimumFragmentSize();
        if (min < 0) {
            return -1;
        }
        return preferredFragmentSize <= 0 ? -1 : preferredFragmentSize;
    }

    /**
     * Configures the preferred fragment size in bytes.
     * @param size the size
     */
    public void setPreferredFragmentSize(long size) {
        this.preferredFragmentSize = Math.max(size, 1);
    }

    /**
     * Returns whether split DFS block into multiple splits for optimization.
     * @return the {@code true} to split, otherwise {@code false}
     */
    public boolean isSplitBlocks() {
        return splitBlocks;
    }

    /**
     * Sets whether splits blocks for optimization.
     * @param split {@code true} to split, otherwise {@code false}
     */
    public void setSplitBlocks(boolean split) {
        this.splitBlocks = split;
    }

    /**
     * Returns whether combines multiple blocks into a fragment for optimization.
     * @return the {@code true} to combine, otherwise {@code false}
     */
    public boolean isCombineBlocks() {
        return combineBlocks;
    }

    /**
     * Sets whether combines blocks for optimization.
     * @param combine {@code true} to combine, otherwise {@code false}
     */
    public void setCombineBlocks(boolean combine) {
        this.combineBlocks = combine;
    }

    /**
     * Returns whether output staging is required.
     * @return {@code true} to required, otherwise {@code false}.
     */
    public boolean isOutputStaging() {
        return outputStaging;
    }

    /**
     * Sets whether output staging is required.
     * @param required {@code true} to required, otherwise {@code false}
     */
    public void setOutputStaging(boolean required) {
        this.outputStaging = required;
    }

    /**
     * Returns whether output streaming is required.
     * @return {@code true} to required, otherwise {@code false}.
     */
    public boolean isOutputStreaming() {
        return outputStreaming;
    }

    /**
     * Sets whether output streaming is required.
     * @param required {@code true} to required, otherwise {@code false}
     */
    public void setOutputStreaming(boolean required) {
        this.outputStreaming = required;
    }

    /**
     * Returns keep-alive interval.
     * @return keep-alive interval in ms, or {@code 0} if keep-alive is disabled
     * @since 0.2.6
     */
    public long getKeepAliveInterval() {
        return keepAliveInterval;
    }

    /**
     * Sets keep-alive interval.
     * @param interval keep-alive interval in ms, or {@code 0} to disable keep-alive
     * @since 0.2.6
     */
    public void setKeepAliveInterval(long interval) {
        this.keepAliveInterval = interval;
    }

    /**
     * Returns the number of threads to move staged files to committed area.
     * @return the number of threads
     * @since 0.9.0
     */
    public int getRollforwardThreads() {
        return rollforwardThreads;
    }

    /**
     * Sets the number of threads to move staged files to committed area.
     * @param threads the number of threads
     * @since 0.9.0
     */
    public void setRollforwardThreads(int threads) {
        this.rollforwardThreads = threads;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HadoopDataSourceProfile [id="); //$NON-NLS-1$
        builder.append(id);
        builder.append(", contextPath="); //$NON-NLS-1$
        builder.append(contextPath);
        builder.append(", fileSystemPath="); //$NON-NLS-1$
        builder.append(fileSystemPath);
        builder.append(", temporaryPath="); //$NON-NLS-1$
        builder.append(temporaryPath);
        builder.append(", outputStaging="); //$NON-NLS-1$
        builder.append(outputStaging);
        builder.append(", outputStreaming="); //$NON-NLS-1$
        builder.append(outputStreaming);
        builder.append(", minimumFragmentSize="); //$NON-NLS-1$
        builder.append(minimumFragmentSize);
        builder.append(", preferredFragmentSize="); //$NON-NLS-1$
        builder.append(preferredFragmentSize);
        builder.append(", splitBlocks="); //$NON-NLS-1$
        builder.append(splitBlocks);
        builder.append(", combineBlocks="); //$NON-NLS-1$
        builder.append(combineBlocks);
        builder.append(", keepAliveInterval="); //$NON-NLS-1$
        builder.append(keepAliveInterval);
        builder.append(", rollforwardThreads="); //$NON-NLS-1$
        builder.append(rollforwardThreads);
        builder.append(", fileSystem="); //$NON-NLS-1$
        builder.append(fileSystem);
        builder.append(", localFileSystem="); //$NON-NLS-1$
        builder.append(localFileSystem);
        builder.append("]"); //$NON-NLS-1$
        return builder.toString();
    }

    /**
     * Converts the {@link DirectDataSourceProfile} into this profile.
     * @param profile target profile
     * @param conf Hadoop configuration
     * @return the converted profile
     * @throws IOException if failed to convert
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static HadoopDataSourceProfile convert(
            DirectDataSourceProfile profile,
            Configuration conf) throws IOException {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        Map<String, String> attributes = new HashMap<>(profile.getAttributes());
        Path fsPath = takeFsPath(profile, attributes, conf);
        if (fsPath == null) {
            throw new IOException(MessageFormat.format(
                    "The directio configuration \"{0} ({1})\" does not have \"{2}\"",
                    profile.getId(),
                    profile.getPath().isEmpty() ? ROOT_REPRESENTATION : profile.getPath(),
                    fqn(profile, KEY_PATH)));
        }
        Path tempPath = takeTempPath(profile, attributes, conf, fsPath);
        FileSystem fileSystem = fsPath.getFileSystem(conf);
        FileSystem tempFs = tempPath.getFileSystem(conf);
        if (getFsIdentity(fileSystem).equals(getFsIdentity(tempFs)) == false) {
            throw new IOException(MessageFormat.format(
                    "The directio target and temporary path must be on same file system ({0}={1} <=> {2}={3})",
                    fqn(profile, KEY_PATH),
                    fsPath,
                    fqn(profile, KEY_TEMP),
                    tempPath));
        }
        fsPath = fileSystem.makeQualified(fsPath);
        tempPath = fileSystem.makeQualified(tempPath);
        HadoopDataSourceProfile result = new HadoopDataSourceProfile(
                conf,
                profile.getId(), profile.getPath(),
                fsPath, tempPath);
        long minFragment = takeMinFragment(profile, attributes, conf);
        result.setMinimumFragmentSize(minFragment);
        long prefFragment = takePrefFragment(profile, attributes, conf);
        result.setPreferredFragmentSize(prefFragment);
        result.setOutputStaging(takeBoolean(profile, attributes, KEY_OUTPUT_STAGING, DEFAULT_OUTPUT_STAGING));
        result.setOutputStreaming(takeBoolean(profile, attributes, KEY_OUTPUT_STREAMING, DEFAULT_OUTPUT_STREAMING));
        result.setSplitBlocks(takeBoolean(profile, attributes, KEY_SPLIT_BLOCKS, DEFAULT_SPLIT_BLOCKS));
        result.setCombineBlocks(takeBoolean(profile, attributes, KEY_COMBINE_BLOCKS, DEFAULT_COMBINE_BLOCKS));
        result.setKeepAliveInterval(
                takePositive(profile, attributes, KEY_KEEPALIVE_INTERVAL, DEFAULT_KEEPALIVE_INTERVAL));
        result.setRollforwardThreads(
                takePositive(profile, attributes, KEY_ROLLFORWARD_THREADS, DEFAULT_ROLLFORWARD_THREADS));

        if (attributes.isEmpty() == false) {
            throw new IOException(MessageFormat.format(
                    "Unknown attributes in \"{0}\": {1}",
                    profile.getId(),
                    new TreeSet<>(attributes.keySet())));
        }
        return result;
    }

    static String getFsIdentity(FileSystem fileSystem) {
        assert fileSystem != null;
        return fileSystem.getUri().toString();
    }

    private static Object fqn(DirectDataSourceProfile profile, String key) {
        assert profile != null;
        assert key != null;
        return MessageFormat.format(
                "{0}.{1}", //$NON-NLS-1$
                profile.getId(),
                key);
    }

    private static Path takeFsPath(
            DirectDataSourceProfile profile, Map<String, String> attributes, Configuration conf) {
        assert conf != null;
        assert attributes != null;
        String fsPathString = attributes.remove(KEY_PATH);
        if (fsPathString != null) {
            return new Path(fsPathString);
        }
        return null;
    }

    private static Path takeTempPath(
            DirectDataSourceProfile profile, Map<String, String> attributes, Configuration conf, Path fsPath) {
        assert attributes != null;
        assert conf != null;
        assert fsPath != null;
        String tempPathString = attributes.remove(KEY_TEMP);
        Path tempPath;
        if (tempPathString != null) {
            tempPath = new Path(tempPathString);
        } else {
            tempPath = new Path(fsPath, DEFAULT_TEMP_SUFFIX);
        }
        return tempPath;
    }

    private static long takeMinFragment(
            DirectDataSourceProfile profile,
            Map<String, String> attributes,
            Configuration conf) throws IOException {
        assert profile != null;
        assert attributes != null;
        assert conf != null;
        String string = attributes.remove(KEY_MIN_FRAGMENT);
        if (string == null) {
            return DEFAULT_MIN_FRAGMENT;
        }
        try {
            long value = Long.parseLong(string);
            if (value == 0) {
                throw new IOException(MessageFormat.format(
                        "Minimum fragment size must not be zero: {0}",
                        fqn(profile, KEY_MIN_FRAGMENT)));
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IOException(MessageFormat.format(
                    "Minimum fragment size must be integer: {0}={1}",
                    fqn(profile, KEY_MIN_FRAGMENT),
                    string));
        }
    }

    private static long takePrefFragment(
            DirectDataSourceProfile profile,
            Map<String, String> attributes,
            Configuration conf) throws IOException {
        assert profile != null;
        assert attributes != null;
        assert conf != null;
        String string = attributes.remove(KEY_PREF_FRAGMENT);
        if (string == null) {
            return DEFAULT_PREF_FRAGMENT;
        }
        try {
            long value = Long.parseLong(string);
            if (value <= 0) {
                throw new IOException(MessageFormat.format(
                        "Preferred fragment size must be > 0: {0}={1}",
                        fqn(profile, KEY_PREF_FRAGMENT),
                        string));
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IOException(MessageFormat.format(
                    "Preferred fragment size must be integer: {0}={1}",
                    fqn(profile, KEY_PREF_FRAGMENT),
                    string));
        }
    }

    private static boolean takeBoolean(
            DirectDataSourceProfile profile,
            Map<String, String> attributes,
            String key,
            boolean defaultValue) throws IOException {
        assert profile != null;
        assert attributes != null;
        assert key != null;
        String string = attributes.remove(key);
        if (string == null) {
            return defaultValue;
        }
        if (string.equalsIgnoreCase("true")) { //$NON-NLS-1$
            return true;
        } else if (string.equalsIgnoreCase("false")) { //$NON-NLS-1$
            return false;
        } else {
            throw new IOException(MessageFormat.format(
                    "\"{0}\" must be boolean: {1}",
                    fqn(profile, key),
                    string));
        }
    }

    private static int takePositive(
            DirectDataSourceProfile profile,
            Map<String, String> attributes,
            String key,
            int defaultValue) throws IOException {
        assert profile != null;
        assert attributes != null;
        assert key != null;
        String string = attributes.remove(key);
        if (string == null) {
            return defaultValue;
        }
        try {
            int result = Integer.parseInt(string.trim());
            if (result < 0) {
                throw new IOException(MessageFormat.format(
                        "\"{0}\" must be positive integer: {1}",
                        fqn(profile, key),
                        string));
            }
            return result;
        } catch (NumberFormatException e) {
            throw new IOException(MessageFormat.format(
                    "\"{0}\" must be positive integer: {1}",
                    fqn(profile, key),
                    string), e);
        }
    }

    private static long takePositive(
            DirectDataSourceProfile profile,
            Map<String, String> attributes,
            String key,
            long defaultValue) throws IOException {
        assert profile != null;
        assert attributes != null;
        assert key != null;
        String string = attributes.remove(key);
        if (string == null) {
            return defaultValue;
        }
        try {
            long result = Integer.parseInt(string.trim());
            if (result < 0) {
                throw new IOException(MessageFormat.format(
                        "\"{0}\" must be positive integer: {1}",
                        fqn(profile, key),
                        string));
            }
            return result;
        } catch (NumberFormatException e) {
            throw new IOException(MessageFormat.format(
                    "\"{0}\" must be positive integer: {1}",
                    fqn(profile, key),
                    string), e);
        }
    }
}
