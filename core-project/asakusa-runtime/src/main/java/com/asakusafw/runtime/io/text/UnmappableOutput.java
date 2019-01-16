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
package com.asakusafw.runtime.io.text;

import java.io.Serializable;
import java.text.MessageFormat;

/**
 * Represents an unmappable output.
 * @since 0.9.1
 */
public class UnmappableOutput implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;

    private final int fieldIndex;

    private final String sequence;

    /**
     * Creates a new instance.
     * @param errorCode the error code
     * @param fieldIndex the 0-origin field index where this error was occurred
     * @param sequence the erroneous sequence (nullable)
     */
    public UnmappableOutput(ErrorCode errorCode, int fieldIndex, String sequence) {
        this.errorCode = errorCode;
        this.fieldIndex = fieldIndex;
        this.sequence = sequence;
    }

    /**
     * Returns the error code.
     * @return the error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the field index where this error was occurred.
     * @return the field index (0-origin)
     */
    public int getFieldIndex() {
        return fieldIndex;
    }

    /**
     * Returns the erroneous sequence.
     * @return the sequence, or {@code null} if it is not defined
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Returns the textual reason of this error.
     * @return the error message
     */
    public String getReason() {
        return errorCode.toReason(sequence);
    }

    @Override
    public String toString() {
        if (sequence != null) {
            return String.format("%s(%s)@%d", errorCode, TextUtil.quote(sequence), fieldIndex); //$NON-NLS-1$
        } else {
            return String.format("%s@%d", errorCode, fieldIndex); //$NON-NLS-1$
        }
    }

    /**
     * Represents an error code of {@link UnmappableOutput}.
     * @since 0.9.1
     */
    public enum ErrorCode {

        /**
         * An extra empty field was generated.
         */
        EXTRA_EMPTY_FIELD("empty line generated extra empty field"),

        /**
         * An extra field separator was occurred in field contents.
         */
        EXTRA_FIELD_SEPARATOR("value contains a bare field separator"),

        /**
         * An extra record separator was occurred in field contents.
         */
        EXTRA_RECORD_SEPARATOR("value contains a bare record separator"),

        /**
         * The trailing field separator was lost.
         */
        LOST_FIELD_SEPARATOR("value ends with escape character removed a field separator"),

        /**
         * The trailing record separator was lost.
         */
        LOST_RECORD_SEPARATOR("value ends with escape character removed a record separator"),

        /**
         * {@code NULL} format was not defined.
         */
        UNDEFINED_NULL_SEQUENCE("null sequence was not defined"),

        /**
         * Output sequence conflicts with another sequence.
         */
        CONFLICT_SEQUENCE("{0} represents other sequence", true),

        /**
         * The output sequence is restricted.
         */
        RESTRICTED_SEQUENCE("{0} is restricted", true),
        ;

        private final String message;

        private final boolean formatted;

        ErrorCode(String message) {
            this(message, false);
        }

        ErrorCode(String message, boolean formatted) {
            this.message = message;
            this.formatted = formatted;
        }

        String toReason(Object argument) {
            if (formatted) {
                return MessageFormat.format(message, argument);
            } else {
                return message;
            }
        }
    }
}
