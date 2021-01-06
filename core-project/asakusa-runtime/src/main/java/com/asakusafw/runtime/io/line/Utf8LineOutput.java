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
package com.asakusafw.runtime.io.line;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.io.Text;

import com.asakusafw.runtime.value.StringOption;

/**
 * A simple line writer for UTF-8 text.
 * @since 0.7.5
 */
public class Utf8LineOutput extends LineOutput {

    private final OutputStream output;

    /**
     * Creates a new instance.
     * @param stream the target stream
     * @param path the destination path
     * @param configuration current configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see LineOutput#newInstance(OutputStream, String, LineConfiguration)
     */
    public Utf8LineOutput(OutputStream stream, String path, LineConfiguration configuration) {
        if (stream == null) {
            throw new IllegalArgumentException("stream must not be null"); //$NON-NLS-1$
        }
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null"); //$NON-NLS-1$
        }
        if (configuration.getCharset().equals(INTERNAL_CHARSET) == false) {
            throw new IllegalArgumentException("configuration.charset must be UTF-8"); //$NON-NLS-1$
        }
        this.output = stream;
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
        output.write(entity.getBytes(), 0, entity.getLength());
        output.write(LINE_BREAK);
    }

    @Override
    public void close() throws IOException {
        output.close();
    }
}
