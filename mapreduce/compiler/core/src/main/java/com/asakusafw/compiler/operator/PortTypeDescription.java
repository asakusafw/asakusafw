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
package com.asakusafw.compiler.operator;

import javax.lang.model.type.TypeMirror;

import com.asakusafw.compiler.common.Precondition;

/**
 * Represents the data type of each port.
 * @since 0.2.0
 */
public final class PortTypeDescription {

    private final Kind kind;

    private final TypeMirror representation;

    private final TypeMirror direct;

    private final String reference;

    private PortTypeDescription(Kind kind, TypeMirror representation, TypeMirror direct, String reference) {
        assert kind != null;
        assert representation != null;
        assert direct != null || reference != null;
        this.kind = kind;
        this.representation = representation;
        this.direct = direct;
        this.reference = reference;
    }

    /**
     * Creates a new instance as a direct typed.
     * @param type the type
     * @return the created instance
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static PortTypeDescription direct(TypeMirror type) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        return new PortTypeDescription(Kind.DIRECT, type, type, null);
    }

    /**
     * Creates a new instance as a reference typed.
     * @param representation the representation type
     * @param variableName the referential variable name holding the actual type
     * @return the created instance
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static PortTypeDescription reference(TypeMirror representation, String variableName) {
        Precondition.checkMustNotBeNull(representation, "representation"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(variableName, "variableName"); //$NON-NLS-1$
        return new PortTypeDescription(Kind.REFERENCE, representation, null, variableName);
    }

    /**
     * Returns the kind of actual type.
     * @return the kind of actual type
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * Returns the representation type.
     * <p>
     * This will use for type of variables to hold each port object.
     * </p>
     * @return the representation type
     */
    public TypeMirror getRepresentation() {
        return representation;
    }

    /**
     * Returns the actual type.
     * @return the referenced variable name
     * @throws IllegalStateException if this does not have actual direct types
     * @see #getKind()
     */
    public TypeMirror getDirect() {
        return direct;
    }

    /**
     * Returns the variable name which holds the actual type.
     * @return the referenced variable name
     * @throws IllegalStateException if this does not have actual type references
     * @see #getKind()
     */
    public String getReference() {
        if (kind != Kind.REFERENCE) {
            throw new IllegalStateException();
        }
        return reference;
    }

    /**
     * Type representation kind.
     * @since 0.2.0
     */
    public enum Kind {

        /**
         * Directory specified.
         */
        DIRECT,

        /**
         * Reference to input.
         */
        REFERENCE,
    }
}
