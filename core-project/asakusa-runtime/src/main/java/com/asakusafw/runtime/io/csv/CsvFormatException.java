/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
         * Unexpected line break is appearred in value (only if restricted).
         */
        UNEXPECTED_LINE_BREAK,

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
         * Creates a new instance.
         * @param reason the reason of this status
         * @param path the source path
         * @param lineNumber current (physical) line number (1-origin)
         * @param recordNumber current record number (1-origin)
         * @param columnNumber current column number (1-origin)
         * @param expected the expected status description
         * @param actual the actual status description
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
         * Return the reason of this status.
         * @return the reason
         */
        public Reason getReason() {
            return reason;
        }

        /**
         * Return the path to the target file.
         * @return the path
         */
        public String getPath() {
            return path;
        }

        /**
         * Returns the current line number (1-origin).
         * @return the line number
         */
        public int getLineNumber() {
            return lineNumber;
        }

        /**
         * Returns the current record number (1-origin).
         * @return the record number
         */
        public int getRecordNumber() {
            return recordNumber;
        }

        /**
         * Returns the current column number (1-origin).
         * @return the column number
         */
        public int getColumnNumber() {
            return columnNumber;
        }

        /**
         * Returns the expected status description.
         * @return the expected
         */
        public String getExpected() {
            return expected;
        }

        /**
         * Returns the actual status description.
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
