/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.utils.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Provides {@link FileInputStream}.
 * @since 0.6.0
 */
public class FileInputStreamProvider implements Provider<FileInputStream> {

    private final File file;

    /**
     * Creates a new instance.
     * @param file the source file
     */
    public FileInputStreamProvider(File file) {
        this.file = file;
    }

    @Override
    public FileInputStream open() throws IOException, InterruptedException {
        return new FileInputStream(file);
    }

    @Override
    public void close() throws IOException {
        return;
    }

    @Override
    public String toString() {
        return file.getPath();
    }
}
