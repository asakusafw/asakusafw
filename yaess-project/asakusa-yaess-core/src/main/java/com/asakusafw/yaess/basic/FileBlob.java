/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.yaess.basic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.asakusafw.yaess.core.Blob;

/**
 * An implementation of {@link Blob} which data is stored on a local file.
 * @since 0.8.0
 */
public class FileBlob implements Blob {

    private final File file;

    /**
     * Creates a new instance.
     * @param file the target file
     */
    public FileBlob(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        this.file = file;
    }

    /**
     * Returns the file.
     * @return the file
     */
    public File getFile() {
        return file;
    }

    @Override
    public InputStream open() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public long getSize() throws IOException {
        return file.length();
    }

    @Override
    public String getFileExtension() {
        String name = file.getName();
        int at = name.lastIndexOf('.');
        return at > 0 ? name.substring(at + 1) : DEFAULT_FILE_EXTENSION;
    }

    @Override
    public String toString() {
        return file.getPath();
    }
}
