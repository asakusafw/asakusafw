/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.hadoopfs.HadoopFsLogger;

/**
 * Puts files onto Hadoop File System from {@link FileList} in the standard input.
 * @since 0.2.2
 */
public class WindGateHadoopPut {

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
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static void main(String[] args) {
        if (args == null) {
            throw new IllegalArgumentException("args must not be null"); //$NON-NLS-1$
        }
        WGLOG.info("I21000");
        long start = System.currentTimeMillis();
        Configuration conf = new Configuration();
        int result = new WindGateHadoopPut(conf).execute(args);
        long end= System.currentTimeMillis();
        WGLOG.info("I21999", result, end - start);
        System.exit(result);
    }

    int execute(String... args) {
        assert args != null;
        if (args.length != 0) {
            WGLOG.error("E21001",
                    Arrays.asList(args));
            System.err.printf("usage: java -classpath ... %s%n",
                    WindGateHadoopPut.class.getName());
            return 1;
        }
        try {
            WGLOG.info("I21001");
            FileList.Reader reader = FileList.createReader(new BufferedInputStream(System.in, BUFFER_SIZE));
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
            FileStatus status = source.getCurrentFile();
            InputStream input = source.openContent();
            try {
                doPut(fs, status, input);
            } finally {
                input.close();
            }
        }
    }

    private void doPut(FileSystem fs, FileStatus status, InputStream input) throws IOException {
        assert fs != null;
        assert status != null;
        assert input != null;
        WGLOG.info("I21003",
                fs.getUri(),
                status.getPath());
        long transferred = 0;
        FSDataOutputStream output = fs.create(status.getPath(), true, BUFFER_SIZE);
        try {
            try {
                byte[] buf = new byte[256];
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
        } finally {
            output.close();
        }
        WGLOG.info("I21004",
                fs.getUri(),
                status.getPath(),
                transferred);
    }
}
