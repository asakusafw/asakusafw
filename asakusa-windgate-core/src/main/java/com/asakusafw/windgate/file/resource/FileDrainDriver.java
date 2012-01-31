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
package com.asakusafw.windgate.file.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.MessageFormat;

import com.asakusafw.windgate.core.resource.DrainDriver;

/**
 * An implementation for {@link DrainDriver} using {@link ObjectOutputStream}.
 * @param <T> the type of target data
 * @since 0.2.2
 */
class FileDrainDriver<T> implements DrainDriver<T> {

    private final File file;

    private ObjectOutputStream output;

    /**
     * Creates a new instance.
     * @param type the type of target data
     * @param file the target file
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public FileDrainDriver(Class<T> type, File file) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (file == null) {
            throw new IllegalArgumentException("file must not be null"); //$NON-NLS-1$
        }
        // this.type = type;
        this.file = file;
    }

    @Override
    public void prepare() throws IOException {
        if (file.exists() && file.delete() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to delete {0}",
                    file));
        }
        boolean green = false;
        FileOutputStream out = new FileOutputStream(file);
        try {
            this.output = new ObjectOutputStream(out);
            green = true;
        } finally {
            if (green == false) {
                out.close();
            }
        }
    }

    @Override
    public void put(T object) throws IOException {
        output.writeObject(object);
    }

    @Override
    public void close() throws IOException {
        if (output != null) {
            output.close();
        }
        this.output = null;
    }
}
