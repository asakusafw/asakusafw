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
package com.asakusafw.directio.hive.serde;


/**
 * Configuration for {@link DataModelDriver}.
 * @since 0.7.0
 */
public final class DataModelMapping {

    private FieldMappingStrategy fieldMappingStrategy = FieldMappingStrategy.NAME;

    private ExceptionHandlingStrategy onMissingSource = ExceptionHandlingStrategy.LOGGING;

    private ExceptionHandlingStrategy onMissingTarget = ExceptionHandlingStrategy.LOGGING;

    private ExceptionHandlingStrategy onIncompatibleType = ExceptionHandlingStrategy.LOGGING;

    /**
     * Returns the field mapping strategy.
     * @return the field mapping strategy
     */
    public FieldMappingStrategy getFieldMappingStrategy() {
        return fieldMappingStrategy;
    }

    /**
     * Sets the field mapping strategy.
     * @param value the strategy
     */
    public void setFieldMappingStrategy(FieldMappingStrategy value) {
        this.fieldMappingStrategy = value;
    }

    /**
     * Returns the exception handling strategy for missing source fields.
     * @return the exception handling strategy
     */
    public ExceptionHandlingStrategy getOnMissingSource() {
        return onMissingSource;
    }

    /**
     * Sets the exception handling strategy for missing source fields.
     * @param value the strategy
     */
    public void setOnMissingSource(ExceptionHandlingStrategy value) {
        this.onMissingSource = value;
    }

    /**
     * Returns the exception handling strategy for missing target fields.
     * @return the exception handling strategy
     */
    public ExceptionHandlingStrategy getOnMissingTarget() {
        return onMissingTarget;
    }

    /**
     * Sets the exception handling strategy for missing target fields.
     * @param value the strategy
     */
    public void setOnMissingTarget(ExceptionHandlingStrategy value) {
        this.onMissingTarget = value;
    }

    /**
     * Returns the exception handling strategy for incompatible field type.
     * @return the exception handling strategy
     */
    public ExceptionHandlingStrategy getOnIncompatibleType() {
        return onIncompatibleType;
    }

    /**
     * Sets the exception handling strategy for incompatible field type.
     * @param value the strategy
     */
    public void setOnIncompatibleType(ExceptionHandlingStrategy value) {
        this.onIncompatibleType = value;
    }


    /**
     * Mapping strategy between source field and target field.
     * @since 0.7.0
     */
    public enum FieldMappingStrategy {

        /**
         * Mapping fields by their name.
         */
        NAME,

        /**
         * Mapping fields by their position.
         */
        POSITION,
    }

    /**
     * Exception handling strategy.
     * @since 0.7.0
     */
    public enum ExceptionHandlingStrategy {

        /**
         * Ignores on exception.
         */
        IGNORE,

        /**
         * Logging on exception.
         */
        LOGGING,

        /**
         * Raise {@link IllegalArgumentException} on exception.
         */
        FAIL,
    }
}