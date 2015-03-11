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
package com.asakusafw.testdriver.core;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;

/**
 * A data-model representation in record form.
 * @since 0.2.0
 * @version 0.5.1
 */
public class DataModelReflection implements Serializable {

    private static final char[] ASCII_SPECIAL_ESCAPE = new char[0x80];
    static {
        ASCII_SPECIAL_ESCAPE['"'] = '"';
        ASCII_SPECIAL_ESCAPE['\b'] = 'b';
        ASCII_SPECIAL_ESCAPE['\t'] = 't';
        ASCII_SPECIAL_ESCAPE['\n'] = 'n';
        ASCII_SPECIAL_ESCAPE['\f'] = 'f';
        ASCII_SPECIAL_ESCAPE['\r'] = 'r';
        ASCII_SPECIAL_ESCAPE['\\'] = '\\';
    }

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
        StringBuilder buf = new StringBuilder();
        buf.append('{');
        if (properties.isEmpty() == false) {
            for (Map.Entry<PropertyName, ?> entry : properties.entrySet()) {
                buf.append(entry.getKey());
                buf.append('=');
                buf.append(toStringRepresentation(entry.getValue()));
                buf.append(',');
                buf.append(' ');
            }
            buf.delete(buf.length() - 2, buf.length());
        }
        buf.append('}');
        return buf.toString();
    }

    /**
     * Returns formatted string representation of values.
     * @param value original value
     * @return formatted string representation
     * @since 0.5.1
     */
    public static String toStringRepresentation(Object value) {
        if (value instanceof String) {
            return toStringLiteral((String) value);
        } else if (value instanceof Calendar) {
            Calendar c = (Calendar) value;
            if (c.isSet(Calendar.HOUR_OF_DAY)) {
                return new SimpleDateFormat(DateTime.FORMAT).format(c.getTime());
            } else {
                return new SimpleDateFormat(Date.FORMAT).format(c.getTime());
            }
        } else if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        }
        return String.valueOf(value);
    }

    private static String toStringLiteral(String value) {
        assert value != null;
        StringBuilder buf = new StringBuilder();
        buf.append('"');
        for (char c : value.toCharArray()) {
            if (c <= 0x7f && ASCII_SPECIAL_ESCAPE[c] != 0) {
                buf.append('\\');
                buf.append(ASCII_SPECIAL_ESCAPE[c]);
            } else if (Character.isISOControl(c) || !Character.isDefined(c)) {
                buf.append(String.format("\\u%04x", (int) c)); //$NON-NLS-1$
            } else {
                buf.append(c);
            }
        }
        buf.append('"');
        return buf.toString();
    }
}
