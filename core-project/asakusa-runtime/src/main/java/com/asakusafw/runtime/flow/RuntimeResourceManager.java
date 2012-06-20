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
package com.asakusafw.runtime.flow;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;

import com.asakusafw.runtime.core.HadoopConfiguration;
import com.asakusafw.runtime.core.ResourceConfiguration;
import com.asakusafw.runtime.core.RuntimeResource;

/**
 * 実行時リソースのライフサイクルを管理する。
 */
public class RuntimeResourceManager {

    static final Log LOG = LogFactory.getLog(RuntimeResourceManager.class);

    /**
     * 標準的な設定ファイルの名前。
     */
    public static final String CONFIGURATION_FILE_NAME = "asakusa-resources.xml";

    /**
     * The path to configuration file (relative from $ASAKUSA_HOME).
     * @since 0.2.5
     */
    public static final String CONFIGURATION_FILE_PATH = "core/conf/" + CONFIGURATION_FILE_NAME;

    private final ResourceConfiguration configuration;

    private List<RuntimeResource> resources;

    /**
     * インスタンスを生成する。
     * @param configuration 設定情報
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public RuntimeResourceManager(Configuration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        this.configuration = new HadoopConfiguration(configuration);
        this.resources = Collections.emptyList();
    }

    /**
     * このリソースを初期化する。
     * @throws IOException リソースの初期化に失敗した場合
     * @throws InterruptedException 初期化中に割り込みが発生した場合
     * @throws IllegalArgumentException 設定が不正である場合
     * @throws IllegalStateException 同一のリソースが複数回初期化された場合
     */
    public void setup() throws IOException, InterruptedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading runtime plugins");
        }
        List<? extends RuntimeResource> loaded = load();
        this.resources = new ArrayList<RuntimeResource>();
        for (RuntimeResource resource : loaded) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Activating runtime plugin: {0}",
                        resource.getClass().getName()));
            }
            resource.setup(configuration);
            resources.add(resource);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Loaded {0} runtime plugins",
                    resources.size()));
        }
    }

    /**
     * このリソースを解放する。
     * @throws IOException リソースの初期化に失敗した場合
     * @throws InterruptedException 初期化中に割り込みが発生した場合
     * @throws IllegalArgumentException 設定が不正である場合
     * @throws IllegalStateException 同一のリソースが複数回初期化された場合
     */
    public void cleanup() throws IOException, InterruptedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Unloading {0} runtime plugins",
                    resources.size()));
        }
        try {
            for (RuntimeResource resource : resources) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Deactivating runtime plugin: {0}",
                            resource.getClass().getName()));
                }
                resource.cleanup(configuration);
            }
        } finally {
            // TODO もう少しbest effortで解放するか
            this.resources = Collections.emptyList();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Unloaded {0} runtime plugins",
                    resources.size()));
        }
    }

    /**
     * 利用可能なリソースの一覧をロードする。
     * <p>
     * 実行環境からロード可能な全てのクラスパスの
     * {@code META-INF/services/com.asakusafw.runtime.core.RuntimeResource}
     * に記載された内容を元に、必要なリソースの実体をロードする。
     * </p>
     * @return 利用可能なリソースの一覧
     * @throws IOException ロードに失敗した場合
     */
    protected List<RuntimeResource> load() throws IOException {
        List<RuntimeResource> results = new ArrayList<RuntimeResource>();
        ClassLoader loader = configuration.getClassLoader();
        try {
            for (RuntimeResource resource : ServiceLoader.load(RuntimeResource.class, loader)) {
                results.add(resource);
            }
        } catch (RuntimeException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to load resources ({0})",
                    RuntimeResource.class.getName()),
                    e);
        }
        return results;
    }
}
