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
package com.asakusafw.windgate.hadoopfs.ssh;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.compatibility.FileSystemCompatibility;
import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.runtime.io.util.VoidInputStream;
import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.hadoopfs.HadoopFsLogger;
import com.asakusafw.windgate.hadoopfs.ssh.FileList.Writer;

/**
 * Gets files from Hadoop File System and write them as {@link FileList} to the standard output.
 * @since 0.2.2
 * @version 0.4.0
 */
public class WindGateHadoopGet {

    static {
        StdioHelper.load();
    }

    static final WindGateLogger WGLOG = new HadoopFsLogger(WindGateHadoopGet.class);

    static final Logger LOG = LoggerFactory.getLogger(WindGateHadoopGet.class);

    static final int BUFFER_SIZE = 1024 * 1024;

    final Configuration conf;

    /**
     * Creates a new instance.
     * @param conf the configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public WindGateHadoopGet(Configuration conf) {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        this.conf = conf;
    }

    /**
     * Program entry.
     * This writes results {@link FileList} into the standard output.
     * @param args list of path to get
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static void main(String[] args) {
        RuntimeContext.set(RuntimeContext.DEFAULT.apply(System.getenv()));
        RuntimeContext.get().verifyApplication(WindGateHadoopGet.class.getClassLoader());
        WGLOG.info("I20000");
        long start = System.currentTimeMillis();
        Configuration conf = new Configuration();
        int result = new WindGateHadoopGet(conf).execute(StdioHelper.getOriginalStdout(), args);
        long end = System.currentTimeMillis();
        WGLOG.info("I20999", result, end - start);
        System.exit(result);
    }

    int execute(OutputStream out, String... args) {
        assert args != null;
        if (args.length == 0) {
            WGLOG.error("E20001",
                    Arrays.toString(args));
            System.err.printf("usage: java -classpath ... %s file1 [file2 ...]%n",
                    WindGateHadoopGet.class.getName());
            return 1;
        }
        List<Path> paths = new ArrayList<Path>();
        for (String arg : args) {
            paths.add(new Path(arg));
        }
        try {
            WGLOG.info("I20001",
                    paths);
            FileList.Writer writer = FileList.createWriter(new BufferedOutputStream(out, BUFFER_SIZE));
            doGet(paths, writer);
            WGLOG.info("I20002",
                    paths);
            writer.close();
            return 0;
        } catch (IOException e) {
            WGLOG.error(e, "E20002",
                    paths);
            return 1;
        } catch (InterruptedException e) {
            WGLOG.error(e, "E20003",
                    paths);
            return 1;
        }
    }

    void doGet(final List<Path> paths, FileList.Writer drain) throws IOException, InterruptedException {
        assert paths != null;
        assert drain != null;
        final BlockingQueue<Pair> queue = new SynchronousQueue<Pair>();
        final FileSystem fs = FileSystem.get(conf);
        ExecutorService executor = Executors.newFixedThreadPool(1);
        try {
            Future<Void> fetcher = executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    fetch(fs, paths, queue);
                    queue.put(Pair.eof());
                    return null;
                }
            });
            while (true) {
                Pair next = queue.poll(1, TimeUnit.SECONDS);
                if (next != null) {
                    if (next.isEof()) {
                        break;
                    } else {
                        transfer(fs, next.status, next.input, drain);
                    }
                } else if (fetcher.isDone()) {
                    break;
                }
            }
            try {
                fetcher.get();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof Error) {
                    throw (Error) cause;
                } else if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else if (cause instanceof IOException) {
                    throw (IOException) cause;
                } else if (cause instanceof InterruptedException) {
                    throw (InterruptedException) cause;
                }
                throw new AssertionError(e);
            } catch (Exception e) {
                throw new IOException(e);
            }
        } finally {
            executor.shutdownNow();
            while (true) {
                Pair next = queue.poll();
                if (next == null) {
                    break;
                }
                try {
                    next.input.close();
                } catch (IOException e) {
                    // ignored
                }
            }
        }
    }

    void fetch(FileSystem fs, List<Path> paths, BlockingQueue<Pair> queue) throws IOException, InterruptedException {
        assert fs != null;
        assert paths != null;
        assert queue != null;
        for (Path path : paths) {
            boolean found = false;
            WGLOG.info("I20003",
                    fs.getUri(),
                    path);
            FileStatus[] results = fs.globStatus(path);
            if (results != null) {
                for (FileStatus status : results) {
                    if (FileSystemCompatibility.isDirectory(status)) {
                        continue;
                    }
                    found = true;
                    InputStream in;
                    if (RuntimeContext.get().isSimulation()) {
                        in = new VoidInputStream();
                    } else {
                        in = fs.open(status.getPath(), BUFFER_SIZE);
                    }
                    boolean succeed = false;
                    try {
                        queue.put(new Pair(in, status));
                        succeed = true;
                    } finally {
                        if (succeed == false) {
                            in.close();
                        }
                    }
                }
            }
            if (found == false && RuntimeContext.get().isSimulation() == false) {
                throw new FileNotFoundException(paths.toString());
            }
        }
    }

    private void transfer(FileSystem fs, FileStatus status, InputStream input, Writer drain) throws IOException {
        assert fs != null;
        assert status != null;
        assert input != null;
        assert drain != null;
        WGLOG.info("I20004",
                fs.getUri(),
                status.getPath());
        long transferred = 0;
        try {
            if (RuntimeContext.get().isSimulation() == false) {
                OutputStream output = drain.openNext(status);
                try {
                    byte[] buf = new byte[1024];
                    while (true) {
                        int read = input.read(buf);
                        if (read < 0) {
                            break;
                        }
                        output.write(buf, 0, read);
                        transferred += read;
                    }
                } finally {
                    output.close();
                }
            }
        } finally {
            input.close();
        }
        WGLOG.info("I20005",
                fs.getUri(),
                status.getPath(),
                transferred);
    }

    private static class Pair {

        final InputStream input;

        final FileStatus status;

        Pair(InputStream input, FileStatus status) {
            this.input = input;
            this.status = status;
        }

        static Pair eof() {
            return new Pair(null, null);
        }

        boolean isEof() {
            return input == null && status == null;
        }
    }
}
