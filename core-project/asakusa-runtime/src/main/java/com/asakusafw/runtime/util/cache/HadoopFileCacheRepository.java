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
package com.asakusafw.runtime.util.cache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.zip.Checksum;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.PureJavaCrc32;

import com.asakusafw.runtime.util.lock.LockObject;
import com.asakusafw.runtime.util.lock.LockProvider;
import com.asakusafw.runtime.util.lock.RetryObject;
import com.asakusafw.runtime.util.lock.RetryStrategy;

/**
 * Manages cache files on Hadoop file system.
 * @since 0.7.0
 */
public class HadoopFileCacheRepository implements FileCacheRepository {

    static final Log LOG = LogFactory.getLog(HadoopFileCacheRepository.class);

    static final String KEY_CHECK_BEFORE_DELETE = "com.asakusafw.cache.hadoop.deleteOnlyIfExists";

    static final boolean DEFAULT_CHECK_BEFORE_DELETE = true;

    private final Configuration configuration;

    private final Path repository;

    private final LockProvider<? super Path> lockProvider;

    private final RetryStrategy retryStrategy;

    private final boolean checkBeforeDelete;

    private final ThreadLocal<byte[]> byteBuffers = new ThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[1024];
        }
    };

    /**
     * Creates a new instance.
     * @param configuration the current configuration
     * @param repository the cache root path (must be absolute)
     * @param lockProvider the cache lock provider
     * @param retryStrategy the retry strategy
     */
    public HadoopFileCacheRepository(
            Configuration configuration,
            Path repository,
            LockProvider<? super Path> lockProvider,
            RetryStrategy retryStrategy) {
        if (repository.toUri().getScheme() == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Cache repository location must contan the scheme: {0}",
                    repository));
        }
        this.configuration = configuration;
        this.repository = repository;
        this.lockProvider = lockProvider;
        this.retryStrategy = retryStrategy;
        this.checkBeforeDelete = configuration.getBoolean(KEY_CHECK_BEFORE_DELETE, DEFAULT_CHECK_BEFORE_DELETE);
    }

    @Override
    public Path resolve(Path file) throws IOException, InterruptedException {
        FileSystem fs = file.getFileSystem(configuration);
        Path qualified = fs.makeQualified(file);
        return doResolve(qualified);
    }

    private Path doResolve(Path sourcePath) throws IOException, InterruptedException {
        assert sourcePath.isAbsolute();
        FileSystem fs = sourcePath.getFileSystem(configuration);
        if (fs.exists(sourcePath) == false) {
            throw new FileNotFoundException(sourcePath.toString());
        }
        long sourceChecksum = computeChecksum(fs, sourcePath);
        Path cachePath = computeCachePath(sourcePath);
        Path cacheChecksumPath = computeCacheChecksumPath(cachePath);

        IOException firstException = null;
        RetryObject retry = retryStrategy.newInstance(MessageFormat.format(
                "preparing cache ({0} -> {1})",
                sourcePath,
                cachePath));
        do {
            try {
                // TODO reduce lock scope?
                LockObject<? super Path> lock = lockProvider.tryLock(cachePath);
                if (lock == null) {
                    continue;
                }
                try {
                    if (isCached(cachePath, cacheChecksumPath, sourceChecksum)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(MessageFormat.format(
                                    "cache hit: {0} -> {1}",
                                    sourcePath,
                                    cachePath));
                        }
                        // just returns cached file
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(MessageFormat.format(
                                    "cache miss: {0} -> {1}",
                                    sourcePath,
                                    cachePath));
                        }
                        updateCache(sourcePath, sourceChecksum, cachePath, cacheChecksumPath);
                    }
                    return cachePath;
                } finally {
                    lock.close();
                }
            } catch (IOException e) {
                LOG.warn(MessageFormat.format(
                        "Failed to prepare cache: {0} -> {1}",
                        sourcePath,
                        cachePath), e);
                if (firstException == null) {
                    firstException = e;
                }
            }
        } while (retry.waitForNextAttempt());
        if (firstException == null) {
            throw new IOException(MessageFormat.format(
                    "Failed to acquire a lock for remote cache file: {0} ({1})",
                    sourcePath,
                    cachePath));
        }
        throw firstException;
    }

    private long computeChecksum(FileSystem fs, Path file) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Computing checksum: {0}",
                    file));
        }
        Checksum checksum = new PureJavaCrc32();
        byte[] buf = byteBuffers.get();
        FSDataInputStream input = fs.open(file);
        try {
            while (true) {
                int read = input.read(buf);
                if (read < 0) {
                    break;
                }
                checksum.update(buf, 0, read);
            }
        } finally {
            input.close();
        }
        return checksum.getValue();
    }

    private Path computeCachePath(Path file) {
        assert repository != null;
        String directoryName;
        Path parent = file.getParent();
        if (parent == null) {
            directoryName = String.format("%08x", 0);
        } else {
            directoryName = String.format("%08x", parent.toString().hashCode());
        }
        Path directory = new Path(repository, directoryName);
        Path target = new Path(directory, file.getName());
        return target;
    }

    private Path computeCacheChecksumPath(Path cachePath) {
        Path parent = cachePath.getParent();
        String name = String.format("%s.acrc", cachePath.getName());
        return new Path(parent, name);
    }

    private boolean isCached(Path cacheFilePath, Path cacheChecksumPath, long checksum) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "checking remote cache: {0}",
                    cacheFilePath));
        }
        FileSystem fs = cacheChecksumPath.getFileSystem(configuration);
        if (fs.exists(cacheChecksumPath) == false || fs.exists(cacheFilePath) == false) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "remote cache is not found: {0}",
                        cacheFilePath));
            }
            return false;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "reading remote cache checksum: {0}",
                        cacheFilePath));
            }
            long other;
            FSDataInputStream input = fs.open(cacheChecksumPath);
            try {
                other = input.readLong();
            } finally {
                input.close();
            }
            return checksum == other;
        }
    }

    private void updateCache(Path file, long checksum, Path cachePath, Path cacheChecksumPath) throws IOException {
        if (LOG.isInfoEnabled()) {
            LOG.info(MessageFormat.format(
                    "updating library cache: {0} -> {1}",
                    file,
                    cachePath));
        }

        FileSystem sourceFs = file.getFileSystem(configuration);
        FileSystem cacheFs = cachePath.getFileSystem(configuration);

        // remove checksum file -> cachePath
        delete(cacheFs, cacheChecksumPath);
        delete(cacheFs, cachePath);

        // sync source file to cache file
        FSDataOutputStream checksumOutput = cacheFs.create(cacheChecksumPath, false);
        try {
            checksumOutput.writeLong(checksum);
            syncFile(sourceFs, file, cacheFs, cachePath);
        } finally {
            checksumOutput.close();
        }
    }

    private void delete(FileSystem fs, Path path) throws IOException {
        if (checkBeforeDelete && fs.exists(path) == false) {
            return;
        }
        fs.delete(path, false);
    }

    private void syncFile(
            FileSystem sourceFs, Path sourceFile,
            FileSystem targetFs, Path targetFile) throws IOException {
        byte[] buf = byteBuffers.get();
        FSDataOutputStream output = targetFs.create(targetFile, false);
        try {
            FSDataInputStream input = sourceFs.open(sourceFile);
            try {
                while (true) {
                    int read = input.read(buf);
                    if (read < 0) {
                        break;
                    }
                    output.write(buf, 0, read);
                }
            } finally {
                input.close();
            }
        } finally {
            output.close();
        }
    }
}
