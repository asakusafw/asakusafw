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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport.DataModelResultSet;

/**
 * An implementation of {@link SourceDriver} using JDBC.
 * @param <T> the type of data model object
 * @since 0.2.2
 */
public class JdbcSourceDriver<T> implements SourceDriver<T> {

    static final WindGateLogger WGLOG = new JdbcLogger(JdbcSourceDriver.class);

    static final Logger LOG = LoggerFactory.getLogger(JdbcSourceDriver.class);

    private final JdbcProfile profile;

    private final JdbcScript<T> script;

    private final Connection connection;

    private final T object;

    private ResultSet resultSet;

    private DataModelResultSet<? super T> support;

    private boolean sawNext;

    /**
     * Creates a new instance.
     * @param profile the profile of the target database
     * @param script the script of this action
     * @param connection the connection
     * @param object the data model object used as a buffer
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public JdbcSourceDriver(
            JdbcProfile profile,
            JdbcScript<T> script,
            Connection connection,
            T object) {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        if (connection == null) {
            throw new IllegalArgumentException("connection must not be null"); //$NON-NLS-1$
        }
        if (object == null) {
            throw new IllegalArgumentException("object must not be null"); //$NON-NLS-1$
        }
        this.profile = profile;
        this.script = script;
        this.connection = connection;
        this.object = object;
    }

    @Override
    public void prepare() throws IOException {
        LOG.debug("Preparing JDBC resource source (resource={}, table={})",
                profile.getResourceName(),
                script.getTableName());
        try {
            this.resultSet = prepareResultSet();
        } catch (SQLException e) {
            WGLOG.error(e, "E03001",
                    profile.getResourceName(),
                    script.getName(),
                    script.getTableName(),
                    script.getColumnNames());
            throw new IOException(MessageFormat.format(
                    "Failed to prepare JDBC source (resource={0}, table={1}, columns={2})",
                    profile.getResourceName(),
                    script.getTableName(),
                    script.getColumnNames()), e);
        }
        LOG.debug("Creating ResultSet support {} for {}",
                script.getSupport().getClass().getName(),
                script.getColumnNames());
        support = script.getSupport().createResultSetSupport(resultSet, script.getColumnNames());
    }

    private ResultSet prepareResultSet() throws SQLException {
        String sql = createSql();
        Statement statement = connection.createStatement();
        boolean succeed = false;
        try {
            WGLOG.info("I03001",
                    profile.getResourceName(),
                    script.getName(),
                    script.getTableName(),
                    script.getColumnNames());
            if (profile.getBatchGetUnit() != 0) {
                statement.setFetchSize(profile.getBatchGetUnit());
            }
            LOG.debug("Executing SQL: {}", sql);
            ResultSet result = statement.executeQuery(sql);
            LOG.debug("Executed SQL: {}", sql);
            WGLOG.info("I03002",
                    profile.getResourceName(),
                    script.getName(),
                    script.getTableName(),
                    script.getColumnNames());
            succeed = true;
            return result;
        } finally {
            if (succeed == false) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    WGLOG.warn(e, "W03001",
                            profile.getResourceName(),
                            script.getName(),
                            script.getTableName(),
                            script.getColumnNames());
                }
            }
        }
    }

    private String createSql() {
        assert script.getColumnNames().isEmpty() == false;
        if (script.getCondition() != null) {
            assert script.getCondition().isEmpty() == false;
            return MessageFormat.format(
                    "SELECT {1} FROM {0} WHERE {2}",
                    script.getTableName(),
                    JdbcResourceUtil.join(script.getColumnNames()),
                    script.getCondition());
        } else {
            return MessageFormat.format(
                    "SELECT {1} FROM {0}",
                    script.getTableName(),
                    JdbcResourceUtil.join(script.getColumnNames()));
        }
    }

    @Override
    public boolean next() throws IOException {
        try {
            sawNext = support.next(object);
            return sawNext;
        } catch (SQLException e) {
            sawNext = false;
            WGLOG.error(e, "E03001",
                    profile.getResourceName(),
                    script.getName(),
                    script.getTableName(),
                    script.getColumnNames());
            throw new IOException(MessageFormat.format(
                    "Failed to fetch next object from JDBC source (resource={0}, table={1})",
                    profile.getResourceName(),
                    script.getTableName()), e);
        }
    }

    @Override
    public T get() throws IOException {
        if (sawNext == false) {
            throw new IOException("Next data model was not prepared");
        }
        return object;
    }

    @Override
    public void close() throws IOException {
        LOG.debug("Closing JDBC resource source (resource={}, table={})",
                profile.getResourceName(),
                script.getTableName());
        sawNext = false;
        if (resultSet != null) {
            Statement statement = null;
            try {
                statement = resultSet.getStatement();
            } catch (SQLException e) {
                WGLOG.warn(e, "W03001",
                        profile.getResourceName(),
                        script.getName(),
                        script.getTableName(),
                        script.getColumnNames());
            }
            try {
                resultSet.close();
                resultSet = null;
                support = null;
            } catch (SQLException e) {
                WGLOG.warn(e, "W03001",
                        profile.getResourceName(),
                        script.getName(),
                        script.getTableName(),
                        script.getColumnNames());
            }
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                WGLOG.warn(e, "W03001",
                        profile.getResourceName(),
                        script.getName(),
                        script.getTableName(),
                        script.getColumnNames());
            }
        }
        try {
            connection.close();
        } catch (SQLException e) {
            WGLOG.warn(e, "W02001",
                    profile.getResourceName(),
                    script.getName());
        }
    }
}
