/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Represents a kind of declaration modifiers.
 */
public enum ModifierKind {

    /**
     * {@code public}.
     */
    PUBLIC(
        of(CLASS, INTERFACE, ENUM, ANNOTATION, FIELD, CONSTRUCTOR, METHOD),
        of(ANNOTATION_ELEMENT, ENUM_CONSTANT)
    ),

    /**
     * {@code protected}.
     */
    PROTECTED(
        of(CLASS, INTERFACE, ENUM, ANNOTATION, FIELD, CONSTRUCTOR, METHOD),
        Collections.<DeclarationKind>emptySet()
    ),

    /**
     * {@code private}.
     */
    PRIVATE(
        of(CLASS, INTERFACE, ENUM, ANNOTATION, FIELD, CONSTRUCTOR, METHOD),
        Collections.<DeclarationKind>emptySet()
    ),

    /**
     * {@code static}.
     */
    STATIC(
        of(CLASS, INTERFACE, ENUM, ANNOTATION, FIELD, METHOD),
        of(ENUM_CONSTANT)
    ),

    /**
     * {@code abstract}.
     */
    ABSTRACT(
        of(CLASS, INTERFACE, ENUM, ANNOTATION, FIELD, METHOD),
        of(ANNOTATION_ELEMENT)
    ),

    /**
     * {@code native}.
     */
    NATIVE(
        of(METHOD),
        Collections.<DeclarationKind>emptySet()
    ),

    /**
     * {@code final}.
     */
    FINAL(
        of(CLASS, FIELD, METHOD),
        of(ENUM, ENUM_CONSTANT)
    ),

    /**
     * {@code synchronized}.
     */
    SYNCHRONIZED(
        of(METHOD),
        Collections.<DeclarationKind>emptySet()
    ),

    /**
     * {@code transient}.
     */
    TRANSIENT(
        of(FIELD),
        Collections.<DeclarationKind>emptySet()
    ),

    /**
     * {@code volatile}.
     */
    VOLATILE(
        of(FIELD),
        Collections.<DeclarationKind>emptySet()
    ),

    /**
     * {@code strictfp}.
     */
    STRICTFP(
        of(CLASS, INTERFACE, ENUM, ANNOTATION, METHOD),
        Collections.<DeclarationKind>emptySet()
    ),

    /**
     * {@code ACC_SUPER}.
     */
    SUPER(
        Collections.<DeclarationKind>emptySet(),
        of(CLASS, INTERFACE, ENUM, ANNOTATION)
    ),

    /**
     * {@code ACC_BRIDGE}.
     */
    BRIDGE(
        Collections.<DeclarationKind>emptySet(),
        of(METHOD)
    ),

    /**
     * {@code ACC_VARARGS}.
     */
    VARARGS(
        Collections.<DeclarationKind>emptySet(),
        of(METHOD, CONSTRUCTOR)
    ),

    /**
     * {@code ACC_SYNTHETIC}.
     */
    SYNTHETIC(
        Collections.<DeclarationKind>emptySet(),
        of(CLASS, INTERFACE, ENUM, ANNOTATION, FIELD, CONSTRUCTOR, METHOD, ANNOTATION_ELEMENT, ENUM_CONSTANT)
    ),
    ;

    private final Set<DeclarationKind> declarable;
    private final Set<DeclarationKind> grantable;

    private ModifierKind(Set<DeclarationKind> declarable, Set<DeclarationKind> grant) {
        assert declarable != null;
        assert grant != null;
        this.declarable = Collections.unmodifiableSet(declarable);
        Set<DeclarationKind> allGrants;
        if (grant.isEmpty()) {
            allGrants = declarable;
        } else {
            allGrants = new HashSet<>(grant);
            allGrants.addAll(declarable);
        }
        this.grantable = Collections.unmodifiableSet(allGrants);
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
     * Returns whether this modifier can be implicitly declared on the target declaration or not.
     * For example, the {@code public} modifier cannot be explicitly declared on enum constants,
     * but it modifier is implicitly declared on their enum constants in class files.
     * @param kind the target declaration kind
     * @return {@code true} if this modifier can be declared on the target declaration, otherwise {@code false}
     */
    public boolean canBeGrantedIn(DeclarationKind kind) {
        return grantable.contains(kind);
    }

    /**
     * Returns a string representation of the set of modifier kinds.
     * @param modifiers the target modifier kinds
     * @return the string representation
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static String toStringAll(Set<ModifierKind> modifiers) {
        if (modifiers == null) {
            throw new IllegalArgumentException("modifiers must not be null"); //$NON-NLS-1$
        }
        StringBuilder buf = new StringBuilder();
        Iterator<ModifierKind> iter = modifiers.iterator();
        if (iter.hasNext()) {
            buf.append(iter.next().name());
            while (iter.hasNext()) {
                buf.append(',');
                buf.append(iter.next().name());
            }
        }
        return buf.toString();
    }

    /**
     * Restores modifier kinds from its string representation.
     * @param values the string representation of modifier kinds (in form of {@link #toStringAll(Set)})
     * @return a set of the restored modifier kinds
     * @throws IllegalArgumentException if the string representation is malformed
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @see #toStringAll(Set)
     */
    public static Set<ModifierKind> allValueOf(String values) {
        if (values == null) {
            throw new IllegalArgumentException("values must not be null"); //$NON-NLS-1$
        }
        if (values.length() == 0) {
            return Collections.emptySet();
        }
        EnumSet<ModifierKind> results = EnumSet.noneOf(ModifierKind.class);
        int start = 0;
        while (true) {
            int index = values.indexOf(',', start);
            if (index < 0) {
                break;
            }
            String token = values.substring(start, index);
            results.add(ModifierKind.valueOf(token));
            start = index + 1;
        }
        results.add(ModifierKind.valueOf(values.substring(start)));
        return results;
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

    private static Set<DeclarationKind> of(DeclarationKind...kinds) {
        return new HashSet<>(Arrays.asList(kinds));
    }
}
