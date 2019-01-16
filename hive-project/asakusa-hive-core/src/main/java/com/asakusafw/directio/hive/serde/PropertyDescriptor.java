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
package com.asakusafw.directio.hive.serde;

import java.text.MessageFormat;

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;

import com.asakusafw.directio.hive.util.SchemaUtil;
import com.asakusafw.info.hive.ColumnInfo;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Represents a property in data model object.
 * @since 0.7.0
 * @version 0.7.2
 */
public abstract class PropertyDescriptor implements StructField, ValueSerde, PropertyExtractor {

    static final int INVALID_FIELD_ID = -1;

    private final String name;

    private final ValueSerde serde;

    private final String comment;

    private int fieldId = INVALID_FIELD_ID;

    /**
     * Creates a new instance.
     * @param name the property name
     * @param serde the property accessor
     */
    public PropertyDescriptor(String name, ValueSerde serde) {
        this(name, serde, null);
    }

    /**
     * Creates a new instance.
     * @param name the property name
     * @param serde the property accessor
     * @param comment comment for the property (nullable)
     */
    public PropertyDescriptor(String name, ValueSerde serde, String comment) {
        this.name = name;
        this.serde = serde;
        this.comment = comment;
    }

    @Override
    public abstract ValueOption<?> extract(Object dataModel);

    @Override
    public TypeInfo getTypeInfo() {
        return serde.getTypeInfo();
    }

    @Override
    public Class<? extends ValueOption<?>> getValueClass() {
        return serde.getValueClass();
    }

    @Override
    public ObjectInspector getInspector() {
        return serde.getInspector();
    }

    @Override
    public ValueDriver getDriver(ObjectInspector target) {
        return serde.getDriver(target);
    }

    @Override
    public String getFieldName() {
        return name;
    }

    @Override
    public ObjectInspector getFieldObjectInspector() {
        return getInspector();
    }

    @Override
    public String getFieldComment() {
        return comment;
    }

    @Override
    public int getFieldID() {
        return fieldId;
    }

    /**
     * Sets the field ID.
     * @param id the ID
     * @since 0.7.2
     */
    void setFieldId(int id) {
        this.fieldId = id;
    }

    /**
     * Returns the {@link ColumnInfo} of this property.
     * @return the {@link ColumnInfo}
     * @since 0.8.1
     */
    public ColumnInfo getSchema() {
        return new ColumnInfo(getFieldName(), SchemaUtil.toPortable(getTypeInfo()), getFieldComment());
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}:{1}", //$NON-NLS-1$
                getFieldName(),
                getFieldObjectInspector().getTypeName());
    }
}
