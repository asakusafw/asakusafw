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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.directio.DirectDataSourceProfile;

/**
 * A structured profile for {@link HadoopDataSource}.
 * @since 0.2.5
 */
public class HadoopDataSourceProfile {

    // fs.staging

    /**
     * The property key name for {@link #getFileSystemPath()}.
     * Default is {@link FileSystem#getWorkingDirectory()}.
     */
    public static final String KEY_PATH = "fs.path";

    /**
     * The property key name for {@link #getTemporaryFileSystemPath()}.
     */
    public static final String KEY_TEMP = "fs.tempdir";

    /**
     * The property key name for {@link #getMinimumFragmentSize(BinaryStreamFormat)}.
     */
    public static final String KEY_MIN_FRAGMENT = "fragment.min";

    /**
     * The property key name for {@link #getPreferredFragmentSize(BinaryStreamFormat)}.
     */
    public static final String KEY_PREF_FRAGMENT = "fragment.pref";

    private static final String DEFAULT_TEMP_SUFFIX = "_directio_temp";

    private static final long DEFAULT_MIN_FRAGMENT = 16 * 1024 * 1024;

    private static final long DEFAULT_PREF_FRAGMENT = 64 * 1024 * 1024;

    private final String contextPath;

    private final FileSystem fileSystem;

    private final Path fileSystemPath;

    private final Path temporaryPath;

    private long minimumFragmentSize = DEFAULT_MIN_FRAGMENT;

    private long preferredFragmentSize = DEFAULT_PREF_FRAGMENT;

    private final String id;

    /**
     * Creates a new instance.
     * @param id the ID of this datasource
     * @param contextPath the logical context path
     * @param fileSystem the file system object
     * @param fileSystemPath the mapping target path
     * @param temporaryPath the temporary root path
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public HadoopDataSourceProfile(
            String id,
            String contextPath,
            FileSystem fileSystem,
            Path fileSystemPath,
            Path temporaryPath) {
        this.id = id;
        this.contextPath = contextPath;
        this.fileSystem = fileSystem;
        this.fileSystemPath = fileSystemPath;
        this.temporaryPath = temporaryPath;
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
     * Returns the minimum fragment size.
     * @param format target format
     * @return the minimum fragment size, or {@code < 0} if fragmentation is restricted
     * @throws IOException if failed to compute size by I/O error
     * @throws InterruptedException if interrupted
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public long getMinimumFragmentSize(BinaryStreamFormat<?> format) throws IOException, InterruptedException {
        if (format == null) {
            throw new IllegalArgumentException("format must not be null"); //$NON-NLS-1$
        }
        long formatMin = format.getMinimumFragmentSize();
        long totalMin = Math.min(formatMin, minimumFragmentSize);
        if (totalMin <= 0) {
            return -1;
        }
        return totalMin;
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
    public long getPreferredFragmentSize(BinaryStreamFormat<?> format) throws IOException, InterruptedException {
        if (format == null) {
            throw new IllegalArgumentException("format must not be null"); //$NON-NLS-1$
        }
        long min = getMinimumFragmentSize(format);
        if (min <= 0) {
            return -1;
        }
        long formatPref = format.getPreferredFragmentSize();
        if (formatPref > 0) {
            return Math.max(formatPref, min);
        }
        return Math.max(preferredFragmentSize, min);
    }

    /**
     * Configures the preferred fragment size in bytes.
     * @param size the size
     */
    public void setPreferredFragmentSize(long size) {
        this.preferredFragmentSize = Math.max(size, 1);
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
        Map<String, String> attributes = new HashMap<String, String>(profile.getAttributes());
        Path fsPath = takeFsPath(attributes, conf);
        if (fsPath == null) {
            throw new IOException(MessageFormat.format(
                    "The directio configuration \"{0} ({1})\" does not have \"{2}\"",
                    profile.getId(),
                    profile.getPath(),
                    fqn(profile, KEY_PATH)));
        }
        Path tempPath = takeTempPath(attributes, conf, fsPath);
        FileSystem fileSystem = fsPath.getFileSystem(conf);
        FileSystem tempFs = tempPath.getFileSystem(conf);
        if (fileSystem.getCanonicalServiceName().equals(tempFs.getCanonicalServiceName()) == false) {
            throw new IOException(MessageFormat.format(
                    "The directio target and temporary path must be on same file system ({0}={1} <=> {2}={3})",
                    fqn(profile, KEY_PATH),
                    fsPath,
                    fqn(profile, KEY_TEMP),
                    tempPath));
        }
        fsPath = fsPath.makeQualified(fileSystem);
        tempPath = tempPath.makeQualified(fileSystem);
        HadoopDataSourceProfile result =
            new HadoopDataSourceProfile(profile.getId(), profile.getPath(), fileSystem, fsPath, tempPath);
        long minFragment = takeMinFragment(profile, attributes, conf);
        result.setMinimumFragmentSize(minFragment);
        long prefFragment = takePrefFragment(profile, attributes, conf);
        result.setPreferredFragmentSize(prefFragment);

        if (attributes.isEmpty() == false) {
            throw new IOException(MessageFormat.format(
                    "Unknown attributes in \"{0}\": {1}",
                    profile.getId(),
                    new TreeSet<String>(attributes.keySet())));
        }
        return result;
    }

    private static Object fqn(DirectDataSourceProfile profile, String key) {
        assert profile != null;
        assert key != null;
        return MessageFormat.format(
                "{0}.{1}",
                profile.getId(),
                key);
    }

    private static Path takeFsPath(Map<String, String> attributes, Configuration conf) {
        assert conf != null;
        assert attributes != null;
        String fsPathString = attributes.remove(KEY_PATH);
        if (fsPathString != null) {
            return new Path(fsPathString);
        }
        return null;
    }

    private static Path takeTempPath(Map<String, String> attributes, Configuration conf, Path fsPath) {
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
                        fqn(profile, string)));
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IOException(MessageFormat.format(
                    "Minimum fragment size must be integer: {0}={1}",
                    fqn(profile, string),
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
                        fqn(profile, string),
                        string));
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IOException(MessageFormat.format(
                    "Preferred fragment size must be integer: {0}={1}",
                    fqn(profile, string),
                    string));
        }
    }
}
