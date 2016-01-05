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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelDefinition.Builder;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelScanner;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.PropertyName;

/**
 * Obtain model objects from a database table.
 * @param <T> type of model object to be obtained
 */
public class TableSource<T> implements DataModelSource {

    static final Logger LOG = LoggerFactory.getLogger(TableSource.class);

    private final SqlDriver driver;

    /**
     * Creates a new instance.
     * @param table target table info
     * @param connection connection to access the target table
     * @throws IOException if failed to open table to read data
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TableSource(TableInfo<T> table, Connection connection) throws IOException {
        if (table == null) {
            throw new IllegalArgumentException("table must not be null"); //$NON-NLS-1$
        }
        if (connection == null) {
            throw new IllegalArgumentException("connection must not be null"); //$NON-NLS-1$
        }
        try {
            this.driver = new SqlDriver(table, connection);
        } catch (SQLException e) {
            throw new IOException(MessageFormat.format(
                    "テーブル{0}からデータを取り出せませんでした",
                    table.getTableName()), e);
        }
    }

    @Override
    public DataModelReflection next() throws IOException {
        try {
            return driver.next();
        } catch (SQLException e) {
            throw new IOException(MessageFormat.format(
                    "テーブル{0}からデータを取り出せませんでした",
                    driver.table.getTableName()), e);
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

    private static class SqlDriver extends DataModelScanner<Builder<?>, SQLException> {

        final TableInfo<?> table;

        private final Connection connection;

        private final Statement statement;

        private final ResultSet resultSet;

        private int index = 1;

        SqlDriver(TableInfo<?> table, Connection connection) throws SQLException {
            assert table != null;
            assert connection != null;
            this.table = table;
            this.connection = connection;
            this.statement = connection.createStatement();
            this.resultSet = select();
        }

        private ResultSet select() throws SQLException {
            assert table != null;
            assert connection != null;
            LOG.debug("Building select statement: {}", table);
            Set<String> columns = table.getColumnsToProperties().keySet();
            boolean green = false;
            try {
                ResultSet rs = statement.executeQuery(MessageFormat.format(
                    "SELECT {1} FROM {0}",
                    table.getTableName(),
                    Util.join(columns)));
                green = true;
                return rs;
            } finally {
                if (green == false) {
                    statement.close();
                }
            }
        }

        public DataModelReflection next() throws SQLException {
            if (resultSet.next() == false) {
                return null;
            }
            index = 1;
            DataModelDefinition<?> def = table.getDefinition();
            Builder<?> builder = def.newReflection();
            for (PropertyName name : table.getColumnsToProperties().values()) {
                scan(def, name, builder);
                index++;
            }
            DataModelReflection ref = builder.build();
            return ref;
        }

        public void close() throws SQLException {
            try {
                try {
                    resultSet.close();
                } finally {
                    statement.close();
                }
            } finally {
                connection.close();
            }
        }

        @Override
        public void booleanProperty(PropertyName name, Builder<?> context)
                throws SQLException {
            boolean value = resultSet.getBoolean(index);
            if (resultSet.wasNull()) {
                return;
            }
            context.add(name, value);
        }

        @Override
        public void byteProperty(PropertyName name, Builder<?> context)
                throws SQLException {
            byte value = resultSet.getByte(index);
            if (resultSet.wasNull()) {
                return;
            }
            context.add(name, value);
        }

        @Override
        public void shortProperty(PropertyName name, Builder<?> context)
                throws SQLException {
            short value = resultSet.getShort(index);
            if (resultSet.wasNull()) {
                return;
            }
            context.add(name, value);
        }

        @Override
        public void intProperty(PropertyName name, Builder<?> context)
                throws SQLException {
            int value = resultSet.getInt(index);
            if (resultSet.wasNull()) {
                return;
            }
            context.add(name, value);
        }

        @Override
        public void longProperty(PropertyName name, Builder<?> context)
                throws SQLException {
            long value = resultSet.getLong(index);
            if (resultSet.wasNull()) {
                return;
            }
            context.add(name, value);
        }

        @Override
        public void floatProperty(PropertyName name, Builder<?> context)
                throws SQLException {
            float value = resultSet.getFloat(index);
            if (resultSet.wasNull()) {
                return;
            }
            context.add(name, value);
        }

        @Override
        public void doubleProperty(PropertyName name, Builder<?> context)
                throws SQLException {
            double value = resultSet.getDouble(index);
            if (resultSet.wasNull()) {
                return;
            }
            context.add(name, value);
        }

        @Override
        public void decimalProperty(PropertyName name, Builder<?> context)
                throws SQLException {
            BigDecimal value = resultSet.getBigDecimal(index);
            if (resultSet.wasNull()) {
                return;
            }
            context.add(name, value);
        }

        @Override
        public void stringProperty(PropertyName name, Builder<?> context)
                throws SQLException {
            String value = resultSet.getString(index);
            if (resultSet.wasNull()) {
                return;
            }
            context.add(name, value);
        }

        @Override
        public void dateProperty(PropertyName name, Builder<?> context)
                throws SQLException {
            java.sql.Date value = resultSet.getDate(index);
            if (resultSet.wasNull()) {
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(value);

            Calendar trimmed = Calendar.getInstance();
            trimmed.clear();
            copyField(calendar, trimmed, Calendar.YEAR);
            copyField(calendar, trimmed, Calendar.MONTH);
            copyField(calendar, trimmed, Calendar.DATE);
            context.add(name, trimmed);
        }

        @Override
        public void timeProperty(PropertyName name, Builder<?> context)
                throws SQLException {
            java.sql.Time value = resultSet.getTime(index);
            if (resultSet.wasNull()) {
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(value);

            Calendar trimmed = Calendar.getInstance();
            trimmed.clear();
            copyField(calendar, trimmed, Calendar.HOUR_OF_DAY);
            copyField(calendar, trimmed, Calendar.MINUTE);
            copyField(calendar, trimmed, Calendar.SECOND);
            context.add(name, trimmed);
        }

        @Override
        public void datetimeProperty(PropertyName name, Builder<?> context)
                throws SQLException {
            java.sql.Timestamp value = resultSet.getTimestamp(index);
            if (resultSet.wasNull()) {
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(value);

            Calendar trimmed = Calendar.getInstance();
            trimmed.clear();
            copyField(calendar, trimmed, Calendar.YEAR);
            copyField(calendar, trimmed, Calendar.MONTH);
            copyField(calendar, trimmed, Calendar.DATE);
            copyField(calendar, trimmed, Calendar.HOUR_OF_DAY);
            copyField(calendar, trimmed, Calendar.MINUTE);
            copyField(calendar, trimmed, Calendar.SECOND);
            context.add(name, trimmed);
        }

        private void copyField(Calendar from, Calendar to, int field) {
            assert from != null;
            assert to != null;
            to.set(field, from.get(field));
        }

        @Override
        public void anyProperty(PropertyName name, Builder<?> context)
                throws SQLException {
            throw new SQLException(MessageFormat.format(
                    "Invalid value type to obtain from {0}: {3} ({2} in {1})",
                    table.getTableName(),
                    table.getDefinition().getModelClass().getName(),
                    name,
                    table.getDefinition().getType(name)));
        }
    }
}
