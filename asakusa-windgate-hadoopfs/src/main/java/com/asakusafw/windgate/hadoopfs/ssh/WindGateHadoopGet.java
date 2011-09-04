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

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.hadoopfs.HadoopFsLogger;

/**
 * Gets files from Hadoop File System and write them as {@link FileList} to the standard output.
 * @since 0.2.2
 */
public class WindGateHadoopGet {

    static final WindGateLogger WGLOG = new HadoopFsLogger(WindGateHadoopGet.class);

    static final Logger LOG = LoggerFactory.getLogger(WindGateHadoopGet.class);

    private static final int BUFFER_SIZE = 1024 * 1024;

    private final Configuration conf;

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
        WGLOG.info("I20000");
        Configuration conf = new Configuration();
        int result = new WindGateHadoopGet(conf).execute(args);
        WGLOG.info("I20999", result);
        System.exit(result);
    }

    int execute(String... args) {
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
            FileList.Writer writer = FileList.createWriter(new BufferedOutputStream(System.out, BUFFER_SIZE));
            doGet(paths, writer);
            WGLOG.info("I20002",
                    paths);
            writer.close();
            return 0;
        } catch (IOException e) {
            WGLOG.error(e, "E20002",
                    paths);
            return 1;
        }
    }

    void doGet(List<Path> paths, FileList.Writer drain) throws IOException {
        assert paths != null;
        assert drain != null;
        FileSystem fs = FileSystem.get(conf);
        try {
            for (Path path : paths) {
                boolean found = false;
                WGLOG.info("I20003",
                        fs.getUri(),
                        path);
                FileStatus[] results = fs.globStatus(path);
                if (results != null) {
                    for (FileStatus status : results) {
                        if (status.isDir()) {
                            continue;
                        }
                        found = true;
                        doGet(fs, status, drain);
                    }
                }
                if (found == false) {
                    throw new FileNotFoundException(paths.toString());
                }
            }
        } finally {
            fs.close();
        }
    }

    private void doGet(FileSystem fs, FileStatus status, FileList.Writer drain) throws IOException {
        assert fs != null;
        assert status != null;
        assert drain != null;
        WGLOG.info("I20004",
                fs.getUri(),
                status.getPath());
        long transferred = 0;
        FSDataInputStream stream = fs.open(status.getPath(), BUFFER_SIZE);
        try {
            OutputStream output = drain.openNext(status);
            try {
                byte[] buf = new byte[256];
                while (true) {
                    int read = stream.read(buf);
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
            stream.close();
        }
        WGLOG.info("I20005",
                fs.getUri(),
                status.getPath(),
                transferred);
    }
}
