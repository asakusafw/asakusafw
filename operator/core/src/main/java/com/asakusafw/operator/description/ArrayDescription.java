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
package com.asakusafw.operator.description;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents an array of values.
 */
public class ArrayDescription implements ValueDescription {

    private final ArrayTypeDescription arrayType;

    private final List<ValueDescription> elements;

    /**
     * Creates a new instance.
     * @param arrayType the array type
     * @param elements the array elements
     */
    public ArrayDescription(
            ArrayTypeDescription arrayType,
            List<? extends ValueDescription> elements) {
        this.arrayType = arrayType;
        this.elements = Collections.unmodifiableList(new ArrayList<>(elements));
    }

    /**
     * Creates a new instance.
     * @param elementType the element type
     * @param elements the array elements
     * @return the created instance
     */
    public static ArrayDescription elementsOf(
            ReifiableTypeDescription elementType,
            List<? extends ValueDescription> elements) {
        return new ArrayDescription(new ArrayTypeDescription(elementType), elements);
    }

    /**
     * Creates a new instance.
     * @param elementType the element type
     * @param elements the array elements
     * @return the created instance
     */
    public static ArrayDescription elementsOf(
            ReifiableTypeDescription elementType,
            ValueDescription... elements) {
        return elementsOf(elementType, Arrays.asList(elements));
    }

    /**
     * Creates a new instance.
     * @param array the array object
     * @return the created instance
     */
    public static ArrayDescription of(Object array) {
        Class<?> arrayType = array.getClass();
        if (arrayType.isArray() == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "must be an array: {0}", //$NON-NLS-1$
                    array));
        }
        List<ValueDescription> elements = new ArrayList<>();
        for (int i = 0, n = Array.getLength(array); i < n; i++) {
            ValueDescription element = Descriptions.valueOf(Array.get(array, i));
            elements.add(element);
        }
        return new ArrayDescription(ArrayTypeDescription.of(arrayType), elements);
    }

    @Override
    public ValueKind getValueKind() {
        return ValueKind.ARRAY;
    }

    @Override
    public ArrayTypeDescription getValueType() {
        return arrayType;
    }

    /**
     * Returns the array elements.
     * @return the array elements
     */
    public List<ValueDescription> getElements() {
        return elements;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + arrayType.hashCode();
        result = prime * result + elements.hashCode();
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
        ArrayDescription other = (ArrayDescription) obj;
        if (!arrayType.equals(other.arrayType)) {
            return false;
        }
        if (!elements.equals(other.elements)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Array(type={0}, size={1})", //$NON-NLS-1$
                arrayType,
                elements.size());
    }
}
