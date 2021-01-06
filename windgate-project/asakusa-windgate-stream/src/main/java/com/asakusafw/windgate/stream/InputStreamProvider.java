/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
import java.io.InputStream;

/**
 * Provides {@link InputStream}s.
 * @since 0.2.4
 */
public abstract class InputStreamProvider implements Closeable {

    /**
     * Returns whether a next {@link InputStream} exists,
     * and advances the current stream to the next one.
     * Note that the {@link #openStream() current} stream should be closed by the client.
     * @return {@code true} if the next {@link InputStream} exists, otherwise {@code false}.
     * @throws IOException if failed to obtain next stream
     */
    public abstract boolean next() throws IOException;

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
    public abstract CountingInputStream openStream() throws IOException;
}
