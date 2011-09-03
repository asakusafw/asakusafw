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
package com.asakusafw.windgate.jdbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport;

/**
 * A structured gate script fragment for JDBC.
 * @param <T> the type of target data model
 * @since 0.2.3
 */
public class JdbcScript<T> {

    private final String name;

    private final DataModelJdbcSupport<? super T> support;

    private final String tableName;

    private final List<String> columnNames;

    private final String condition;

    /**
     * Creates a new instance.
     * @param name the name of original process
     * @param support the support object for the script
     * @param tableName the target table name
     * @param columnNames the target column names
     * @param condition the condition, or {@code null} if not used
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public JdbcScript(
            String name,
            DataModelJdbcSupport<? super T> support,
            String tableName,
            List<String> columnNames,
            String condition) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (support == null) {
            throw new IllegalArgumentException("support must not be null"); //$NON-NLS-1$
        }
        if (tableName == null) {
            throw new IllegalArgumentException("tableName must not be null"); //$NON-NLS-1$
        }
        if (columnNames == null) {
            throw new IllegalArgumentException("columnNames must not be null"); //$NON-NLS-1$
        }
        if (condition != null && isEmpty(condition)) {
            throw new IllegalArgumentException("condition must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.support = support;
        this.tableName = tableName;
        for (String columnName : columnNames) {
            if (isEmpty(columnName)) {
                throw new IllegalArgumentException("columnNames must not contain empty"); //$NON-NLS-1$
            }
        }
        this.columnNames = Collections.unmodifiableList(new ArrayList<String>(columnNames));
        this.condition = condition;
    }

    private boolean isEmpty(String string) {
        return (string == null || string.trim().isEmpty());
    }

    /**
     * Returns the name of original process.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a {@link DataModelJdbcSupport} object to be used in drivers.
     * @return the support object
     */
    public DataModelJdbcSupport<? super T> getSupport() {
        return support;
    }

    /**
     * Returns the target table name.
     * @return the table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Returns the target column names.
     * @return the column names
     */
    public List<String> getColumnNames() {
        return columnNames;
    }

    /**
     * Returns the SQL condition expression.
     * @return the condition expression, or {@code null} if not used
     */
    public String getCondition() {
        return condition;
    }
}
