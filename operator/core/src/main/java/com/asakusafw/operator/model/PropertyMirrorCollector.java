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
package com.asakusafw.operator.model;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import com.asakusafw.operator.CompileEnvironment;

/**
 * Collects {@link PropertyMirror}.
 * @see ConcreteDataModelMirror
 * @see PartialDataModelMirror
 */
public interface PropertyMirrorCollector {

    /**
     * Creates and returns the set of properties.
     * @param environment current environment
     * @param targetType target property owner type
     * @param upperBounds upper-bound types of owner type
     * @return the collected properties mirrors
     */
    Set<PropertyMirror> collect(
            CompileEnvironment environment,
            TypeMirror targetType,
            List<TypeElement> upperBounds);
}
