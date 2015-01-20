/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.windgate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport;
import com.asakusafw.windgate.core.vocabulary.JdbcProcess;

/**
 * An abstract super class that describes exporter process using JDBC/WindGate.
 * Each subclass must satisfy following requirements:
 * <ul>
 * <li> declared as {@code public} </li>
 * <li> not declared as {@code abstract} </li>
 * <li> not declared type parameters </li>
 * <li> not declared any explicit constructors </li>
 * </ul>
 * @since 0.2.2
 */
public abstract class JdbcExporterDescription extends WindGateExporterDescription {

    /**
     * Returns an implementation of {@link DataModelJdbcSupport} class.
     * The class must be supports target {@link #getModelType() data model type} and
     * target {@link #getColumnNames() columns}.
     * @return the class of {@link DataModelJdbcSupport}
     */
    public abstract Class<? extends DataModelJdbcSupport<?>> getJdbcSupport();

    /**
     * Returns the target table name.
     * @return the target table name
     */
    public abstract String getTableName();

    /**
     * Returns the target column names.
     * @return the target column names
     */
    public abstract List<String> getColumnNames();

    @Override
    public final DriverScript getDriverScript() {
        String descriptionClass = getClass().getName();
        Class<?> modelType = getModelType();
        Class<? extends DataModelJdbcSupport<?>> supportClass = getJdbcSupport();
        String table = getTableName();
        List<String> columns = getColumnNames();

        JdbcDescriptionUtil.checkCommonConfig(descriptionClass, modelType, supportClass, table, columns);

        Map<String, String> configuration = new HashMap<String, String>();
        configuration.put(JdbcProcess.TABLE.key(), table);
        configuration.put(JdbcProcess.COLUMNS.key(), JdbcDescriptionUtil.join(columns));
        configuration.put(JdbcProcess.JDBC_SUPPORT.key(), supportClass.getName());
        configuration.put(JdbcProcess.OPERATION.key(), JdbcProcess.OperationKind.INSERT_AFTER_TRUNCATE.value());

        return new DriverScript(Constants.JDBC_RESOURCE_NAME, configuration);
    }
}
