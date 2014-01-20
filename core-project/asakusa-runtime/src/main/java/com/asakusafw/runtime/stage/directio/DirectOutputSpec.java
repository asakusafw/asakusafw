/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.directio;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.io.util.WritableRawComparableUnion;

/**
 * A spec for each direct output stage.
 * @since 0.2.5
 */
public class DirectOutputSpec {

    private final Class<? extends Writable> valueType;

    private final String path;

    private final Class<? extends DataFormat<?>> formatClass;

    private final Class<? extends StringTemplate> namingClass;

    private final Class<? extends DirectOutputOrder> orderClass;

    /**
     * Creates a new instance.
     * @param valueType  data type
     * @param path the path
     * @param formatClass format class
     * @param namingClass naming class
     * @param orderClass ordering class
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DirectOutputSpec(
            Class<? extends Writable> valueType,
            String path,
            Class<? extends DataFormat<?>> formatClass,
            Class<? extends StringTemplate> namingClass,
            Class<? extends DirectOutputOrder> orderClass) {
        if (valueType == null) {
            throw new IllegalArgumentException("valueType must not be null"); //$NON-NLS-1$
        }
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        if (formatClass == null) {
            throw new IllegalArgumentException("formatClass must not be null"); //$NON-NLS-1$
        }
        if (namingClass == null) {
            throw new IllegalArgumentException("namingClass must not be null"); //$NON-NLS-1$
        }
        if (orderClass == null) {
            throw new IllegalArgumentException("orderClass must not be null"); //$NON-NLS-1$
        }
        this.valueType = valueType;
        this.path = path;
        this.formatClass = formatClass;
        this.namingClass = namingClass;
        this.orderClass = orderClass;
    }

    static Class<?>[] getValueTypes(DirectOutputSpec[] specs) {
        if (specs == null) {
            throw new IllegalArgumentException("specs must not be null"); //$NON-NLS-1$
        }
        Class<?>[] results = new Class<?>[specs.length];
        for (int i = 0; i < specs.length; i++) {
            DirectOutputSpec spec = specs[i];
            if (spec == null) {
                results[i] = DirectOutputGroup.EMPTY.getDataType();
            } else {
                results[i] = specs[i].valueType;
            }
        }
        return results;
    }

    static WritableRawComparableUnion createGroupUnion(DirectOutputSpec[] specs) {
        if (specs == null) {
            throw new IllegalArgumentException("specs must not be null"); //$NON-NLS-1$
        }
        DirectOutputGroup[] elements = new DirectOutputGroup[specs.length];
        for (int i = 0; i < specs.length; i++) {
            DirectOutputSpec spec = specs[i];
            if (spec == null) {
                elements[i] = DirectOutputGroup.EMPTY;
            } else {
                elements[i] = spec.createGroup();
            }
        }
        return new WritableRawComparableUnion(elements);
    }

    static WritableRawComparableUnion createOrderUnion(DirectOutputSpec[] specs) {
        if (specs == null) {
            throw new IllegalArgumentException("specs must not be null"); //$NON-NLS-1$
        }
        DirectOutputOrder[] elements = new DirectOutputOrder[specs.length];
        for (int i = 0; i < specs.length; i++) {
            DirectOutputSpec spec = specs[i];
            if (spec == null) {
                elements[i] = DirectOutputOrder.EMPTY;
            } else {
                elements[i] = spec.createOrder();
            }
        }
        return new WritableRawComparableUnion(elements);
    }

    private DirectOutputGroup createGroup() {
        DataFormat<?> format = ReflectionUtils.newInstance(formatClass, null);
        StringTemplate nameGenerator = ReflectionUtils.newInstance(namingClass, null);
        return new DirectOutputGroup(path, valueType, format, nameGenerator);
    }

    private DirectOutputOrder createOrder() {
        DirectOutputOrder order = ReflectionUtils.newInstance(orderClass, null);
        return order;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DirectOutputSpec [valueType=");
        builder.append(valueType);
        builder.append(", path=");
        builder.append(path);
        builder.append(", formatClass=");
        builder.append(formatClass);
        builder.append(", namingClass=");
        builder.append(namingClass);
        builder.append(", orderClass=");
        builder.append(orderClass);
        builder.append("]");
        return builder.toString();
    }
}
