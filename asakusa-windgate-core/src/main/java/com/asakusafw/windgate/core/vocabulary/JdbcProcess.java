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
package com.asakusafw.windgate.core.vocabulary;

/**
 * WindGate database resource configuration.
 * @since 0.2.2
 */
public enum JdbcProcess implements ConfigurationItem {

    /**
     * The script key of {@link DataModelJdbcSupport} class.
     */
    JDBC_SUPPORT(
            "jdbcSupport", //$NON-NLS-1$
            "DataModelJdbcSupport class name"
    ),

    /**
     * The script key of target table name.
     */
    TABLE(
            "table", //$NON-NLS-1$
            "Target table name"
    ),

    /**
     * The script key of target column names.
     */
    COLUMNS(
            "columns", //$NON-NLS-1$
            "Target column names separated by comma"
    ),

    /**
     * The script key of search condition (for sources).
     * The parameters in value will be replaced.
     */
    CONDITION(
            "condition", //$NON-NLS-1$
            "The condition expression"
    ),

    /**
     * The script key of target operation (for drains).
     * @see OperationKind
     */
    OPERATION(
            "operation",
            "Target operation kind"
    ),
    ;

    private final String key;

    private final String description;

    private JdbcProcess(String key, String description) {
        assert key != null;
        assert description != null;
        this.key = key;
        this.description = description;
    }

    @Override
    public final String key() {
        return key;
    }

    @Override
    public String description() {
        return description;
    }

    /**
     * The drain operation kinds.
     * These contain optional operations.
     * @since 0.2.2
     */
    public enum OperationKind {

        /**
         * Attempts to insert all.
         * If primary/unique keys are conflicted, this operation was failed.
         */
        INSERT,

        /**
         * Truncates target table and then inserts rows.
         */
        INSERT_AFTER_TRUNCATE,
        ;

        /**
         * Returns the value representation of this kind.
         * @return the value representation of this
         */
        public String value() {
            return name().toLowerCase();
        }

        /**
         * Returns the corresponded kind to the value representation.
         * @param value target representation
         * @return the corresponded kind, or {@code null} if not found
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public static OperationKind find(String value) {
            if (value == null) {
                throw new IllegalArgumentException("value must not be null"); //$NON-NLS-1$
            }
            try {
                return OperationKind.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
