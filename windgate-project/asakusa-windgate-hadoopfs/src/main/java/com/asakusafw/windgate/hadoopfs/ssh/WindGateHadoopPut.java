/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.runtime.io.util.VoidOutputStream;
import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.hadoopfs.HadoopFsLogger;

/**
 * Puts files onto Hadoop File System from {@link FileList} in the standard input.
 * @since 0.2.2
 * @version 0.4.0
 */
public class WindGateHadoopPut extends WindGateHadoopBase {

    static final WindGateLogger WGLOG = new HadoopFsLogger(WindGateHadoopPut.class);

    static final Logger LOG = LoggerFactory.getLogger(WindGateHadoopPut.class);

    private static final int BUFFER_SIZE = 1024 * 1024;

    private final Configuration conf;

    /**
     * Creates a new instance.
     * @param conf the configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public WindGateHadoopPut(Configuration conf) {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        this.conf = conf;
    }

    /**
     * Program entry.
     * This requires {@link FileList} protocol in standard input.
     * @param args must be empty
     */
    public static void main(String[] args) {
        RuntimeContext.set(RuntimeContext.DEFAULT.apply(System.getenv()));
        RuntimeContext.get().verifyApplication(WindGateHadoopPut.class.getClassLoader());
        WGLOG.info("I21000");
        long start = System.currentTimeMillis();
        Configuration conf = new Configuration();
        int result = new WindGateHadoopPut(conf).execute(StdioHelper.getOriginalStdin(), args);
        long end = System.currentTimeMillis();
        WGLOG.info("I21999", result, end - start);
        System.exit(result);
    }

    int execute(InputStream in, String... args) {
        assert args != null;
        if (args.length != 0) {
            WGLOG.error("E21001",
                    Arrays.asList(args));
            System.err.printf("usage: java -classpath ... %s%n",
                    WindGateHadoopPut.class.getName());
            return 1;
        }
        WGLOG.info("I21001");
        try (FileList.Reader reader = FileList.createReader(new BufferedInputStream(in, BUFFER_SIZE))) {
            doPut(reader);
            WGLOG.info("I21002");
            reader.close();
            return 0;
        } catch (IOException e) {
            WGLOG.info(e, "I21002");
            return 1;
        }
    }

    void doPut(FileList.Reader source) throws IOException {
        assert source != null;
        FileSystem fs = FileSystem.get(conf);
        while (source.next()) {
            Path path = source.getCurrentPath();
            try (InputStream input = source.openContent()) {
                doPut(fs, path, input);
            }
        }
    }

    private void doPut(FileSystem fs, Path path, InputStream input) throws IOException {
        assert fs != null;
        assert path != null;
        assert input != null;
        WGLOG.info("I21003",
                fs.getUri(),
                path);
        long transferred = 0;
        try (OutputStream output = getOutput(fs, path)) {
            byte[] buf = new byte[256];
            while (true) {
                int read = input.read(buf);
                if (read < 0) {
                    break;
                }
                output.write(buf, 0, read);
                transferred += read;
            }
        }
        WGLOG.info("I21004",
                fs.getUri(),
                path,
                transferred);
    }

    private OutputStream getOutput(FileSystem fs, Path path) throws IOException {
        if (RuntimeContext.get().isSimulation()) {
            return new VoidOutputStream();
        } else {
            return fs.create(path, true, BUFFER_SIZE);
        }
    }
}
