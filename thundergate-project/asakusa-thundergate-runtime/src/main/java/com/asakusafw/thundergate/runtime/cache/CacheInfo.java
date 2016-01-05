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
package com.asakusafw.thundergate.runtime.cache;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * Cache information.
 * @since 0.2.3
 */
public class CacheInfo {

    private static final String COLUMN_SEPARATOR = ",";

    /**
     * The version of this cache feature.
     */
    public static final String FEATURE_VERSION = "0.2.0";

    /**
     * The key name of feature version.
     * @see #FEATURE_VERSION
     */
    public static final String KEY_FEATURE_VERSION = "cache-feature-version";

    /**
     * The key name of cache ID.
     */
    public static final String KEY_ID = "cache-id";

    /**
     * The key name of timestamp.
     */
    public static final String KEY_TIMESTAMP = "cache-timestamp";

    /**
     * The key name of target table/view name.
     */
    public static final String KEY_TABLE_NAME = "table-name";

    /**
     * The key name of target column names (each column name is splitted by comma).
     */
    public static final String KEY_COLUMN_NAMES = "column-name-list";

    /**
     * The key name of target model class name.
     */
    public static final String KEY_MODEL_CLASS_NAME = "model-class-name";

    /**
     * The key name of target model class version.
     */
    public static final String KEY_MODEL_CLASS_VERSION = "model-version";

    /**
     * The format of timestamp value ({@link #KEY_TIMESTAMP}).
     */
    public static final String FORMAT_TIMESTAMP = "yyyy-MM-dd HH:mm:ss";

    private final String featureVersion;

    private final String id;

    private final Calendar timestamp;

    private final String tableName;

    private final Set<String> columnNames;

    private final String modelClassName;

    private final long modelClassVersion;

    /**
     * Creates a new instance.
     * @param featureVersion the cache feature version
     * @param id cache ID
     * @param timestamp last modified timestamp
     * @param tableName target table name
     * @param columnNames target column names
     * @param modelClassName target model class name
     * @param modelClassVersion target model class version
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CacheInfo(
            String featureVersion,
            String id,
            Calendar timestamp,
            String tableName,
            Collection<String> columnNames,
            String modelClassName,
            long modelClassVersion) {
        if (featureVersion == null) {
            throw new IllegalArgumentException("featureVersion must not be null"); //$NON-NLS-1$
        }
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp must not be null"); //$NON-NLS-1$
        }
        if (tableName == null) {
            throw new IllegalArgumentException("tableName must not be null"); //$NON-NLS-1$
        }
        if (columnNames == null) {
            throw new IllegalArgumentException("columnNames must not be null"); //$NON-NLS-1$
        }
        if (modelClassName == null) {
            throw new IllegalArgumentException("modelClassName must not be null"); //$NON-NLS-1$
        }
        this.featureVersion = featureVersion;
        this.id = id;
        this.timestamp = (Calendar) timestamp.clone();
        this.timestamp.set(Calendar.MILLISECOND, 0);
        this.tableName = tableName;
        this.columnNames = new TreeSet<String>(columnNames);
        this.modelClassName = modelClassName;
        this.modelClassVersion = modelClassVersion;
    }

    /**
     * Returns target feature version of this cache.
     * @return the target cache feature version
     */
    public String getFeatureVersion() {
        return featureVersion;
    }

    /**
     * Returns the cache ID.
     * @return the cache ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the timestamp when this cache was created.
     * @return the timestamp
     */
    public Calendar getTimestamp() {
        return (Calendar) timestamp.clone();
    }

    /**
     * Returns the target table/view name for this cache.
     * @return the table/view name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Returns the column names for this cache.
     * @return the column names
     */
    public Set<String> getColumnNames() {
        return columnNames;
    }

    /**
     * Returns the target model class name.
     * @return the model class name
     */
    public String getModelClassName() {
        return modelClassName;
    }

    /**
     * Returns the target model class version.
     * @return the modelClassVersion
     */
    public long getModelClassVersion() {
        return modelClassVersion;
    }

    /**
     * Loads {@link CacheInfo} from {@link Properties} created by using {@link #storeTo(Properties)}.
     * @param properties the source properties
     * @return the loaded object
     * @throws IllegalArgumentException if source properties object is invalid
     */
    public static CacheInfo loadFrom(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        String featureVersion = loadProperty(properties, KEY_FEATURE_VERSION);
        String id = loadProperty(properties, KEY_ID);
        String timestampString = loadProperty(properties, KEY_TIMESTAMP);
        String tableName = loadProperty(properties, KEY_TABLE_NAME);
        String columnNamesString = loadProperty(properties, KEY_COLUMN_NAMES);
        String modelClassName = loadProperty(properties, KEY_MODEL_CLASS_NAME);
        String modelClassVersionString = loadProperty(properties, KEY_MODEL_CLASS_VERSION);
        Calendar timestamp;
        try {
            Date date = new SimpleDateFormat(FORMAT_TIMESTAMP).parse(timestampString);
            timestamp = Calendar.getInstance();
            timestamp.setTime(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid timestamp \"{0}={2}\", must be \"{1}\"",
                    KEY_TIMESTAMP,
                    FORMAT_TIMESTAMP,
                    timestampString), e);
        }
        Set<String> columnNames = split(columnNamesString);
        long modelClassVersion;
        try {
            modelClassVersion = Long.parseLong(modelClassVersionString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid timestamp \"{0}={1}\", must be a valid signed long",
                    KEY_MODEL_CLASS_VERSION,
                    modelClassVersionString), e);
        }
        return new CacheInfo(
                featureVersion, id, timestamp,
                tableName, columnNames,
                modelClassName, modelClassVersion);
    }

    private static String loadProperty(Properties properties, String key) {
        assert properties != null;
        assert key != null;
        String property = properties.getProperty(key);
        if (property == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid cache info: {0} is missing",
                    key));
        }
        return property.trim();
    }

    /**
     * Stores this object into the target properties object.
     * @param properties target properties object
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #loadFrom(Properties)
     */
    public void storeTo(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        properties.setProperty(KEY_FEATURE_VERSION, featureVersion);
        properties.setProperty(KEY_ID, id);
        properties.setProperty(KEY_TIMESTAMP, new SimpleDateFormat(FORMAT_TIMESTAMP).format(timestamp.getTime()));
        properties.setProperty(KEY_TABLE_NAME, tableName);
        properties.setProperty(KEY_COLUMN_NAMES, join(columnNames));
        properties.setProperty(KEY_MODEL_CLASS_NAME, modelClassName);
        properties.setProperty(KEY_MODEL_CLASS_VERSION, String.valueOf(modelClassVersion));
    }

    private static Set<String> split(String packed) {
        assert packed != null;
        Set<String> results = new TreeSet<String>();
        String[] entries = packed.split(COLUMN_SEPARATOR);
        Collections.addAll(results, entries);
        return results;
    }

    private static String join(Collection<String> strings) {
        assert strings != null;
        StringBuilder buf = new StringBuilder();
        Iterator<String> iter = strings.iterator();
        assert iter.hasNext();
        buf.append(iter.next());
        while (iter.hasNext()) {
            buf.append(COLUMN_SEPARATOR);
            buf.append(iter.next());
        }
        return buf.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + columnNames.hashCode();
        result = prime * result + featureVersion.hashCode();
        result = prime * result + id.hashCode();
        result = prime * result + modelClassName.hashCode();
        result = prime * result + (int) (modelClassVersion ^ (modelClassVersion >>> 32));
        result = prime * result + tableName.hashCode();
        result = prime * result + timestamp.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CacheInfo other = (CacheInfo) obj;
        if (!columnNames.equals(other.columnNames)) {
            return false;
        }
        if (!featureVersion.equals(other.featureVersion)) {
            return false;
        }
        if (!id.equals(other.id)) {
            return false;
        }
        if (!modelClassName.equals(other.modelClassName)) {
            return false;
        }
        if (modelClassVersion != other.modelClassVersion) {
            return false;
        }
        if (!tableName.equals(other.tableName)) {
            return false;
        }
        if (!timestamp.equals(other.timestamp)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CacheInfo [featureVersion=");
        builder.append(featureVersion);
        builder.append(", id=");
        builder.append(id);
        builder.append(", timestamp=");
        builder.append(timestamp);
        builder.append(", tableName=");
        builder.append(tableName);
        builder.append(", columnNames=");
        builder.append(columnNames);
        builder.append(", modelClassName=");
        builder.append(modelClassName);
        builder.append(", modelClassVersion=");
        builder.append(modelClassVersion);
        builder.append("]");
        return builder.toString();
    }
}
