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
package com.asakusafw.windgate.hadoopfs.sequencefile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.hadoopfs.HadoopFsLogger;

/**
 * An implementation of {@link SequenceFileProvider} using {@link FileSystem}.
 * @since 0.2.2
 */
public class FileSystemSequenceFileProvider implements SequenceFileProvider {

    static final WindGateLogger WGLOG = new HadoopFsLogger(FileSystemSequenceFileProvider.class);

    static final Logger LOG = LoggerFactory.getLogger(FileSystemSequenceFileProvider.class);

    private final Configuration configuration;

    private final FileSystem fileSystem;

    private final Iterator<FileStatus> status;

    private FileStatus current;

    /**
     * Creates a new instance.
     * @param configuration the configuration
     * @param fileSystem target file system
     * @param paths source paths
     * @throws IOException if failed to resolve paths
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileSystemSequenceFileProvider(
            Configuration configuration,
            FileSystem fileSystem,
            Iterable<Path> paths) throws IOException {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        if (fileSystem == null) {
            throw new IllegalArgumentException("fileSystem must not be null"); //$NON-NLS-1$
        }
        if (paths == null) {
            throw new IllegalArgumentException("paths must not be null"); //$NON-NLS-1$
        }
        this.configuration = configuration;
        this.fileSystem = fileSystem;
        List<FileStatus> files = new ArrayList<FileStatus>();
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
            Collections.addAll(files, statusList);
        }
        this.status = files.iterator();
    }

    @Override
    public boolean next() throws IOException {
        if (status.hasNext()) {
            current = status.next();
            return true;
        }
        current = null;
        return false;
    }

    @Override
    public SequenceFile.Reader open() throws IOException {
        if (current == null) {
            throw new IOException("Current sequence file is not prepared");
        }
        WGLOG.info("I09002",
                fileSystem.getUri(),
                current.getPath(),
                current.getLen());
        SequenceFile.Reader reader = new SequenceFile.Reader(fileSystem, current.getPath(), configuration);
        return reader;
    }

    @Override
    public void close() throws IOException {
        while (next()) {
            // do nothing
        }
    }
}
