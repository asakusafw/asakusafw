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
package com.asakusafw.info.operator;

import java.util.Objects;

import com.asakusafw.info.value.AnnotationInfo;
import com.asakusafw.info.value.ClassInfo;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Details of user operators.
 * @since 0.9.2
 */
public final class UserOperatorSpec implements OperatorSpec {

    static final String KIND = "user";

    @JsonProperty(Constants.ID_ANNOTATION)
    private final AnnotationInfo annotation;

    @JsonIgnore
    private final ClassInfo declaringClass;

    @JsonIgnore
    private final ClassInfo implementationClass;

    @JsonProperty(Constants.ID_METHOD)
    private final String methodName;

    private UserOperatorSpec(
            AnnotationInfo annotation,
            ClassInfo declaringClass, ClassInfo implementationClass,
            String methodName) {
        this.annotation = annotation;
        this.declaringClass = declaringClass;
        this.implementationClass = implementationClass;
        this.methodName = methodName;
    }

    /**
     * Returns an instance.
     * @param annotation the annotation
     * @param declaringClass the declaring class
     * @param implementationClass the implementation class
     * @param methodName the method name
     * @return the instance
     */
    public static UserOperatorSpec of(
            AnnotationInfo annotation,
            ClassInfo declaringClass,
            ClassInfo implementationClass,
            String methodName) {
        return new UserOperatorSpec(annotation, declaringClass, implementationClass, methodName);
    }

    @JsonCreator
    static UserOperatorSpec restore(
            @JsonProperty(Constants.ID_ANNOTATION) AnnotationInfo annotation,
            @JsonProperty(Constants.ID_CLASS) String declaringClass,
            @JsonProperty(Constants.ID_IMPLEMENTATION) String implementationClass,
            @JsonProperty(Constants.ID_METHOD) String methodName) {
        return of(
                annotation,
                ClassInfo.of(declaringClass),
                ClassInfo.of(implementationClass),
                methodName);
    }

    @JsonProperty(Constants.ID_CLASS)
    String getDeclaringClassName() {
        return declaringClass.getName();
    }

    @JsonProperty(Constants.ID_IMPLEMENTATION)
    String getImplementationClassName() {
        return implementationClass.getName();
    }

    @Override
    public OperatorKind getOperatorKind() {
        return OperatorKind.USER;
    }

    /**
     * Returns the operator annotation.
     * @return the operator annotation
     */
    public AnnotationInfo getAnnotation() {
        return annotation;
    }

    /**
     * Returns the operator class.
     * @return the operator class
     */
    public ClassInfo getDeclaringClass() {
        return declaringClass;
    }

    /**
     * Returns the implementation class.
     * @return the implementation class
     */
    public ClassInfo getImplementationClass() {
        return implementationClass;
    }

    /**
     * Returns the operator method name.
     * @return the operator method name
     */
    public String getMethodName() {
        return methodName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = Objects.hashCode(getOperatorKind());
        result = prime * result + Objects.hashCode(annotation);
        result = prime * result + Objects.hashCode(implementationClass);
        result = prime * result + Objects.hashCode(declaringClass);
        result = prime * result + Objects.hashCode(methodName);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UserOperatorSpec other = (UserOperatorSpec) obj;
        return Objects.equals(annotation, other.annotation)
                && Objects.equals(declaringClass, other.declaringClass)
                && Objects.equals(implementationClass, other.implementationClass)
                && Objects.equals(methodName, other.methodName);
    }

    @Override
    public String toString() {
        return String.format("User(@%s:%s.%s)",
                annotation.getDeclaringClass().getSimpleName(),
                declaringClass.getSimpleName(),
                methodName);
    }
}
