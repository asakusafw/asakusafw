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
package com.asakusafw.windgate.core.vocabulary;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Supports JDBC interfaces for data models.
 * This implementation class must have public constructor without any parameters.
 * @param <T> the type of target data model
 * @since 0.2.2
 * @version 0.9.0
 */
public interface DataModelJdbcSupport<T> {

    /**
     * Returns the supported data model type.
     * @return the supported type
     */
    Class<T> getSupportedType();

    /**
     * Returns the column names into property names mapping.
     * @return the column names mappings
     * @since 0.9.0
     */
    Map<String, String> getColumnMap();

    /**
     * Checks whether this object supports the target column names.
     * @param columnNames the column names
     * @return {@code true} to support the columns names, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    boolean isSupported(List<String> columnNames);

    /**
     * Creates a new {@link DataModelResultSet} for the specified columns.
     * The {@code resultSet} must have results corresponded with the {@code columnNames} in same order.
     * @param resultSet the target result set to be supported
     * @param columnNames the column names corresponding with result columns,
     *     the {@code columnNames[i]} is mapped into {@code resultSet.get*(i + 1)}.
     * @return the created support object
     * @throws IllegalArgumentException if this does not support target columns,
     *     or any parameter is {@code null}
     * @see #isSupported(List)
     */
    DataModelResultSet<T> createResultSetSupport(ResultSet resultSet, List<String> columnNames);

    /**
     * Creates a new {@link DataModelPreparedStatement} for the specified columns.
     * The {@code statement} must have parameters corresponded with the {@code columnNames} in same order.
     * @param statement the target statement to be supported
     * @param columnNames the column names corresponding with paremeters,
     *     the {@code columnNames[i]} is mapped into {@code statement.set*(i + 1, column-value)}.
     * @return the created support object
     * @throws IllegalArgumentException if this does not support target columns,
     *     or any parameter is {@code null}
     * @see #isSupported(List)
     */
    DataModelPreparedStatement<T> createPreparedStatementSupport(
            PreparedStatement statement, List<String> columnNames);

    /**
     * Supports {@link ResultSet} interface for data models.
     * @since 0.2.2
     * @param <T> the type of target data model
     */
    public interface DataModelResultSet<T> {

        /*
         * Note:
         * The 'next' method takes T's object for projective models.
         */

        /**
         * Moves cursor to the next row and then fills the object for the row values.
         * @param object the target object
         * @return {@code true} if the next row exists, otherwise {@code false}
         * @throws SQLException if a database access error occurs
         * @throws IllegalArgumentException if {@code object} is {@code null}
         */
        boolean next(T object) throws SQLException;
    }

    /**
     * Supports {@link PreparedStatement} interface for data models.
     * @since 0.2.2
     * @param <T> the type of target data model
     */
    public interface DataModelPreparedStatement<T> {

        /**
         * Sets the parameters to corresponding {@link PreparedStatement} using the specified data model.
         * @param object the data model
         * @throws SQLException if a database access error occurs
         * @throws IllegalArgumentException if {@code object} is {@code null}
         */
        void setParameters(T object) throws SQLException;
    }
}
