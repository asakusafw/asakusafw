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
package com.asakusafw.runtime.util.cache;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.junit.Assume;
import org.junit.Test;

import com.asakusafw.runtime.util.cache.HadoopFileCacheRepository;
import com.asakusafw.runtime.util.cache.FileCacheRepository;
import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;
import com.asakusafw.runtime.util.lock.ConstantRetryStrategy;
import com.asakusafw.runtime.util.lock.LocalFileLockProvider;
import com.asakusafw.runtime.util.lock.LockProvider;
import com.asakusafw.runtime.util.lock.RetryStrategy;

/**
 * Test for {@link FileCacheRepository}.
 */
public class HadoopFileCacheRepositoryTest extends FileCacheRepositoryTestRoot {

    static final Log LOG = LogFactory.getLog(HadoopFileCacheRepositoryTest.class);

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        File cacheRepo = folder.newFolder();
        Configuration configuration = new ConfigurationProvider().newInstance();
        LockProvider<Path> locks = new LocalFileLockProvider<>(folder.newFolder());
        RetryStrategy retrier = new ConstantRetryStrategy();
        HadoopFileCacheRepository cache = new HadoopFileCacheRepository(configuration, path(cacheRepo), locks, retrier);

        File source = put(folder.newFile(), "Hello, world!");
        Path resolved = cache.resolve(path(source));

        assertThat(get(file(resolved)), is("Hello, world!"));
        assertThat(resolved, is(not(path(source))));
        assertThat(containsFile(cacheRepo, file(resolved)), is(true));
    }

    /**
     * update cache.
     * @throws Exception if failed
     */
    @Test
    public void update() throws Exception {
        File cacheRepo = folder.newFolder();
        Configuration configuration = new ConfigurationProvider().newInstance();
        LockProvider<Path> locks = new LocalFileLockProvider<>(folder.newFolder());
        RetryStrategy retrier = new ConstantRetryStrategy();
        HadoopFileCacheRepository cache = new HadoopFileCacheRepository(configuration, path(cacheRepo), locks, retrier);

        File source = put(folder.newFile(), "Hello, world!");
        Path first = cache.resolve(path(source));

        put(source, "UPDATED");
        Path retry = cache.resolve(path(source));

        assertThat(retry, is(first));
        assertThat(get(file(retry)), is("UPDATED"));
    }

    /**
     * use cached.
     * @throws Exception if failed
     */
    @Test
    public void cached() throws Exception {
        File cacheRepo = folder.newFolder();
        Configuration configuration = new ConfigurationProvider().newInstance();
        LockProvider<Path> locks = new LocalFileLockProvider<>(folder.newFolder());
        RetryStrategy retrier = new ConstantRetryStrategy();
        HadoopFileCacheRepository cache = new HadoopFileCacheRepository(configuration, path(cacheRepo), locks, retrier);

        File source = put(folder.newFile(), "Hello, world!");
        Path first = cache.resolve(path(source));
        long timestamp = file(first).lastModified();
        Assume.assumeThat(timestamp, is(greaterThan(0L)));

        for (int i = 0; i < 10; i++) {
            if (System.currentTimeMillis() != timestamp) {
                break;
            }
            Thread.sleep(10);
        }
        Assume.assumeThat(System.currentTimeMillis(), is(not(timestamp)));

        // may not changed
        Path retry = cache.resolve(path(source));
        assertThat(file(retry).lastModified(), is(timestamp));
    }

    /**
     * Conflict cache creation.
     * @throws Exception if failed
     */
    @Test
    public void conflict() throws Exception {
        File source = folder.newFile();
        byte[] bytes = new byte[1024 * 1024];
        try (OutputStream output = new FileOutputStream(source)) {
            for (int i = 0, n = 50; i < n; i++) {
                output.write(bytes);
            }
        }

        final Path path = path(source);
        File cacheRepo = folder.newFolder();
        Configuration configuration = new ConfigurationProvider().newInstance();
        LockProvider<Path> locks = new LocalFileLockProvider<>(folder.newFolder());
        RetryStrategy retrier = new ConstantRetryStrategy(30, 100, 200);
        final FileCacheRepository cache = new HadoopFileCacheRepository(configuration, path(cacheRepo), locks, retrier);

        List<Future<Path>> futures = new ArrayList<>();
        int count = 10;
        final CountDownLatch latch = new CountDownLatch(count);
        ExecutorService executor = Executors.newFixedThreadPool(count);
        try {
            for (int i = 0; i < count; i++) {
                final String label = String.format("thread-%d", i);
                futures.add(executor.submit(new Callable<Path>() {
                    @Override
                    public Path call() throws Exception {
                        LOG.info("Wait: resolve @" + label);
                        latch.countDown();
                        if (latch.await(5, TimeUnit.SECONDS) == false) {
                            throw new TimeoutException();
                        }
                        LOG.info("Start: resolve @" + label);
                        Path result = cache.resolve(path);

                        LOG.info("Finish: resolve @" + label);
                        return result;
                    }
                }));
            }
            executor.shutdown();
            if (executor.awaitTermination(30, TimeUnit.SECONDS) == false) {
                throw new TimeoutException();
            }
        } finally {
            executor.shutdownNow();
        }
        for (Future<Path> future : futures) {
            future.get();
        }
    }
}
