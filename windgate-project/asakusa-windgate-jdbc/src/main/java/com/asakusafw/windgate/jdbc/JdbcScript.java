/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport;

/**
 * A structured gate script fragment for JDBC.
 * @param <T> the type of target data model
 * @since 0.2.2
 * @version 0.9.0
 */
public class JdbcScript<T> {

    private final String name;

    private final DataModelJdbcSupport<? super T> support;

    private final String tableName;

    private final List<String> columnNames;

    private volatile String condition;

    private volatile String customTruncate;

    private volatile Set<String> options = Collections.emptySet();

    /**
     * Creates a new instance.
     * @param name the name of original process
     * @param support the support object for the script
     * @param tableName the target table name
     * @param columnNames the target column names
     * @throws IllegalArgumentException if any parameter is {@code null}
     * @since 0.9.0
     */
    public JdbcScript(
            String name,
            DataModelJdbcSupport<? super T> support,
            String tableName,
            List<String> columnNames) {
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
        this.name = name;
        this.support = support;
        this.tableName = tableName;
        for (String columnName : columnNames) {
            if (isEmpty(columnName)) {
                throw new IllegalArgumentException("columnNames must not contain empty"); //$NON-NLS-1$
            }
        }
        this.columnNames = Collections.unmodifiableList(new ArrayList<>(columnNames));
    }

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
        this(name, support, tableName, columnNames, condition, null);
    }

    /**
     * Creates a new instance.
     * @param name the name of original process
     * @param support the support object for the script
     * @param tableName the target table name
     * @param columnNames the target column names
     * @param condition the condition, or {@code null} if not used
     * @param customTruncate the custom truncate statement, or {@code null} if not used
     * @throws IllegalArgumentException if any parameter is {@code null}
     * @since 0.7.3
     */
    public JdbcScript(
            String name,
            DataModelJdbcSupport<? super T> support,
            String tableName,
            List<String> columnNames,
            String condition,
            String customTruncate) {
        this(name, support, tableName, columnNames);
        if (condition != null && isEmpty(condition)) {
            throw new IllegalArgumentException("condition must not be empty"); //$NON-NLS-1$
        }
        if (customTruncate != null && isEmpty(customTruncate)) {
            throw new IllegalArgumentException("customTruncate must not be empty"); //$NON-NLS-1$
        }
        this.condition = condition;
        this.customTruncate = customTruncate;
        this.options = Collections.emptySet();
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

    /**
     * Sets the SQL condition expression.
     * @param expression the expression
     * @return this
     * @since 0.9.0
     */
    public JdbcScript<T> withCondition(String expression) {
        if (expression != null && isEmpty(expression)) {
            throw new IllegalArgumentException("condition expression must not be empty"); //$NON-NLS-1$
        }
        this.condition = expression;
        return this;
    }

    /**
     * Returns the custom truncate SQL statement.
     * @return the custom truncate statement, or {@code null} if not used
     * @since 0.7.3
     */
    public String getCustomTruncate() {
        return customTruncate;
    }

    /**
     * Sets the custom truncate SQL statement.
     * @param statement the statement
     * @return this
     * @since 0.9.0
     */
    public JdbcScript<T> withCustomTruncate(String statement) {
        if (statement != null && isEmpty(statement)) {
            throw new IllegalArgumentException("custom truncate statement must not be empty"); //$NON-NLS-1$
        }
        this.customTruncate = statement;
        return this;
    }

    /**
     * Returns the WindGate JDBC options.
     * @return the WindGate JDBC options
     * @since 0.9.0
     */
    public Set<String> getOptions() {
        return options;
    }

    /**
     * Sets the WindGate JDBC options.
     * @param values the options
     * @return this
     * @since 0.9.0
     */
    public JdbcScript<T> withOptions(Collection<String> values) {
        this.options = Collections.unmodifiableSet(new LinkedHashSet<>(values));
        return this;
    }
}
