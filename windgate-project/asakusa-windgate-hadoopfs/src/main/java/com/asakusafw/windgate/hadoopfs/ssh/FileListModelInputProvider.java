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
package com.asakusafw.windgate.hadoopfs.ssh;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.stage.temporary.TemporaryFileInput;
import com.asakusafw.windgate.hadoopfs.temporary.ModelInputProvider;

/**
 * An implementation of {@link ModelInputProvider} using {@link FileList}.
 * @param <T> target data model type
 * @since 0.2.5
 * @version 0.7.4
 */
public class FileListModelInputProvider<T> implements ModelInputProvider<T> {

    static final Logger LOG = LoggerFactory.getLogger(FileListModelInputProvider.class);

    @SuppressWarnings("unused")
    private final Configuration conf;

    private final FileList.Reader fileList;

    @SuppressWarnings("unused")
    private final Class<T> dataModelClass;

    /**
     * Creates a new instance.
     * @param conf the configuration
     * @param fileList target file list
     * @param dataModelClass target data model class
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileListModelInputProvider(Configuration conf, FileList.Reader fileList, Class<T> dataModelClass) {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (fileList == null) {
            throw new IllegalArgumentException("fileList must not be null"); //$NON-NLS-1$
        }
        if (dataModelClass == null) {
            throw new IllegalArgumentException("dataModelClass must not be null"); //$NON-NLS-1$
        }
        this.conf = conf;
        this.fileList = fileList;
        this.dataModelClass = dataModelClass;
    }

    @Override
    public boolean next() throws IOException {
        return fileList.next();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ModelInput<T> open() throws IOException {
        Path path = fileList.getCurrentPath();
        InputStream content = fileList.openContent();
        boolean succeeded = false;
        try {
            LOG.debug("Opening next temporary file: {}", path);
            // FIXME should use TemporaryStorage.openInput()
            ModelInput<Writable> input = new TemporaryFileInput<Writable>(content, 0);
            succeeded = true;
            return (ModelInput<T>) input;
        } finally {
            if (succeeded == false) {
                content.close();
            }
        }
    }

    @Override
    public void close() throws IOException {
        LOG.debug("Closing temporary file list");
        fileList.close();
    }
}
