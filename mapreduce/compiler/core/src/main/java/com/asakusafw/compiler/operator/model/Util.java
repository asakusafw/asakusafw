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

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Types;

import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.compiler.operator.DataModelMirror.PropertyMirror;
import com.asakusafw.compiler.operator.OperatorCompilingEnvironment;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.vocabulary.flow.FlowDescription;

/**
 * Common utility methods for this package.
 * @since 0.2.0
 */
final class Util {

    static boolean isConcrete(OperatorCompilingEnvironment environment, TypeMirror type) {
        assert environment != null;
        assert type != null;
        if (type.getKind() != TypeKind.DECLARED) {
            return false;
        }
        if (isKindMatched(environment, type) == false) {
            return false;
        }
        TypeMirror datamodel = environment.getDeclaredType(DataModel.class);
        return environment.getTypeUtils().isSubtype(type, datamodel);
    }

    static boolean isPartial(OperatorCompilingEnvironment environment, TypeMirror type) {
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

    private static boolean isKindMatched(OperatorCompilingEnvironment environment, TypeMirror type) {
        assert environment != null;
        assert type != null;
        TypeElement element = (TypeElement) environment.getTypeUtils().asElement(type);
        DataModelKind kind = element.getAnnotation(DataModelKind.class);
        return kind != null && kind.value().equals("DMDL"); //$NON-NLS-1$
    }

    private static boolean hasKindMatched(OperatorCompilingEnvironment environment, TypeParameterElement parameter) {
        assert environment != null;
        assert parameter != null;
        for (TypeMirror bound : parameter.getBounds()) {
            if (isKindMatched(environment, bound)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOperatorSource(OperatorCompilingEnvironment environment, Element element) {
        assert environment != null;
        assert element != null;
        if (element.getKind() == ElementKind.METHOD) {
            return true;
        }
        if (element.getKind() == ElementKind.CLASS) {
            Types typeUtils = environment.getTypeUtils();
            DeclaredType type = typeUtils.getDeclaredType((TypeElement) element);
            return typeUtils.isSubtype(type, environment.getDeclaredType(FlowDescription.class));
        }
        return false;
    }

    static PropertyMirror toProperty(ExecutableElement element) {
        assert element != null;
        JavaName name = JavaName.of(element.getSimpleName().toString());
        List<String> segments = name.getSegments();
        if (segments.size() <= 2) {
            return null;
        }
        if (segments.get(0).equals("get") == false //$NON-NLS-1$
                || segments.get(segments.size() - 1).equals("option") == false) { //$NON-NLS-1$
            return null;
        }
        name.removeLast();
        name.removeFirst();
        String propertyName = name.toMemberName();
        return new DefaultPropertyMirror(propertyName, element);
    }

    static String normalize(String name) {
        assert name != null;
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        return JavaName.of(name).toMemberName();
    }

    private Util() {
        return;
    }
}
