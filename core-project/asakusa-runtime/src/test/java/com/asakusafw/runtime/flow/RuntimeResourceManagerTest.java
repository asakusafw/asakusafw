/**
 * Copyright 2011-2019 Asakusa Framework Team.
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

import com.asakusafw.runtime.core.ResourceConfiguration;
import com.asakusafw.runtime.core.legacy.LegacyReport;
import com.asakusafw.runtime.core.legacy.RuntimeResource;

/**
 * {@link RuntimeResourceManager}.
 */
public class RuntimeResourceManagerTest {

    /**
     * test for loading services.
     * @throws Exception if failed
     */
    @Test
    public void load() throws Exception {
        Configuration conf = new Configuration();
        RuntimeResourceManager manager = new RuntimeResourceManager(conf);
        List<RuntimeResource> loaded = manager.load();

        boolean found = false;
        for (RuntimeResource resource : loaded) {
            if (resource instanceof LegacyReport.Initializer) {
                found = true;
                break;
            }
        }
        assertThat(found, is(true));
    }

    /**
     * setup single resource.
     * @throws Exception if failed
     */
    @Test
    public void setup() throws Exception {
        AtomicInteger passed = new AtomicInteger();
        Configuration conf = new Configuration();
        conf.set("test", "OK");
        RuntimeResourceManager manager = new RuntimeResourceManager(conf) {
            @Override
            protected List<RuntimeResource> load() throws IOException {
                return Arrays.asList(new RuntimeResource() {
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
     * error occurred during setup resources.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void setup_exception() throws Exception {
        Configuration conf = new Configuration();
        RuntimeResourceManager manager = new RuntimeResourceManager(conf) {
            @Override
            protected List<RuntimeResource> load() throws IOException {
                return Arrays.asList(new RuntimeResource() {
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
     * setup multiple resources.
     * @throws Exception if failed
     */
    @Test
    public void setup_multi() throws Exception {
        AtomicInteger passed = new AtomicInteger();
        Configuration conf = new Configuration();
        conf.set("test", "OK");
        RuntimeResourceManager manager = new RuntimeResourceManager(conf) {
            @Override
            protected List<RuntimeResource> load() throws IOException {
                RuntimeResource adapter = new RuntimeResource() {
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
     * cleanup single resource.
     * @throws Exception if failed
     */
    @Test
    public void cleanup() throws Exception {
        AtomicInteger passed = new AtomicInteger();
        Configuration conf = new Configuration();
        conf.set("test", "OK");
        RuntimeResourceManager manager = new RuntimeResourceManager(conf) {
            @Override
            protected List<RuntimeResource> load() throws IOException {
                return Arrays.asList(new RuntimeResource() {
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
     * error occurred during cleanup resources.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void cleanup_exception() throws Exception {
        Configuration conf = new Configuration();
        RuntimeResourceManager manager = new RuntimeResourceManager(conf) {
            @Override
            protected List<RuntimeResource> load() throws IOException {
                return Arrays.asList(new RuntimeResource() {
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
     * cleanup multiple resources.
     * @throws Exception if failed
     */
    @Test
    public void cleanup_multi() throws Exception {
        AtomicInteger passed = new AtomicInteger();
        Configuration conf = new Configuration();
        conf.set("test", "OK");
        RuntimeResourceManager manager = new RuntimeResourceManager(conf) {
            @Override
            protected List<RuntimeResource> load() throws IOException {
                RuntimeResource adapter = new RuntimeResource() {
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
     * cleanup resource manager which is partially set-up but error was occurred during setup some resources.
     * @throws Exception if failed
     */
    @Test
    public void partial_setup_cleanup() throws Exception {
        AtomicInteger passed = new AtomicInteger();
        Configuration conf = new Configuration();
        RuntimeResourceManager manager = new RuntimeResourceManager(conf) {
            @Override
            protected List<RuntimeResource> load() throws IOException {
                RuntimeResource adapter = new RuntimeResource() {
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
        assertThat("partially set-up", passed.get(), is(3));
        manager.cleanup();
        assertThat(passed.get(), is(0));
    }
}
