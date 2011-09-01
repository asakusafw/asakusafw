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

import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;

/**
 * An implementation of {@link SequenceFileProvider} using {@link FileSystem}.
 * @since 0.2.3
 */
public class FileSystemSequenceFileProvider implements SequenceFileProvider {

    private final Configuration configuration;

    private final FileSystem fileSystem;

    private final Iterator<Path> paths;

    private Path current;

    /**
     * Creates a new instance.
     * @param configuration the configuration
     * @param fileSystem target file system
     * @param paths source paths
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FileSystemSequenceFileProvider(
            Configuration configuration,
            FileSystem fileSystem,
            Iterable<Path> paths) {
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
        this.paths = paths.iterator();
    }

    @Override
    public boolean next() throws IOException {
        if (paths.hasNext()) {
            current = paths.next();
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
        return new SequenceFile.Reader(fileSystem, current, configuration);
    }

    @Override
    public void close() throws IOException {
        while (next()) {
            // do nothing
        }
    }
}
