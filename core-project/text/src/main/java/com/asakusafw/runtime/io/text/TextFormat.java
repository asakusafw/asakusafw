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
package com.asakusafw.runtime.io.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Provides {@link FieldReader} and {@link FieldWriter}.
 * @since 0.9.1
 */
public interface TextFormat {

    /**
     * Opens {@link FieldReader} for the given input.
     * @param input the source input stream
     * @return the opened {@link FieldReader}
     * @throws IOException if I/O error was occurred while initializing the reader
     */
    FieldReader open(InputStream input) throws IOException;

    /**
     * Opens {@link FieldWriter} for the given output.
     * @param output the destination output stream
     * @return the opened {@link FieldWriter}
     * @throws IOException if I/O error was occurred while initializing the writer
     */
    FieldWriter open(OutputStream output) throws IOException;

    /**
     * Opens {@link FieldReader} for the given input.
     * @param input the source input reader
     * @return the opened {@link FieldReader}
     * @throws IOException if I/O error was occurred while initializing the reader
     */
    FieldReader open(Reader input) throws IOException;

    /**
     * Opens {@link FieldWriter} for the given output.
     * @param output the destination output writer
     * @return the opened {@link FieldWriter}
     * @throws IOException if I/O error was occurred while initializing the writer
     */
    FieldWriter open(Writer output) throws IOException;
}
