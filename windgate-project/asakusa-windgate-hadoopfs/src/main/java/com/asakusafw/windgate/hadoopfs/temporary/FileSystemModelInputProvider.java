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
package com.asakusafw.windgate.hadoopfs.temporary;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.hadoopfs.HadoopFsLogger;

/**
 * An implementation of {@link ModelInputProvider} using {@link FileSystem}.
 * @param <T> target data model class
 * @since 0.2.5
 */
public class FileSystemModelInputProvider<T> implements ModelInputProvider<T> {

    static final WindGateLogger WGLOG = new HadoopFsLogger(FileSystemModelInputProvider.class);

    static final Logger LOG = LoggerFactory.getLogger(FileSystemModelInputProvider.class);

    final FileSystem fileSystem;

    final ExecutorService executor;

    final Future<?> fetcher;

    final BlockingQueue<Entry<T>> queue;

    private Entry<T> current;

    private boolean sawEof;

    private boolean closed;

    /**
     * Creates a new instance.
     * @param configuration the configuration
     * @param fileSystem target file system
     * @param paths source paths
     * @param dataModelClass target data model class
     * @throws IOException if failed to resolve paths
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileSystemModelInputProvider(
            final Configuration configuration,
            final FileSystem fileSystem,
            final Iterable<Path> paths,
            final Class<T> dataModelClass) throws IOException {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        if (fileSystem == null) {
            throw new IllegalArgumentException("fileSystem must not be null"); //$NON-NLS-1$
        }
        if (paths == null) {
            throw new IllegalArgumentException("paths must not be null"); //$NON-NLS-1$
        }
        if (dataModelClass == null) {
            throw new IllegalArgumentException("dataModelClass must not be null"); //$NON-NLS-1$
        }
        this.fileSystem = fileSystem;
        this.queue = new SynchronousQueue<>();
        this.executor = Executors.newFixedThreadPool(1, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "HadoopFileCollector");
                t.setDaemon(true);
                return t;
            }
        });
        this.fetcher = this.executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                for (Path path : paths) {
                    WGLOG.info("I09001",
                            fileSystem.getUri(),
                            paths);
                    FileStatus[] statusList = fileSystem.globStatus(path);
                    if (statusList == null || statusList.length == 0) {
                        throw new FileNotFoundException(MessageFormat.format(
                                "File is not found in {1} (fs={0})",
                                fileSystem.getUri(),
                                paths));
                    }
                    for (FileStatus status : statusList) {
                        WGLOG.info("I09002",
                                fileSystem.getUri(),
                                status.getPath(),
                                status.getLen());
                        ModelInput<T> input = TemporaryStorage.openInput(
                                configuration,
                                dataModelClass,
                                status.getPath());
                        boolean succeed = false;
                        try {
                            queue.put(new Entry<>(status, input));
                            succeed = true;
                        } finally {
                            if (succeed == false) {
                                input.close();
                            }
                        }
                    }
                }
                queue.put(Entry.<T>eof());
                return null;
            }
        });
    }

    @Override
    public boolean next() throws IOException {
        closeCurrent();
        Entry<T> next = fetchNext();
        if (next == Entry.EOF) {
            return false;
        }
        current = next;
        return true;
    }

    private Entry<T> fetchNext() throws IOException {
        if (sawEof) {
            return Entry.eof();
        }
        try {
            while (true) {
                Entry<T> next = queue.poll(1, TimeUnit.SECONDS);
                if (next != null) {
                    return next;
                } else if (fetcher.isDone()) {
                    break;
                }
            }
            fetcher.get();
            sawEof = true;
            return Entry.eof();
        } catch (InterruptedException e) {
            throw new IOException("Operation was interrupted", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw (Error) cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof IOException) {
                throw (IOException) cause;
            } else if (cause instanceof InterruptedException) {
                throw new IOException("Operation was interrupted", cause);
            }
            throw new AssertionError(e);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public ModelInput<T> open() throws IOException {
        if (current == null) {
            throw new IOException("Current temporary file is not prepared");
        }
        ModelInput<T> result = current.input;
        current = null;
        return result;
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        fetcher.cancel(true);
        executor.shutdown();
        closed = true;
        closeCurrent();
        while (true) {
            Entry<T> next = queue.poll();
            if (next == null || next == Entry.EOF) {
                break;
            }
            try {
                next.input.close();
            } catch (IOException e) {
                WGLOG.warn(e, "W09001", fileSystem.getUri(), next.status.getPath());
            }
        }
    }

    private void closeCurrent() {
        if (current != null) {
            try {
                current.input.close();
                current = null;
            } catch (IOException e) {
                WGLOG.warn(e, "W09001", fileSystem.getUri(), current.status.getPath());
            }
        }
    }

    private static class Entry<T> {

        static final Entry<?> EOF = new Entry<>(null, null);

        final FileStatus status;

        final ModelInput<T> input;

        Entry(FileStatus status, ModelInput<T> input) {
            this.status = status;
            this.input = input;
        }

        @SuppressWarnings("unchecked")
        static <T> Entry<T> eof() {
            return (Entry<T>) EOF;
        }
    }
}
