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
package com.asakusafw.runtime.io.line;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.io.Text;

import com.asakusafw.runtime.value.StringOption;

/**
 * A simple line reader for UTF-8 text.
 * @since 0.7.5
 */
public class Utf8LineInput extends LineInput {

    private static final Text EMPTY = new Text();

    private static final int MIN_BUFFER_SIZE = 256;

    private final InputStream input;

    private final String path;

    private final byte[] buffer;

    private int bufferOffset;

    private int bufferLimit;

    private boolean sawCr;

    private boolean sawEof;

    private long lineNumber;

    /**
     * Creates a new instance.
     * @param stream the source stream
     * @param path the source path
     * @param configuration the current configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see LineInput#newInstance(InputStream, String, LineConfiguration)
     */
    public Utf8LineInput(InputStream stream, String path, LineConfiguration configuration) {
        if (stream == null) {
            throw new IllegalArgumentException("stream must not be null"); //$NON-NLS-1$
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        if (configuration.getCharset().equals(INTERNAL_CHARSET) == false) {
            throw new IllegalArgumentException("configuration.charset must be UTF-8"); //$NON-NLS-1$
        }
        this.input = stream;
        this.path = path;
        this.buffer = new byte[Math.max(MIN_BUFFER_SIZE, configuration.getBufferSize())];
        this.bufferOffset = 0;
        this.bufferLimit = 0;
        this.sawCr = false;
        this.sawEof = false;
        this.lineNumber = 0;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public long getLineNumber() {
        if (lineNumber <= 0) {
            return -1;
        }
        return lineNumber;
    }

    @Override
    public boolean readTo(StringOption model) throws IOException {
        if (sawEof) {
            return false;
        }
        boolean changed = false;
        Text entity = null;
        while (true) {
            if (prepare() == false) {
                if (changed) {
                    lineNumber++;
                    return true;
                }
                return changed;
            }
            if (entity == null) {
                entity = reset(model);
            }
            assert entity != null;
            State state = appendBufferTo(entity);
            switch (state) {
            case NOTHING:
                continue;
            case CONTINUE:
                changed = true;
                continue;
            case LINE_BREAK:
                lineNumber++;
                return true;
            default:
                throw new AssertionError();
            }
        }
    }

    private boolean prepare() throws IOException {
        if (bufferLimit > bufferOffset) {
            // already prepared (has remaining)
            return true;
        }
        while (true) {
            int read = input.read(buffer);
            if (read < 0) {
                sawEof = true;
                return false;
            } else if (read > 0) {
                bufferOffset = 0;
                bufferLimit = read;
                break;
            }
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    private Text reset(StringOption model) {
        if (model.isNull()) {
            model.modify(EMPTY);
        } else {
            model.get().clear();
        }
        return model.get();
    }

    private State appendBufferTo(Text entity) {
        assert bufferOffset < bufferLimit;
        byte[] b = buffer;
        // skip LF after CR
        if (sawCr && b[bufferOffset] == '\n') {
            bufferOffset++;
        }
        boolean eol = false;
        int lim = bufferLimit;
        int off = bufferOffset;
        int len = 0;
        // scan buffer until CR/LF/buffer limit
        for (int i = bufferOffset; i < lim; i++) {
            byte c = b[i];
            if (c == '\r' || c == '\n') {
                eol = true;
                sawCr = c == '\r';
                break;
            } else {
                len++;
            }
        }

        // advance buffer cursor
        bufferOffset += len + (eol ? 1 : 0);
        assert bufferOffset <= bufferLimit;

        if (len == 0) {
            return eol ? State.LINE_BREAK : State.NOTHING;
        } else {
            entity.append(b, off, len);
            return eol ? State.LINE_BREAK : State.CONTINUE;
        }
    }

    @Override
    public void close() throws IOException {
        input.close();
    }

    private enum State {

        NOTHING,

        CONTINUE,

        LINE_BREAK,
    }
}
