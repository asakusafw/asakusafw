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
package com.asakusafw.vocabulary.external;

/**
 * An abstract super interface for describing processing details of importers (loading data from external components).
 * Each sub-class must satisfy the following rules:
 * <ul>
 * <li> declared as {@code public} </li>
 * <li> NOT declared as {@code abstract} </li>
 * <li> without any type parameter declarations </li>
 * <li> with a public zero-parameter constructor (or no explicit constructors) </li>
 * </ul>
 */
public interface ImporterDescription {

    /**
     * Returns the data model class of importing data.
     * @return the data model class of importing data
     */
    Class<?> getModelType();

    /**
     * Returns the estimated data size.
     * @return the estimated data size
     */
    default DataSize getDataSize() {
        return DataSize.UNKNOWN;
    }

    /**
     * Represents a kind of estimated input data size.
     */
    enum DataSize {

        /**
         * Unknown or not estimated.
         */
        UNKNOWN,

        /**
         * Tiny data (~10MB in uncompressed form).
         */
        TINY,

        /**
         * Small data (10~200MB in uncompressed form).
         */
        SMALL,

        /**
         * Large data (200MB~ in uncompressed form).
         */
        LARGE,
    }
}
