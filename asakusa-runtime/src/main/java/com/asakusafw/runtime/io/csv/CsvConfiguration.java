/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;

/**
 * CSV configurations.
 * @since 0.2.4
 */
public class CsvConfiguration {

    /**
     * The default charset encoding.
     * @see #getCharset()
     */
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    /**
     * The default header cells (empty list).
     * @see #getHeaderCells()
     */
    public static final List<String> DEFAULT_HEADER_CELLS = Collections.emptyList();

    /**
     * The default {@code true} representation.
     * @see #getTrueFormat()
     */
    public static final String DEFAULT_TRUE_FORMAT = "true";

    /**
     * The default {@code false} representation.
     * @see #getTrueFormat()
     */
    public static final String DEFAULT_FALSE_FORMAT = "false";

    /**
     * The default date format.
     * @see #getDateFormat()
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * The default date time format.
     * @see #getDateTimeFormat()
     */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * The default date time format.
     * @see #isLineBreakInValue()
     */
    public static final char DEFAULT_SEPARATOR_CHAR = ',';

    /**
     * The default date time format.
     * @see #isLineBreakInValue()
     */
    public static final boolean DEFAULT_LINE_BREAK_IN_VALUE = true;

    private final Charset charset;

    private final List<String> headerCells;

    private final String trueFormat;

    private final String falseFormat;

    private final String dateFormat;

    private final String dateTimeFormat;

    private volatile boolean lineBreakInValue = DEFAULT_LINE_BREAK_IN_VALUE;

    private volatile char separatorChar = DEFAULT_SEPARATOR_CHAR;

    /**
     * Creates a new instance.
     * @param charset character set encoding name
     * @param headerCells header cell values, or an empty list for no header
     * @param trueFormat the {@code true} representation
     * @param falseFormat the {@code false} representation
     * @param dateFormat {@link Date} format in {@link SimpleDateFormat}
     * @param dateTimeFormat {@link DateTime} format in {@link SimpleDateFormat}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CsvConfiguration(
            Charset charset,
            List<String> headerCells,
            String trueFormat,
            String falseFormat,
            String dateFormat,
            String dateTimeFormat) {
        if (charset == null) {
            throw new IllegalArgumentException("charset must not be null"); //$NON-NLS-1$
        }
        if (headerCells == null) {
            throw new IllegalArgumentException("headerCells must not be null"); //$NON-NLS-1$
        }
        if (trueFormat == null) {
            throw new IllegalArgumentException("trueFormat must not be null"); //$NON-NLS-1$
        }
        if (falseFormat == null) {
            throw new IllegalArgumentException("falseFormat must not be null"); //$NON-NLS-1$
        }
        if (dateFormat == null) {
            throw new IllegalArgumentException("dateFormat must not be null"); //$NON-NLS-1$
        }
        if (dateTimeFormat == null) {
            throw new IllegalArgumentException("dateTimeFormat must not be null"); //$NON-NLS-1$
        }
        this.charset = charset;
        this.headerCells = headerCells;
        this.trueFormat = trueFormat;
        this.falseFormat = falseFormat;
        this.dateFormat = dateFormat;
        this.dateTimeFormat = dateTimeFormat;
    }

    /**
     * Returns the character set encoding name.
     * @return the encoding
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * The header cell values.
     * @return the cell values, or an empty list to represent no headers
     */
    public List<String> getHeaderCells() {
        return headerCells;
    }

    /**
     * The boolean {@code true} format.
     * @return the format string
     */
    public String getTrueFormat() {
        return trueFormat;
    }

    /**
     * The boolean {@code false} format.
     * @return the format string
     */
    public String getFalseFormat() {
        return falseFormat;
    }

    /**
     * The {@link Date} format in {@link SimpleDateFormat}.
     * @return the format string
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * The {@link DateTime} format in {@link SimpleDateFormat}.
     * @return the format string
     */
    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    /**
     * Returns whether allows line breaks in value.
     * @return {@code true} if allow, otherwise {@code false}
     */
    public boolean isLineBreakInValue() {
        return lineBreakInValue;
    }

    /**
     * Sets whether allows line breaks in value.
     * @param allow {@code true} to allow, otherwise {@code false}
     */
    public void setLineBreakInValue(boolean allow) {
        this.lineBreakInValue = allow;
    }

    /**
     * Returns the field separator character.
     * @return the separator
     */
    public char getSeparatorChar() {
        return separatorChar;
    }

    /**
     * Sets the field separator character.
     * @param separatorChar the separator
     */
    public void setSeparatorChar(char separatorChar) {
        this.separatorChar = separatorChar;
    }
}
