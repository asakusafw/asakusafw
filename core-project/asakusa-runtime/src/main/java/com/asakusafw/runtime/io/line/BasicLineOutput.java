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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.text.MessageFormat;

import org.apache.hadoop.io.Text;

import com.asakusafw.runtime.value.StringOption;

/**
 * A simple line writer for text with any charset encoding.
 * @since 0.7.5
 */
public class BasicLineOutput extends LineOutput {

    private static final int MIN_BUFFER_SIZE = 256;

    private final Writer writer;

    private final String path;

    private final CharsetDecoder decoder;

    private final CharBuffer charBuffer;

    private ByteBuffer wrapperCache;

    /**
     * Creates a new instance.
     * @param stream the target stream
     * @param path the destination path
     * @param configuration current configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #newInstance(OutputStream, String, LineConfiguration)
     */
    public BasicLineOutput(OutputStream stream, String path, LineConfiguration configuration) {
        if (stream == null) {
            throw new IllegalArgumentException("stream must not be null"); //$NON-NLS-1$
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        this.writer = new OutputStreamWriter(stream, configuration.getCharset());
        this.path = path;
        this.decoder = INTERNAL_CHARSET.newDecoder()
                .onMalformedInput(configuration.getMalformedInputAction())
                .onUnmappableCharacter(configuration.getUnmappableCharacterAction());
        this.charBuffer = CharBuffer.wrap(new char[Math.max(MIN_BUFFER_SIZE, configuration.getBufferSize())]);
    }

    @Override
    public void write(StringOption model) throws IOException {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        if (model.isNull()) {
            return;
        }
        Text entity = model.get();
        write(entity);
        writer.write(LINE_BREAK);
    }

    private void write(Text entity) throws IOException {
        if (entity.getLength() == 0) {
            return;
        }
        ByteBuffer buffer = wrapperCache;
        byte[] b = entity.getBytes();
        if (buffer == null || buffer.array() != b) {
            buffer = ByteBuffer.wrap(b);
            wrapperCache = buffer;
        }
        buffer.position(0);
        buffer.limit(entity.getLength());

        boolean flushing = false;
        CharBuffer cs = charBuffer;
        while (true) {
            cs.clear();
            CoderResult result;
            if (flushing) {
                result = decoder.flush(cs);
            } else {
                result = decoder.decode(buffer, cs, true);
            }
            if (result.isError() == false) {
                cs.flip();
                if (cs.hasRemaining()) {
                    writer.append(cs);
                }
                if (result.isUnderflow()) {
                    if (flushing) {
                        flushing = true;
                    } else {
                        break;
                    }
                }
            } else {
                assert result.isError();
                try {
                    result.throwException();
                } catch (CharacterCodingException e) {
                    throw new IOException(MessageFormat.format(
                            "exception occurred while decoding text: {0}",
                            path), e);
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
