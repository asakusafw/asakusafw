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
package com.asakusafw.compiler.windgate.testing.jdbc;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.io.Text;

import com.asakusafw.compiler.windgate.testing.model.Pair;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport;
/**
 * Supports JDBC interfaces for {@link Pair}.
 */
public final class PairJdbcSupport implements DataModelJdbcSupport<Pair> {
    private static final Map<String, Integer> PROPERTY_POSITIONS;
    static {
        Map<String, Integer> map = new TreeMap<String, Integer>();
        map.put("KEY", 0);
        map.put("VALUE", 1);
        PROPERTY_POSITIONS = map;
    }
    @Override public Class<Pair> getSupportedType() {
        return Pair.class;
    }
    @Override public boolean isSupported(List<String> columnNames) {
        if(columnNames == null) {
            throw new IllegalArgumentException("columnNames must not be null");
        }
        if(columnNames.isEmpty()) {
            return false;
        }
        try {
            this.createPropertyVector(columnNames);
            return true;
        }
        catch(IllegalArgumentException e) {
            return false;
        }
    }
    @Override public DataModelJdbcSupport.DataModelResultSet<Pair> createResultSetSupport(ResultSet resultSet, List<
            String> columnNames) {
        if(resultSet == null) {
            throw new IllegalArgumentException("resultSet must not be null");
        }
        if(columnNames == null) {
            throw new IllegalArgumentException("columnNames must not be null");
        }
        int[] vector = this.createPropertyVector(columnNames);
        return new ResultSetSupport(resultSet, vector);
    }
    @Override public DataModelJdbcSupport.DataModelPreparedStatement<Pair> createPreparedStatementSupport(
            PreparedStatement statement, List<String> columnNames) {
        if(statement == null) {
            throw new IllegalArgumentException("statement must not be null");
        }
        if(columnNames == null) {
            throw new IllegalArgumentException("columnNames must not be null");
        }
        int[] vector = this.createPropertyVector(columnNames);
        return new PreparedStatementSupport(statement, vector);
    }
    private int[] createPropertyVector(List<String> columnNames) {
        int[] vector = new int[PROPERTY_POSITIONS.size()];
        for(int i = 0, n = columnNames.size(); i < n; i ++) {
            String column = columnNames.get(i);
            Integer position = PROPERTY_POSITIONS.get(column);
            if(position == null || vector[position]!= 0) {
                throw new IllegalArgumentException(column);
            }
            vector[position]= i + 1;
        }
        return vector;
    }
    private static final class ResultSetSupport implements DataModelJdbcSupport.DataModelResultSet<Pair> {
        private final ResultSet resultSet;
        private final int[] properties;
        private final Text text = new Text();
        ResultSetSupport(ResultSet resultSet, int[] properties) {
            this.resultSet = resultSet;
            this.properties = properties;
        }
        @Override public boolean next(Pair object) throws SQLException {
            if(resultSet.next()== false) {
                return false;
            }
            if(properties[0]!= 0) {
                object.setKey(resultSet.getInt(properties[0]));
                if(resultSet.wasNull()) {
                    object.setKeyOption(null);
                }
            }
            if(properties[1]!= 0) {
                String value = resultSet.getString(properties[1]);
                if(value != null) {
                    text.set(value);
                    object.setValue(text);
                }
                else {
                    object.setValueOption(null);
                }
            }
            return true;
        }
    }
    private static final class PreparedStatementSupport implements DataModelJdbcSupport.DataModelPreparedStatement<Pair> 
            {
        private final PreparedStatement statement;
        private final int[] properties;
        private final int[] types;
        PreparedStatementSupport(PreparedStatement statement, int[] properties) {
            this.statement = statement;
            this.properties = properties;
            types = new int[properties.length];
            try {
                ParameterMetaData meta = statement.getParameterMetaData();
                for(int i = 0; i < types.length; i ++) {
                    if(properties[i]!= 0) {
                        types[i]= meta.getParameterType(properties[i]);
                    }
                }
            }
            catch(SQLException e) {
                throw new IllegalArgumentException(e);
            }
        }
        @Override public void setParameters(Pair object) throws SQLException {
            if(properties[0]!= 0) {
                if(object.getKeyOption().isNull()) {
                    statement.setNull(properties[0], types[0]);
                }
                else {
                    statement.setInt(properties[0], object.getKey());
                }
            }
            if(properties[1]!= 0) {
                if(object.getValueOption().isNull()) {
                    statement.setNull(properties[1], types[1]);
                }
                else {
                    statement.setString(properties[1], object.getValue().toString());
                }
            }
        }
    }
}