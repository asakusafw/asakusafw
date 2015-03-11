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
 * 修飾子の種類。
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
            allGrants = new HashSet<DeclarationKind>(grant);
            allGrants.addAll(declarable);
        }
        this.grantable = Collections.unmodifiableSet(allGrants);
    }

    /**
     * この要素をソースコード上に明記できない場合のみ{@code true}を返す。
     * @return この要素をソースコード上に明記できない場合のみ{@code true}
     */
    public boolean isImplicit() {
        return declarable.isEmpty();
    }

    /**
     * この修飾子を指定の種類の要素に対し、ソースコード上で付与できる場合のみ{@code true}を返す。
     * @param kind 付与先の要素の種類
     * @return この修飾子をソースコード上で付与できる場合のみ{@code true}
     */
    public boolean canBeDeclaredIn(DeclarationKind kind) {
        return declarable.contains(kind);
    }

    /**
     * この修飾子を指定の種類の要素に対し、ソースコードまたはクラスファイル上に付与できる場合のみ{@code true}を返す。
     * @param kind 付与先の要素の種類
     * @return この修飾子をソースコードまたはクラスファイル上に付与できる場合のみ{@code true}
     */
    public boolean canBeGrantedIn(DeclarationKind kind) {
        return grantable.contains(kind);
    }

    /**
     * 指定の修飾子一覧を文字列化して返す。
     * @param modifiers 対象の修飾子一覧
     * @return 対応する文字列
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static String toStringAll(Set<ModifierKind> modifiers) {
        if (modifiers == null) {
            throw new IllegalArgumentException("modifiers is null"); //$NON-NLS-1$
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
     * 修飾子一覧を文字列化したものを、修飾子の一覧に復元する。
     * @param values 修飾子一覧を文字列化したもの
     * @return 対応する修飾子の一覧
     * @throws IllegalArgumentException
     *     復元できない場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
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
     * この修飾子を表現するキーワードを返す。
     * @return この修飾子を表現するキーワード、存在しないキーワードの場合は特殊な文字列
     */
    public String getKeyword() {
        if (isImplicit()) {
            return MessageFormat.format("ACC_{0}", name()); //$NON-NLS-1$
        } else {
            return name().toLowerCase();
        }
    }

    /**
     * この修飾子の種類を表現する文字列を返す。
     * @return この修飾子の種類を表現する文字列
     */
    @Override
    public String toString() {
        return getKeyword();
    }

    private static Set<DeclarationKind> of(DeclarationKind...kinds) {
        return new HashSet<DeclarationKind>(Arrays.asList(kinds));
    }
}
