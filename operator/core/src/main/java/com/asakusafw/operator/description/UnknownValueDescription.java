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
package com.asakusafw.operator.description;

import java.text.MessageFormat;

/**
 * Represents an unknown object.
 */
public class UnknownValueDescription implements ValueDescription {

    private final TypeDescription valueType;

    private final String label;

    /**
     * Creates a new instance.
     * @param valueType the original value type
     * @param label a value label
     */
    public UnknownValueDescription(TypeDescription valueType, String label) {
        this.valueType = valueType;
        this.label = label;
    }

    /**
     * Creates a new instance.
     * @param value the value
     * @return the created instance
     */
    public static UnknownValueDescription of(Object value) {
        Class<?> type = value.getClass();
        return new UnknownValueDescription(ReifiableTypeDescription.of(type), String.valueOf(value));
    }

    @Override
    public ValueKind getValueKind() {
        return ValueKind.UNKNOWN;
    }

    @Override
    public TypeDescription getValueType() {
        return valueType;
    }

    /**
     * Returns the label for this description.
     * @return the label for this description
     */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Unknown(type={0}, label={1})", //$NON-NLS-1$
                valueType,
                label);
    }
}
