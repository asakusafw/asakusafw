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
package com.asakusafw.windgate.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport;

/**
 * Supports {@link Pair}.
 */
public class PairSupport implements DataModelJdbcSupport<Pair> {

    private static final List<String> COLUMNS = Collections.unmodifiableList(Arrays.asList("KEY", "VALUE"));

    @Override
    public Class<Pair> getSupportedType() {
        return Pair.class;
    }

    @Override
    public boolean isSupported(List<String> columnNames) {
        return columnNames.equals(COLUMNS);
    }

    @Override
    public Map<String, String> getColumnMap() {
        return COLUMNS.stream().collect(Collectors.toMap(Function.identity(), String::toLowerCase));
    }

    @Override
    public DataModelResultSet<Pair> createResultSetSupport(
            final ResultSet resultSet, List<String> columnNames) {
        return new DataModelResultSet<Pair>() {
            @Override
            public boolean next(Pair object) throws SQLException {
                if (resultSet.next()) {
                    object.key = resultSet.getInt(1);
                    object.value = resultSet.getString(2);
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public DataModelPreparedStatement<Pair> createPreparedStatementSupport(
            final PreparedStatement statement, List<String> columnNames) {
        return new DataModelPreparedStatement<Pair>() {
            @Override
            public void setParameters(Pair object) throws SQLException {
                statement.setInt(1, object.key);
                statement.setString(2, object.value);
            }
        };
    }
}
