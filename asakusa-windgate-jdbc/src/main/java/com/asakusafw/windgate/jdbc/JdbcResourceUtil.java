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
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport;
import com.asakusafw.windgate.core.vocabulary.JdbcProcess;
import com.asakusafw.windgate.core.vocabulary.JdbcProcess.OperationKind;

/**
 * Common utility classes for this package.
 * @since 0.2.2
 */
class JdbcResourceUtil {

    static final WindGateLogger WGLOG = new JdbcLogger(JdbcResourceUtil.class);

    static final Logger LOG = LoggerFactory.getLogger(JdbcResourceUtil.class);

    static String join(Iterable<String> list) {
        assert list != null;
        Iterator<String> iterator = list.iterator();
        assert iterator.hasNext();
        StringBuilder buf = new StringBuilder();
        buf.append(iterator.next());
        while (iterator.hasNext()) {
            buf.append(", ");
            buf.append(iterator.next());
        }
        return buf.toString();
    }

    static <T> JdbcScript<T> convert(
            JdbcProfile profile,
            ProcessScript<T> process,
            ParameterList arguments,
            DriverScript.Kind kind) throws IOException {
        assert profile != null;
        assert process != null;
        assert arguments != null;
        assert kind != null;
        String supportClassName = extract(profile, process, kind, JdbcProcess.JDBC_SUPPORT, true);
        DataModelJdbcSupport<? super T> support = loadSupport(profile, process, supportClassName);

        String tableName = extract(profile, process, kind, JdbcProcess.TABLE, true);
        String columnNameList = extract(profile, process, kind, JdbcProcess.COLUMNS, true);
        List<String> columnNames = Arrays.asList(columnNameList.split("\\s*,\\s*")); //$NON-NLS-1$
        if (support.isSupported(columnNames) == false) {
            WGLOG.error("E01003",
                    profile.getResourceName(),
                    process.getName(),
                    supportClassName);
            throw new IOException(MessageFormat.format(
                    "JDBC support class {2} does not support columns: {3} (resource={0}, process={1})",
                    profile.getResourceName(),
                    process.getName(),
                    support.getClass().getName(),
                    columnNames));
        }
        String condition = extract(profile, process, kind, JdbcProcess.CONDITION, false);
        if (kind == DriverScript.Kind.SOURCE) {
            if (condition == null || condition.isEmpty()) {
                LOG.debug("\"WHERE\" clause is not specified in source process \"{}\"",
                        profile.getResourceName(),
                        process.getName());
                condition = null;
            } else {
                try {
                    condition = arguments.replace(condition, true);
                } catch (IllegalArgumentException e) {
                    WGLOG.error("E01001",
                            profile.getResourceName(),
                            process.getName(),
                            kind.prefix,
                            JdbcProcess.CONDITION.key(),
                            condition);
                    throw new IOException(MessageFormat.format(
                            "\"{3}\" failed to resolve parameters: \"{4}\" (resource={0}, process={1}, kind={2})",
                            profile.getResourceName(),
                            process.getName(),
                            kind,
                            JdbcProcess.CONDITION.key(),
                            condition), e);
                }
            }
        }
        if (kind == DriverScript.Kind.DRAIN) {
            condition = null;
            String operationString = extract(profile, process, kind, JdbcProcess.OPERATION, true);
            JdbcProcess.OperationKind op = JdbcProcess.OperationKind.find(operationString);
            if (op != OperationKind.INSERT_AFTER_TRUNCATE) {
                WGLOG.error("E01001",
                        profile.getResourceName(),
                        process.getName(),
                        kind.prefix,
                        JdbcProcess.OPERATION.key(),
                        operationString);
                throw new IOException(MessageFormat.format(
                        "Resource \"{0}\" supports only {4} in \"{3}\": \"{5}\" (process={1}, kind={2})",
                        profile.getResourceName(),
                        process.getName(),
                        kind,
                        JdbcProcess.OPERATION.key(),
                        JdbcProcess.OperationKind.INSERT_AFTER_TRUNCATE,
                        operationString));
            }
        }
        return new JdbcScript<T>(process.getName(), support, tableName, columnNames, condition);
    }

    private static String extract(
            JdbcProfile profile,
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
            WGLOG.error("E01001",
                    profile.getResourceName(),
                    process.getName(),
                    kind.prefix,
                    item.key(),
                    value);
            throw new IOException(MessageFormat.format(
                    "Resource \"{0}\" requires config \"{3}\" (process={1}, kind={2})",
                    profile.getResourceName(),
                    process.getName(),
                    kind,
                    item.key()));
        }
        return value == null ? null : value.trim();
    }

    static <T> T newDataModel(
            JdbcProfile profile,
            ProcessScript<T> script) throws IOException {
        assert script != null;
        Class<T> dataClass = script.getDataClass();
        LOG.debug("Creating data model object: {} (resource={}, process={})", new Object[] {
                dataClass.getName(),
                profile.getResourceName(),
                script.getName(),
        });
        try {
            T object = dataClass.newInstance();
            return object;
        } catch (Exception e) {
            WGLOG.error(e, "E01002",
                    profile.getResourceName(),
                    script.getName(),
                    dataClass.getName());
            throw new IOException(MessageFormat.format(
                    "Failed to create a new instance: {2} (resource={0}, process={1})",
                    profile.getResourceName(),
                    script.getName(),
                    dataClass.getName()), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> DataModelJdbcSupport<? super T> loadSupport(
            JdbcProfile profile,
            ProcessScript<T> script,
            String supportClassName) throws IOException {
        assert script != null;
        assert supportClassName != null;
        LOG.debug("Creating JDBC support object: {} (resource={}, process={})", new Object[] {
                supportClassName,
                profile.getResourceName(),
                script.getName(),
        });
        Class<?> supportClass;
        try {
            supportClass = Class.forName(supportClassName, true, profile.getClassLoader());
        } catch (ClassNotFoundException e) {
            WGLOG.error(e, "E01003",
                    profile.getResourceName(),
                    script.getName(),
                    supportClassName);
            throw new IOException(MessageFormat.format(
                    "Failed to load a JDBC support class: {2} (resource={0}, process={1})",
                    profile.getResourceName(),
                    script.getName(),
                    supportClassName), e);
        }
        if (DataModelJdbcSupport.class.isAssignableFrom(supportClass) == false) {
            WGLOG.error("E01003",
                    profile.getResourceName(),
                    script.getName(),
                    supportClassName);
            throw new IOException(MessageFormat.format(
                    "JDBC support class must be a subtype of {3}: {2} (resource={0}, process={1})",
                    profile.getResourceName(),
                    script.getName(),
                    supportClass.getName(),
                    DataModelJdbcSupport.class.getName()));
        }
        DataModelJdbcSupport<?> obj;
        try {
            obj = supportClass.asSubclass(DataModelJdbcSupport.class).newInstance();
        } catch (Exception e) {
            WGLOG.error(e, "E01003",
                    profile.getResourceName(),
                    script.getName(),
                    supportClassName);
            throw new IOException(MessageFormat.format(
                    "Failed to create a JDBC support object: {2} (resource={0}, process={1})",
                    profile.getResourceName(),
                    script.getName(),
                    supportClass.getName()), e);
        }
        if (obj.getSupportedType().isAssignableFrom(script.getDataClass()) == false) {
            WGLOG.error("E01003",
                    profile.getResourceName(),
                    script.getName(),
                    supportClassName);
            throw new IOException(MessageFormat.format(
                    "JDBC support class {2} does not support data model: {3} (resource={0}, process={1})",
                    profile.getResourceName(),
                    script.getName(),
                    supportClass.getName(),
                    script.getDataClass().getName()));
        }
        return (DataModelJdbcSupport<? super T>) obj;
    }
}
