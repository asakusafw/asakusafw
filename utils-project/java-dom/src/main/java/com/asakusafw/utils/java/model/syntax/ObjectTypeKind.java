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
package com.asakusafw.utils.java.model.syntax;

/**
 * Represents a kind of type declarations.
 */
public enum ObjectTypeKind implements DeclarationKind {

    /**
     * Regular class type (not {@link #ENUM enum type}).
     */
    CLASS(true),

    /**
     * Regular interface type (not {@link #ENUM annotation type}).
     */
    INTERFACE(false),

    /**
     * Enum type.
     */
    ENUM(true),

    /**
     * Annotation type.
     */
    ANNOTATION(false),
    ;

    private final boolean classLike;

    ObjectTypeKind(boolean classLike) {
        this.classLike = classLike;
    }

    /**
     * Whether this type kind is class like type or not.
     * @return {@code true} if this type kind is class like type, otherwise {@code false}
     */
    public boolean isClassLike() {
        return classLike;
    }
}
