/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.bulkloader.transfer;

import java.io.Closeable;
import java.io.IOException;

/**
 * An abstract interface of {@link FileList} provider.
 * @since 0.2.3
 */
public interface FileListProvider extends Closeable {

    /**
     * Opens file list reader via this requestor.
     * @return the file list
     * @throws IOException if failed to open the file list
     * @see #discardReader()
     */
    FileList.Reader openReader() throws IOException;

    /**
     * Opens file list writer via this requestor.
     * @param compress if compress the file list
     * @return the file list
     * @throws IOException if failed to open the file list
     * @see #discardWriter()
     */
    FileList.Writer openWriter(boolean compress) throws IOException;

    /**
     * Discards file list reader for this requestor.
     * @throws IOException if failed to discard
     * @see #openReader()
     */
    void discardReader() throws IOException;

    /**
     * Discards file list writer for this requestor.
     * @throws IOException if failed to discard
     * @see #openWriter(boolean)
     */
    void discardWriter() throws IOException;

    /**
     * Waits for this request is completed.
     * @throws IOException if this request was failed
     * @throws InterruptedException if interrupted
     */
    void waitForComplete() throws IOException, InterruptedException;
}
