/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.utils.io.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.asakusafw.utils.io.Source;

/**
 * A simple implementation of CSV reader.
 * @since 0.6.2
 */
public class CsvReader implements Source<List<String>> {

    private final Reader reader;

    private List<String> next;

    /**
     * Creates a new instance.
     * @param reader the CSV source
     */
    public CsvReader(Reader reader) {
        this.reader = reader instanceof BufferedReader ? reader : new BufferedReader(reader);
    }

    @Override
    public boolean next() throws IOException {
        this.next = prepare();
        return next != null;
    }

    private List<String> prepare() throws IOException {
        List<String> line = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        State state = State.INIT;
        LOOP: while (true) {
            int c = reader.read();
            if (c < 0) {
                c = -1;
            }
            switch (c) {
            case '"':
                switch (state) {
                case INIT:
                    state = State.ESCAPE_BODY;
                    break;
                case BODY:
                    // invalid case
                    state = State.ESCAPE_BODY;
                    break;
                case ESCAPE_BODY:
                    state = State.ESCAPE_QUOTE;
                    break;
                case ESCAPE_QUOTE:
                    state = State.ESCAPE_BODY;
                    buf.append('"');
                    break;
                default:
                    throw new AssertionError();
                }
                break;
            case ',':
                switch (state) {
                case INIT:
                case BODY:
                case ESCAPE_QUOTE:
                    // end of field
                    state = State.INIT;
                    line.add(buf.toString());
                    buf.setLength(0);
                    break;
                case ESCAPE_BODY:
                    buf.append(',');
                    break;
                default:
                    throw new AssertionError();
                }
                break;
            case '\r':
                switch (state) {
                case ESCAPE_BODY:
                    buf.append('\r');
                    break;
                default:
                    // ignore CR
                    break;
                }
                break;
            case '\n':
                switch (state) {
                case INIT:
                case BODY:
                case ESCAPE_QUOTE:
                    // end of record
                    line.add(buf.toString());
                    buf.setLength(0);
                    break LOOP;
                case ESCAPE_BODY:
                    buf.append('\n');
                    break;
                default:
                    throw new AssertionError();
                }
                break;
            case -1:
                switch (state) {
                case INIT:
                    // end of record w/o contents
                    break LOOP;
                case BODY:
                case ESCAPE_QUOTE:
                    // end of record w/ contents
                    line.add(buf.toString());
                    buf.setLength(0);
                    break LOOP;
                case ESCAPE_BODY:
                    // invalid state
                    line.add(buf.toString());
                    buf.setLength(0);
                    break LOOP;
                default:
                    throw new AssertionError();
                }
            default:
                switch (state) {
                case INIT:
                case BODY:
                    state = State.BODY;
                    buf.append((char) c);
                    break;
                case ESCAPE_BODY:
                    state = State.ESCAPE_BODY;
                    buf.append((char) c);
                    break;
                case ESCAPE_QUOTE:
                    // invalid state
                    state = State.BODY;
                    buf.append((char) c);
                    break;
                default:
                    throw new AssertionError();
                }
                break;
            }
        }
        if (line.isEmpty()) {
            return null;
        }
        return line;
    }

    @Override
    public List<String> get() throws IOException {
        return next;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private enum State {

        INIT,

        BODY,

        ESCAPE_BODY,

        ESCAPE_QUOTE,
    }
}
