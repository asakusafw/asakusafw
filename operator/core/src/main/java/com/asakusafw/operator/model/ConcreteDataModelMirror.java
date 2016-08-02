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
package com.asakusafw.operator.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Types;

import com.asakusafw.operator.CompileEnvironment;

/**
 * Default implementation of a concrete {@link DataModelMirror}.
 */
public final class ConcreteDataModelMirror implements DataModelMirror {

    private final CompileEnvironment environment;

    private final DeclaredType type;

    private Map<String, PropertyMirror> properties;

    private final PropertyMirrorCollector collector;

    /**
     * Creates a new instance.
     * @param environment the current compilation environment
     * @param type the target data model type
     * @param collector the property collector
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ConcreteDataModelMirror(
            CompileEnvironment environment,
            DeclaredType type,
            PropertyMirrorCollector collector) {
        this.environment = Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        this.type = Objects.requireNonNull(type, "type must not be null"); //$NON-NLS-1$
        this.collector = Objects.requireNonNull(collector, "collector must not be null"); //$NON-NLS-1$
    }

    /**
     * Returns the type which this data model mirror represents.
     * @return the type
     */
    DeclaredType getType() {
        return type;
    }

    @Override
    public Kind getKind() {
        return Kind.CONCRETE;
    }

    @Override
    public boolean isSame(DataModelMirror other) {
        Objects.requireNonNull(other, "other must not be null"); //$NON-NLS-1$
        Types types = environment.getProcessingEnvironment().getTypeUtils();
        if (other instanceof ConcreteDataModelMirror) {
            ConcreteDataModelMirror that = (ConcreteDataModelMirror) other;
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
            return types.isSubtype(this.type, that.type);
        }
        if (other instanceof PartialDataModelMirror) {
            PartialDataModelMirror that = (PartialDataModelMirror) other;
            return types.isSubtype(this.type, that.getType().getUpperBound());
        }
        return false;
    }

    @Override
    public boolean canContain(DataModelMirror other) {
        Objects.requireNonNull(other, "other must not be null"); //$NON-NLS-1$
        Types types = environment.getProcessingEnvironment().getTypeUtils();
        if (other instanceof ConcreteDataModelMirror) {
            ConcreteDataModelMirror that = (ConcreteDataModelMirror) other;
            return types.isSameType(this.type, that.type);
        }
        if (other instanceof PartialDataModelMirror) {
            PartialDataModelMirror that = (PartialDataModelMirror) other;
            return types.isSubtype(this.type, that.getType().getUpperBound())
                && types.isSubtype(that.getType().getLowerBound(), this.type);
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
            TypeElement element = (TypeElement) getType().asElement();
            Set<PropertyMirror> propertySet = collector.collect(
                    environment, type, Collections.singletonList(element));
            this.properties = new HashMap<>();
            for (PropertyMirror property : propertySet) {
                properties.put(JavaName.of(property.getName()).toMemberName(), property);
            }
        }
        return properties;
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
