/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.resource;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;

import com.asakusafw.runtime.compatibility.JobCompatibility;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;

/**
 * ステージリソースを利用するためのドライバ。
 * @since 0.1.0
 * @version 0.7.1
 */
public class StageResourceDriver implements Closeable {

    static final Log LOG = LogFactory.getLog(StageResourceDriver.class);

    private static final String KEY_PREFIX = "com.asakusafw.stage.resource."; //$NON-NLS-1$

    private static final String PREFIX_LOCAL_CACHE_NAME = KEY_PREFIX + "local."; //$NON-NLS-1$

    private static final String PREFIX_REMOTE_PATH = KEY_PREFIX + "remote."; //$NON-NLS-1$

    private static final String KEY_SIZE = KEY_PREFIX + "size"; //$NON-NLS-1$

    private static final String KEY_ACCESS_MODE = KEY_PREFIX + "mode"; //$NON-NLS-1$

    private final Configuration configuration;

    private final FileSystem localFileSystem;

    private final AccessMode accessMode;

    /**
     * インスタンスを生成する。
     * @param configuration 設定情報
     * @throws IOException ファイルシステムの利用に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public StageResourceDriver(Configuration configuration) throws IOException {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        this.configuration = configuration;
        this.localFileSystem = FileSystem.getLocal(configuration);
        this.accessMode = AccessMode.decode(configuration.get(KEY_ACCESS_MODE));
    }

    /**
     * このオブジェクトが利用する設定情報の一覧を返す。
     * @return 設定情報の一覧
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * このドライバに登録されたリソースへのパスを返す。
     * @param resourceName リソースの名前
     * @return 対応するリソースへのパス一覧
     * @throws IOException リソースの検索に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public List<Path> findCache(String resourceName) throws IOException {
        if (resourceName == null) {
            throw new IllegalArgumentException("cacheName must not be null"); //$NON-NLS-1$
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "finding stage resource: {0} (mode={1})", //$NON-NLS-1$
                    resourceName,
                    accessMode));
        }
        switch (accessMode) {
        case DIRECT:
            return findCacheFromRemote(resourceName);
        case CACHE:
            return findCacheFromCached(resourceName);
        default:
            throw new AssertionError(accessMode);
        }
    }

    private List<Path> findCacheFromRemote(String resourceName) {
        assert resourceName != null;
        List<Path> results = new ArrayList<Path>();
        for (String remotePath : restoreStrings(getConfiguration(), getRemotePathKey(resourceName))) {
            results.add(new Path(remotePath));
        }
        return results;
    }

    private List<Path> findCacheFromCached(String resourceName) throws IOException {
        assert resourceName != null;
        List<Path> results = new ArrayList<Path>();
        for (String localName : restoreStrings(getConfiguration(), getLocalCacheNameKey(resourceName))) {
            Path resolvedPath = findLocalCache(resourceName, localName);
            if (resolvedPath == null) {
                return Collections.emptyList();
            }
            results.add(resolvedPath);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Cache file resolved: resource={0}, paths={1}", //$NON-NLS-1$
                    resourceName,
                    results));
        }
        return results;
    }

    private Path findLocalCache(String resourceName, String localName) throws IOException {
        assert localName != null;
        Path cache = new Path(localName);
        if (localFileSystem.exists(cache)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("symlink found: " + cache); //$NON-NLS-1$
            }
            return localFileSystem.makeQualified(cache);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("symlink not found: " + localName); //$NON-NLS-1$
        }
        Path directPath = findCacheForLocalMode(resourceName, localName);
        return directPath;
    }

    private Path findCacheForLocalMode(String resourceName, String localName) throws IOException {
        assert resourceName != null;
        assert localName != null;
        Path remotePath = null;
        String remoteName = null;
        for (URI uri : DistributedCache.getCacheFiles(configuration)) {
            if (localName.equals(uri.getFragment())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("fragment matched: " + uri); //$NON-NLS-1$
                }
                String rpath = uri.getPath();
                remotePath = new Path(uri);
                remoteName = rpath.substring(rpath.lastIndexOf('/') + 1);
                break;
            }
        }
        if (remoteName == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("fragment not matched: " + resourceName); //$NON-NLS-1$
            }
            return null;
        }
        assert remotePath != null;
        for (Path path : getLocalCacheFiles()) {
            String localFileName = path.getName();
            if (remoteName.equals(localFileName) == false) {
                continue;
            }
            if (localFileSystem.exists(path) == false) {
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("local path matched: " + path); //$NON-NLS-1$
            }
            return localFileSystem.makeQualified(path);
        }
        FileSystem remoteFileSystem = remotePath.getFileSystem(configuration);
        remotePath = remoteFileSystem.makeQualified(remotePath);
        if (LOG.isDebugEnabled()) {
            LOG.debug("distributed cache is not localized explicitly: " + remotePath); //$NON-NLS-1$
        }
        if (isLocal(remoteFileSystem) == false) {
            LOG.warn(MessageFormat.format(
                    "Failed to resolve stage resource in local cache \"{1}\" (resource={0})",
                    resourceName,
                    localName));
        }
        return remotePath;
    }

    private List<Path> getLocalCacheFiles() throws IOException {
        Path[] results = DistributedCache.getLocalCacheFiles(configuration);
        if (results == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(results);
        }
    }

    private boolean isLocal(FileSystem fs) {
        assert fs != null;
        if (fs == localFileSystem) {
            return true;
        }
        // TODO user getCanonicalUri() on 1.0.0
        return fs.getUri().equals(localFileSystem.getUri());
    }

    @Override
    public void close() throws IOException {
        // do not close local file system
        return;
    }

    /**
     * 指定のジョブにリソースの情報を追加する。
     * @param job 対象の情報
     * @param resourcePath リソースへのパス (for temporary storage)
     * @param resourceName リソースの名前
     * @throws IOException リソースの情報が不明であった場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static void add(Job job, String resourcePath, String resourceName) throws IOException {
        if (job == null) {
            throw new IllegalArgumentException("job must not be null"); //$NON-NLS-1$
        }
        if (resourcePath == null) {
            throw new IllegalArgumentException("resourcePath must not be null"); //$NON-NLS-1$
        }
        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName must not be null"); //$NON-NLS-1$
        }
        Configuration conf = job.getConfiguration();
        List<FileStatus> list = TemporaryStorage.listStatus(conf, new Path(resourcePath));
        if (list.isEmpty()) {
            throw new IOException(MessageFormat.format(
                    "Resource not found: {0}",
                    resourcePath));
        }
        List<String> localNames = restoreStrings(conf, getLocalCacheNameKey(resourceName));
        List<String> remotePaths = restoreStrings(conf, getRemotePathKey(resourceName));
        long size = conf.getLong(KEY_SIZE, 0L);
        int index = localNames.size();
        for (FileStatus status : list) {
            String name = String.format("%s-%04d", resourceName, index++); //$NON-NLS-1$
            StringBuilder buf = new StringBuilder();
            buf.append(status.getPath().toString());
            buf.append('#');
            buf.append(name);
            String cachePath = buf.toString();

            remotePaths.add(status.getPath().toString());
            localNames.add(name);
            try {
                URI uri = new URI(cachePath);
                DistributedCache.addCacheFile(uri, conf);
            } catch (URISyntaxException e) {
                throw new IllegalStateException(e);
            }
            size += status.getLen();
        }
        conf.setStrings(
                getLocalCacheNameKey(resourceName),
                localNames.toArray(new String[localNames.size()]));
        conf.setStrings(
                getRemotePathKey(resourceName),
                remotePaths.toArray(new String[remotePaths.size()]));
        conf.setLong(KEY_SIZE, size);
        if (JobCompatibility.isLocalMode(job)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("symlinks for distributed cache will not be created in standalone mode"); //$NON-NLS-1$
            }
        } else {
            DistributedCache.createSymlink(conf);
        }
    }

    private static ArrayList<String> restoreStrings(Configuration conf, String key) {
        assert conf != null;
        assert key != null;
        ArrayList<String> results = new ArrayList<String>();
        String[] old = conf.getStrings(key);
        if (old != null && old.length >= 1) {
            Collections.addAll(results, old);
        }
        return results;
    }

    /**
     * Returns the estimated resource data-size.
     * @param context the current job context
     * @return the estimated resource data-size in bytes
     * @throws InterruptedException if interrupted while
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.7.1
     */
    public static long estimateResourceSize(JobContext context) throws InterruptedException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        return context.getConfiguration().getLong(KEY_SIZE, 0L);
    }

    /**
     * Returns the access mode for stage resources in the job.
     * @param context the current job context
     * @return the access mode
     * @since 0.7.1
     */
    public static AccessMode getAccessMode(JobContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        return AccessMode.decode(context.getConfiguration().get(KEY_ACCESS_MODE));
    }

    /**
     * Sets the access mode for stage resources in the job.
     * @param context the current job context
     * @param mode the access mode
     * @since 0.7.1
     */
    public static void setAccessMode(JobContext context, AccessMode mode) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (mode == null) {
            throw new IllegalArgumentException("mode must not be null"); //$NON-NLS-1$
        }
        context.getConfiguration().set(KEY_ACCESS_MODE, mode.encode());
    }

    private static String getLocalCacheNameKey(String resourceName) {
        assert resourceName != null;
        return PREFIX_LOCAL_CACHE_NAME + resourceName;
    }

    private static String getRemotePathKey(String resourceName) {
        assert resourceName != null;
        return PREFIX_REMOTE_PATH + resourceName;
    }

    /**
     * Represents the access mode for {@link StageResourceDriver}.
     * @since 0.7.1
     */
    public static enum AccessMode {

        /**
         * Accesses to resources via distributed cache.
         */
        CACHE,

        /**
         * Accesses to resources directly.
         */
        DIRECT,
        ;

        private static final AccessMode DEFAULT = CACHE;

        String encode() {
            return name();
        }

        static AccessMode decode(String value) {
            if (value != null) {
                try {
                    return AccessMode.valueOf(value);
                } catch (IllegalArgumentException e) {
                    LOG.warn(MessageFormat.format(
                            "invalid access mode for stage resources: {0}",
                            value), e);
                }
            }
            return DEFAULT;
        }
    }
}
