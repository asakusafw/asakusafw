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
package com.asakusafw.dmdl.directio.text;

import com.asakusafw.runtime.io.text.value.BooleanOptionFieldAdapter;
import com.asakusafw.runtime.io.text.value.DateOptionFieldAdapter;
import com.asakusafw.runtime.io.text.value.DateTimeOptionFieldAdapter;
import com.asakusafw.runtime.io.text.value.DecimalOptionFieldAdapter;
import com.asakusafw.runtime.io.text.value.ValueOptionFieldAdapter;

/**
 * Constant values of formatted text.
 * @since 0.9.1
 */
public final class TextFormatConstants {

    private TextFormatConstants() {
        return;
    }

    /**
     * The package segment name.
     */
    public static final String PACKAGE_SEGMENT = "text"; //$NON-NLS-1$

    /**
     * The common prefix of name-space.
     */
    public static final String PREFIX_NAMESPACE = "directio.text."; //$NON-NLS-1$

    /**
     * The attribute name of field.
     */
    public static final String ATTRIBUTE_FIELD = PREFIX_NAMESPACE + "field"; //$NON-NLS-1$

    /**
     * The attribute name of ignored field.
     */
    public static final String ATTRIBUTE_IGNORED_FIELD = PREFIX_NAMESPACE + "ignore"; //$NON-NLS-1$

    /**
     * The attribute name of file-name field.
     */
    public static final String ATTRIBUTE_FILE_NAME_FIELD = PREFIX_NAMESPACE + "file_name"; //$NON-NLS-1$

    /**
     * The attribute name of line-number field.
     */
    public static final String ATTRIBUTE_LINE_NUMBER_FIELD = PREFIX_NAMESPACE + "line_number"; //$NON-NLS-1$

    /**
     * The attribute name of record-number field.
     */
    public static final String ATTRIBUTE_RECORD_NUMBER_FIELD = PREFIX_NAMESPACE + "record_number"; //$NON-NLS-1$

    /**
     * The element name of field name.
     */
    public static final String ELEMENT_FIELD_NAME = "name"; //$NON-NLS-1$

    /**
     * The element name of compression type.
     */
    public static final String ELEMENT_COMPRESSION_TYPE = "compression"; //$NON-NLS-1$

    /**
     * The element name of character-set encoding name.
     */
    public static final String ELEMENT_CHARSET_NAME = "charset"; //$NON-NLS-1$

    /**
     * The element name of header type.
     */
    public static final String ELEMENT_HEADER_TYPE = "header"; //$NON-NLS-1$

    /**
     * The element name of line separator type.
     */
    public static final String ELEMENT_LINE_SEPARATOR = "line_separator"; //$NON-NLS-1$

    /**
     * The element name of field separator character.
     */
    public static final String ELEMENT_FIELD_SEPARATOR = "field_separator"; //$NON-NLS-1$

    /**
     * The element name of {@code NULL} value format.
     */
    public static final String ELEMENT_NULL_FORMAT = "null_format"; //$NON-NLS-1$

    /**
     * The element name of {@code TRUE} value format.
     */
    public static final String ELEMENT_TRUE_FORMAT = "true_format"; //$NON-NLS-1$

    /**
     * The element name of {@code FALSE} value format.
     */
    public static final String ELEMENT_FALSE_FORMAT = "false_format"; //$NON-NLS-1$

    /**
     * The element name of  output format.
     */
    public static final String ELEMENT_NUMBER_FORMAT = "number_format"; //$NON-NLS-1$

    /**
     * The element name of {@code DECIMAL} type output style.
     */
    public static final String ELEMENT_DECIMAL_OUTPUT_STYLE = "decimal_output_style"; //$NON-NLS-1$

    /**
     * The element name of {@code DATE} type value format.
     */
    public static final String ELEMENT_DATE_FORMAT = "date_format"; //$NON-NLS-1$

    /**
     * The element name of {@code DATETIME} type value format.
     */
    public static final String ELEMENT_DATETIME_FORMAT = "datetime_format"; //$NON-NLS-1$

    /**
     * The element name of whether or not trim leading/trailing white-space characters in inputs fields.
     */
    public static final String ELEMENT_TRIM_INPUT_WHITESPACES = "trim_input"; //$NON-NLS-1$

    /**
     * The element name of whether or not skip empty input fields.
     */
    public static final String ELEMENT_SKIP_EMPTY_INPUT = "skip_empty_input"; //$NON-NLS-1$

    /**
     * The element name of error action type for extra input fields.
     */
    public static final String ELEMENT_MORE_INPUT_ACTION = "on_more_input"; //$NON-NLS-1$

    /**
     * The element name of error action type for less input fields.
     */
    public static final String ELEMENT_LESS_INPUT_ACTION = "on_less_input"; //$NON-NLS-1$

    /**
     * The element name of error action type for malformed inputs.
     */
    public static final String ELEMENT_MALFORMED_INPUT_ACTION = "on_malformed_input"; //$NON-NLS-1$

    /**
     * The element name of error action type for unmappable outputs.
     */
    public static final String ELEMENT_UNMAPPABLE_OUTPUT_ACTION = "on_unmappable_output"; //$NON-NLS-1$

    /**
     * The element name of input transformer.
     */
    public static final String ELEMENT_INPUT_TRANSFORMER = "input_transformer"; //$NON-NLS-1$

    /**
     * The element name of output transformer.
     */
    public static final String ELEMENT_OUTPUT_TRANSFORMER = "output_transformer"; //$NON-NLS-1$

    /**
     * The element name of field adapter.
     */
    public static final String ELEMENT_FIELD_ADAPTER = "adapter"; //$NON-NLS-1$

    /**
     * The default value of {@link #ELEMENT_NULL_FORMAT}.
     */
    public static final String DEFAULT_NULL_FORMAT = ValueOptionFieldAdapter.DEFAULT_NULL_FORMAT;

    /**
     * The default value of {@link #ELEMENT_TRUE_FORMAT}.
     */
    public static final String DEFAULT_TRUE_FORMAT = BooleanOptionFieldAdapter.DEFAULT_TRUE_FORMAT;

    /**
     * The default value of {@link #ELEMENT_FALSE_FORMAT}.
     */
    public static final String DEFAULT_FALSE_FORMAT = BooleanOptionFieldAdapter.DEFAULT_FALSE_FORMAT;

    /**
     * The default value of {@link #ELEMENT_DECIMAL_OUTPUT_STYLE}.
     */
    public static final DecimalOptionFieldAdapter.OutputStyle DEFAULT_DECIMAL_OUTPUT_STYLE =
            DecimalOptionFieldAdapter.DEFAULT_OUTPUT_STYLE;

    /**
     * The default value of {@link #ELEMENT_DATE_FORMAT}.
     */
    public static final String DEFAULT_DATE_FORMAT = DateOptionFieldAdapter.DEFAULT_FORMAT;

    /**
     * The default value of {@link #ELEMENT_DATETIME_FORMAT}.
     */
    public static final String DEFAULT_DATETIME_FORMAT = DateTimeOptionFieldAdapter.DEFAULT_FORMAT;

    /**
     * The value name of {@code null}.
     */
    public static final String VALUE_NULL = "null"; //$NON-NLS-1$

    /**
     * The value name of {@code true}.
     */
    public static final String VALUE_TRUE = "true"; //$NON-NLS-1$

    /**
     * The value name of {@code false}.
     */
    public static final String VALUE_FALSE = "false"; //$NON-NLS-1$

    // CSV

    /**
     * The element name of quote style.
     */
    public static final String ELEMENT_QUOTE_STYLE = "quote_style"; //$NON-NLS-1$

    /**
     * The element name of quote character.
     */
    public static final String ELEMENT_QUOTE_CHARACTER = "quote_character"; //$NON-NLS-1$

    /**
     * The element name of whether or not LF character can appear as field value.
     */
    public static final String ELEMENT_ALLOW_LINE_FEED_IN_FIELD = "allow_linefeed"; //$NON-NLS-1$

    /**
     * The element name of default quote style.
     */
    public static final String ELEMENT_DEFAULT_QUOTE_STYLE = ELEMENT_QUOTE_STYLE;

    /**
     * The element name of header quote style.
     */
    public static final String ELEMENT_HEADER_QUOTE_STYLE = "header_" + ELEMENT_QUOTE_STYLE; //$NON-NLS-1$

    // delimited text

    /**
     * The element name of escape character.
     */
    public static final String ELEMENT_ESCAPE_CHARACTER = "escape_character"; //$NON-NLS-1$

    /**
     * The element name of escape sequence map.
     */
    public static final String ELEMENT_ESCAPE_SEQUENCE_MAP = "escape_sequence"; //$NON-NLS-1$

    /**
     * The element name of whether or not escape line separators.
     */
    public static final String ELEMENT_ESCAPE_LINE_SEPARATOR = "escape_line_separator"; //$NON-NLS-1$
}
