/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hadoop.conf.Configuration;
import org.junit.Test;

import com.asakusafw.runtime.core.Report;
import com.asakusafw.runtime.core.ResourceConfiguration;
import com.asakusafw.runtime.core.RuntimeResource;

/**
 * {@link RuntimeResourceManager}.
 */
public class RuntimeResourceManagerTest {

    /**
     * ServiceProviderに関するテスト。
     * @throws Exception エラー
     */
    @Test
    public void load() throws Exception {
        Configuration conf = new Configuration();
        RuntimeResourceManager manager = new RuntimeResourceManager(conf);
        List<RuntimeResource> loaded = manager.load();

        boolean found = false;
        for (RuntimeResource resource : loaded) {
            if (resource instanceof Report.Initializer) {
                found = true;
                break;
            }
        }
        assertThat(found, is(true));
    }

    /**
     * 単一リソースのセットアップ。
     * @throws Exception エラー
     */
    @Test
    public void setup() throws Exception {
        final AtomicInteger passed = new AtomicInteger();
        Configuration conf = new Configuration();
        conf.set("test", "OK");
        RuntimeResourceManager manager = new RuntimeResourceManager(conf) {
            @Override
            protected List<RuntimeResource> load() throws IOException {
                return Arrays.<RuntimeResource>asList(new Adapter() {
                    @Override
                    public void setup(ResourceConfiguration configuration) {
                        passed.incrementAndGet();
                        assertThat(configuration.get("test", null), is("OK"));
                    }
                });
            }
        };
        manager.setup();
        assertThat(passed.get(), is(1));
    }

    /**
     * セットアップ時に例外発生。
     * @throws Exception エラー
     */
    @Test(expected = IOException.class)
    public void setup_exception() throws Exception {
        Configuration conf = new Configuration();
        RuntimeResourceManager manager = new RuntimeResourceManager(conf) {
            @Override
            protected List<RuntimeResource> load() throws IOException {
                return Arrays.<RuntimeResource>asList(new Adapter() {
                    @Override
                    public void setup(ResourceConfiguration configuration) throws IOException {
                        throw new IOException();
                    }
                });
            }
        };
        manager.setup();
    }

    /**
     * 複数リソースのセットアップ。
     * @throws Exception エラー
     */
    @Test
    public void setup_multi() throws Exception {
        final AtomicInteger passed = new AtomicInteger();
        Configuration conf = new Configuration();
        conf.set("test", "OK");
        RuntimeResourceManager manager = new RuntimeResourceManager(conf) {
            @Override
            protected List<RuntimeResource> load() throws IOException {
                RuntimeResource adapter = new Adapter() {
                    @Override
                    public void setup(ResourceConfiguration configuration) {
                        passed.addAndGet(1);
                        assertThat(configuration.get("test", null), is("OK"));
                    }
                };
                return Arrays.asList(adapter, adapter, adapter);
            }
        };
        manager.setup();
        assertThat(passed.get(), is(3));
    }

    /**
     * 単一リソースのクリーンアップ。
     * @throws Exception エラー
     */
    @Test
    public void cleanup() throws Exception {
        final AtomicInteger passed = new AtomicInteger();
        Configuration conf = new Configuration();
        conf.set("test", "OK");
        RuntimeResourceManager manager = new RuntimeResourceManager(conf) {
            @Override
            protected List<RuntimeResource> load() throws IOException {
                return Arrays.<RuntimeResource>asList(new Adapter() {
                    @Override
                    public void cleanup(ResourceConfiguration configuration) {
                        passed.incrementAndGet();
                        assertThat(configuration.get("test", null), is("OK"));
                    }
                });
            }
        };
        manager.setup();
        assertThat(passed.get(), is(0));
        manager.cleanup();
        assertThat(passed.get(), is(1));
    }

    /**
     * クリーンアップ時に例外発生。
     * @throws Exception エラー
     */
    @Test(expected = IOException.class)
    public void cleanup_exception() throws Exception {
        Configuration conf = new Configuration();
        RuntimeResourceManager manager = new RuntimeResourceManager(conf) {
            @Override
            protected List<RuntimeResource> load() throws IOException {
                return Arrays.<RuntimeResource>asList(new Adapter() {
                    @Override
                    public void cleanup(ResourceConfiguration configuration) throws IOException {
                        throw new IOException();
                    }
                });
            }
        };
        manager.setup();
        manager.cleanup();
    }

    /**
     * 複数リソースのクリーンアップ。
     * @throws Exception エラー
     */
    @Test
    public void cleanup_multi() throws Exception {
        final AtomicInteger passed = new AtomicInteger();
        Configuration conf = new Configuration();
        conf.set("test", "OK");
        RuntimeResourceManager manager = new RuntimeResourceManager(conf) {
            @Override
            protected List<RuntimeResource> load() throws IOException {
                RuntimeResource adapter = new Adapter() {
                    @Override
                    public void cleanup(ResourceConfiguration configuration) {
                        passed.addAndGet(1);
                        assertThat(configuration.get("test", null), is("OK"));
                    }
                };
                return Arrays.asList(adapter, adapter, adapter);
            }
        };
        manager.setup();
        manager.cleanup();
        assertThat(passed.get(), is(3));
    }

    /**
     * セットアップ時に途中でエラーが発生し、そこまでのセットアップしたものをクリーンアップ。
     * @throws Exception エラー
     */
    @Test
    public void partial_setup_cleanup() throws Exception {
        final AtomicInteger passed = new AtomicInteger();
        Configuration conf = new Configuration();
        RuntimeResourceManager manager = new RuntimeResourceManager(conf) {
            @Override
            protected List<RuntimeResource> load() throws IOException {
                RuntimeResource adapter = new Adapter() {
                    @Override
                    public void setup(ResourceConfiguration configuration) throws IOException {
                        if (passed.get() >= 3) {
                            throw new IOException();
                        }
                        passed.addAndGet(1);
                    }
                    @Override
                    public void cleanup(ResourceConfiguration configuration) {
                        passed.addAndGet(-1);
                    }
                };
                return Arrays.asList(adapter, adapter, adapter, adapter, adapter);
            }
        };
        try {
            manager.setup();
            fail();
        } catch (IOException e) {
            // ok
        }
        assertThat("途中までは成功している", passed.get(), is(3));
        manager.cleanup();
        assertThat("成功したものだけクリーンナップ", passed.get(), is(0));
    }

    static class Adapter implements RuntimeResource {

        @Override
        public void setup(ResourceConfiguration configuration) throws IOException, InterruptedException {
            return;
        }

        @Override
        public void cleanup(ResourceConfiguration configuration) throws IOException, InterruptedException {
            return;
        }
    }
}
