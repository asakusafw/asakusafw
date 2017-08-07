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
package com.asakusafw.info.hive;

import java.text.MessageFormat;
import java.util.Objects;

import com.asakusafw.info.hive.syntax.HiveSyntax;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents Hive delimited text row format.
 * @since 0.8.1
 */
public class DelimitedRowFormatInfo implements RowFormatInfo {

    private static final String K_LINE_SEPARATOR = "line";

    private static final String K_FIELD_SEPARATOR = "field";

    private static final String K_FIELD_SEPARATOR_ESCAPE = "escape";

    private static final String K_COLLECTION_ITEM_SEPARATOR = "item";

    private static final String K_MAP_PAIR_SEPARATOR = "pair";

    private static final String K_NULL_SYMBOL = "null";

    private final String lineSeparator;

    private final String fieldSeparator;

    private final String fieldSeparatorEscape;

    private final String collectionItemSeparator;

    private final String mapPairSeparator;

    private final String nullSymbol;

    /**
     * Creates a new instance.
     * @param lineSeparator the line separator character
     * @param fieldSeparator the field separator character
     * @param fieldSeparatorEscape the escape character (for field separators)
     * @param collectionItemSeparator the collection item separator character
     * @param mapPairSeparator the map key-value separator character
     * @param nullSymbol the {@code null} symbol character
     */
    @JsonCreator
    public DelimitedRowFormatInfo(
            @JsonProperty(value = K_LINE_SEPARATOR, required = false) String lineSeparator,
            @JsonProperty(value = K_FIELD_SEPARATOR, required = false) String fieldSeparator,
            @JsonProperty(value = K_FIELD_SEPARATOR_ESCAPE, required = false) String fieldSeparatorEscape,
            @JsonProperty(value = K_COLLECTION_ITEM_SEPARATOR, required = false) String collectionItemSeparator,
            @JsonProperty(value = K_MAP_PAIR_SEPARATOR, required = false) String mapPairSeparator,
            @JsonProperty(value = K_NULL_SYMBOL, required = false) String nullSymbol) {
        this.lineSeparator = validate(lineSeparator);
        this.fieldSeparator = validate(fieldSeparator);
        this.fieldSeparatorEscape = validate(fieldSeparatorEscape);
        this.collectionItemSeparator = validate(collectionItemSeparator);
        this.mapPairSeparator = validate(mapPairSeparator);
        this.nullSymbol = validate(nullSymbol);
    }

    private static String validate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (value.length() >= 2) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "delimited text option must be a single character: {0}",
                    HiveSyntax.quoteLiteral('"', value)));
        }
        return value;
    }

    @JsonProperty("kind")
    @Override
    public FormatKind getFormatKind() {
        return FormatKind.DELIMITED;
    }

    /**
     * Returns the line separator character.
     * @return the line separator character, or {@code null} if it is specified
     */
    @JsonProperty(K_LINE_SEPARATOR)
    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Returns the field separator character.
     * @return the field separator character, or {@code null} if it is specified
     */
    @JsonProperty(K_FIELD_SEPARATOR)
    public String getFieldSeparator() {
        return fieldSeparator;
    }

    /**
     * Returns the escape character for the field separator.
     * @return the escape character, or {@code null} if it is specified
     */
    @JsonProperty(K_FIELD_SEPARATOR_ESCAPE)
    public String getFieldSeparatorEscape() {
        return fieldSeparatorEscape;
    }

    /**
     * Returns the collection items terminator character.
     * @return the collection items terminator character, or {@code null} if it is specified
     */
    @JsonProperty(K_COLLECTION_ITEM_SEPARATOR)
    public String getCollectionItemSeparator() {
        return collectionItemSeparator;
    }

    /**
     * Returns the map key terminator character.
     * @return the map key terminator character, or {@code null} if it is specified
     */
    @JsonProperty(K_MAP_PAIR_SEPARATOR)
    public String getMapPairSeparator() {
        return mapPairSeparator;
    }

    /**
     * Returns the {@code null} character.
     * @return the {@code null} character, or {@code null} if it is specified
     */
    @JsonProperty(K_NULL_SYMBOL)
    public String getNullSymbol() {
        return nullSymbol;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(lineSeparator);
        result = prime * result + Objects.hashCode(fieldSeparator);
        result = prime * result + Objects.hashCode(fieldSeparatorEscape);
        result = prime * result + Objects.hashCode(collectionItemSeparator);
        result = prime * result + Objects.hashCode(mapPairSeparator);
        result = prime * result + Objects.hashCode(nullSymbol);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DelimitedRowFormatInfo other = (DelimitedRowFormatInfo) obj;
        if (!Objects.equals(lineSeparator, other.lineSeparator)) {
            return false;
        }
        if (!Objects.equals(fieldSeparator, other.fieldSeparator)) {
            return false;
        }
        if (!Objects.equals(fieldSeparatorEscape, other.fieldSeparatorEscape)) {
            return false;
        }
        if (!Objects.equals(collectionItemSeparator, other.collectionItemSeparator)) {
            return false;
        }
        if (!Objects.equals(mapPairSeparator, other.mapPairSeparator)) {
            return false;
        }
        if (!Objects.equals(nullSymbol, other.nullSymbol)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "%s(line=%s, field=%s, escape=%s, collection=%s, map=%s, null=%s)", //$NON-NLS-1$
                getFormatKind(),
                toString(lineSeparator),
                toString(fieldSeparator),
                toString(fieldSeparatorEscape),
                toString(collectionItemSeparator),
                toString(mapPairSeparator),
                toString(nullSymbol));
    }

    private static String toString(String value) {
        if (value == null) {
            return "N/A"; //$NON-NLS-1$
        } else {
            return HiveSyntax.quoteLiteral('\'', value);
        }
    }

    /**
     * A builder for {@link DelimitedRowFormatInfo}.
     * @since 0.8.1
     */
    public static class Builder {

        private String lineSeparator;

        private String fieldSeparator;

        private String fieldSeparatorEscape;

        private String collectionItemSeparator;

        private String mapPairSeparator;

        private String nullValue;

        /**
         * {@code FIELDS TERMINATED BY <c>}.
         * @param c the character
         * @return this
         */
        public Builder fieldsTerminatedBy(char c) {
            this.fieldSeparator = String.valueOf(c);
            return this;
        }

        /**
         * {@code FIELDS TERMINATED .. ESCAPED BY <c>}.
         * @param c the character
         * @return this
         */
        public Builder escapedBy(char c) {
            this.fieldSeparatorEscape = String.valueOf(c);
            return this;
        }

        /**
         * {@code COLLECTION ITEMS TERMINATED BY <c>}.
         * @param c the character
         * @return this
         */
        public Builder collectionItemsTerminatedBy(char c) {
            this.collectionItemSeparator = String.valueOf(c);
            return this;
        }

        /**
         * {@code MAP KEYS TERMINATED BY <c>}.
         * @param c the character
         * @return this
         */
        public Builder mapKeysTerminatedBy(char c) {
            this.mapPairSeparator = String.valueOf(c);
            return this;
        }

        /**
         * {@code LINES TERMINATED BY <c>}.
         * @param c the character
         * @return this
         */
        public Builder linesTerminatedBy(char c) {
            this.lineSeparator = String.valueOf(c);
            return this;
        }

        /**
         * {@code NULL DEFINED AS <c>}.
         * @param c the character
         * @return this
         */
        public Builder nullDefinedAs(char c) {
            this.nullValue = String.valueOf(c);
            return this;
        }

        /**
         * Builds an object.
         * @return the built object
         */
        public DelimitedRowFormatInfo build() {
            return new DelimitedRowFormatInfo(
                    lineSeparator,
                    fieldSeparator, fieldSeparatorEscape,
                    collectionItemSeparator, mapPairSeparator,
                    nullValue);
        }
    }
}
