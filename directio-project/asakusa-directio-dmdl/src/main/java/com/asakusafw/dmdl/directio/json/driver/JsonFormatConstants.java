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
package com.asakusafw.dmdl.directio.json.driver;

/**
 * Constant values of JSON format.
 * @since 0.10.3
 */
public final class JsonFormatConstants {

    private JsonFormatConstants() {
        return;
    }

    /**
     * The package segment name.
     */
    public static final String PACKAGE_SEGMENT = "json"; //$NON-NLS-1$

    /**
     * The common prefix of name-space.
     */
    public static final String BASE_NAMESPACE = "directio.json"; //$NON-NLS-1$

    /**
     * The attribute name of property.
     */
    public static final String ATTRIBUTE_PROPERTY = BASE_NAMESPACE + ".field"; //$NON-NLS-1$

    /**
     * The attribute name of ignored field.
     */
    public static final String ATTRIBUTE_IGNORED_FIELD = BASE_NAMESPACE + ".ignore"; //$NON-NLS-1$

    /**
     * The attribute name of file-name field.
     */
    public static final String ATTRIBUTE_FILE_NAME_FIELD = BASE_NAMESPACE + ".file_name"; //$NON-NLS-1$

    /**
     * The attribute name of line-number field.
     */
    public static final String ATTRIBUTE_LINE_NUMBER_FIELD = BASE_NAMESPACE + ".line_number"; //$NON-NLS-1$

    /**
     * The attribute name of record-number field.
     */
    public static final String ATTRIBUTE_RECORD_NUMBER_FIELD = BASE_NAMESPACE + ".record_number"; //$NON-NLS-1$

    /**
     * The element name of property name.
     */
    public static final String ELEMENT_FIELD_NAME = "name"; //$NON-NLS-1$

    /**
     * The element name of JSON format kind (JSON or JSONL).
     */
    public static final String ELEMENT_FORMAT_KIND = "format"; //$NON-NLS-1$

    /**
     * The element name of compression type.
     */
    public static final String ELEMENT_COMPRESSION_TYPE = "compression"; //$NON-NLS-1$

    /**
     * The element name of character-set encoding name.
     */
    public static final String ELEMENT_CHARSET_NAME = "charset"; //$NON-NLS-1$

    /**
     * The element name of line separator type.
     */
    public static final String ELEMENT_LINE_SEPARATOR = "line_separator"; //$NON-NLS-1$

    /**
     * The element name of {@code DATE} type value format.
     */
    public static final String ELEMENT_DATE_FORMAT = "date_format"; //$NON-NLS-1$

    /**
     * The element name of {@code DATETIME} type value format.
     */
    public static final String ELEMENT_DATETIME_FORMAT = "datetime_format"; //$NON-NLS-1$

    /**
     * The element name of time-zone.
     */
    public static final String ELEMENT_TIME_ZONE = "timezone"; //$NON-NLS-1$

    /**
     * The element name of {@code DECIMAL} type output style.
     */
    public static final String ELEMENT_DECIMAL_STYLE = "decimal_output_style"; //$NON-NLS-1$

    /**
     * The element name of {@code NULL} value format.
     */
    public static final String ELEMENT_NULL_STYLE = "null_style"; //$NON-NLS-1$

    /**
     * The element name of whether escape no-ASCII characters in string values.
     */
    public static final String ELEMENT_ESCAPE_NO_ASCII = "escape_non_ascii"; //$NON-NLS-1$

    /**
     * The element name of error action type for malformed inputs.
     */
    public static final String ELEMENT_MALFORMED_INPUT_ACTION = "on_malformed_input"; //$NON-NLS-1$

    /**
     * The element name of error action type for less input fields.
     */
    public static final String ELEMENT_MISSING_PROPERTY_ACTION = "on_missing_input"; //$NON-NLS-1$

    /**
     * The element name of error action type for extra input fields.
     */
    public static final String ELEMENT_UNKNOWN_PROPERTY_ACTION = "on_unknown_input"; //$NON-NLS-1$

    /**
     * The element name of field adapter class.
     */
    public static final String ELEMENT_FIELD_ADAPTER = "adapter"; //$NON-NLS-1$

    /**
     * The default value of {@link #ELEMENT_FORMAT_KIND}.
     */
    public static final JsonFormatKind DEFAULT_FORMAT_KIND = JsonFormatKind.JSON;

    /**
     * Option of {@link JsonFormatConstants#ELEMENT_FORMAT_KIND}.
     * @since 0.10.3
     */
    public enum JsonFormatKind {

        /**
         * Basic JSON format.
         */
        JSON,

        /**
         * JSON Lines format.
         */
        JSONL,
    }

    /**
     * Options of {@link JsonFormatConstants#ELEMENT_DECIMAL_STYLE}.
     * @since 0.10.3
     */
    public enum DecimalStyle {

        /**
         * Never use exponential notation.
         */
        PLAIN,

        /**
         * Use exponential notation.
         */
        SCIENTIFIC,
    }
}
