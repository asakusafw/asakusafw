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
package com.asakusafw.utils.java.model.syntax;

import static com.asakusafw.utils.java.model.syntax.ExecutableKind.*;
import static com.asakusafw.utils.java.model.syntax.FieldKind.*;
import static com.asakusafw.utils.java.model.syntax.ObjectTypeKind.*;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents a kind of declaration modifiers.
 * @since 0.1.0
 * @version 0.2.0
 */
public enum ModifierKind {

    /**
     * {@code public}.
     */
    PUBLIC(CLASS, INTERFACE, ENUM, ANNOTATION, FIELD, CONSTRUCTOR, METHOD),

    /**
     * {@code protected}.
     */
    PROTECTED(CLASS, INTERFACE, ENUM, ANNOTATION, FIELD, CONSTRUCTOR, METHOD),

    /**
     * {@code private}.
     */
    PRIVATE(CLASS, INTERFACE, ENUM, ANNOTATION, FIELD, CONSTRUCTOR, METHOD),

    /**
     * {@code static}.
     */
    STATIC(CLASS, INTERFACE, ENUM, ANNOTATION, FIELD, METHOD),

    /**
     * {@code abstract}.
     */
    ABSTRACT(CLASS, INTERFACE, ENUM, ANNOTATION, FIELD, METHOD),

    /**
     * {@code default}.
     * @since 0.9.0
     */
    DEFAULT(METHOD),

    /**
     * {@code native}.
     */
    NATIVE(METHOD),

    /**
     * {@code final}.
     */
    FINAL(CLASS, FIELD, METHOD),

    /**
     * {@code synchronized}.
     */
    SYNCHRONIZED(METHOD),

    /**
     * {@code transient}.
     */
    TRANSIENT(FIELD),

    /**
     * {@code volatile}.
     */
    VOLATILE(FIELD),

    /**
     * {@code strictfp}.
     */
    STRICTFP(CLASS, INTERFACE, ENUM, ANNOTATION, METHOD),

    /**
     * {@code ACC_SUPER}.
     */
    SUPER(),

    /**
     * {@code ACC_BRIDGE}.
     */
    BRIDGE(),

    /**
     * {@code ACC_VARARGS}.
     */
    VARARGS(),

    /**
     * {@code ACC_SYNTHETIC}.
     */
    SYNTHETIC(),
    ;

    private final Set<DeclarationKind> declarable;

    ModifierKind(DeclarationKind... declarable) {
        assert declarable != null;
        this.declarable = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(declarable)));
    }

    /**
     * Returns whether this modifier kind is implicit or not.
     * Implicit modifiers cannot be described on any source code.
     * @return {@code true} if this modifier kind is implicit, otherwise {@code false}
     */
    public boolean isImplicit() {
        return declarable.isEmpty();
    }

    /**
     * Returns whether this modifier can be explicitly declared on the target declaration or not.
     * @param kind the target declaration kind
     * @return {@code true} if this modifier can be declared on the target declaration, otherwise {@code false}
     */
    public boolean canBeDeclaredIn(DeclarationKind kind) {
        return declarable.contains(kind);
    }

    /**
     * Returns the keyword of this modifier.
     * @return the modifier keyword, or a special keyword if this modifier kind {@link #isImplicit() is implicit}
     */
    public String getKeyword() {
        if (isImplicit()) {
            return MessageFormat.format("ACC_{0}", name()); //$NON-NLS-1$
        } else {
            return name().toLowerCase();
        }
    }

    @Override
    public String toString() {
        return getKeyword();
    }
}
