/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.runtime.io;

/**
 * The constants about TSV format.
 * @since 0.1.0
 */
public final class TsvConstants {

    /**
     * The string representation of {@code true}.
     */
    public static final char BOOLEAN_TRUE = '1';

    /**
     * The string representation of {@code false}.
     */
    public static final char BOOLEAN_FALSE = '0';

    /**
     * The escape character.
     */
    public static final char ESCAPE_CHAR = '\\';

    /**
     * The suffix character of {@code NULL} columns for escape sequence.
     */
    public static final char ESCAPE_NULL_COLUMN = 'N';

    /**
     * The suffix character of tabs (HT) for escape sequence.
     */
    public static final char ESCAPE_HT = '\t';

    /**
     * The suffix character of line breaks (LF) for escape sequence.
     */
    public static final char ESCAPE_LF = '\n';

    /**
     * The cell separator character.
     */
    public static final char CELL_SEPARATOR = '\t';

    /**
     * The record separator character.
     */
    public static final char RECORD_SEPARATOR = '\n';

    /**
     * The date field separator character.
     */
    public static final char DATE_FIELD_SEPARATOR = '-';

    /**
     * The time field separator character.
     */
    public static final char TIME_FIELD_SEPARATOR = ':';

    /**
     * The date-time separator character.
     */
    public static final char DATE_TIME_SEPARATOR = ' ';

    /**
     * The character length of year fields.
     */
    public static final int YEAR_FIELD_LENGTH = 4;

    /**
     * The character length of month fields.
     */
    public static final int MONTH_FIELD_LENGTH = 2;

    /**
     * The character length of date fields.
     */
    public static final int DATE_FIELD_LENGTH = 2;

    /**
     * The character length of hour fields.
     */
    public static final int HOUR_FIELD_LENGTH = 2;

    /**
     * The character length of minute fields.
     */
    public static final int MINUTE_FIELD_LENGTH = 2;

    /**
     * The character length of second fields.
     */
    public static final int SECOND_FIELD_LENGTH = 2;

    private TsvConstants() {
        return;
    }
}
