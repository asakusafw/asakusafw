/**
 * Copyright 2011 Asakusa Framework Team.
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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;

/**
 * Puts files onto Hadoop File System from {@link FileList} in the standard input.
 * @since 0.2.3
 */
public class WindGateHadoopPut {

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
        Configuration conf = new Configuration();
        int result = new WindGateHadoopPut(conf).execute(args);
        System.exit(result);
    }

    int execute(String... args) {
        assert args != null;
        if (args.length != 0) {
            System.err.printf("usage: java -classpath ... %s%n",
                    WindGateHadoopPut.class.getName());
            return 1;
        }
        FileList.Reader reader;
        try {
            reader = FileList.createReader(new BufferedInputStream(System.in, BUFFER_SIZE));
        } catch (IOException e) {
            // TODO logging
            e.printStackTrace(System.err);
            return 1;
        }
        try {
            doPut(reader);
            reader.close();
            return 0;
        } catch (IOException e) {
            // TODO logging
            e.printStackTrace(System.err);
            return 1;
        }
    }

    void doPut(FileList.Reader source) throws IOException {
        assert source != null;
        FileSystem fs = FileSystem.get(conf);
        try {
            while (source.next()) {
                FileStatus status = source.getCurrentFile();
                InputStream input = source.openContent();
                try {
                    doPut(fs, status, input);
                } finally {
                    input.close();
                }
            }
        } finally {
            fs.close();
        }
    }

    private void doPut(FileSystem fs, FileStatus status, InputStream input) throws IOException {
        assert fs != null;
        assert status != null;
        assert input != null;
        // TODO logging
        System.err.printf("Storing %s%n", status.getPath());
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
                }
            } finally {
                output.close();
            }
        } finally {
            output.close();
        }
    }
}
