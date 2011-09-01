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
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * Deletes files from Hadoop File System and report them as {@link FileList} to the standard output.
 * @since 0.2.3
 */
public class WindGateHadoopDelete {

    private static final int BUFFER_SIZE = 1024 * 1024;

    private final Configuration conf;

    private static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * Creates a new instance.
     * @param conf the configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public WindGateHadoopDelete(Configuration conf) {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        this.conf = conf;
    }

    /**
     * Program entry.
     * This writes results {@link FileList} into the standard output.
     * @param args the arguments
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static void main(String[] args) {
        Configuration conf = new Configuration();
        int result = new WindGateHadoopDelete(conf).execute(args);
        System.exit(result);
    }

    int execute(String... args) {
        assert args != null;
        if (args.length == 0) {
            System.err.printf("usage: java -classpath ... %s file1 [file2 ...]%n",
                    WindGateHadoopDelete.class.getName());
            return 1;
        }
        List<Path> paths = new ArrayList<Path>();
        for (String arg : args) {
            paths.add(new Path(arg));
        }
        FileList.Writer writer;
        try {
            writer = FileList.createWriter(new BufferedOutputStream(System.out, BUFFER_SIZE));
        } catch (IOException e) {
            // TODO logging
            e.printStackTrace(System.err);
            return 1;
        }
        try {
            doDelete(paths, writer);
            writer.close();
            return 0;
        } catch (IOException e) {
            // TODO logging
            e.printStackTrace(System.err);
            return 1;
        }
    }

    void doDelete(List<Path> paths, FileList.Writer drain) throws IOException {
        assert paths != null;
        assert drain != null;
        FileSystem fs = FileSystem.get(conf);
        try {
            for (Path path : paths) {
                // TODO logging
                System.err.printf("Finding %s%n", path);
                FileStatus[] results = fs.globStatus(path);
                if (results == null) {
                    continue;
                }
                for (FileStatus status : results) {
                    doDelete(fs, status, drain);
                }
            }
        } finally {
            fs.close();
        }
    }

    private void doDelete(FileSystem fs, FileStatus status, FileList.Writer drain) throws IOException {
        assert fs != null;
        assert status != null;
        assert drain != null;
        // TODO logging
        System.err.printf("Deleting %s%n", status.getPath());
        OutputStream output = drain.openNext(status);
        try {
            String failReason = null;
            try {
                boolean deleted = fs.delete(status.getPath(), true);
                if (deleted == false) {
                    if (fs.exists(status.getPath())) {
                        failReason = "Unknown";
                    }
                }
            } catch (IOException e) {
                // TODO logging
                e.printStackTrace(System.err);
                failReason = e.toString();
            }
            if (failReason != null) {
                output.write(failReason.getBytes(UTF8));
            }
        } finally {
            output.close();
        }
    }
}
