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
package com.asakusafw.testdriver.bulkloader;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelScanner;
import com.asakusafw.testdriver.core.PropertyName;

/**
 * Insert model objects into a database table.
 * @param <T> type of model object to be inserted
 */
public class TableOutput<T> implements ModelOutput<T> {

    static final Logger LOG = LoggerFactory.getLogger(TableOutput.class);

    private final DataModelDefinition<T> definition;

    private final DmlDriver driver;

    /**
     * Creates a new instance.
     * @param table target table info
     * @param connection connection to access the target table
     * @throws IOException if failed to create a statement to output to table
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TableOutput(TableInfo<T> table, Connection connection) throws IOException {
        if (table == null) {
            throw new IllegalArgumentException("table must not be null"); //$NON-NLS-1$
        }
        if (connection == null) {
            throw new IllegalArgumentException("connection must not be null"); //$NON-NLS-1$
        }
        this.definition = table.getDefinition();
        try {
            this.driver = new DmlDriver(table, connection);
        } catch (SQLException e) {
            throw new IOException(MessageFormat.format(
                    "テーブル{0}に出力するステートメントの構築に失敗しました",
                    table.getTableName()), e);
        }
    }

    @Override
    public void write(T model) throws IOException {
        DataModelReflection ref = definition.toReflection(model);
        try {
            driver.insert(ref);
        } catch (SQLException e) {
            throw new IOException(MessageFormat.format(
                    "テーブル{0}に{1}を出力できませんでした",
                    driver.table.getTableName(),
                    ref), e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            driver.close();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    private static class DmlDriver extends DataModelScanner<DataModelReflection, SQLException> {

        final TableInfo<?> table;

        private final Connection connection;

        private final PreparedStatement statement;

        private int index = 1;

        DmlDriver(TableInfo<?> table, Connection connection) throws SQLException {
            assert table != null;
            assert connection != null;
            this.table = table;
            this.connection = connection;
            this.statement = createStatement();
        }

        private PreparedStatement createStatement() throws SQLException {
            assert table != null;
            assert connection != null;
            LOG.debug("Building insert statement: {}", table);

            String timestamp = table.getTimestampColumn();
            List<String> columns = new ArrayList<String>(table.getColumnsToProperties().keySet());
            if (timestamp != null) {
                columns.remove(timestamp);
                columns.add(timestamp);
            }
            return connection.prepareStatement(MessageFormat.format(
                    "INSERT INTO {0} ({1}) VALUES ({2})",
                    table.getTableName(),
                    Util.join(columns),
                    Util.join(Collections.nCopies(columns.size(), "?"))));
        }

        public void insert(DataModelReflection ref) throws SQLException {
            assert ref != null;
            statement.clearParameters();
            index = 1;
            String timestamp = table.getTimestampColumn();
            DataModelDefinition<?> def = table.getDefinition();
            for (Map.Entry<String, PropertyName> entry : table.getColumnsToProperties().entrySet()) {
                if (entry.getKey().equals(timestamp) == false) {
                    scan(def, entry.getValue(), ref);
                    index++;
                }
            }
            if (timestamp != null) {
                statement.setTimestamp(index, new Timestamp(0L));
            }
            statement.executeUpdate();
        }

        public void close() throws SQLException {
            try {
                statement.close();
            } finally {
                connection.close();
            }
        }

        @Override
        public void booleanProperty(PropertyName name, DataModelReflection context) throws SQLException {
            Boolean value = (Boolean) context.getValue(name);
            if (value == null) {
                statement.setNull(index, Types.BOOLEAN);
            } else {
                statement.setBoolean(index, value);
            }
        }

        @Override
        public void byteProperty(PropertyName name, DataModelReflection context) throws SQLException {
            Byte value = (Byte) context.getValue(name);
            if (value == null) {
                statement.setNull(index, Types.TINYINT);
            } else {
                statement.setByte(index, value);
            }
        }

        @Override
        public void shortProperty(PropertyName name, DataModelReflection context) throws SQLException {
            Short value = (Short) context.getValue(name);
            if (value == null) {
                statement.setNull(index, Types.SMALLINT);
            } else {
                statement.setShort(index, value);
            }
        }

        @Override
        public void intProperty(PropertyName name, DataModelReflection context) throws SQLException {
            Integer value = (Integer) context.getValue(name);
            if (value == null) {
                statement.setNull(index, Types.INTEGER);
            } else {
                statement.setInt(index, value);
            }
        }

        @Override
        public void longProperty(PropertyName name, DataModelReflection context) throws SQLException {
            Long value = (Long) context.getValue(name);
            if (value == null) {
                statement.setNull(index, Types.BIGINT);
            } else {
                statement.setLong(index, value);
            }
        }

        @Override
        public void floatProperty(PropertyName name, DataModelReflection context) throws SQLException {
            Float value = (Float) context.getValue(name);
            if (value == null) {
                statement.setNull(index, Types.FLOAT);
            } else {
                statement.setFloat(index, value);
            }
        }

        @Override
        public void doubleProperty(PropertyName name, DataModelReflection context) throws SQLException {
            Double value = (Double) context.getValue(name);
            if (value == null) {
                statement.setNull(index, Types.DOUBLE);
            } else {
                statement.setDouble(index, value);
            }
        }

        @Override
        public void decimalProperty(PropertyName name, DataModelReflection context) throws SQLException {
            BigDecimal value = (BigDecimal) context.getValue(name);
            if (value == null) {
                statement.setNull(index, Types.DECIMAL);
            } else {
                statement.setBigDecimal(index, value);
            }
        }

        @Override
        public void stringProperty(PropertyName name, DataModelReflection context) throws SQLException {
            String value = (String) context.getValue(name);
            if (value == null) {
                statement.setNull(index, Types.VARCHAR);
            } else {
                statement.setString(index, value);
            }
        }

        @Override
        public void dateProperty(PropertyName name, DataModelReflection context) throws SQLException {
            Calendar value = (Calendar) context.getValue(name);
            if (value == null) {
                statement.setNull(index, Types.DATE);
            } else {
                java.sql.Date date = new java.sql.Date(value.getTimeInMillis());
                statement.setDate(index, date);
            }
        }

        @Override
        public void timeProperty(PropertyName name, DataModelReflection context) throws SQLException {
            Calendar value = (Calendar) context.getValue(name);
            if (value == null) {
                statement.setNull(index, Types.TIME);
            } else {
                java.sql.Time time = new java.sql.Time(value.getTimeInMillis());
                statement.setTime(index, time);
            }
        }

        @Override
        public void datetimeProperty(PropertyName name, DataModelReflection context) throws SQLException {
            Calendar value = (Calendar) context.getValue(name);
            if (value == null) {
                statement.setNull(index, Types.TIME);
            } else {
                java.sql.Timestamp timestamp = new java.sql.Timestamp(value.getTimeInMillis());
                statement.setTimestamp(index, timestamp);
            }
        }

        @Override
        public void anyProperty(PropertyName name, DataModelReflection context)
                throws SQLException {
            throw new SQLException(MessageFormat.format(
                    "Invalid value type to insert into {0}: {3} ({2} in {1})",
                    table.getTableName(),
                    table.getDefinition().getModelClass().getName(),
                    name,
                    table.getDefinition().getType(name)));
        }
    }
}
