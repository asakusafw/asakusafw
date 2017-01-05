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
package com.asakusafw.directio.hive.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a Hive table structure.
 * @see Builder
 * @since 0.8.1
 */
public class TableInfo {

    private static final String K_NAME = "name";

    private static final String K_COLUMNS = "columns";

    private static final String K_COMMENT = "comment";

    private static final String K_ROW = "row";

    private static final String K_STORAGE = "storage";

    private static final String K_PROPERTIES = "properties";

    private final String name;

    private final List<ColumnInfo> columns;

    private final String comment;

    private final RowFormatInfo rowFormat;

    private final StorageFormatInfo storageFormat;

    private final Map<String, String> properties;

    /**
     * Creates a new instance.
     * @param name the table name
     * @param columns the fields
     * @param comment the table comment (nullable)
     * @param rowFormat the row format (nullable)
     * @param storageFormat the storage format (nullable)
     * @param properties the table properties (nullable)
     */
    @JsonCreator
    public TableInfo(
            @JsonProperty(value = K_NAME, required = true) String name,
            @JsonProperty(value = K_COLUMNS, required = true) List<ColumnInfo> columns,
            @JsonProperty(value = K_COMMENT, required = false) String comment,
            @JsonProperty(value = K_ROW, required = false) RowFormatInfo rowFormat,
            @JsonProperty(value = K_STORAGE, required = false) StorageFormatInfo storageFormat,
            @JsonProperty(value = K_PROPERTIES, required = false) Map<String, String> properties) {
        this.name = name;
        this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
        this.comment = comment;
        this.rowFormat = rowFormat;
        this.storageFormat = storageFormat;
        this.properties = properties == null || properties.isEmpty()
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(properties));
    }

    /**
     * Returns the table name.
     * @return the table name
     */
    @JsonProperty(K_NAME)
    public String getName() {
        return name;
    }

    /**
     * Returns the columns.
     * @return the columns
     */
    @JsonProperty(K_COLUMNS)
    public List<ColumnInfo> getColumns() {
        return columns;
    }

    /**
     * Returns the table comment.
     * @return the table comment, or {@code null} if it is not specified
     */
    @JsonProperty(K_COMMENT)
    public String getComment() {
        return comment;
    }

    /**
     * Returns the table row format.
     * @return the table row format, or {@code null} if it is not specified
     */
    @JsonProperty(K_ROW)
    public RowFormatInfo getRowFormat() {
        return rowFormat;
    }

    /**
     * Returns the table storage format.
     * @return the table storage format, or {@code null} if it is not specified
     */
    @JsonProperty(K_STORAGE)
    public StorageFormatInfo getStorageFormat() {
        return storageFormat;
    }

    /**
     * Returns the table properties.
     * @return the table properties, or an empty map if it is not specified
     */
    @JsonProperty(K_PROPERTIES)
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(name);
        result = prime * result + Objects.hashCode(columns);
        result = prime * result + Objects.hashCode(rowFormat);
        result = prime * result + Objects.hashCode(storageFormat);
        result = prime * result + Objects.hashCode(properties);
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
        TableInfo other = (TableInfo) obj;
        if (!Objects.equals(name, other.name)) {
            return false;
        }
        if (!Objects.equals(columns, other.columns)) {
            return false;
        }
        if (!Objects.equals(rowFormat, other.rowFormat)) {
            return false;
        }
        if (!Objects.equals(storageFormat, other.storageFormat)) {
            return false;
        }
        if (!Objects.equals(properties, other.properties)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format(
                "Table(%s)", //$NON-NLS-1$
                getName());
    }

    /**
     * Provides {@link TableInfo}.
     * @since 0.8.1
     */
    @FunctionalInterface
    public interface Provider {

        /**
         * Returns the {@link TableInfo} of this object.
         * @return the table schema
         */
        TableInfo getSchema();
    }

    /**
     * A builder for {@link TableInfo}.
     * @since 0.8.1
     */
    public static class Builder {

        private final String name;

        private final List<ColumnInfo> columns = new ArrayList<>();

        private final Map<String, String> properties = new LinkedHashMap<>();

        private String comment;

        private RowFormatInfo rowFormat;

        private StorageFormatInfo storageFormat;

        /**
         * Creates a new instance.
         * @param name the table name
         */
        public Builder(String name) {
            this.name = name;
        }

        /**
         * Adds a column.
         * @param info a new entry
         * @return this
         */
        public Builder withColumn(ColumnInfo info) {
            columns.add(info);
            return this;
        }

        /**
         * Adds a column.
         * @param columnName the column name
         * @param columnType the column type
         * @return this
         */
        public Builder withColumn(String columnName, FieldType columnType) {
            return withColumn(new ColumnInfo(columnName, columnType));
        }

        /**
         * Adds a column.
         * @param columnName the column name
         * @param columnType the column type name (must be a plain type)
         * @return this
         */
        public Builder withColumn(String columnName, FieldType.TypeName columnType) {
            return withColumn(new ColumnInfo(columnName, PlainType.of(columnType)));
        }

        /**
         * Sets the table comment.
         * @param text the comment text
         * @return this
         */
        public Builder withComment(String text) {
            comment = text;
            return this;
        }

        /**
         * Sets the row format.
         * @param info the row format information
         * @return this
         */
        public Builder withRowFormat(RowFormatInfo info) {
            rowFormat = info;
            return this;
        }

        /**
         * Sets the storage format.
         * @param info the storage format information
         * @return this
         */
        public Builder withStorageFormat(StorageFormatInfo info) {
            storageFormat = info;
            return this;
        }

        /**
         * Sets the storage format.
         * @param info the storage format information
         * @return this
         */
        public Builder withStorageFormat(StorageFormatInfo.FormatKind info) {
            return withStorageFormat(BuiltinStorageFormatInfo.of(info));
        }

        /**
         * Adds a table property.
         * @param key the property key
         * @param value the property value
         * @return this
         */
        public Builder withProperty(String key, String value) {
            properties.put(key, value);
            return this;
        }

        /**
         * Adds table properties.
         * @param additions the property map
         * @return this
         */
        public Builder withProperties(Map<String, String> additions) {
            properties.putAll(additions);
            return this;
        }

        /**
         * Builds a {@link TableInfo}.
         * @return the built object
         */
        public TableInfo build() {
            return new TableInfo(name, columns, comment, rowFormat, storageFormat, properties);
        }
    }
}
