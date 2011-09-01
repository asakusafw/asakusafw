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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Collections;

import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport.DataModelPreparedStatement;

/**
 * An implementation of {@link DrainDriver} using JDBC.
 * @param <T> the type of data model object
 * @since 0.2.3
 */
public class JdbcDrainDriver<T> implements DrainDriver<T> {

    private final JdbcScript<T> script;

    private final Connection connection;

    private final boolean truncateOnPrepare;

    private final long batchPutUnit;

    private long putLimitRest;

    private long putCount;

    private PreparedStatement statement;

    private DataModelPreparedStatement<? super T> support;

    private boolean sawError;

    /**
     * Creates a new instance.
     * @param profile the profile of the target database
     * @param script the script of this action
     * @param connection the connection
     * @param truncateOnPrepare {@code true} to truncate the target table on preparation
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public JdbcDrainDriver(
            JdbcProfile profile,
            JdbcScript<T> script,
            Connection connection,
            boolean truncateOnPrepare) {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        if (connection == null) {
            throw new IllegalArgumentException("connection must not be null"); //$NON-NLS-1$
        }
        this.script = script;
        this.connection = connection;
        this.batchPutUnit = profile.getBatchPutUnit();
        this.truncateOnPrepare = truncateOnPrepare;
    }

    @Override
    public void prepare() throws IOException {
        try {
            if (truncateOnPrepare) {
                truncate();
            }
            this.statement = prepareStatement();
        } catch (SQLException e) {
            // TODO logging
            sawError = true;
            throw new IOException(e);
        }
        support = script.getSupport().createPreparedStatementSupport(statement, script.getColumnNames());
        putLimitRest = batchPutUnit;
    }

    private void truncate() throws SQLException {
        String sql = String.format(
                "TRUNCATE TABLE %s",
                script.getTableName());
        Statement truncater = connection.createStatement();
        try {
            // TODO logging
            truncater.execute(sql);
        } finally {
            truncater.close();
        }
    }

    private PreparedStatement prepareStatement() throws SQLException {
        String sql = createSql();
        return connection.prepareStatement(sql);
    }

    private String createSql() {
        assert script.getColumnNames().isEmpty() == false;
        assert script.getCondition() == null;
        return MessageFormat.format(
                "INSERT INTO {0} ({1}) VALUES ({2})",
                script.getTableName(),
                JdbcUtil.join(script.getColumnNames()),
                JdbcUtil.join(Collections.nCopies(script.getColumnNames().size(), "?"))); //$NON-NLS-1$
    }

    @Override
    public void put(T object) throws IOException {
        try {
            support.setParameters(object);
            statement.addBatch();
        } catch (SQLException e) {
            // TODO logging
            sawError = true;
            throw new IOException(e);
        }
        putLimitRest--;
        if (putLimitRest == 0) {
            flush();
        }
        assert putLimitRest > 0;
    }

    private void flush() throws IOException {
        assert putLimitRest != batchPutUnit;
        try {
            statement.executeBatch();
            connection.commit();
            putCount += batchPutUnit - putLimitRest;
            putLimitRest = batchPutUnit;
        } catch (SQLException e) {
            // TODO logging
            sawError = true;
            throw new IOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        IOException occurred = null;
        if (statement != null) {
            if (sawError == false && putLimitRest != batchPutUnit) {
                try {
                    flush();
                } catch (IOException e) {
                    // TODO logging
                    occurred = e;
                }
            }
            try {
                statement.close();
            } catch (SQLException e) {
                // TODO logging
            }
        }
        try {
            connection.close();
        } catch (SQLException e) {
            // TODO logging
        }
        if (occurred != null) {
            throw occurred;
        }
    }
}
