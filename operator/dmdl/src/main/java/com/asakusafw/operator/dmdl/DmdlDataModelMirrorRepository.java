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
package com.asakusafw.operator.dmdl;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.Constants;
import com.asakusafw.operator.DataModelMirrorRepository;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.model.ConcreteDataModelMirror;
import com.asakusafw.operator.model.DataModelMirror;
import com.asakusafw.operator.model.JavaName;
import com.asakusafw.operator.model.PartialDataModelMirror;
import com.asakusafw.operator.model.PropertyMirror;
import com.asakusafw.operator.model.PropertyMirrorCollector;
import com.asakusafw.operator.util.AnnotationHelper;

/**
 * Implementation of {@link DataModelMirrorRepository} for DMDL.
 */
public class DmdlDataModelMirrorRepository implements DataModelMirrorRepository, PropertyMirrorCollector {

    private static final ClassDescription TYPE_DATA_MODEL =
            new ClassDescription("com.asakusafw.runtime.model.DataModel"); //$NON-NLS-1$

    private static final ClassDescription TYPE_DATA_MODEL_KIND =
            new ClassDescription("com.asakusafw.runtime.model.DataModelKind"); //$NON-NLS-1$

    private static final String MEMBER_DATA_MODEL_KIND = "value"; //$NON-NLS-1$

    private static final String SYMBOL_KIND = "DMDL"; //$NON-NLS-1$

    @Override
    public DataModelMirror load(CompileEnvironment environment, TypeMirror type) {
        Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(type, "type must not be null"); //$NON-NLS-1$
        if (isConcrete(environment, type)) {
            return new ConcreteDataModelMirror(environment, (DeclaredType) type, this);
        } else if (isPartial(environment, type)) {
            return new PartialDataModelMirror(environment, (TypeVariable) type, this);
        }
        return null;
    }

    private boolean isConcrete(CompileEnvironment environment, TypeMirror type) {
        assert environment != null;
        assert type != null;
        if (type.getKind() != TypeKind.DECLARED) {
            return false;
        }
        if (isKindMatched(environment, type) == false) {
            return false;
        }
        TypeMirror datamodel = environment.findDeclaredType(TYPE_DATA_MODEL);
        return environment.getProcessingEnvironment().getTypeUtils().isSubtype(type, datamodel);
    }

    private boolean isPartial(CompileEnvironment environment, TypeMirror type) {
        assert environment != null;
        assert type != null;
        if (type.getKind() != TypeKind.TYPEVAR) {
            return false;
        }
        TypeVariable var = (TypeVariable) type;
        TypeParameterElement parameter = (TypeParameterElement) var.asElement();
        if (hasKindMatched(environment, parameter) == false) {
            return false;
        }
        Element parent = parameter.getEnclosingElement();
        // MEMO Eclipse JDT returns null for "TypeParameterElement.enclosingElement."
        if (parent == null) {
            return true;
        }
        return isOperatorSource(environment, parent);
    }

    private boolean isKindMatched(CompileEnvironment environment, TypeMirror type) {
        assert environment != null;
        assert type != null;
        TypeElement element = (TypeElement) environment.getProcessingEnvironment().getTypeUtils().asElement(type);
        TypeElement annotation = environment.findTypeElement(TYPE_DATA_MODEL_KIND);
        AnnotationMirror mirror = AnnotationHelper.findAnnotation(environment, annotation, element);
        if (mirror == null) {
            return false;
        }
        AnnotationValue value = AnnotationHelper.getValue(environment, mirror, MEMBER_DATA_MODEL_KIND);
        return value.getValue().equals(SYMBOL_KIND);
    }

    private boolean hasKindMatched(CompileEnvironment environment, TypeParameterElement parameter) {
        assert environment != null;
        assert parameter != null;
        for (TypeMirror bound : parameter.getBounds()) {
            if (isKindMatched(environment, bound)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOperatorSource(CompileEnvironment environment, Element element) {
        assert environment != null;
        assert element != null;
        if (element.getKind() == ElementKind.METHOD) {
            return true;
        }
        if (element.getKind() == ElementKind.CLASS) {
            Types typeUtils = environment.getProcessingEnvironment().getTypeUtils();
            DeclaredType type = typeUtils.getDeclaredType((TypeElement) element);
            DeclaredType desc = environment.findDeclaredType(Constants.TYPE_FLOW_DESCRIPTION);
            return typeUtils.isSubtype(type, desc);
        }
        return false;
    }

    @Override
    public Set<PropertyMirror> collect(
            CompileEnvironment environment,
            TypeMirror targetType,
            List<TypeElement> upperBounds) {
        Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(targetType, "targetType must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(upperBounds, "upperBounds must not be null"); //$NON-NLS-1$
        Set<PropertyMirror> results = new LinkedHashSet<>();
        Elements elements = environment.getProcessingEnvironment().getElementUtils();
        for (TypeElement element : upperBounds) {
            for (ExecutableElement method : ElementFilter.methodsIn(elements.getAllMembers(element))) {
                PropertyMirror property = toProperty(method);
                if (property != null) {
                    results.add(property);
                }
            }
        }
        return results;
    }

    private PropertyMirror toProperty(ExecutableElement element) {
        assert element != null;
        JavaName name = JavaName.of(element.getSimpleName().toString());
        List<String> segments = name.getSegments();
        if (segments.size() <= 2) {
            return null;
        }
        String first = segments.get(0);
        String last = segments.get(segments.size() - 1);
        if (first.equals("get") == false || last.equals("option") == false) { //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }
        name.removeLast();
        name.removeFirst();
        String propertyName = name.toMemberName();
        return new PropertyMirror(propertyName, element.getReturnType());
    }
}
