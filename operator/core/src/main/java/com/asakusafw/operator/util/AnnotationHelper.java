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
package com.asakusafw.operator.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import com.asakusafw.operator.CompileEnvironment;

/**
 * Common helper methods about annotations.
 */
public final class AnnotationHelper {

    private AnnotationHelper() {
        return;
    }

    /**
     * Returns the annotation presented in the target element.
     * @param environment current environment
     * @param annotationDecl target annotation type element
     * @param element target annotated element
     * @return target annotation (includes inherited annotations), or {@code null} if does not present
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static AnnotationMirror findAnnotation(
            CompileEnvironment environment,
            TypeElement annotationDecl,
            Element element) {
        Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(annotationDecl, "annotationDecl must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(element, "element must not be null"); //$NON-NLS-1$
        List<? extends AnnotationMirror> annotations =
                environment.getProcessingEnvironment().getElementUtils().getAllAnnotationMirrors(element);
        for (AnnotationMirror annotation : annotations) {
            TypeElement type = (TypeElement) annotation.getAnnotationType().asElement();
            if (type.equals(annotationDecl)) {
                return annotation;
            }
        }
        return null;
    }

    /**
     * Returns the name-value pairs in annotation.
     * @param environment current environment
     * @param annotation target annotation
     * @return element name and its value pairs
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Map<String, AnnotationValue> getValues(CompileEnvironment environment, AnnotationMirror annotation) {
        Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(annotation, "annotation must not be null"); //$NON-NLS-1$
        Elements elements = environment.getProcessingEnvironment().getElementUtils();
        Map<? extends ExecutableElement, ? extends AnnotationValue> map =
                elements.getElementValuesWithDefaults(annotation);
        Map<String, AnnotationValue> results = new HashMap<>();
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : map.entrySet()) {
            results.put(entry.getKey().getSimpleName().toString(), entry.getValue());
        }
        return results;
    }

    /**
     * Returns the value of annotation member.
     * @param environment current environment
     * @param annotation target annotation
     * @param name target annotation member name
     * @return the related value (may be default value), or {@code null} if the member is not declared
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static AnnotationValue getValue(
            CompileEnvironment environment,
            AnnotationMirror annotation,
            String name) {
        Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(annotation, "annotation must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(name, "name must not be null"); //$NON-NLS-1$
        Map<? extends ExecutableElement, ? extends AnnotationValue> values =
                environment.getProcessingEnvironment().getElementUtils().getElementValuesWithDefaults(annotation);
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Converts the value into component list.
     * If target value does not represents a list, this returns just consists of the target value.
     * @param environment current environment
     * @param value target annotation value
     * @return the component list
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static List<AnnotationValue> toValueList(CompileEnvironment environment, AnnotationValue value) {
        Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(value, "value must not be null"); //$NON-NLS-1$
        Object component = value.getValue();
        if (component instanceof List<?>) {
            List<AnnotationValue> results = new ArrayList<>();
            for (Object componentValue : (Iterable<?>) component) {
                results.add((AnnotationValue) componentValue);
            }
            return results;
        } else {
            return Collections.singletonList(value);
        }
    }

    /**
     * Extracts component values in the target list.
     * @param <T> component value type
     * @param environment current environment
     * @param componentValueType target value type, the returning list only includes values with this type
     * @param values target annotation values
     * @return component values in the list
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static <T> List<T> extractList(
            CompileEnvironment environment,
            Class<T> componentValueType,
            List<? extends AnnotationValue> values) {
        Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(componentValueType, "componentValueType must not be null"); //$NON-NLS-1$
        Objects.requireNonNull(values, "values must not be null"); //$NON-NLS-1$
        List<T> results = new ArrayList<>();
        for (AnnotationValue componentHolder : values) {
            Object componentValue = componentHolder.getValue();
            if (componentValueType.isInstance(componentValue)) {
                results.add(componentValueType.cast(componentValue));
            }
        }
        return results;
    }
}
