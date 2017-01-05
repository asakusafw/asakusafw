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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.operator.DataModelMirror;
import com.asakusafw.compiler.operator.OperatorCompilingEnvironment;

/**
 * Default implementation of a partial {@link DataModelMirror}.
 * @since 0.2.0
 */
final class PartialDataModelMirror implements DataModelMirror {

    private final OperatorCompilingEnvironment environment;

    final TypeVariable type;

    private Map<String, PropertyMirror> properties;

    /**
     * Creates a new instance.
     * @param environment the current compilation environment
     * @param type the target data model type
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    PartialDataModelMirror(OperatorCompilingEnvironment environment, TypeVariable type) {
        if (environment == null) {
            throw new IllegalArgumentException("environment must not be null"); //$NON-NLS-1$
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        this.environment = environment;
        this.type = type;
    }

    @Override
    public Kind getKind() {
        return Kind.PARTIAL;
    }

    @Override
    public boolean isSame(DataModelMirror other) {
        Precondition.checkMustNotBeNull(other, "other"); //$NON-NLS-1$
        if (other instanceof PartialDataModelMirror) {
            PartialDataModelMirror that = (PartialDataModelMirror) other;
            return environment.getTypeUtils().isSameType(this.type, that.type);
        }
        return false;
    }

    @Override
    public boolean canInvoke(DataModelMirror other) {
        Precondition.checkMustNotBeNull(other, "other"); //$NON-NLS-1$
        if (other instanceof ConcreteDataModelMirror) {
            ConcreteDataModelMirror that = (ConcreteDataModelMirror) other;
            return environment.getTypeUtils().isSubtype(this.type, that.type);
        }
        if (other instanceof PartialDataModelMirror) {
            PartialDataModelMirror that = (PartialDataModelMirror) other;
            return environment.getTypeUtils().isSubtype(this.type, that.type.getUpperBound());
        }
        return false;
    }

    @Override
    public boolean canContain(DataModelMirror other) {
        Precondition.checkMustNotBeNull(other, "other"); //$NON-NLS-1$
        if (other instanceof PartialDataModelMirror) {
            PartialDataModelMirror that = (PartialDataModelMirror) other;
            Types typeUtils = environment.getTypeUtils();
            return typeUtils.isSubtype(this.type, that.type.getUpperBound())
                && typeUtils.isSubtype(that.type.getLowerBound(), this.type);
        }
        return false;
    }

    @Override
    public PropertyMirror findProperty(String name) {
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        String normalized = Util.normalize(name);
        synchronized (this) {
            if (properties == null) {
                properties = buildProperties();
            }
            return properties.get(normalized);
        }
    }

    private Map<String, PropertyMirror> buildProperties() {
        Map<String, PropertyMirror> results = new LinkedHashMap<>();
        Elements elementUtils = environment.getElementUtils();
        for (TypeElement element : collectUpperBounds()) {
            for (ExecutableElement method : ElementFilter.methodsIn(elementUtils.getAllMembers(element))) {
                PropertyMirror property = Util.toProperty(method);
                if (property != null) {
                    results.put(property.getName(), property);
                }
            }
        }
        return results;
    }

    private List<TypeElement> collectUpperBounds() {
        LinkedList<TypeMirror> works = new LinkedList<>();
        works.add(type);
        Set<TypeMirror> saw = new HashSet<>();
        List<TypeElement> types = new ArrayList<>();
        int countDown = 100; // avoid infinite loop
        while (works.isEmpty() == false && --countDown >= 0) {
            TypeMirror target = works.removeFirst();
            if (saw.contains(target)) {
                continue;
            }
            saw.add(target);
            Element element = environment.getTypeUtils().asElement(target);
            if (element == null) {
                continue;
            }
            switch (element.getKind()) {
            case CLASS:
            case INTERFACE:
                types.add((TypeElement) element);
                break;
            case TYPE_PARAMETER:
                works.addAll(((TypeParameterElement) element).getBounds());
                break;
            default:
                // ignore other types
                continue;
            }
        }
        return types;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
