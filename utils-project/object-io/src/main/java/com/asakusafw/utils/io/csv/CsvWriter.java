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
package com.asakusafw.utils.io.csv;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asakusafw.utils.io.RecordWriter;

/**
 * A simple implementation of CSV writer.
 * @since 0.6.2
 */
public class CsvWriter implements RecordWriter {

    private static final Pattern PATTERN_ESCAPE = Pattern.compile("([,\"\r\n])"); //$NON-NLS-1$

    private final Appendable writer;

    private boolean headOfRecord = true;

    private final StringBuilder buffer = new StringBuilder();

    /**
     * Creates a new instance.
     * @param writer the target writer
     */
    public CsvWriter(Appendable writer) {
        this.writer = writer;
    }

    @Override
    public void putField(CharSequence value) throws IOException {
        if (headOfRecord) {
            headOfRecord = false;
        } else {
            writer.append(',');
        }
        writer.append(toFieldValue(value));
    }

    private CharSequence toFieldValue(CharSequence value) {
        Matcher matcher = PATTERN_ESCAPE.matcher(value);
        StringBuilder buf = null;
        int start = 0;
        while (matcher.find(start)) {
            if (buf == null) {
                buf = this.buffer;
                buf.setLength(0);
                buf.append('"');
            }
            int at = matcher.start();
            if (at > start) {
                buf.append(value.subSequence(start, at));
            }
            char c = value.charAt(at);
            if (c == '"') {
                buf.append('"');
            }
            buf.append(c);
            start = matcher.end();
        }
        if (buf == null) {
            return value;
        }
        buf.append(value.subSequence(start, value.length()));
        buf.append('"');
        return buf;
    }

    @Override
    public void putEndOfRecord() throws IOException {
        writer.append("\r\n"); //$NON-NLS-1$
        headOfRecord = true;
    }

    @Override
    public void flush() throws IOException {
        if (writer instanceof Flushable) {
            ((Flushable) writer).flush();
        }
    }

    @Override
    public void close() throws IOException {
        if (writer instanceof Closeable) {
            ((Closeable) writer).close();
        }
    }
}
