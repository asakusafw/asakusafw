/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.text.MessageFormat;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.io.util.WritableRawComparableUnion;

/**
 * A spec for each direct output stage.
 * @since 0.2.5
 * @version 0.8.1
 */
public class DirectOutputSpec {

    private final Class<? extends Writable> valueType;

    private final String outputId;

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
        this(valueType, null, path, formatClass, namingClass, orderClass);
    }

    /**
     * Creates a new instance.
     * @param valueType  data type
     * @param outputId the output ID (nullable)
     * @param path the path
     * @param formatClass format class
     * @param namingClass naming class
     * @param orderClass ordering class
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.8.1
     */
    public DirectOutputSpec(
            Class<? extends Writable> valueType,
            String outputId,
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
        this.outputId = outputId;
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
        return new DirectOutputGroup(valueType, outputId, path, format, nameGenerator);
    }

    private DirectOutputOrder createOrder() {
        DirectOutputOrder order = ReflectionUtils.newInstance(orderClass, null);
        return order;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "DirectOutputSpec(valueType={0}, id={1}, path={2}, format={3}, naming={4}, order={5})", //$NON-NLS-1$
                valueType.getName(),
                outputId,
                path,
                formatClass.getName(),
                namingClass.getName(),
                orderClass.getName());
    }
}
