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

import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A data model definition for testing.
 * @param <T> type of data model
 * @since 0.2.0
 * @version 0.9.1
 */
public interface DataModelDefinition<T> {

    /**
     * Returns the corresponded data model class.
     * @return data model class
     */
    Class<T> getModelClass();

    /**
     * Returns this element's annotation for the specified annotation type.
     * @param <A> type of annotation
     * @param annotationType class of annotation
     * @return this element's annotation for the specified annotation type,
     *     or {@code null} if does not present
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    <A extends Annotation> A getAnnotation(Class<A> annotationType);

    /**
     * returns the all public property names.
     * @return property names
     */
    Collection<PropertyName> getProperties();

    /**
     * Returns the property type.
     * @param name the property name
     * @return property type, or {@code null} if no such property exists
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    PropertyType getType(PropertyName name);

    /**
     * Returns the property's annotation for the specified annotation type.
     * @param <A> type of annotation
     * @param name target property
     * @param annotationType class of annotation
     * @return this property's annotation for the specified annotation type,
     *     or {@code null} if neither the property nor its specified annotation do not present
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    <A extends Annotation> A getAnnotation(PropertyName name, Class<A> annotationType);

    /**
     * Starts to build a new {@link DataModelReflection}.
     * @return a builder object
     */
    DataModelDefinition.Builder<T> newReflection();

    /**
     * Converts a target object into a generic reflection model.
     * @param object target object
     * @return the converted model
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    DataModelReflection toReflection(T object);

    /**
     * Converts a generic reflection model into a target object.
     * @param reflection a reflection model
     * @return the converted object
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    T toObject(DataModelReflection reflection);

    /**
     * Converts the given raw property value into a corresponded property value for {@link DataModelReflection}.
     * @param value the raw property value
     * @return the resolved value
     * @since 0.9.1
     */
    default Object resolveRawValue(Object value) {
        return value;
    }

    /**
     * Builds a {@link DataModelReflection}.
     * @param <T> type of data model
     * @since 0.2.0
     */
    class Builder<T> {

        /**
         * The current definition object.
         */
        protected final DataModelDefinition<T> definition;

        /**
         * The current properties (includes {@code null} values).
         */
        protected final Map<PropertyName, Object> properties;

        /**
         * Creates a new instance.
         * @param definition the target model definition object
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public Builder(DataModelDefinition<T> definition) {
            if (definition == null) {
                throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
            }
            this.definition = definition;
            this.properties = new HashMap<>();
        }

        /**
         * Adds a property to the building object.
         * @param name the property name
         * @param value the property value (nullable)
         * @return this object (for method chain)
         * @throws IllegalArgumentException
         *     if the property is not defined (on {@code check = true}),
         *     or the property value has inconsistent type,
         *     or some parameters were {@code null}
         * @throws IllegalStateException the property has been already added
         */
        public Builder<T> add(PropertyName name, Object value) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
            }
            if (properties.containsKey(name)) {
                throw new IllegalStateException(MessageFormat.format(
                        Messages.getString("DataModelDefinition.errorConflictProperty"), //$NON-NLS-1$
                        name,
                        definition));
            }
            PropertyType type = definition.getType(name);
            if (type != null && value != null && type.getRepresentation().isInstance(value) == false) {
                throw new IllegalArgumentException(MessageFormat.format(
                        Messages.getString("DataModelDefinition.errorInconsistentPropertyValue"), //$NON-NLS-1$
                        name,
                        type,
                        value,
                        definition));
            }
            properties.put(name, value);
            return this;
        }

        /**
         * Builds a reflection object with since added properties.
         * @return the built
         */
        public DataModelReflection build() {
            return new DataModelReflection(properties);
        }
    }
}
