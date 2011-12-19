/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.csv;

import java.io.Serializable;
import java.text.MessageFormat;

import com.asakusafw.runtime.io.RecordFormatException;

/**
 * Represents a CSV format exception.
 * @since 0.2.4
 */
public class CsvFormatException extends RecordFormatException {

    private static final long serialVersionUID = 1L;

    private final Status status;

    /**
     * Creates a new instance.
     * @param status describes this exception
     * @param cause the original cause (nullable)
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CsvFormatException(Status status, Throwable cause) {
        super(toMessage(status), cause);
        this.status = status;
    }

    private static String toMessage(Status status) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null"); //$NON-NLS-1$
        }
        return status.toString();
    }

    /**
     * Returns the status of this exception.
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * The reason of CSV format exception.
     * @since 0.2.4
     */
    public enum Reason {

        /**
         * Unexpected EOF is appearred (may be in double-quote).
         */
        UNEXPECTED_EOF,

        /**
         * Unexpected character is appearred after quote was closed.
         */
        CHARACTER_AFTER_QUOTE,

        /**
         * Invalid cell format.
         */
        INVALID_CELL_FORMAT,

        /**
         * Too short cells in a record.
         */
        TOO_SHORT_RECORD,

        /**
         * Too many cells in a record.
         */
        TOO_LONG_RECORD,
    }

    /**
     * Statuses of CSV format exception.
     * @since 0.2.4
     */
    public static final class Status implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Reason reason;

        private final String path;

        private final int lineNumber;

        private final int recordNumber;

        private final int columnNumber;

        private final String expected;

        private final String actual;

        /**
         * @param reason
         * @param path
         * @param lineNumber
         * @param recordNumber
         * @param columnNumber
         * @param expected
         * @param actual
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public Status(
                Reason reason,
                String path,
                int lineNumber,
                int recordNumber,
                int columnNumber,
                String expected,
                String actual) {
            this.reason = reason;
            this.path = path;
            this.lineNumber = lineNumber;
            this.recordNumber = recordNumber;
            this.columnNumber = columnNumber;
            this.expected = expected;
            this.actual = actual;
        }

        /**
         * @return the reason
         */
        public Reason getReason() {
            return reason;
        }

        /**
         * @return the path
         */
        public String getPath() {
            return path;
        }

        /**
         * @return the lineNumber
         */
        public int getLineNumber() {
            return lineNumber;
        }

        /**
         * @return the recordNumber
         */
        public int getRecordNumber() {
            return recordNumber;
        }

        /**
         * @return the columnNumber
         */
        public int getColumnNumber() {
            return columnNumber;
        }

        /**
         * @return the expected
         */
        public String getExpected() {
            return expected;
        }

        /**
         * @return the actual
         */
        public String getActual() {
            return actual;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "{0} (at {1}:{2}, record={3}, column={4}, expected={5}, actual={6})",
                    getReason(),
                    getPath(),
                    getLineNumber(),
                    getRecordNumber(),
                    getColumnNumber(),
                    getExpected(),
                    getActual());
        }
    }
}
