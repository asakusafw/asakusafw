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
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.ResourceMirror;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport;
import com.asakusafw.windgate.core.vocabulary.JdbcProcess;
import com.asakusafw.windgate.core.vocabulary.JdbcProcess.OperationKind;

/**
 * An implementation of {@link ResourceMirror} using JDBC.
 * @since 0.2.3
 */
public class JdbcResourceMirror extends ResourceMirror {

    static final Logger LOG = LoggerFactory.getLogger(JdbcResourceMirror.class);

    private final JdbcProfile profile;

    private final ParameterList arguments;

    /**
     * Creates a new instance.
     * @param profile the profile of this resource
     * @param arguments the runtime arguments
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JdbcResourceMirror(JdbcProfile profile, ParameterList arguments) {
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
    public void prepare(GateScript script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Preparing JDBC resource: {}",
                getName());
        for (ProcessScript<?> process : script.getProcesses()) {
            if (process.getSourceScript().getResourceName().equals(getName())) {
                convert(process, DriverScript.Kind.SOURCE);
                newDataModel(process);
            }
            if (process.getDrainScript().getResourceName().equals(getName())) {
                convert(process, DriverScript.Kind.DRAIN);
            }
        }
    }

    @Override
    public <T> SourceDriver<T> createSource(ProcessScript<T> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Creating source driver for resource \"{}\" in process \"{}\"",
                getName(),
                script.getName());
        JdbcScript<T> jdbcScript = convert(script, DriverScript.Kind.SOURCE);
        T object = newDataModel(script);
        // TODO logging INFO
        Connection connection = profile.openConnection();
        boolean succeed = false;
        try {
            JdbcSourceDriver<T> driver = new JdbcSourceDriver<T>(profile, jdbcScript, connection, object);
            succeed = true;
            return driver;
        } finally {
            if (succeed == false) {
                try {
                    LOG.debug("Disposing source driver for resource \"{}\" in process \"{}\"",
                            getName(),
                            script.getName());
                    connection.close();
                } catch (SQLException e) {
                    // TODO logging WARN
                }
            }
        }
    }

    @Override
    public <T> DrainDriver<T> createDrain(ProcessScript<T> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Creating drain driver for resource \"{}\" in process \"{}\"",
                getName(),
                script.getName());
        JdbcScript<T> jdbcScript = convert(script, DriverScript.Kind.DRAIN);
        // TODO logging INFO
        Connection connection = profile.openConnection();
        boolean succeed = false;
        try {
            JdbcDrainDriver<T> driver = new JdbcDrainDriver<T>(profile, jdbcScript, connection, true);
            succeed = true;
            return driver;
        } finally {
            if (succeed == false) {
                try {
                    LOG.debug("Disposing drain driver for resource \"{}\" in process \"{}\"",
                            getName(),
                            script.getName());
                    connection.close();
                } catch (SQLException e) {
                    // TODO logging WARN
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        LOG.debug("Closing JDBC resource: {}",
                getName());
    }

    private <T> JdbcScript<T> convert(ProcessScript<T> process, DriverScript.Kind kind) throws IOException {
        assert process != null;
        assert kind != null;
        String supportClassName = extract(process, kind, JdbcProcess.JDBC_SUPPORT, true);
        DataModelJdbcSupport<? super T> support = loadSupport(process, supportClassName);

        String tableName = extract(process, kind, JdbcProcess.TABLE, true);
        String columnNameList = extract(process, kind, JdbcProcess.COLUMNS, true);
        List<String> columnNames = Arrays.asList(columnNameList.split("\\s*,\\s*")); //$NON-NLS-1$
        if (support.isSupported(columnNames) == false) {
            throw new IOException(MessageFormat.format(
                    "JDBC support class {2} does not support columns: {3} (resource={0}, process={1})",
                    getName(),
                    process.getName(),
                    support.getClass().getName(),
                    columnNames));
        }
        String condition = extract(process, kind, JdbcProcess.CONDITION, false);
        if (kind == DriverScript.Kind.SOURCE) {
            if (condition == null || condition.isEmpty()) {
                LOG.debug("Where clause is not specified in source process \"{}\"",
                        getName(),
                        process.getName());
                condition = null;
            } else {
                try {
                    condition = arguments.replace(condition, true);
                } catch (IllegalArgumentException e) {
                    throw new IOException(MessageFormat.format(
                            "\"{3}\" failed to resolve parameters: \"{4}\" (resource={0}, process={1}, kind={2})",
                            getName(),
                            process.getName(),
                            kind,
                            JdbcProcess.CONDITION.key(),
                            condition), e);
                }
            }
        }
        if (kind == DriverScript.Kind.DRAIN) {
            condition = null;
            String operationString = extract(process, kind, JdbcProcess.OPERATION, true);
            JdbcProcess.OperationKind op = JdbcProcess.OperationKind.find(operationString);
            if (op != OperationKind.INSERT_AFTER_TRUNCATE) {
                throw new IOException(MessageFormat.format(
                        "Resource \"{0}\" supports only {4} in \"{3}\": \"{5}\" (process={1}, kind={2})",
                        getName(),
                        process.getName(),
                        kind,
                        JdbcProcess.OPERATION.key(),
                        JdbcProcess.OperationKind.INSERT_AFTER_TRUNCATE,
                        operationString));
            }
        }
        return new JdbcScript<T>(support, tableName, columnNames, condition);
    }

    private String extract(
            ProcessScript<?> process,
            DriverScript.Kind kind,
            JdbcProcess item,
            boolean mandatory) throws IOException {
        assert process != null;
        assert kind != null;
        assert item != null;
        Map<String, String> conf = process.getDriverScript(kind).getConfiguration();
        String value = conf.get(item.key());
        if (mandatory && (value == null || value.isEmpty())) {
            throw new IOException(MessageFormat.format(
                    "Resource \"{0}\" requires config \"{3}\" (process={1}, kind={2})",
                    getName(),
                    process.getName(),
                    kind,
                    item.key()));
        }
        return value == null ? null : value.trim();
    }

    private <T> T newDataModel(ProcessScript<T> script) throws IOException {
        assert script != null;
        Class<T> dataClass = script.getDataClass();
        LOG.debug("Creating data model object: {} (resource={}, process={})", new Object[] {
                dataClass.getName(),
                getName(),
                script.getName(),
        });
        try {
            T object = dataClass.newInstance();
            return object;
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Failed to create a new instance: {2} (resource={0}, process={1})",
                    getName(),
                    script.getName(),
                    dataClass.getName()), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> DataModelJdbcSupport<? super T> loadSupport(
            ProcessScript<T> script,
            String supportClassName) throws IOException {
        assert script != null;
        assert supportClassName != null;
        LOG.debug("Creating JDBC support object: {} (resource={}, process={})", new Object[] {
                supportClassName,
                getName(),
                script.getName(),
        });
        Class<?> supportClass;
        try {
            supportClass = Class.forName(supportClassName, true, profile.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IOException(MessageFormat.format(
                    "Failed to load a JDBC support class: {2} (resource={0}, process={1})",
                    getName(),
                    script.getName(),
                    supportClassName), e);
        }
        if (DataModelJdbcSupport.class.isAssignableFrom(supportClass) == false) {
            throw new IOException(MessageFormat.format(
                    "JDBC support class must be a subtype of {3}: {2} (resource={0}, process={1})",
                    getName(),
                    script.getName(),
                    supportClass.getName(),
                    DataModelJdbcSupport.class.getName()));
        }
        DataModelJdbcSupport<?> obj;
        try {
            obj = supportClass.asSubclass(DataModelJdbcSupport.class).newInstance();
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Failed to create a JDBC support object: {2} (resource={0}, process={1})",
                    getName(),
                    script.getName(),
                    supportClass.getName()), e);
        }
        if (obj.getSupportedType().isAssignableFrom(script.getDataClass()) == false) {
            throw new IOException(MessageFormat.format(
                    "JDBC support class {2} does not support data model: {3} (resource={0}, process={1})",
                    getName(),
                    script.getName(),
                    supportClass.getName(),
                    script.getDataClass().getName()));
        }
        return (DataModelJdbcSupport<? super T>) obj;
    }
}
