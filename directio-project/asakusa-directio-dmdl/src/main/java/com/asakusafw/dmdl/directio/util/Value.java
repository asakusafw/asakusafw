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
package com.asakusafw.dmdl.directio.util;

import java.util.Objects;
import java.util.function.Consumer;

import com.asakusafw.dmdl.model.AstAttributeElement;

/**
 * Represents a value.
 * @param <T> the value type
 * @since 0.9.1
 */
public final class Value<T> {

    private static final Value<?> UNDEFINED = new Value<>(null, null, false);

    private final AstAttributeElement declaration;

    private final T entity;

    private final boolean defined;

    private Value(AstAttributeElement declaration, T entity, boolean defined) {
        this.declaration = declaration;
        this.entity = entity;
        this.defined = defined;
    }

    /**
     * Returns an undefined value.
     * @param <T> the value type
     * @return undefined value
     */
    @SuppressWarnings("unchecked")
    public static <T> Value<T> undefined() {
        return (Value<T>) UNDEFINED;
    }

    /**
     * Returns a new value.
     * @param <T> the value type
     * @param declaration the original declaration
     * @param entity the actual value
     * @return the created value
     */
    public static <T> Value<T> of(AstAttributeElement declaration, T entity) {
        return new Value<>(declaration, entity, true);
    }

    /**
     * Returns this if defined, or the default value.
     * @param defaultValue the default value.
     * @return this if defined, otherwise the default value
     */
    public T orElse(T defaultValue) {
        return defined ? getEntity() : defaultValue;
    }

    /**
     * Returns this if defined, or the default value.
     * @param defaultValue the default value.
     * @return this if defined, otherwise the default value
     */
    public Value<T> orDefault(Value<T> defaultValue) {
        return defined ? this : defaultValue;
    }

    /**
     * Returns the declaration.
     * @return the declaration
     */
    public AstAttributeElement getDeclaration() {
        return declaration;
    }

    /**
     * Returns whether or not this value is explicitly defined.
     * @return {@code true} if it is defined
     */
    public boolean isDefined() {
        return defined;
    }

    /**
     * Returns whether or not this value is present.
     * @return {@code true} if it is present
     */
    public boolean isPresent() {
        return defined && entity != null;
    }

    /**
     * Accepts the entity only if this value is present.
     * @param action the action
     */
    public void ifPresent(Consumer<T> action) {
        if (isPresent()) {
            action.accept(entity);
        }
    }

    /**
     * Returns the entity.
     * @return the entity
     */
    public T getEntity() {
        return entity;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Boolean.hashCode(defined);
        result = prime * result + Objects.hashCode(entity);
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
        Value<?> other = (Value<?>) obj;
        if (defined != other.defined) {
            return false;
        }
        if (!Objects.equals(entity, other.entity)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return defined ? String.valueOf(entity) : "undefined"; //$NON-NLS-1$
    }
}
