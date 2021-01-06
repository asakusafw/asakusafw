/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.operator.util;

import java.util.Objects;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.Constants;

/**
 * Common helper methods about types.
 */
public final class TypeHelper {

    private TypeHelper() {
        return;
    }

    /**
     * Returns whether the target type represents an input dataset.
     * @param environment current environment
     * @param type target type
     * @return {@code true} if represents an input, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see Constants#TYPE_IN
     */
    public static boolean isIn(CompileEnvironment environment, TypeMirror type) {
        Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(type, "type must not be null"); //$NON-NLS-1$
        if (type.getKind() != TypeKind.DECLARED) {
            return false;
        }
        DeclaredType expected = environment.findDeclaredType(Constants.TYPE_IN);
        DeclaredType erasure = (DeclaredType) environment.getErasure(type);
        return environment.getProcessingEnvironment().getTypeUtils().isSameType(expected, erasure);
    }

    /**
     * Returns the type argument of {@code In} type.
     * @param environment current environment
     * @param type target {@code In} type
     * @return the type argument, or {@code null} if target type is not {@code In} type
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #isIn(CompileEnvironment, TypeMirror)
     */
    public static TypeMirror getInType(CompileEnvironment environment, TypeMirror type) {
        Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(type, "type must not be null"); //$NON-NLS-1$
        if (isIn(environment, type) == false) {
            return null;
        }
        return getTypeArgument((DeclaredType) type, 0);
    }

    /**
     * Returns the type argument of {@code Out} type.
     * @param environment current environment
     * @param type target {@code Out} type
     * @return the type argument, or {@code null} if target type is not {@code Out} type
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #isIn(CompileEnvironment, TypeMirror)
     */
    public static TypeMirror getOutType(CompileEnvironment environment, TypeMirror type) {
        Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(type, "type must not be null"); //$NON-NLS-1$
        if (isOut(environment, type) == false) {
            return null;
        }
        return getTypeArgument((DeclaredType) type, 0);
    }

    private static TypeMirror getTypeArgument(DeclaredType type, int index) {
        if (index >= type.getTypeArguments().size()) {
            return null;
        }
        return type.getTypeArguments().get(index);
    }

    /**
     * Returns whether the target type represents an output dataset.
     * @param environment current environment
     * @param type target type
     * @return {@code true} if represents an output, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see Constants#TYPE_IN
     */
    public static boolean isOut(CompileEnvironment environment, TypeMirror type) {
        Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(type, "type must not be null"); //$NON-NLS-1$
        if (type.getKind() != TypeKind.DECLARED) {
            return false;
        }
        DeclaredType expected = environment.findDeclaredType(Constants.TYPE_OUT);
        DeclaredType erasure = (DeclaredType) environment.getErasure(type);
        return environment.getProcessingEnvironment().getTypeUtils().isSameType(expected, erasure);
    }

    /**
     * Returns whether the target type is an operator helper annotation.
     * @param environment current environment
     * @param type target type
     * @return {@code true} if is an operator helper annotation, otherwise {@code false}
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see Constants#TYPE_ANNOTATION_HELPER
     */
    public static boolean isOperatorHelper(CompileEnvironment environment, TypeMirror type) {
        if (type.getKind() != TypeKind.DECLARED) {
            return false;
        }
        TypeElement element = (TypeElement) ((DeclaredType) type).asElement();
        if (element.getKind() != ElementKind.ANNOTATION_TYPE) {
            return false;
        }
        DeclaredType operatorHelperType = environment.findDeclaredType(Constants.TYPE_ANNOTATION_HELPER);
        for (AnnotationMirror metaAnnotation : element.getAnnotationMirrors()) {
            Types types = environment.getProcessingEnvironment().getTypeUtils();
            if (types.isSameType(operatorHelperType, metaAnnotation.getAnnotationType())) {
                return true;
            }
        }
        return false;
    }
}
