/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.hadoop.fs.Path;
import org.junit.Test;

/**
 * Test for {@link ConcurrentBatchFileCacheRepository}.
 */
public class ConcurrentBatchFileCacheRepositoryTest extends FileCacheRepositoryTestRoot {

    /**
     * simple case.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        File source = put(newFile(), "Hello, world!");
        Map<File, File> results;
        ExecutorService executor = Executors.newFixedThreadPool(1);
        try {
            BatchFileCacheRepository batch = new ConcurrentBatchFileCacheRepository(
                    new MockFileCacheRepository(folder.newFolder()), executor);
            results = files(batch.resolve(Arrays.asList(path(source))));
        } finally {
            executor.shutdown();
        }
        assertThat(results.size(), is(1));
        File target = results.get(source);
        assertThat(target, is(notNullValue()));
        assertThat(get(target), is("Hello, world!"));
        assertThat(target, is(not(source)));
    }

    /**
     * concurrent.
     * @throws Exception if failed
     */
    @Test
    public void multiple() throws Exception {
        List<File> sources = new ArrayList<>();
        List<Path> paths = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            File source = put(newFile(), String.format("hello%02d", i));
            sources.add(source);
            paths.add(path(source));
        }
        Map<File, File> results;
        ExecutorService executor = Executors.newFixedThreadPool(
                Math.min(4, Runtime.getRuntime().availableProcessors()));
        try {
            BatchFileCacheRepository batch = new ConcurrentBatchFileCacheRepository(
                    new MockFileCacheRepository(folder.newFolder()), executor);
            results = files(batch.resolve(paths));
        } finally {
            executor.shutdown();
        }
        assertThat(results.size(), is(sources.size()));
        for (File source : sources) {
            File target = results.get(source);
            assertThat(target, is(notNullValue()));
            assertThat(get(target), is(get(source)));
            assertThat(target, is(not(source)));
        }
    }

    /**
     * Handles exception and returns an alternative.
     * @throws Exception if failed
     */
    @Test
    public void exception_suppress() throws Exception {
        File source = put(newFile(), "Hello, world!");
        File alter = newFile();
        Map<File, File> results;
        ExecutorService executor = Executors.newFixedThreadPool(1);
        try {
            BatchFileCacheRepository batch = new ConcurrentBatchFileCacheRepository(
                    file -> {
                        throw new IOException();
                    }, executor, (path, exception) -> path(alter));
            results = files(batch.resolve(Arrays.asList(path(source))));
        } finally {
            executor.shutdown();
        }
        assertThat(results.size(), is(1));
        assertThat(results.get(source), is(alter));
    }

    /**
     * Handles exception and re-throw it.
     * @throws Exception if failed
     */
    @Test
    public void exception_rethrow() throws Exception {
        File source = put(newFile(), "Hello, world!");
        ExecutorService executor = Executors.newFixedThreadPool(1);
        try {
            BatchFileCacheRepository batch = new ConcurrentBatchFileCacheRepository(
                    file -> {
                        throw new IOException();
                    }, executor, (path, exception) -> {
                        throw exception;
                    });
            batch.resolve(Arrays.asList(path(source)));
            fail();
        } catch (IOException e) {
            // ok.
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Cancel executors.
     * @throws Exception if failed
     */
    @Test
    public void cancel() throws Exception {
        File source = put(newFile(), "Hello, world!");
        Map<File, File> results;
        ExecutorService executor = Executors.newFixedThreadPool(1);
        try {
            BatchFileCacheRepository batch = new ConcurrentBatchFileCacheRepository(
                    file -> {
                        throw new InterruptedException();
                    }, executor);
            results = files(batch.resolve(Arrays.asList(path(source))));
        } finally {
            executor.shutdown();
        }
        assertThat(results.size(), is(1));
        assertThat(results.get(source), is(nullValue()));
    }

    private File newFile() throws IOException {
        return folder.newFile().getCanonicalFile();
    }
}
