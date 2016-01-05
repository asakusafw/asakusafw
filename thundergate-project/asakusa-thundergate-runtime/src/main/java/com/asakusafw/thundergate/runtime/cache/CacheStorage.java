/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.thundergate.runtime.cache;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.asakusafw.runtime.stage.output.TemporaryOutputFormat;

/**
 * An abstraction of ThunderGate Cache Storage.
 * @since 0.2.3
 */
public class CacheStorage implements Closeable {

    /**
     * The directory name of cache head version.
     */
    public static final String HEAD_DIRECTORY_NAME = "HEAD";

    /**
     * The directory name of cache patch.
     */
    public static final String PATCH_DIRECTORY_NAME = "PATCH";

    /**
     * The directory name of cache temporary directory.
     */
    public static final String TEMP_DIRECTORY_NAME = ".cachetmp";

    /**
     * The file name of cache metadata.
     */
    public static final String META_FILE_NAME = "cache.properties";

    /**
     * The file name prefix of cache contents.
     */
    public static final String CONTENT_FILE_PREFIX = TemporaryOutputFormat.DEFAULT_FILE_NAME + "-";

    /**
     * The file glob of cache contents.
     */
    public static final String CONTENT_FILE_GLOB = CONTENT_FILE_PREFIX + "*";

    private final FileSystem fs;

    private final Path cacheDir;

    /**
     * Creates a new instance.
     * @param configuration a configuration object
     * @param cacheDir the cache directory
     * @throws IOException if failed to initialize file system object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CacheStorage(Configuration configuration, URI cacheDir) throws IOException {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        if (cacheDir == null) {
            throw new IllegalArgumentException("cacheDir must not be null"); //$NON-NLS-1$
        }
        this.cacheDir = new Path(cacheDir);
        this.fs = this.cacheDir.getFileSystem(configuration);
    }

    /**
     * Returns the {@link FileSystem} for this cache.
     * @return {@link FileSystem} object
     */
    public FileSystem getFileSystem() {
        return fs;
    }

    /**
     * Returns the configuration for this cache.
     * @return the {@link Configuration} object
     */
    public Configuration getConfiguration() {
        return fs.getConf();
    }

    /**
     * Deletes the cache head version.
     * @throws IOException if failed to delete the cache
     */
    public void deleteHead() throws IOException {
        fs.delete(getHeadDirectory(), true);
    }

    /**
     * Deletes the cache patch version.
     * @throws IOException if failed to delete the cache
     */
    public void deletePatch() throws IOException {
        fs.delete(getPatchDirectory(), true);
    }

    /**
     * Returns the cache information for the HEAD version of target cache.
     * @return the target cache information
     * @throws IOException if failed to abtain cache information
     */
    public CacheInfo getHeadCacheInfo() throws IOException {
        return getCacheInfo(getHeadProperties());
    }

    /**
     * Returns the cache information for the PATCH version of target cache.
     * @return the target cache information
     * @throws IOException if failed to abtain cache information
     */
    public CacheInfo getPatchCacheInfo() throws IOException {
        return getCacheInfo(getPatchProperties());
    }

    private CacheInfo getCacheInfo(Path path) throws IOException {
        assert path != null;
        if (fs.exists(path) == false) {
            return null;
        }
        Properties properties = new Properties();
        FSDataInputStream in = fs.open(path);
        try {
            properties.load(in);
        } finally {
            in.close();
        }
        try {
            return CacheInfo.loadFrom(properties);
        } catch (IllegalArgumentException e) {
            throw new IOException(MessageFormat.format(
                    "Cache information was broken: {0}",
                    path), e);
        }
    }

    /**
     * Puts the cache information onto HEAD version.
     * @param info source information
     * @throws IOException if failed to deploy by I/O exception
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void putHeadCacheInfo(CacheInfo info) throws IOException {
        if (info == null) {
            throw new IllegalArgumentException("info must not be null"); //$NON-NLS-1$
        }
        putCacheInfo(info, getHeadProperties());
    }

    /**
     * Puts the cache information onto patch version.
     * @param info source information
     * @throws IOException if failed to deploy by I/O exception
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void putPatchCacheInfo(CacheInfo info) throws IOException {
        if (info == null) {
            throw new IllegalArgumentException("info must not be null"); //$NON-NLS-1$
        }
        putCacheInfo(info, getPatchProperties());
    }

    private void putCacheInfo(CacheInfo info, Path path) throws IOException {
        assert info != null;
        assert path != null;
        Properties properties = new Properties();
        info.storeTo(properties);
        FSDataOutputStream out = fs.create(path);
        try {
            properties.store(out, MessageFormat.format(
                    "Cache for {0}",
                    info.getId()));
        } finally {
            out.close();
        }
    }

    /**
     * Deletes this storage.
     * @return {@code true} if successfully deleted, otherwise {@code false}
     * @throws IOException if failed to delete by I/O exception
     */
    public boolean deleteAll() throws IOException {
        if (fs.exists(cacheDir) == false) {
            return false;
        }
        return fs.delete(cacheDir, true);
    }

    /**
     * Returns the path to the cache temporary directory.
     * @return the path
     */
    public Path getTempoaryDirectory() {
        return cacheDir;
    }

    /**
     * Returns the path to the cache HEAD directory.
     * @return the path
     */
    public Path getHeadDirectory() {
        return new Path(cacheDir, HEAD_DIRECTORY_NAME);
    }

    /**
     * Returns the path to the cache metadata of HEAD version.
     * @return the path
     */
    public Path getHeadProperties() {
        return new Path(getHeadDirectory(), META_FILE_NAME);
    }

    /**
     * Returns the path to the cache content file of HEAD version.
     * @param suffix file name suffix
     * @return the path
     */
    public Path getHeadContents(String suffix) {
        return new Path(getHeadDirectory(), CONTENT_FILE_PREFIX + suffix);
    }

    /**
     * Returns the path to the cache PATCH directory.
     * @return the path
     */
    public Path getPatchDirectory() {
        return new Path(cacheDir, PATCH_DIRECTORY_NAME);
    }

    /**
     * Returns the path to the cache metadata of PATCH version.
     * @return the path
     */
    public Path getPatchProperties() {
        return new Path(getPatchDirectory(), META_FILE_NAME);
    }

    /**
     * Returns the path to the cache content file of PATCH version.
     * @param suffix file name suffix
     * @return the path
     */
    public Path getPatchContents(String suffix) {
        if (suffix == null) {
            throw new IllegalArgumentException("suffix must not be null"); //$NON-NLS-1$
        }
        return new Path(getPatchDirectory(), CONTENT_FILE_PREFIX + suffix);
    }

    @Override
    public void close() throws IOException {
        return;
    }
}
