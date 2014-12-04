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
package com.asakusafw.runtime.core;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 実行時に利用され、ライフサイクルを持つリソース。
 */
public interface RuntimeResource {

    /**
     * このリソースを初期化する。
     * @param configuration 初期化の設定
     * @throws IOException リソースの初期化に失敗した場合
     * @throws InterruptedException 初期化中に割り込みが発生した場合
     * @throws IllegalArgumentException 設定が不正である場合
     * @throws IllegalStateException 同一のリソースが複数回初期化された場合
     */
    void setup(ResourceConfiguration configuration)
        throws IOException, InterruptedException;

    /**
     * このリソースを解放する。
     * @param configuration 初期化の設定
     * @throws IOException リソースの初期化に失敗した場合
     * @throws InterruptedException 初期化中に割り込みが発生した場合
     * @throws IllegalArgumentException 設定が不正である場合
     * @throws IllegalStateException 同一のリソースが複数回初期化された場合
     */
    void cleanup(ResourceConfiguration configuration)
        throws IOException, InterruptedException;

    /**
     * 委譲オブジェクトの登録と解除を行う骨格実装。
     * @param <D> 初期化対象の委譲オブジェクト型
     */
    abstract class DelegateRegisterer<D> implements RuntimeResource {

        static final Log LOG = LogFactory.getLog(RuntimeResource.DelegateRegisterer.class);

        private D registered;

        protected abstract String getClassNameKey();

        protected abstract Class<? extends D> getInterfaceType();

        protected abstract void register(D delegate, ResourceConfiguration configuration)
                throws IOException, InterruptedException;

        protected abstract void unregister(D delegate, ResourceConfiguration configuration)
                throws IOException, InterruptedException;

        @Override
        public void setup(ResourceConfiguration configuration) throws IOException,
                InterruptedException {
            String className = configuration.get(getClassNameKey(), null);
            if (className == null) {
                LOG.warn(MessageFormat.format(
                        "Missing \"{0}\" in plugin configurations (API:{1})",
                        getClassNameKey(),
                        getInterfaceType().getName()));
                return;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Loading plugin for {0}: key={1}, value={2}",
                        getInterfaceType().getName(),
                        getClassNameKey(),
                        className));
            }
            D loaded = loadDelegate(configuration, className);

            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Registering plugin {2} into {0}",
                        getInterfaceType().getName(),
                        getClassNameKey(),
                        className));
            }
            register(loaded, configuration);

            this.registered = loaded;
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Registered plugin {2} into {0}",
                        getInterfaceType().getName(),
                        getClassNameKey(),
                        className));
            }
        }

        @Override
        public void cleanup(ResourceConfiguration configuration)
                throws IOException, InterruptedException {
            if (registered != null) {
                unregister(registered, configuration);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Unregistered plugin {1} from {0}",
                            getInterfaceType().getName(),
                            registered.getClass().getName()));
                }
                registered = null;
            }
        }

        private D loadDelegate(
                ResourceConfiguration configuration,
                String className) throws IOException {
            assert configuration != null;
            assert className != null;
            try {
                Class<?> aClass = configuration.getClassLoader().loadClass(className);
                Class<? extends D> delegate = aClass.asSubclass(getInterfaceType());
                D instance = delegate.newInstance();
                return instance;
            } catch (Exception e) {
                throw new IOException(MessageFormat.format(
                        "Failed to initialize a plugin {0}",
                        className), e);
            }
        }
    }

}
