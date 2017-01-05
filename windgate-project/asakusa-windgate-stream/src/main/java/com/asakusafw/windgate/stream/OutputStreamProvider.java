/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.windgate.stream;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Provides {@link OutputStream}s.
 * @since 0.2.4
 */
public abstract class OutputStreamProvider implements Closeable {

    /**
     * Returns the desired each stream size.
     * If the client write data to a stream over this size,
     * the client must must use the {@link #next()} stream instead current one.
     * @return the desired stream size, or {@code 0} to unlimited (default: 0)
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public long getDesiredStreamSize() {
        return 0L;
    }

    /**
     * Advances the current stream to the next one.
     * Note that the {@link #openStream() current} stream should be closed by the client.
     * @throws IOException if failed to obtain next stream
     */
    public abstract void next() throws IOException;

    /**
     * Returns the path about the current stream.
     * @return the path
     * @throws IllegalStateException if the current stream is not prepared
     */
    public abstract String getCurrentPath();

    /**
     * Returns the current stream.
     * @return the opened stream
     * @throws IOException if failed to obtain the current stream
     * @throws IllegalStateException if the current stream is not prepared
     */
    public abstract CountingOutputStream openStream() throws IOException;
}
