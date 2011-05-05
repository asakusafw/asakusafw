/**
 * Copyright 2011 Asakusa Framework Team.
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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * A data model definition for testing.
 * @param <T> type of data model
 * @since 0.2.0
 */
public interface DataModelDefinition<T> {

    /**
     * Returns the corresponded data model class.
     * @return data model class
     */
    Class<T> getModelClass();

    /**
     * Returns the property type.
     * This can returns one of the following type:
     * <ul>
     * <li> wrapper types of any primitive types, </li>
     * <li> {@link java.lang.String}, </li>
     * <li> {@link java.math.BigInteger}, </li>
     * <li> {@link java.math.BigDecimal}, </li>
     * <li> {@link java.util.Calendar}, </li>
     * <li> {@link DataModelDefinition}, </li>
     * <li> or {@link java.lang.Object} (means "variant"). </li>
     * </ul>
     * @param name the property name
     * @return property type, or {@code null} if no such property exists
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    Class<?> getType(PropertyName name);

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
     * Builds a {@link DataModelReflection}.
     * @param <T> type of data model
     * @since 0.2.0
     */
    public class Builder<T> {

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
            this.properties = new HashMap<PropertyName, Object>();
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
                        "The property \"{0}\" was already set in {1}",
                        name,
                        definition));
            }
            Class<?> type = definition.getType(name);
            if (type != null && value != null && type.isInstance(value) == false) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "The property \"{0}\" must be type of {1}, but was {2} ({3})",
                        name,
                        type.getName(),
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
