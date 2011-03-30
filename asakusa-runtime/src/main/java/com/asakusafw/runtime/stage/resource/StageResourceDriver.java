/**
 * Copyright 2011 Asakusa Framework Team.
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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;

/**
 * ステージリソースを利用するためのドライバ。
 */
public class StageResourceDriver implements Closeable {

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
     * @return 対応するリソースへのパス、存在しない場合は{@code null}
     * @throws IOException リソースの検索に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public Path findCache(String resourceName) throws IOException {
        if (resourceName == null) {
            throw new IllegalArgumentException("cacheName must not be null"); //$NON-NLS-1$
        }
        Path cache = new Path(resourceName);
        if (fileSystem.exists(cache)) {
            return cache;
        }
        return findCacheForLocalMode(resourceName);
    }

    private Path findCacheForLocalMode(String resourceName) throws IOException {
        assert resourceName != null;
        String remoteName = null;
        for (URI uri : DistributedCache.getCacheFiles(configuration)) {
            if (resourceName.equals(uri.getFragment())) {
                String rpath = uri.getPath();
                remoteName = rpath.substring(rpath.lastIndexOf('/') + 1);
                break;
            }
        }
        if (remoteName == null) {
            return null;
        }
        for (Path path : DistributedCache.getLocalCacheFiles(configuration)) {
            String localName = path.getName();
            if (remoteName.equals(localName)) {
                return path;
            }
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        this.fileSystem.close();
    }

    /**
     * 指定のジョブにリソースの情報を追加する。
     * @param job 対象の情報
     * @param resourcePath リソースへのパス
     * @param resourceName リソースの名前
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static void add(Job job, Path resourcePath, String resourceName) {
        StringBuilder buf = new StringBuilder();
        buf.append(resourcePath.toString());
        buf.append('#');
        buf.append(resourceName);
        try {
            URI uri = new URI(buf.toString());
            DistributedCache.addCacheFile(uri, job.getConfiguration());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
        DistributedCache.createSymlink(job.getConfiguration());
    }
}
