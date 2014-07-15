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
package com.asakusafw.runtime.util.cache;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;

/**
 * Executes {@link FileCacheRepository#resolve(Path)} concurrently.
 * @since 0.7.0
 */
public class ConcurrentBatchFileCacheRepository implements BatchFileCacheRepository {

    static final Log LOG = LogFactory.getLog(ConcurrentBatchFileCacheRepository.class);

    private static final int CHECK_TIMEOUT = 100;

    private final FileCacheRepository repository;

    private final ExecutorService executor;

    private final ExceptionHandler exceptionHandler;

    /**
     * Creates a new instance with default exception handler.
     * @param repository the batch processing target {@link FileCacheRepository}
     * @param executor the executor for process each file
     */
    public ConcurrentBatchFileCacheRepository(FileCacheRepository repository, ExecutorService executor) {
        this(repository, executor, null);
    }

    /**
     * Creates a new instance.
     * @param repository the batch processing target {@link FileCacheRepository}
     * @param executor the executor for process each file
     * @param exceptionHandler exception handler for processing each file,
     *     or {@code null} to just report (as 'WARN' level) for each exception
     */
    public ConcurrentBatchFileCacheRepository(
            FileCacheRepository repository,
            ExecutorService executor,
            ExceptionHandler exceptionHandler) {
        this.repository = repository;
        this.executor = executor;
        this.exceptionHandler = exceptionHandler != null ? exceptionHandler : new ExceptionHandler() {
            @Override
            public Path handle(Path path, IOException exception) throws IOException {
                LOG.warn(MessageFormat.format(
                        "Processing cache is failed: {0}",
                        path), exception);
                return null;
            }
        };
    }

    @Override
    public Map<Path, Path> resolve(List<? extends Path> files) throws IOException, InterruptedException {
        Map<Path, Future<Path>> futures = new LinkedHashMap<Path, Future<Path>>();
        Map<Path, Path> results = new HashMap<Path, Path>();
        for (Path file : files) {
            if (futures.containsKey(file)) {
                // skip same file
                continue;
            }
            Future<Path> future = executor.submit(new Task(repository, file));
            futures.put(file, future);
            results.put(file, null);
        }
        try {
            while (futures.isEmpty() == false) {
                for (Path file : files) {
                    Future<Path> future = futures.get(file);
                    if (future == null) {
                        continue;
                    }
                    try {
                        Path result = future.get(CHECK_TIMEOUT, TimeUnit.MILLISECONDS);

                        futures.remove(file);
                        if (result != null) {
                            results.put(file, result);
                        }
                    } catch (CancellationException e) {
                        futures.remove(file);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(MessageFormat.format(
                                    "Processing cache is cancelled: {0}",
                                    file), e);
                        }
                    } catch (ExecutionException e) {
                        futures.remove(file);
                        Path result = handleException(file, e);
                        if (result != null) {
                            results.put(file, result);
                        }
                    } catch (TimeoutException e) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace(MessageFormat.format(
                                    "Trying to wait for next task complete: {0}",
                                    file), e);
                        }
                    }
                }
            }
        } finally {
            for (Map.Entry<Path, Future<Path>> entry : futures.entrySet()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format(
                            "Request cancel for processing cache: {0}",
                            entry.getKey()));
                }
                entry.getValue().cancel(true);
            }
        }
        return results;
    }

    private Path handleException(Path file, ExecutionException e) throws IOException {
        Throwable cause = e.getCause();
        if (cause instanceof Error) {
            throw (Error) cause;
        } else if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        } else if (cause instanceof IOException) {
            return exceptionHandler.handle(file, (IOException) cause);
        } else if (cause instanceof InterruptedException) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Processing cache is cancelled: {0}",
                        file), e);
            }
            return null;
        } else {
            throw new IllegalStateException(MessageFormat.format(
                    "Unhandled exception while processing cache: {0}",
                    file), e);
        }
    }

    private static final class Task implements Callable<Path> {

        private final FileCacheRepository repository;

        private final Path file;

        Task(FileCacheRepository repository, Path file) {
            this.repository = repository;
            this.file = file;
        }

        @Override
        public Path call() throws IOException, InterruptedException {
            return repository.resolve(file);
        }
    }

    /**
     * Exception handler.
     */
    public interface ExceptionHandler {

        /**
         * Process exception and returns an alternative result.
         * @param path the original file path
         * @param exception the occurred exception
         * @return the alternative result, or {@code null} for unsupported file
         * @throws IOException if propagate the original exception
         */
        Path handle(Path path, IOException exception) throws IOException;
    }
}
