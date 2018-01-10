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
package com.asakusafw.yaess.core;

import java.io.IOException;
import java.io.InputStream;

/**
 * An abstract super interface of binary large objects.
 * @since 0.8.0
 */
public interface Blob {

    /**
     * The default {@link #getFileExtension() file extension}.
     */
    String DEFAULT_FILE_EXTENSION = "blob"; //$NON-NLS-1$

    /**
     * Returns an {@link InputStream} for obtaining the BLOB contents.
     * @return the {@link InputStream}
     * @throws IOException if I/O error was occurred while opening the contents
     */
    InputStream open() throws IOException;

    /**
     * Returns the object size in bytes.
     * @return the object size in bytes
     * @throws IOException if I/O error was occurred while obtaining the object size
     */
    long getSize() throws IOException;

    /**
     * Returns the file extension for this object.
     * If the target BLOB has no explicit file extension, the {@link #DEFAULT_FILE_EXTENSION} will be returned.
     * @return the file extension, never {@code null}
     */
    String getFileExtension();
}
