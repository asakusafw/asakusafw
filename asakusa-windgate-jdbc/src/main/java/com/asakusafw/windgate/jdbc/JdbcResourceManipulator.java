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
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.ResourceManipulator;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.util.ProcessUtil;
import com.asakusafw.windgate.core.vocabulary.JdbcProcess;

/**
 * An implementation of {@link ResourceManipulator} using JDBC.
 * @since 0.2.2
 */
public class JdbcResourceManipulator extends ResourceManipulator {

    static final Logger LOG = LoggerFactory.getLogger(JdbcResourceManipulator.class);

    private final JdbcProfile profile;

    private final ParameterList arguments;

    /**
     * Creates a new instance.
     * @param profile the profile
     * @param arguments the arguments
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JdbcResourceManipulator(JdbcProfile profile, ParameterList arguments) {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        this.profile = profile;
        this.arguments = arguments;
    }

    @Override
    public String getName() {
        return profile.getResourceName();
    }

    @Override
    public void cleanupSource(ProcessScript<?> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        JdbcScript<?> jdbc = createOppositeJdbcScript(script, DriverScript.Kind.SOURCE);
        truncate(jdbc);
    }

    @Override
    public void cleanupDrain(ProcessScript<?> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        JdbcScript<?> jdbc = createOppositeJdbcScript(script, DriverScript.Kind.DRAIN);
        truncate(jdbc);
    }

    private void truncate(JdbcScript<?> jdbc) throws IOException {
        assert jdbc != null;
        Connection conn = profile.openConnection();
        try {
            LOG.info("Truncating table: {} (for {})",
                    jdbc.getTableName(),
                    jdbc.getName());
            Statement statement = conn.createStatement();
            statement.execute(profile.getTruncateStatement(jdbc.getTableName()));
            conn.commit();
        } catch (SQLException e) {
            LOG.warn(MessageFormat.format(
                    "Failed to truncate table: {1} (process={0})",
                    jdbc.getName(),
                    jdbc.getTableName()), e);
        } finally {
            close(conn);
        }
    }

    @Override
    public <T> SourceDriver<T> createSourceForSource(ProcessScript<T> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        JdbcScript<T> jdbc = JdbcResourceUtil.convert(profile, script, arguments, DriverScript.Kind.SOURCE);
        T object = ProcessUtil.newDataModel(profile.getResourceName(), script);
        boolean succeed = false;
        Connection conn = profile.openConnection();
        try {
            JdbcSourceDriver<T> result = new JdbcSourceDriver<T>(profile, jdbc, conn, object);
            succeed = true;
            return result;
        } finally {
            if (succeed == false) {
                close(conn);
            }
        }
    }

    @Override
    public <T> DrainDriver<T> createDrainForSource(ProcessScript<T> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        JdbcScript<T> jdbc = createOppositeJdbcScript(script, DriverScript.Kind.SOURCE);

        boolean succeed = false;
        Connection conn = profile.openConnection();
        try {
            JdbcDrainDriver<T> result = new JdbcDrainDriver<T>(profile, jdbc, conn, false);
            succeed = true;
            return result;
        } finally {
            if (succeed == false) {
                close(conn);
            }
        }
    }

    @Override
    public <T> SourceDriver<T> createSourceForDrain(ProcessScript<T> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        JdbcScript<T> jdbc = createOppositeJdbcScript(script, DriverScript.Kind.DRAIN);
        T object = ProcessUtil.newDataModel(profile.getResourceName(), script);
        boolean succeed = false;
        Connection conn = profile.openConnection();
        try {
            JdbcSourceDriver<T> result = new JdbcSourceDriver<T>(profile, jdbc, conn, object);
            succeed = true;
            return result;
        } finally {
            if (succeed == false) {
                close(conn);
            }
        }
    }

    @Override
    public <T> DrainDriver<T> createDrainForDrain(ProcessScript<T> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        JdbcScript<T> jdbc = JdbcResourceUtil.convert(profile, script, arguments, DriverScript.Kind.DRAIN);
        boolean succeed = false;
        Connection conn = profile.openConnection();
        try {
            JdbcDrainDriver<T> result = new JdbcDrainDriver<T>(profile, jdbc, conn, false);
            succeed = true;
            return result;
        } finally {
            if (succeed == false) {
                close(conn);
            }
        }
    }

    private void close(Connection conn) {
        assert conn != null;
        try {
            conn.close();
        } catch (SQLException e) {
            LOG.warn(MessageFormat.format(
                    "Failed to close JDBC connection: {0}",
                    getName()), e);
        }
    }

    private <T> JdbcScript<T> createOppositeJdbcScript(
            ProcessScript<T> process,
            DriverScript.Kind kind) throws IOException {
        assert process != null;
        assert kind != null;
        DriverScript driver = process.getDriverScript(kind);
        if (driver.getResourceName().equals(getName()) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid resource: {0} (direction={1})",
                    process.getName(),
                    kind.prefix));
        }
        ProcessScript<T> opposite;
        if (kind == DriverScript.Kind.SOURCE) {
            opposite = createDrainProcessFromSource(process);
        } else if (kind == DriverScript.Kind.DRAIN) {
            opposite = createSourceProcessFromDrain(process);
        } else {
            throw new AssertionError(kind);
        }
        return JdbcResourceUtil.convert(profile, opposite, arguments, kind.opposite());
    }

    private <T> ProcessScript<T> createSourceProcessFromDrain(ProcessScript<T> script) {
        assert script != null;
        Map<String, String> rebuilt = new HashMap<String, String>(script.getDrainScript().getConfiguration());
        rebuilt.remove(JdbcProcess.CONDITION.key());
        rebuilt.remove(JdbcProcess.OPERATION.key());
        return new ProcessScript<T>(
                script.getName(),
                script.getProcessType(),
                script.getDataClass(),
                new DriverScript(script.getDrainScript().getResourceName(), rebuilt),
                script.getSourceScript());
    }

    private <T> ProcessScript<T> createDrainProcessFromSource(ProcessScript<T> script) {
        assert script != null;
        Map<String, String> rebuilt = new HashMap<String, String>(script.getSourceScript().getConfiguration());
        rebuilt.remove(JdbcProcess.CONDITION.key());
        rebuilt.put(JdbcProcess.OPERATION.key(), JdbcProcess.OperationKind.INSERT_AFTER_TRUNCATE.value());
        return new ProcessScript<T>(
                script.getName(),
                script.getProcessType(),
                script.getDataClass(),
                script.getDrainScript(),
                new DriverScript(script.getSourceScript().getResourceName(), rebuilt));
    }
}
