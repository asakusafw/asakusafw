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
package com.asakusafw.operator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;

import com.asakusafw.operator.model.ConcreteDataModelMirror;
import com.asakusafw.operator.model.DataModelMirror;
import com.asakusafw.operator.model.PartialDataModelMirror;
import com.asakusafw.operator.model.PropertyMirror;
import com.asakusafw.operator.model.PropertyMirrorCollector;

/**
 * Mock {@link DataModelMirrorRepository}.
 */
public class MockDataModelMirrorRepository implements DataModelMirrorRepository, PropertyMirrorCollector {

    private final Set<String> targets = new HashSet<>();

    /**
     * Adds supported classes.
     * @param classes supported classes
     * @return this
     */
    public MockDataModelMirrorRepository add(Class<?>... classes) {
        for (Class<?> aClass : classes) {
            targets.add(aClass.getName());
        }
        return this;
    }

    /**
     * Add supported class names.
     * @param packageName package name
     * @param typeNames type names
     * @return this
     */
    public MockDataModelMirrorRepository add(String packageName, String... typeNames) {
        for (String typeName : typeNames) {
            targets.add(packageName + "." + typeName);
        }
        return this;
    }

    @Override
    public DataModelMirror load(CompileEnvironment environment, TypeMirror type) {
        if (isTargetType(environment, type) == false) {
            return null;
        }
        if (type.getKind() == TypeKind.DECLARED) {
            return new ConcreteDataModelMirror(environment, (DeclaredType) type, this);
        } else if (type.getKind() == TypeKind.TYPEVAR) {
            return new PartialDataModelMirror(environment, (TypeVariable) type, this);
        }
        return null;
    }

    private boolean isTargetType(CompileEnvironment environment, TypeMirror type) {
        TypeMirror target = environment.getErasure(type);
        if (target.getKind() != TypeKind.DECLARED) {
            return false;
        }
        DeclaredType decl = (DeclaredType) target;
        String name = ((TypeElement) decl.asElement()).getQualifiedName().toString();
        if (targets.contains(name) == false) {
            return false;
        }
        return true;
    }

    @Override
    public Set<PropertyMirror> collect(
            CompileEnvironment environment,
            TypeMirror targetType,
            List<TypeElement> upperBounds) {
        if (environment == null) {
            throw new IllegalArgumentException("environment must not be null"); //$NON-NLS-1$
        }
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null"); //$NON-NLS-1$
        }
        if (upperBounds == null) {
            throw new IllegalArgumentException("upperBounds must not be null"); //$NON-NLS-1$
        }
        Set<PropertyMirror> results = new HashSet<>();
        Elements elements = environment.getProcessingEnvironment().getElementUtils();
        for (TypeElement type : upperBounds) {
            for (VariableElement field : ElementFilter.fieldsIn(elements.getAllMembers(type))) {
                if (field.getModifiers().contains(Modifier.STATIC) == false) {
                    results.add(new PropertyMirror(
                            field.getSimpleName().toString(),
                            field.asType()));
                }
            }
        }
        return results;
    }
}
