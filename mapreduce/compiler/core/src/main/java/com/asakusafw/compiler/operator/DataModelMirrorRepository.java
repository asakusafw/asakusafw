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
package com.asakusafw.compiler.operator;

import javax.lang.model.type.TypeMirror;

/**
 * Repository of {@link DataModelMirror}.
 * <p>
 * Adding a new data model kinds to Operator DSL compiler, clients can implement this
 * and put the class name in
 * {@code META-INF/services/com.asakusafw.compiler.operator.DataModelMirrorRepository}.
 * </p>
 * @since 0.2.0
 */
public interface DataModelMirrorRepository {

    /**
     * Loads a mirror of the data model corresponded to the specified type.
     * @param environment the compiling environment
     * @param type the corresponded type
     * @return the loaded data model mirror,
     *     or {@code null} if the type does not represent a valid data model for this repository
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    DataModelMirror load(OperatorCompilingEnvironment environment, TypeMirror type);
}