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
package com.asakusafw.vocabulary.windgate;

import java.text.MessageFormat;
import java.util.List;

import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport;

/**
 * Common utilities for JDBC.
 * @since 0.2.2
 */
final class JdbcDescriptionUtil {

    static void checkCommonConfig(
            String descriptionClass,
            Class<?> modelType,
            Class<? extends DataModelJdbcSupport<?>> supportClass,
            String table,
            List<String> columns) {
        if (isEmpty(table)) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("JdbcDescriptionUtil.errorEmptyProperty"), //$NON-NLS-1$
                    descriptionClass,
                    "getTableName()")); //$NON-NLS-1$
        }

        if (columns == null) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("JdbcDescriptionUtil.errorNullProperty"), //$NON-NLS-1$
                    descriptionClass,
                    "getColumNames()")); //$NON-NLS-1$
        }
        if (columns.isEmpty()) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("JdbcDescriptionUtil.errorEmptyProperty"), //$NON-NLS-1$
                    descriptionClass,
                    "getColumNames()")); //$NON-NLS-1$
        }
        for (String column : columns) {
            if (isEmpty(column)) {
                throw new IllegalStateException(MessageFormat.format(
                        Messages.getString("JdbcDescriptionUtil.errorContainEmptyStringProperty"), //$NON-NLS-1$
                        descriptionClass,
                        "getColumnNames()")); //$NON-NLS-1$
            }
        }

        if (supportClass == null) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("JdbcDescriptionUtil.errorNullProperty"), //$NON-NLS-1$
                    descriptionClass,
                    "getJdbcSupport()")); //$NON-NLS-1$
        }

        DataModelJdbcSupport<?> support;
        try {
            support = supportClass.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("JdbcDescriptionUtil.errorFailedToInstantiate"), //$NON-NLS-1$
                    descriptionClass,
                    supportClass.getName()), e);
        }
        if (support.getSupportedType().isAssignableFrom(modelType) == false) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("JdbcDescriptionUtil.errorIncompatibleDataType"), //$NON-NLS-1$
                    descriptionClass,
                    supportClass.getName(),
                    modelType.getName()));
        }
        if (support.isSupported(columns) == false) {
            throw new IllegalStateException(MessageFormat.format(
                    Messages.getString("JdbcDescriptionUtil.errorUnsupportedColumns"), //$NON-NLS-1$
                    descriptionClass,
                    supportClass.getName(),
                    columns));
        }
    }

    static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    static String join(List<String> columns) {
        assert columns != null;
        assert columns.isEmpty() == false;
        StringBuilder buf = new StringBuilder();
        buf.append(columns.get(0));
        for (int i = 1, n = columns.size(); i < n; i++) {
            buf.append(", "); //$NON-NLS-1$
            buf.append(columns.get(i));
        }
        return buf.toString();
    }

    private JdbcDescriptionUtil() {
        return;
    }
}
