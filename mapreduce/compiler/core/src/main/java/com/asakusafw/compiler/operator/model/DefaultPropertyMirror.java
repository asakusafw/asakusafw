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
package com.asakusafw.compiler.operator.model;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.DataModelMirror;

/**
 * Default implementation of a property mirror of {@link DataModelMirror}.
 * @since 0.2.0
 */
public class DefaultPropertyMirror implements DataModelMirror.PropertyMirror {

    private final String name;

    private final ExecutableElement element;

    /**
     * Creates a new instance.
     * @param name the name of the target property
     * @param element the type of the target property
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DefaultPropertyMirror(String name, ExecutableElement element) {
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
        this.name = name;
        this.element = element;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public TypeMirror getType() {
        return element.getReturnType();
    }
}
