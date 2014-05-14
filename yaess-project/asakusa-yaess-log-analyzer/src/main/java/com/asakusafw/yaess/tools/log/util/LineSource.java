/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.yaess.tools.log.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.NoSuchElementException;

import com.asakusafw.utils.io.Source;

/**
 * Provides lines from a character stream.
 * @since 0.6.2
 */
public class LineSource implements Source<String> {

    private final BufferedReader reader;

    private volatile String nextLine;

    /**
     * Creates a new instance.
     * @param reader the original reader
     */
    public LineSource(Reader reader) {
        this.reader = toBufferedReader(reader);
    }

    private static BufferedReader toBufferedReader(Reader reader) {
        if (reader instanceof BufferedReader) {
            return (BufferedReader) reader;
        }
        return new BufferedReader(reader);
    }

    @Override
    public boolean next() throws IOException {
        this.nextLine = reader.readLine();
        return nextLine != null;
    }

    @Override
    public String get() throws IOException {
        String next = nextLine;
        if (next == null) {
            throw new NoSuchElementException();
        }
        return next;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
