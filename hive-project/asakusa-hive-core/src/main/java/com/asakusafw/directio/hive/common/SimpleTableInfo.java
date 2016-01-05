/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.directio.hive.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple implementation of {@link HiveTableInfo}.
 * @since 0.7.0
 */
public class SimpleTableInfo implements HiveTableInfo {

    private final String tableName;

    private final List<HiveFieldInfo> fields = new ArrayList<>();

    private Class<?> dataModelClass;

    private String tableComment;

    private RowFormatInfo rowFormat;

    private String formatName;

    private final Map<String, String> tableProperties = new LinkedHashMap<>();

    /**
     * Creates a new instance.
     * @param tableName the target table name
     */
    public SimpleTableInfo(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public Class<?> getDataModelClass() {
        return dataModelClass;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public List<? extends HiveFieldInfo> getFields() {
        return fields;
    }

    @Override
    public String getTableComment() {
        return tableComment;
    }

    @Override
    public RowFormatInfo getRowFormat() {
        return rowFormat;
    }

    @Override
    public String getFormatName() {
        return formatName;
    }

    @Override
    public Map<String, String> getTableProperties() {
        if (tableProperties == null) {
            return Collections.emptyMap();
        }
        return tableProperties;
    }

    /**
     * Sets the related data model class.
     * @param value the class
     * @return this
     */
    public SimpleTableInfo withDataModelClass(Class<?> value) {
        this.dataModelClass = value;
        return this;
    }

    /**
     * Adds a field.
     * @param value the class
     * @return this
     */
    public SimpleTableInfo withField(HiveFieldInfo value) {
        this.fields.add(value);
        return this;
    }

    /**
     * Sets fields.
     * @param value the fields
     * @return this
     */
    public SimpleTableInfo withFields(List<? extends HiveFieldInfo> value) {
        this.fields.clear();
        this.fields.addAll(value);
        return this;
    }

    /**
     * Sets the comment for the target table.
     * @param value comment text
     * @return this
     */
    public SimpleTableInfo withTableComment(String value) {
        this.tableComment = value;
        return this;
    }

    /**
     * Sets the row format information for the target table.
     * @param value the format info
     * @return this
     */
    public SimpleTableInfo withRowFormat(RowFormatInfo value) {
        this.rowFormat = value;
        return this;
    }

    /**
     * Sets the data format name for the target table.
     * @param value the data format name
     * @return this
     */
    public SimpleTableInfo withFormatName(String value) {
        this.formatName = value;
        return this;
    }

    /**
     * Adds a table property.
     * @param key the property key
     * @param value the property value
     * @return this
     */
    public SimpleTableInfo withTableProperty(String key, String value) {
        this.tableProperties.put(key, value);
        return this;
    }

    /**
     * Sets table properties for the target table.
     * @param value the table properties
     * @return this
     */
    public SimpleTableInfo withTableProperties(Map<String, String> value) {
        this.tableProperties.clear();
        this.tableProperties.putAll(value);
        return this;
    }
}
