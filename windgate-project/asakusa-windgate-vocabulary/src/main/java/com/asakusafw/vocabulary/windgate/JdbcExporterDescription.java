/**
 * Copyright 2011-2019 Asakusa Framework Team.
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

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.asakusafw.runtime.util.VariableTable;
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
 * @version 0.9.0
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

    /**
     * Returns the custom truncate statement in SQL (for EXPERT).
     * This will execute before loading export data to target table.
     * @return the custom truncate statement, or {@code null} to use the default truncate statement
     * @since 0.7.3
     */
    public String getCustomTruncate() {
        return null;
    }

    /**
     * Returns WindGate JDBC export options.
     * @return options
     * @since 0.9.0
     */
    public Collection<? extends JdbcAttribute> getOptions() {
        return Collections.emptySet();
    }

    @Override
    public final DriverScript getDriverScript() {
        String descriptionClass = getClass().getName();
        Class<?> modelType = getModelType();
        Class<? extends DataModelJdbcSupport<?>> supportClass = getJdbcSupport();
        String table = getTableName();
        List<String> columns = getColumnNames();
        String customTruncate = getCustomTruncate();
        Collection<? extends JdbcAttribute> options = getOptions();

        JdbcDescriptionUtil.checkCommonConfig(descriptionClass, modelType, supportClass, table, columns, options);

        if (customTruncate != null && customTruncate.trim().isEmpty()) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("JdbcExporterDescription.errorEmptyStringProperty"), //$NON-NLS-1$
                    customTruncate,
                    "getCustomTruncate()")); //$NON-NLS-1$
        }

        Map<String, String> configuration = new HashMap<>();
        configuration.put(JdbcProcess.TABLE.key(), table);
        configuration.put(JdbcProcess.COLUMNS.key(), JdbcDescriptionUtil.join(columns));
        configuration.put(JdbcProcess.JDBC_SUPPORT.key(), supportClass.getName());
        configuration.put(JdbcProcess.OPERATION.key(), JdbcProcess.OperationKind.INSERT_AFTER_TRUNCATE.value());
        if (customTruncate != null) {
            configuration.put(JdbcProcess.CUSTOM_TRUNCATE.key(), customTruncate);
        }
        if (options != null && options.isEmpty() == false) {
            configuration.put(JdbcProcess.OPTIONS.key(), JdbcDescriptionUtil.join(options.stream()
                    .map(JdbcAttribute::getSymbol)
                    .collect(Collectors.toList())));
        }

        Set<String> parameters = VariableTable.collectVariableNames(customTruncate);
        return new DriverScript(Constants.JDBC_RESOURCE_NAME, configuration, parameters);
    }

    /**
     * JDBC export options.
     * @see JdbcExporterDescription#getOptions()
     * @since 0.9.0
     */
    public enum Option implements JdbcAttribute {

        /**
         * Use direct path insert instead of conventional insert on Oracle.
         */
        ORACLE_DIRPATH(JdbcProcess.OptionSymbols.ORACLE_DIRPATH),
        ;

        private final String symbol;

        Option(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String getSymbol() {
            return symbol;
        }
    }
}
