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
package com.asakusafw.testdriver.bulkloader;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;
import com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport;
import com.asakusafw.vocabulary.bulkloader.OriginalName;

/**
 * Target table information.
 * @param <T> type of mapped model
 */
public class TableInfo<T> {

    static final Logger LOG = LoggerFactory.getLogger(TableInfo.class);

    private static final Set<PropertyType> SUPPORTED_TYPES;
    static {
        Set<PropertyType> set = EnumSet.allOf(PropertyType.class);
        set.remove(PropertyType.OBJECT);
        set.remove(PropertyType.SEQUENCE);
        SUPPORTED_TYPES = Collections.unmodifiableSet(set);
    }

    private final DataModelDefinition<T> definition;

    private final String tableName;

    private final Map<String, PropertyName> columnsToProperties;

    private final String timestampColumn;

    /**
     * Creates a new instance.
     * If the target model supports the cache feature, this will extract timestamp column.
     * @param definition mapped model definition
     * @param tableName target table name
     * @param columnNames list of column names
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #TableInfo(DataModelDefinition, String, List, boolean)
     */
    public TableInfo(
            DataModelDefinition<T> definition,
            String tableName,
            List<String> columnNames) {
        this(definition, tableName, columnNames, true);
    }

    /**
     * Creates a new instance.
     * @param definition mapped model definition
     * @param tableName target table name
     * @param columnNames list of column names
     * @param extractTimestamp {@code true} to extract timestamp column if exists, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #getTimestampColumn()
     */
    public TableInfo(
            DataModelDefinition<T> definition,
            String tableName,
            List<String> columnNames,
            boolean extractTimestamp) {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        if (tableName == null) {
            throw new IllegalArgumentException("tableName must not be null"); //$NON-NLS-1$
        }
        if (columnNames == null) {
            throw new IllegalArgumentException("columnNames must not be null"); //$NON-NLS-1$
        }
        this.definition = definition;
        this.tableName = tableName;
        this.columnsToProperties = createMappings(columnNames);
        if (extractTimestamp) {
            this.timestampColumn = extractTimestampColumn();
        } else {
            this.timestampColumn = null;
        }
    }

    /**
     * Returns the model definition mapped the target table.
     * @return the model definition
     */
    public DataModelDefinition<T> getDefinition() {
        return definition;
    }

    /**
     * Returns the target table name.
     * @return the table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Returns the name of last modified timestamp column.
     * @return column name, or {@code null} if not defined
     */
    public String getTimestampColumn() {
        return timestampColumn;
    }

    /**
     * Returns relation between target tables's column names and
     * mapped model's property names.
     * <p>
     * Keys in which returned map keep their order as specified column list in the
     * {@link TableInfo#TableInfo(DataModelDefinition, String, List) constructor}.
     * </p>
     * @return the relation
     */
    public Map<String, PropertyName> getColumnsToProperties() {
        return columnsToProperties;
    }

    private String extractTimestampColumn() {
        assert definition != null;
        if (ThunderGateCacheSupport.class.isAssignableFrom(definition.getModelClass()) == false) {
            return null;
        }
        String columnName;
        try {
            ThunderGateCacheSupport support = definition.getModelClass()
                .asSubclass(ThunderGateCacheSupport.class)
                .newInstance();
            columnName = support.__tgc__TimestampColumn();
        } catch (Exception e) {
            LOG.warn(MessageFormat.format(
                    "テーブル{0}の最終更新時刻のカラム算出に失敗しました",
                    tableName), e);
            return null;
        }
        return columnName;
    }

    private Map<String, PropertyName> createMappings(List<String> columnNames) {
        assert definition != null;
        assert columnNames != null;
        Map<String, PropertyName> allMapping = extractAllMappings();
        Map<String, PropertyName> results = new LinkedHashMap<String, PropertyName>();
        for (String column : columnNames) {
            PropertyName propertyName = allMapping.get(column);
            if (propertyName == null) {
                LOG.warn(MessageFormat.format(
                        "カラム{0}.{1}に対応するプロパティが{2}に見つかりませんでした。このカラムをスキップします",
                        tableName,
                        column,
                        definition.getModelClass().getName()));
                continue;
            }
            results.put(column, propertyName);
        }
        return Collections.unmodifiableMap(results);
    }

    private Map<String, PropertyName> extractAllMappings() {
        assert definition != null;
        Map<String, PropertyName> results = new TreeMap<String, PropertyName>(String.CASE_INSENSITIVE_ORDER);
        for (PropertyName name : definition.getProperties()) {
            PropertyType type = definition.getType(name);
            assert type != null;
            if (acceptsType(name, type) == false) {
                continue;
            }
            String columnName = getOriginalName(name);
            if (columnName == null) {
                // currently may not null
                continue;
            }
            results.put(columnName, name);
        }
        return results;
    }

    private String getOriginalName(PropertyName name) {
        assert name != null;
        assert definition.getType(name) != null;
        OriginalName a = definition.getAnnotation(name, OriginalName.class);
        if (a != null) {
            return a.value();
        }

        Iterator<String> iter = name.getWords().iterator();
        assert iter.hasNext();
        StringBuilder buf = new StringBuilder();
        buf.append(iter.next());
        while (iter.hasNext()) {
            buf.append('_');
            buf.append(iter.next());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "The property \"{1}\" (in {0}) is not annotated with {2}, column name will be inferred as \"{3}\"",
                    definition.getModelClass().getName(),
                    name,
                    OriginalName.class.getSimpleName(),
                    buf));
        }
        return buf.toString();
    }

    private boolean acceptsType(PropertyName name, PropertyType type) {
        assert name != null;
        assert type != null;
        return SUPPORTED_TYPES.contains(type);
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Table({0}, model={1}, mapping={2})",
                tableName,
                definition.getModelClass().getName(),
                columnsToProperties);
    }
}
