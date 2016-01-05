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
package com.asakusafw.vocabulary.external;

/**
 * An abstract super interface for describing processing details of <em>export operations</em>
 * (storing data to external components). Each sub-class must satisfy the following rules:
 * <ul>
 * <li> declared as {@code public} </li>
 * <li> NOT declared as {@code abstract} </li>
 * <li> without any type parameter declarations </li>
 * <li> with a public zero-parameter constructor (or no explicit constructors) </li>
 * </ul>
 */
public interface ExporterDescription {

    /**
     * Returns the data model class of exporting data.
     * @return the data model class of exporting data
     */
    Class<?> getModelType();
}
