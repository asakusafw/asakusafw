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
package com.asakusafw.runtime.io.line;

import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;

/**
 * Line based text configurations.
 * @since 0.7.5
 */
public class LineConfiguration {

    /**
     * The default charset.
     * @see #getCharset()
     */
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$

    /**
     * The default buffer size hint in bytes.
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    /**
     * The default charset coding error action type.
     */
    public static final CodingErrorAction DEFAULT_CODING_ERROR_ACTION = CodingErrorAction.REPORT;

    private Charset charset = DEFAULT_CHARSET;

    private int bufferSize = DEFAULT_BUFFER_SIZE;

    private CodingErrorAction malformedInputAction = DEFAULT_CODING_ERROR_ACTION;

    private CodingErrorAction unmappableCharacterAction = DEFAULT_CODING_ERROR_ACTION;

    /**
     * Returns the text charset.
     * @return the text charset
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Sets the text charset.
     * @param newValue the value
     * @return this
     */
    public LineConfiguration withCharset(Charset newValue) {
        this.charset = newValue == null ? DEFAULT_CHARSET : newValue;
        return this;
    }

    /**
     * Returns the buffer size (hint) in bytes.
     * @return the buffer size (hint)
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Sets the buffer size (hint) in bytes.
     * @param newValue the value
     * @return this
     */
    public LineConfiguration withBufferSize(int newValue) {
        this.bufferSize = newValue;
        return this;
    }

    /**
     * Returns the action type for malformed inputs.
     * @return the action type
     */
    public CodingErrorAction getMalformedInputAction() {
        return malformedInputAction;
    }

    /**
     * Sets the action type for malformed inputs.
     * @param newValue the value
     * @return this
     */
    public LineConfiguration withMalformedInputAction(CodingErrorAction newValue) {
        this.malformedInputAction = newValue == null ? DEFAULT_CODING_ERROR_ACTION : newValue;
        return this;
    }

    /**
     * Returns the action type for unmappable inputs.
     * @return the action type
     */
    public CodingErrorAction getUnmappableCharacterAction() {
        return unmappableCharacterAction;
    }

    /**
     * Sets the action type for unmappable inputs.
     * @param newValue the value
     * @return this
     */
    public LineConfiguration withUnmappableCharacterAction(CodingErrorAction newValue) {
        this.unmappableCharacterAction = newValue == null ? DEFAULT_CODING_ERROR_ACTION : newValue;
        return this;
    }
}
