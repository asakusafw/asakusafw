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
package com.asakusafw.testdriver.core;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

import com.asakusafw.testdriver.model.SimpleDataModelDefinition;

/**
 * {@link DataModelDefinition} which only holds single value.
 * @param <T> type of holding value
 * @since 0.2.0
 */
public class ValueDefinition<T> implements DataModelDefinition<T> {

    /**
     * Self name.
     */
    public static final PropertyName VALUE = PropertyName.newInstance("value");

    private final Class<T> type;

    private final PropertyType kind;

    /**
     * Creates a new instance.
     * @param <T> type of holding value
     * @param type holding value type
     * @return the created instance
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <T> ValueDefinition<T> of(Class<T> type) {
        return new ValueDefinition<>(type);
    }

    /**
     * Creates a new instance.
     * @param type holding value type
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ValueDefinition(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        this.type = type;
        this.kind = SimpleDataModelDefinition.getType(VALUE, type);
        if (kind == null) {
            throw new IllegalArgumentException(type.getName());
        }
    }

    @Override
    public Class<T> getModelClass() {
        return type;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return type.getAnnotation(annotationType);
    }

    @Override
    public Collection<PropertyName> getProperties() {
        return Collections.singleton(VALUE);
    }

    @Override
    public PropertyType getType(PropertyName name) {
        if (VALUE.equals(name)) {
            return kind;
        }
        return null;
    }

    @Override
    public <A extends Annotation> A getAnnotation(PropertyName name, Class<A> annotationType) {
        return null;
    }

    @Override
    public Builder<T> newReflection() {
        return new Builder<>(this);
    }

    @Override
    public DataModelReflection toReflection(T object) {
        return newReflection().add(VALUE, object).build();
    }

    @Override
    public T toObject(DataModelReflection reflection) {
        return type.cast(reflection.getValue(VALUE));
    }
}
