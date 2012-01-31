/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;

import com.asakusafw.runtime.stage.temporary.TemporaryStorage;

/**
 * ステージリソースを利用するためのドライバ。
 */
public class StageResourceDriver implements Closeable {

    static final Log LOG = LogFactory.getLog(StageResourceDriver.class);

    private static final String PREFIX_LOCAL_CACHE_NAME = "com.asakusafw.cache.";

    private final Configuration configuration;

    private final FileSystem fileSystem;

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
        this.fileSystem = FileSystem.getLocal(configuration);
    }

    /**
     * リソースを保持するファイルシステムのオブジェクトを返す。
     * @return リソースを保持するファイルシステムのオブジェクト
     */
    public FileSystem getResourceFileSystem() {
        return this.fileSystem;
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
     * <p>
     * このパスは、{@link #getResourceFileSystem()}によって得られるファイルシステム上のパスを表す。
     * </p>
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
            LOG.debug("finding cache: " + resourceName);
        }
        String[] localNames = getConfiguration().getStrings(getLocalCacheNameKey(resourceName));
        List<Path> results = new ArrayList<Path>();
        for (String localName : localNames) {
            Path resolvedPath = findLocalCache(resourceName, localName);
            if (resolvedPath == null) {
                return Collections.emptyList();
            }
            results.add(fileSystem.makeQualified(resolvedPath));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Cache file resolved: resource={0}, paths={1}",
                    resourceName,
                    results));
        }
        return results;
    }

    private Path findLocalCache(String resouceName, String localName) throws IOException {
        assert localName != null;
        Path cache = new Path(localName);
        if (fileSystem.exists(cache)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("symlink found: " + cache);
            }
            return cache;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("symlink not found: " + localName);
        }
        Path directPath = findCacheForLocalMode(resouceName, localName);
        if (directPath == null || fileSystem.exists(directPath) == false) {
            LOG.warn(MessageFormat.format(
                    "Failed to resolve stage resource \"{1}\" (resource={0})",
                    resouceName,
                    localName));
        }
        return directPath;
    }

    private Path findCacheForLocalMode(String resourceName, String localName) throws IOException {
        assert resourceName != null;
        assert localName != null;
        String remoteName = null;
        for (URI uri : DistributedCache.getCacheFiles(configuration)) {
            if (localName.equals(uri.getFragment())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("fragment matched: " + uri);
                }
                String rpath = uri.getPath();
                remoteName = rpath.substring(rpath.lastIndexOf('/') + 1);
                break;
            }
        }
        if (remoteName == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("fragment not matched: " + resourceName);
            }
            return null;
        }
        for (Path path : DistributedCache.getLocalCacheFiles(configuration)) {
            String localFileName = path.getName();
            if (remoteName.equals(localFileName)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("local path matched: " + path);
                }
                return path;
            }
        }
        return null;
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
        List<Path> list = TemporaryStorage.list(job.getConfiguration(), new Path(resourcePath));
        if (list.isEmpty()) {
            throw new IOException(MessageFormat.format(
                    "Resource not found: {0}",
                    resourcePath));
        }
        String[] added = job.getConfiguration().getStrings(getLocalCacheNameKey(resourceName));
        List<String> localNames = new ArrayList<String>();
        if (added != null && added.length >= 1) {
            Collections.addAll(localNames, added);
        }
        int index = localNames.size();
        for (Path path : list) {
            String name = String.format("%s-%04d", resourceName, index++);
            StringBuilder buf = new StringBuilder();
            buf.append(path.toString());
            buf.append('#');
            buf.append(name);

            localNames.add(name);
            try {
                URI uri = new URI(buf.toString());
                DistributedCache.addCacheFile(uri, job.getConfiguration());
            } catch (URISyntaxException e) {
                throw new IllegalStateException(e);
            }
        }
        job.getConfiguration().setStrings(
                getLocalCacheNameKey(resourceName),
                localNames.toArray(new String[localNames.size()]));
        DistributedCache.createSymlink(job.getConfiguration());
    }

    private static String getLocalCacheNameKey(String resourceName) {
        assert resourceName != null;
        return PREFIX_LOCAL_CACHE_NAME + resourceName;
    }
}
