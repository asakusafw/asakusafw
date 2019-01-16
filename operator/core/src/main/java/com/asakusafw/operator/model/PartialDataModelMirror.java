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
package com.asakusafw.operator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Types;

import com.asakusafw.operator.CompileEnvironment;

/**
 * Default implementation of a partial {@link DataModelMirror}.
 */
public final class PartialDataModelMirror implements DataModelMirror {

    private final CompileEnvironment environment;

    private final TypeVariable type;

    private Map<String, PropertyMirror> properties;

    private final PropertyMirrorCollector collector;

    /**
     * Creates a new instance.
     * @param environment the current compilation environment
     * @param type the target data model type
     * @param collector the property collector
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public PartialDataModelMirror(
            CompileEnvironment environment,
            TypeVariable type,
            PropertyMirrorCollector collector) {
        this.environment = Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        this.type = Objects.requireNonNull(type, "type must not be null"); //$NON-NLS-1$
        this.collector = Objects.requireNonNull(collector, "collector must not be null"); //$NON-NLS-1$
    }

    /**
     * Returns the type which this data model mirror represents.
     * @return the type
     */
    TypeVariable getType() {
        return type;
    }

    @Override
    public String getSimpleName() {
        return String.valueOf(type.asElement().getSimpleName());
    }

    @Override
    public Kind getKind() {
        return Kind.PARTIAL;
    }

    @Override
    public boolean isSame(DataModelMirror other) {
        Objects.requireNonNull(other, "other must not be null"); //$NON-NLS-1$
        Types types = environment.getProcessingEnvironment().getTypeUtils();
        if (other instanceof PartialDataModelMirror) {
            PartialDataModelMirror that = (PartialDataModelMirror) other;
            return types.isSameType(this.type, that.type);
        }
        return false;
    }

    @Override
    public boolean canInvoke(DataModelMirror other) {
        Objects.requireNonNull(other, "other must not be null"); //$NON-NLS-1$
        Types types = environment.getProcessingEnvironment().getTypeUtils();
        if (other instanceof ConcreteDataModelMirror) {
            ConcreteDataModelMirror that = (ConcreteDataModelMirror) other;
            return types.isSubtype(this.type, that.getType());
        }
        if (other instanceof PartialDataModelMirror) {
            PartialDataModelMirror that = (PartialDataModelMirror) other;
            return types.isSubtype(this.type, that.type.getUpperBound());
        }
        return false;
    }

    @Override
    public boolean canContain(DataModelMirror other) {
        Objects.requireNonNull(other, "other must not be null"); //$NON-NLS-1$
        Types types = environment.getProcessingEnvironment().getTypeUtils();
        if (other instanceof PartialDataModelMirror) {
            PartialDataModelMirror that = (PartialDataModelMirror) other;
            return types.isSubtype(this.type, that.type.getUpperBound())
                && types.isSubtype(that.type.getLowerBound(), this.type);
        }
        return false;
    }

    @Override
    public PropertyMirror findProperty(String name) {
        Objects.requireNonNull(name, "name must not be null"); //$NON-NLS-1$
        return prepareProperties().get(JavaName.of(name).toMemberName());
    }

    private synchronized Map<String, PropertyMirror> prepareProperties() {
        if (properties == null) {
            List<TypeElement> upperBounds = collectUpperBounds();
            Set<PropertyMirror> propertySet = collector.collect(environment, type, upperBounds);
            this.properties = new HashMap<>();
            for (PropertyMirror property : propertySet) {
                properties.put(JavaName.of(property.getName()).toMemberName(), property);
            }
        }
        return properties;
    }

    private List<TypeElement> collectUpperBounds() {
        LinkedList<TypeMirror> works = new LinkedList<>();
        works.add(type);
        Set<TypeMirror> saw = new HashSet<>();
        Types types = environment.getProcessingEnvironment().getTypeUtils();
        List<TypeElement> results = new ArrayList<>();
        int countDown = 100; // avoid infinite loop
        while (works.isEmpty() == false && --countDown >= 0) {
            TypeMirror target = works.removeFirst();
            if (saw.contains(target)) {
                continue;
            }
            saw.add(target);
            Element element = types.asElement(target);
            if (element == null) {
                continue;
            }
            switch (element.getKind()) {
            case CLASS:
            case INTERFACE:
                results.add((TypeElement) element);
                break;
            case TYPE_PARAMETER:
                works.addAll(((TypeParameterElement) element).getBounds());
                break;
            default:
                // ignore other types
                continue;
            }
        }
        return results;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
