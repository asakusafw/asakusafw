/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport.DataModelPreparedStatement;

/**
 * An implementation of {@link DrainDriver} using JDBC.
 * @param <T> the type of data model object
 * @since 0.2.2
 */
public class JdbcDrainDriver<T> implements DrainDriver<T> {

    static final WindGateLogger WGLOG = new JdbcLogger(JdbcDrainDriver.class);

    static final Logger LOG = LoggerFactory.getLogger(JdbcDrainDriver.class);

    private final JdbcProfile profile;

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
        this.profile = profile;
        this.script = script;
        this.connection = connection;
        this.batchPutUnit = profile.getBatchPutUnit();
        this.truncateOnPrepare = truncateOnPrepare;
    }

    @Override
    public void prepare() throws IOException {
        LOG.debug("Preparing JDBC resource drain (resource={}, table={})",
                profile.getResourceName(),
                script.getTableName());
        try {
            if (truncateOnPrepare) {
                truncate();
            }
        } catch (SQLException e) {
            sawError = true;
            for (SQLException ex = e; ex != null; ex = ex.getNextException()) {
                WGLOG.error(ex, "E04001",
                        profile.getResourceName(),
                        script.getName(),
                        script.getTableName());
            }
            throw new IOException(MessageFormat.format(
                    "Failed to prepare JDBC drain (resource={0}, table={1}, columns={2})",
                    profile.getResourceName(),
                    script.getTableName(),
                    script.getColumnNames()), e);
        }
        try {
            this.statement = prepareStatement();
        } catch (SQLException e) {
            sawError = true;
            for (SQLException ex = e; ex != null; ex = ex.getNextException()) {
                WGLOG.error(ex, "E04002",
                        profile.getResourceName(),
                        script.getName(),
                        script.getTableName(),
                        script.getColumnNames());
            }
            throw new IOException(MessageFormat.format(
                    "Failed to prepare JDBC drain (resource={0}, table={1}, columns={2})",
                    profile.getResourceName(),
                    script.getTableName(),
                    script.getColumnNames()), e);
        }
        LOG.debug("Creating PreparedStatement support {} for {}",
                script.getSupport().getClass().getName(),
                script.getColumnNames());
        support = script.getSupport().createPreparedStatementSupport(statement, script.getColumnNames());
        putLimitRest = batchPutUnit;
    }

    private void truncate() throws SQLException {
        String sql = profile.getTruncateStatement(script.getTableName());
        Statement truncater = connection.createStatement();
        try {
            WGLOG.info("I04001",
                    profile.getResourceName(),
                    script.getName(),
                    script.getTableName());
            LOG.debug("Executing SQL: {}", sql);
            truncater.execute(sql);
            LOG.debug("Executed SQL: {}", sql);
            connection.commit();
            LOG.debug("Committed {}", sql);
        } finally {
            truncater.close();
        }
    }

    private PreparedStatement prepareStatement() throws SQLException {
        String sql = createSql();
        LOG.debug("Preparing SQL: {}", sql);
        return connection.prepareStatement(sql);
    }

    private String createSql() {
        assert script.getColumnNames().isEmpty() == false;
        assert script.getCondition() == null;
        return MessageFormat.format(
                "INSERT INTO {0} ({1}) VALUES ({2})",
                script.getTableName(),
                JdbcResourceUtil.join(script.getColumnNames()),
                JdbcResourceUtil.join(Collections.nCopies(script.getColumnNames().size(), "?"))); //$NON-NLS-1$
    }

    @Override
    public void put(T object) throws IOException {
        try {
            support.setParameters(object);
            statement.addBatch();
        } catch (SQLException e) {
            sawError = true;
            for (SQLException ex = e; ex != null; ex = ex.getNextException()) {
                WGLOG.error(ex, "E04003",
                        profile.getResourceName(),
                        script.getName(),
                        script.getTableName(),
                        script.getColumnNames());
            }
            throw new IOException(MessageFormat.format(
                    "Failed to put object to JDBC drain: {2} (resource={0}, table={1})",
                    profile.getResourceName(),
                    script.getTableName(),
                    object), e);
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
            LOG.debug("Flushing {} rows into {}",
                    batchPutUnit - putLimitRest,
                    script.getTableName());
            statement.executeBatch();
            connection.commit();
            putCount += batchPutUnit - putLimitRest;
            putLimitRest = batchPutUnit;
        } catch (SQLException e) {
            sawError = true;
            for (SQLException ex = e; ex != null; ex = ex.getNextException()) {
                WGLOG.error(ex, "E04004",
                        profile.getResourceName(),
                        script.getName(),
                        script.getTableName(),
                        script.getColumnNames());
            }
            throw new IOException(MessageFormat.format(
                    "Failed to flush table into JDBC drain (resource={0}, table={1})",
                    profile.getResourceName(),
                    script.getTableName()), e);
        }
    }

    @Override
    public void close() throws IOException {
        LOG.debug("Closing JDBC resource drain (resource={}, table={})",
                profile.getResourceName(),
                script.getTableName());
        IOException occurred = null;
        if (statement != null) {
            if (sawError == false && putLimitRest != batchPutUnit) {
                try {
                    flush();
                } catch (IOException e) {
                    occurred = e;
                }
            }
            try {
                statement.close();
            } catch (SQLException e) {
                for (SQLException ex = e; ex != null; ex = ex.getNextException()) {
                    WGLOG.warn(ex, "W04001",
                            profile.getResourceName(),
                            script.getName(),
                            script.getTableName(),
                            script.getColumnNames());
                }
            }
        }
        try {
            connection.close();
        } catch (SQLException e) {
            for (SQLException ex = e; ex != null; ex = ex.getNextException()) {
                WGLOG.warn(ex, "W02001",
                        profile.getResourceName(),
                        script.getName());
            }
        }
        if (occurred != null) {
            throw occurred;
        }
    }
}
