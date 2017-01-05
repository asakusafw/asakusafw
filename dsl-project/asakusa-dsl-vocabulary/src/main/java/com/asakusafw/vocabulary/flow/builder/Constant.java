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
package com.asakusafw.vocabulary.flow.builder;

import java.lang.reflect.Type;

/**
 * Represents a constant.
 * @since 0.9.0
 */
public class Constant implements Data {

    private final Type type;

    private final Object value;

    /**
     * Creates a new instance.
     * @param type the value type
     * @param value the constant value, or {@code null} if the constant represents {@code null} value
     */
    public Constant(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Returns an instance which represents the specified value.
     * @param value the constant value, or {@code null} if the constant represents {@code null} value
     * @return the created instance
     */
    public static Constant of(Object value) {
        Type type;
        if (value == null) {
            type = Object.class;
        } else {
            type = value.getClass();
        }
        return new Constant(type, value);
    }

    @Override
    public Kind getKind() {
        return Kind.CONSTANT;
    }

    /**
     * Returns the type.
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the constant value.
     * @return the value
     */
    public Object getValue() {
        return value;
    }
}
