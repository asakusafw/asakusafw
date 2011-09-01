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

import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.io.SequenceFile;

import com.asakusafw.windgate.hadoopfs.sequencefile.SequenceFileProvider;
import com.asakusafw.windgate.hadoopfs.sequencefile.SequenceFileUtil;

/**
 * An implementation of {@link SequenceFileProvider} using {@link FileList}.
 * @since 0.2.3
 */
public class FileListSequenceFileProvider implements SequenceFileProvider {

    private final Configuration conf;

    private final FileList.Reader fileList;

    /**
     * Creates a new instance.
     * @param conf the configuration
     * @param fileList target file list
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileListSequenceFileProvider(Configuration conf, FileList.Reader fileList) {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (fileList == null) {
            throw new IllegalArgumentException("fileList must not be null"); //$NON-NLS-1$
        }
        this.conf = conf;
        this.fileList = fileList;
    }

    @Override
    public boolean next() throws IOException {
        return fileList.next();
    }

    @Override
    public SequenceFile.Reader open() throws IOException {
        FileStatus status = fileList.getCurrentFile();
        InputStream content = fileList.openContent();
        boolean succeeded = false;
        try {
            SequenceFile.Reader reader = SequenceFileUtil.openReader(content, status, conf);
            succeeded = true;
            return reader;
        } finally {
            if (succeeded == false) {
                content.close();
            }
        }
    }

    @Override
    public void close() throws IOException {
        fileList.close();
    }
}
