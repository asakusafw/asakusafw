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
package com.asakusafw.operator.description;

import java.lang.annotation.Annotation;

/**
 * Utilities for {@link Description}.
 */
public final class Descriptions {

    private Descriptions() {
        return;
    }

    /**
     * Converts an object into related {@link ValueDescription}.
     * @param value the original value
     * @return the related {@link ValueDescription}
     */
    public static ValueDescription valueOf(Object value) {
        if (value == null) {
            return ImmediateDescription.of(null);
        }
        Class<?> aClass = value.getClass();
        if (ImmediateDescription.isBoxed(aClass) || value instanceof String) {
            return ImmediateDescription.of(value);
        } else if (value instanceof Class<?>) {
            return ReifiableTypeDescription.of((Class<?>) value);
        } else if (value instanceof Enum<?>) {
            return EnumConstantDescription.of((Enum<?>) value);
        } else if (value instanceof Annotation) {
            return AnnotationDescription.of((Annotation) value);
        } else if (aClass.isArray()) {
            return ArrayDescription.of(value);
        }
        return UnknownValueDescription.of(value);
    }

    /**
     * Converts a class object into related {@link ReifiableTypeDescription}.
     * @param aClass the reflective object
     * @see ReifiableTypeDescription#of(Class)
     * @return the related instance
     */
    public static ReifiableTypeDescription typeOf(Class<?> aClass) {
        return ReifiableTypeDescription.of(aClass);
    }

    /**
     * Converts a class object into related {@link ClassDescription}.
     * @param aClass the reflective object (must be class or interface type)
     * @return the related instance
     * @see ClassDescription#of(Class)
     * @see #typeOf(Class)
     */
    public static ClassDescription classOf(Class<?> aClass) {
        return ClassDescription.of(aClass);
    }
}
