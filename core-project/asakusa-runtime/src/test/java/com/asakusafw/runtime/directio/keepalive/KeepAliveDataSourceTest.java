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
package com.asakusafw.runtime.directio.keepalive;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DataDefinition;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.OutputAttemptContext;
import com.asakusafw.runtime.directio.OutputTransactionContext;
import com.asakusafw.runtime.directio.ResourceInfo;
import com.asakusafw.runtime.directio.ResourcePattern;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * Test for {@link KeepAliveDataSource}.
 */
public class KeepAliveDataSourceTest {

    final KeepAliveDataSource ds = new KeepAliveDataSource(new WaitDataSource(), 10);

    final Mock counter = new Mock();

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        try {
            assertThat(ds.heartbeat.isEmpty(), is(true));
        } finally {
            ds.heartbeat.close();
        }
    }

    /**
     * Test method for openInput.
     * @throws Exception if failed
     */
    @Test
    public void testOpenInput() throws Exception {
        try (ModelInput<Object> input = ds.openInput(null, null, counter)) {
            assertKeepAlive(true);
        }
        assertKeepAlive(false);
    }

    /**
     * Test method for openOutput.
     * @throws Exception if failed
     */
    @Test
    public void testOpenOutput() throws Exception {
        try (ModelOutput<Object> output = ds.openOutput(null, null, null, null, counter)) {
            assertKeepAlive(true);
        }
        assertKeepAlive(false);
    }

    /**
     * Test method for setupAttemptOutput.
     * @throws Exception if failed
     */
    @Test
    public void testSetupAttemptOutput() throws Exception {
        OutputAttemptContext context = context();
        long s1 = counter.count;
        ds.setupAttemptOutput(context);
        long s2 = counter.count;
        assertThat(s2, greaterThan(s1));
        assertKeepAlive(false);
    }

    /**
     * Test method for commitAttemptOutput.
     * @throws Exception if failed
     */
    @Test
    public void testCommitAttemptOutput() throws Exception {
        OutputAttemptContext context = context();
        long s1 = counter.count;
        ds.commitAttemptOutput(context);
        long s2 = counter.count;
        assertThat(s2, greaterThan(s1));
        assertKeepAlive(false);
    }

    /**
     * Test method for cleanupAttemptOutput.
     * @throws Exception if failed
     */
    @Test
    public void testCleanupAttemptOutput() throws Exception {
        OutputAttemptContext context = context();
        long s1 = counter.count;
        ds.cleanupAttemptOutput(context);
        long s2 = counter.count;
        assertThat(s2, greaterThan(s1));
        assertKeepAlive(false);
    }

    /**
     * Test method for setupTransactionOutput.
     * @throws Exception if failed
     */
    @Test
    public void testSetupTransactionOutput() throws Exception {
        OutputAttemptContext context = context();
        long s1 = counter.count;
        ds.setupTransactionOutput(context.getTransactionContext());
        long s2 = counter.count;
        assertThat(s2, greaterThan(s1));
        assertKeepAlive(false);
    }

    /**
     * Test method for commitTransactionOutput.
     * @throws Exception if failed
     */
    @Test
    public void testCommitTransactionOutput() throws Exception {
        OutputAttemptContext context = context();
        long s1 = counter.count;
        ds.commitTransactionOutput(context.getTransactionContext());
        long s2 = counter.count;
        assertThat(s2, greaterThan(s1));
        assertKeepAlive(false);
    }

    /**
     * Test method for cleanupTransactionOutput.
     * @throws Exception if failed
     */
    @Test
    public void testCleanupTransactionOutput() throws Exception {
        OutputAttemptContext context = context();
        long s1 = counter.count;
        ds.cleanupTransactionOutput(context.getTransactionContext());
        long s2 = counter.count;
        assertThat(s2, greaterThan(s1));
        assertKeepAlive(false);
    }

    private OutputAttemptContext context() {
        return new OutputAttemptContext("tx", "at", "o", counter);
    }

    private void assertKeepAlive(boolean b) throws InterruptedException {
        long s1 = counter.count;
        Thread.sleep(200);
        long s2 = counter.count;
        assertThat(s2, b ? greaterThan(s1) : is(s1));
    }

    private static class Mock extends Counter {

        volatile long count;

        Mock() {
            return;
        }

        @Override
        protected void onChanged() {
            count++;
        }
    }

    private static class WaitDataSource implements DirectDataSource {

        public WaitDataSource() {
            return;
        }

        @Override
        public String path(String basePath, ResourcePattern resourcePattern) {
            return String.format("%s/%s", basePath, resourcePattern);
        }

        @Override
        public String path(String basePath) {
            return basePath;
        }

        @Override
        public <T> List<DirectInputFragment> findInputFragments(
                DataDefinition<T> definition,
                String basePath,
                ResourcePattern resourcePattern) throws IOException, InterruptedException {
            return Collections.emptyList();
        }

        @Override
        public <T> ModelInput<T> openInput(DataDefinition<T> definition,
                DirectInputFragment fragment,
                Counter counter) throws IOException, InterruptedException {
            return new ModelInput<T>() {
                @Override
                public boolean readTo(T model) throws IOException {
                    return false;
                }
                @Override
                public void close() throws IOException {
                    return;
                }
            };
        }

        @Override
        public <T> ModelOutput<T> openOutput(
                OutputAttemptContext context,
                DataDefinition<T> definition,
                String basePath,
                String resourcePath,
                Counter counter) throws IOException, InterruptedException {
            return new ModelOutput<T>() {
                @Override
                public void write(T model) throws IOException {
                    return;
                }
                @Override
                public void close() throws IOException {
                    return;
                }
            };
        }

        @Override
        public List<ResourceInfo> list(
                String basePath,
                ResourcePattern resourcePattern,
                Counter counter) throws IOException, InterruptedException {
            return Collections.emptyList();
        }

        @Override
        public boolean delete(
                String basePath,
                ResourcePattern resourcePattern,
                boolean recursive,
                Counter counter) throws IOException, InterruptedException {
            return false;
        }

        @Override
        public void setupAttemptOutput(OutputAttemptContext context) throws IOException,
                InterruptedException {
            Thread.sleep(200);
        }

        @Override
        public void commitAttemptOutput(OutputAttemptContext context) throws IOException,
                InterruptedException {
            Thread.sleep(200);
        }

        @Override
        public void cleanupAttemptOutput(OutputAttemptContext context) throws IOException,
                InterruptedException {
            Thread.sleep(200);
        }

        @Override
        public void setupTransactionOutput(OutputTransactionContext context) throws IOException,
                InterruptedException {
            Thread.sleep(200);
        }

        @Override
        public void commitTransactionOutput(OutputTransactionContext context) throws IOException,
                InterruptedException {
            Thread.sleep(200);
        }

        @Override
        public void cleanupTransactionOutput(OutputTransactionContext context) throws IOException,
                InterruptedException {
            Thread.sleep(200);
        }
    }
}
