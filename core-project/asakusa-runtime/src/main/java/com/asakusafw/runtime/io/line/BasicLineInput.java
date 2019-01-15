/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.text.MessageFormat;

import org.apache.hadoop.io.Text;

import com.asakusafw.runtime.value.StringOption;

/**
 * A simple line reader for text with any charset encoding.
 * @since 0.7.5
 */
public class BasicLineInput extends LineInput {

    private static final Text EMPTY = new Text();

    private static final int MIN_BUFFER_SIZE = 256;

    private final Reader reader;

    private final String path;

    private final CharsetEncoder encoder;

    private final CharBuffer charBuffer;

    private final ByteBuffer byteBuffer;

    private boolean sawCr;

    private boolean sawEof;

    private long lineNumber;

    /**
     * Creates a new instance.
     * @param stream the source stream
     * @param path the source path
     * @param configuration the current configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #newInstance(InputStream, String, LineConfiguration)
     */
    public BasicLineInput(InputStream stream, String path, LineConfiguration configuration) {
        if (stream == null) {
            throw new IllegalArgumentException("stream must not be null"); //$NON-NLS-1$
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        this.reader = new InputStreamReader(stream, configuration.getCharset());
        this.path = path;
        this.encoder = INTERNAL_CHARSET.newEncoder()
                .onMalformedInput(configuration.getMalformedInputAction())
                .onUnmappableCharacter(configuration.getUnmappableCharacterAction());
        this.charBuffer = CharBuffer.wrap(new char[Math.max(MIN_BUFFER_SIZE, configuration.getBufferSize())]);
        this.charBuffer.clear();
        this.charBuffer.flip();
        this.byteBuffer = ByteBuffer.wrap(new byte[Math.max(MIN_BUFFER_SIZE, configuration.getBufferSize()) / 2]);
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
                return false;
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
        CharBuffer b = charBuffer;
        if (b.hasRemaining()) {
            // already prepared
            return true;
        }
        b.clear();
        while (true) {
            int read = reader.read(b);
            if (read < 0) {
                sawEof = true;
                return false;
            } else if (read > 0) {
                b.flip();
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

    private State appendBufferTo(Text entity) throws IOException {
        CharBuffer b = charBuffer;
        assert b.hasRemaining();
        char[] cs = charBuffer.array();
        // skip LF after CR
        if (sawCr && cs[b.position()] == '\n') {
            b.position(b.position() + 1);
        }
        boolean eol = false;
        int len = 0;
        // scan buffer until CR/LF/buffer limit
        for (int i = b.position(), n = b.limit(); i < n; i++) {
            char c = cs[i];
            if (c == '\r' || c == '\n') {
                eol = true;
                sawCr = c == '\r';
                break;
            } else {
                len++;
            }
        }
        if (len > 0) {
            append(entity, len);
        }
        // skip next LF
        if (eol) {
            b.position(b.position() + 1);
        }
        if (len == 0) {
            return eol ? State.LINE_BREAK : State.NOTHING;
        } else {
            return eol ? State.LINE_BREAK : State.CONTINUE;
        }
    }

    private void append(Text entity, int len) throws IOException {
        ByteBuffer bs = byteBuffer;
        CharBuffer cs = charBuffer;
        int limit = cs.limit();
        // slice the buffer
        cs.limit(cs.position() + len);
        while (true) {
            bs.clear();
            CoderResult result = encoder.encode(cs, bs, false);
            if (result.isError() == false) {
                bs.flip();
                entity.append(bs.array(), bs.position(), bs.limit());
                if (result.isUnderflow()) {
                    break;
                }
            } else {
                assert result.isError();
                try {
                    result.throwException();
                } catch (CharacterCodingException e) {
                    throw new IOException(MessageFormat.format(
                            "exception occurred while encoding text: {0}",
                            path), e);
                }
            }
        }
        cs.limit(limit);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private enum State {

        NOTHING,

        CONTINUE,

        LINE_BREAK,
    }
}
