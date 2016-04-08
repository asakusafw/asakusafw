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
package com.asakusafw.testdriver.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

/**
 * Type variation for properties.
 * @since 0.2.0
 */
public enum PropertyType {

    /**
     * {@link Boolean} type.
     */
    BOOLEAN(Boolean.class),

    /**
     * {@link Byte} sized integr type.
     */
    BYTE(Byte.class),

    /**
     * {@link Short} sized integer type.
     */
    SHORT(Short.class),

    /**
     * {@link Integer} sized integer type.
     */
    INT(Integer.class),

    /**
     * {@link Long} sized integer type.
     */
    LONG(Long.class),

    /**
     * {@link BigInteger variable sized integer} type.
     */
    INTEGER(BigInteger.class),

    /**
     * {@link Float single precised} floating point number type.
     */
    FLOAT(Float.class),

    /**
     * {@link Float double precised} floating point number type.
     */
    DOUBLE(Double.class),

    /**
     * {@link BigDecimal decimal number} type.
     */
    DECIMAL(BigDecimal.class),

    /**
     * {@link String} type.
     */
    STRING(String.class),

    /**
     * {@link Calendar date (yyyy/mm/dd)} type.
     */
    DATE(Calendar.class),

    /**
     * {@link Calendar time (hh:mm:ss)} type.
     */
    TIME(Calendar.class),

    /**
     * {@link Calendar datetime (yyyy/mm/dd hh:mm:ss)} type.
     */
    DATETIME(Calendar.class),

    /**
     * {@link Sequence} type.
     */
    SEQUENCE(Sequence.class),

    /**
     * {@link DataModelReflection other data model object} type.
     */
    OBJECT(DataModelReflection.class),

    ;
    private final Class<?> representation;

    PropertyType(Class<?> representation) {
        assert representation != null;
        this.representation = representation;
    }

    /**
     * Returns the representation of this type in Java.
     * @return the representation in Java
     */
    public Class<?> getRepresentation() {
        return representation;
    }
}
