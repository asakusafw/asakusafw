/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.asakusafw.runtime.value.ValueOption;

/**
 * Describes an Asakusa data model.
 * @since 0.7.0
 */
public class DataModelDescriptor {

    private final Class<?> dataModelClass;

    private final String comment;

    private final List<? extends PropertyDescriptor> propertyDescriptors;

    private final Map<String, PropertyDescriptor> names;

    /**
     * Creates a new instance.
     * @param dataModelClass the data model class
     * @param properties the data model property descriptors
     */
    public DataModelDescriptor(Class<?> dataModelClass, List<? extends PropertyDescriptor> properties) {
        this(dataModelClass, null, properties);
    }

    /**
     * Creates a new instance.
     * @param dataModelClass the data model class
     * @param comment comment for the data model
     * @param properties the data model property descriptors
     */
    public DataModelDescriptor(
            Class<?> dataModelClass,
            String comment,
            List<? extends PropertyDescriptor> properties) {
        this.dataModelClass = dataModelClass;
        this.comment = comment;
        this.propertyDescriptors = properties;
        this.names = new HashMap<>();
        for (PropertyDescriptor property : properties) {
            PropertyDescriptor p = property;
            if (p.getFieldID() == PropertyDescriptor.INVALID_FIELD_ID) {
                p.setFieldId(names.size());
            } else {
                p = new PropertyDescriptor(p.getFieldName(), p, p.getFieldComment()) {
                    @Override
                    public ValueOption<?> extract(Object dataModel) {
                        return property.extract(dataModel);
                    }
                };
                p.setFieldId(names.size());
            }
            names.put(normalizeName(p.getFieldName()), p);
        }
    }

    /**
     * Creates a new instance of the target data model.
     * @return the created instance
     */
    public Object createDataModelObject() {
        try {
            return dataModelClass.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the target data model class.
     * @return the data model class
     */
    public Class<?> getDataModelClass() {
        return dataModelClass;
    }

    /**
     * Returns comment for this data model.
     * @return comment, or {@code null} if it is not set
     */
    public String getDataModelComment() {
        return comment;
    }

    /**
     * Returns a property descriptor.
     * @param name the target property name
     * @return the property descriptor, or {@code null} if there is no such a property
     */
    public PropertyDescriptor findPropertyDescriptor(String name) {
        return names.get(normalizeName(name));
    }

    /**
     * Returns the property descriptors in the target data model.
     * @return the property descriptors
     */
    public List<? extends PropertyDescriptor> getPropertyDescriptors() {
        return propertyDescriptors;
    }

    private String normalizeName(String name) {
        return name.toUpperCase(Locale.ENGLISH);
    }

    @Override
    public String toString() {
        String name = getDataModelClass().getName();
        String c = getDataModelComment();
        if (c == null) {
            return name;
        } else {
            return MessageFormat.format("{0} ({1})", getDataModelClass().getName(), c); //$NON-NLS-1$
        }
    }
}
