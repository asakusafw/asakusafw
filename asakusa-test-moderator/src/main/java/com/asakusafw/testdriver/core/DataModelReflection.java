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
package com.asakusafw.testdriver.core;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A data-model representation in record form.
 * @since 0.2.0
 */
public class DataModelReflection implements Serializable {

    private static final long serialVersionUID = -7083466307911956679L;

    /**
     * The actual properties in this data-model.
     */
    protected final Map<PropertyName, ?> properties;

    /**
     * Creates a new instance.
     * @param properties current property list
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DataModelReflection(Map<PropertyName, ?> properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        this.properties = normalize(properties);
    }

    private static Map<PropertyName, ?> normalize(Map<PropertyName, ?> properties) {
        assert properties != null;
        Map<PropertyName, Object> results = new LinkedHashMap<PropertyName, Object>();
        for (Map.Entry<PropertyName, ?> entry : properties.entrySet()) {
            if (entry.getKey() != null) {
                results.put(entry.getKey(), entry.getValue());
            }
        }
        return Collections.unmodifiableMap(results);
    }

    /**
     * Returns the property value.
     * This can returns one of the following type:
     * <ul>
     * <li> wrapper types of any primitive types, </li>
     * <li> {@link java.lang.String}, </li>
     * <li> {@link java.math.BigInteger}, </li>
     * <li> {@link java.math.BigDecimal}, </li>
     * <li> {@link java.util.Calendar}, </li>
     * <li> {@link DataModelReflection}, </li>
     * <li> or {@link java.lang.Object} (means "variant"). </li>
     * </ul>
     * Each value must be an instance of the corresponded
     * {@link DataModelDefinition#getType(PropertyName)} property type.
     * @param name the property name
     * @return property value, or {@code null} if the property was already {@code null} / not defined
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Object getValue(PropertyName name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        Object value = properties.get(name);
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + properties.hashCode();
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
        DataModelReflection other = (DataModelReflection) obj;
        if (!properties.equals(other.properties)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}",
                properties);
    }
}
