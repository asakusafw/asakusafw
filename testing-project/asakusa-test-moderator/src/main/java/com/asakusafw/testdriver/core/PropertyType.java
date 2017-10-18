/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
import java.util.Optional;

import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.DoubleOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Type variation for properties.
 * @since 0.2.0
 * @version 0.10.0
 */
public enum PropertyType {

    /**
     * {@link Boolean} type.
     */
    BOOLEAN(Boolean.class, BooleanOption.class),

    /**
     * {@link Byte} sized integer type.
     */
    BYTE(Byte.class, ByteOption.class),

    /**
     * {@link Short} sized integer type.
     */
    SHORT(Short.class, ShortOption.class),

    /**
     * {@link Integer} sized integer type.
     */
    INT(Integer.class, IntOption.class),

    /**
     * {@link Long} sized integer type.
     */
    LONG(Long.class, LongOption.class),

    /**
     * {@link BigInteger variable sized integer} type.
     */
    INTEGER(BigInteger.class),

    /**
     * {@link Float single precise} floating point number type.
     */
    FLOAT(Float.class, FloatOption.class),

    /**
     * {@link Float double precise} floating point number type.
     */
    DOUBLE(Double.class, DoubleOption.class),

    /**
     * {@link BigDecimal decimal number} type.
     */
    DECIMAL(BigDecimal.class, DecimalOption.class),

    /**
     * {@link String} type.
     */
    STRING(String.class, StringOption.class),

    /**
     * {@link Calendar date (yyyy/mm/dd)} type.
     */
    DATE(Calendar.class, DateOption.class),

    /**
     * {@link Calendar time (hh:mm:ss)} type.
     */
    TIME(Calendar.class),

    /**
     * {@link Calendar datetime (yyyy/mm/dd hh:mm:ss)} type.
     */
    DATETIME(Calendar.class, DateTimeOption.class),

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

    private final Class<? extends ValueOption<?>> implementation;

    PropertyType(Class<?> representation) {
        this(representation, null);
    }

    PropertyType(Class<?> representation, Class<? extends ValueOption<?>> implementation) {
        assert representation != null;
        this.representation = representation;
        this.implementation = implementation;
    }

    /**
     * Returns the representation of this type in Java.
     * @return the representation in Java
     */
    public Class<?> getRepresentation() {
        return representation;
    }

    /**
     * Returns the implementation type of this type.
     * @return the implementation type, or {@code empty} if it is not implemented
     * @since 0.10.0
     */
    public Optional<? extends Class<? extends ValueOption<?>>> getImplementation() {
        return Optional.ofNullable(implementation);
    }
}
