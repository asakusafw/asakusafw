/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.utils.java.model.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.AnnotationElement;
import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Modifier;
import com.asakusafw.utils.java.model.syntax.ModifierKind;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.Type;

/**
 * 修飾子を構築するビルダー。
 * <p>
 * このクラスのオブジェクトは、自身を破壊的に変更して修飾子等を構築する。
 * 特定の状態のビルダーを再利用する場合、{@link #copy()}を利用すること。
 * </p>
 * @since 0.1.0
 * @version 0.5.1
 */
public class AttributeBuilder {

    private final ModelFactory f;

    private final List<Attribute> attributes;

    /**
     * インスタンスを生成する。
     * @param factory モデルを生成するためのファクトリ
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder(ModelFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        this.f = factory;
        this.attributes = new ArrayList<Attribute>();
    }

    /**
     * 現在のビルダーと同等の内容を持つビルダーを新しく作成して返す。
     * @return コピーしたビルダー
     */
    public AttributeBuilder copy() {
        AttributeBuilder copy = new AttributeBuilder(f);
        copy.attributes.addAll(attributes);
        return copy;
    }

    /**
     * ここまでに構築した内容をリストに変換して返す。
     * <p>
     * 返されるリスト内の要素は、ビルダーに追加順に整列される。
     * </p>
     * @return 構築した内容を含むリスト
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public List<Attribute> toAttributes() {
        return new ArrayList<Attribute>(attributes);
    }

    /**
     * ここまでに構築した内容のうち、修飾子だけをリストに含めて返す。
     * <p>
     * 返されるリスト内の要素は、ビルダーに追加順に整列される。
     * </p>
     * @return 構築した内容を含むリスト
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public List<Modifier> toModifiers() {
        List<Modifier> results = new ArrayList<Modifier>();
        for (Attribute attribute : toAttributes()) {
            if (attribute instanceof Modifier) {
                results.add((Modifier) attribute);
            }
        }
        return results;
    }

    /**
     * ここまでに構築した内容のうち、注釈だけをリストに含めて返す。
     * <p>
     * 返されるリスト内の要素は、ビルダーに追加順に整列される。
     * </p>
     * @return 構築した内容を含むリスト
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public List<Annotation> toAnnotations() {
        List<Annotation> results = new ArrayList<Annotation>();
        for (Attribute attribute : toAttributes()) {
            if (attribute instanceof Annotation) {
                results.add((Annotation) attribute);
            }
        }
        return results;
    }

    /**
     * {@code public}を追加したビルダーを返す。
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder Public() {
        return modifier(ModifierKind.PUBLIC);
    }

    /**
     * {@code protected}を追加したビルダーを返す。
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder Protected() {
        return modifier(ModifierKind.PROTECTED);
    }

    /**
     * {@code private}を追加したビルダーを返す。
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder Private() {
        return modifier(ModifierKind.PRIVATE);
    }

    /**
     * {@code static}を追加したビルダーを返す。
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder Static() {
        return modifier(ModifierKind.STATIC);
    }

    /**
     * {@code abstract}を追加したビルダーを返す。
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder Abstract() {
        return modifier(ModifierKind.ABSTRACT);
    }

    /**
     * {@code native}を追加したビルダーを返す。
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder Native() {
        return modifier(ModifierKind.NATIVE);
    }

    /**
     * {@code final}を追加したビルダーを返す。
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder Final() {
        return modifier(ModifierKind.FINAL);
    }

    /**
     * {@code synchronized}を追加したビルダーを返す。
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder Synchronized() {
        return modifier(ModifierKind.SYNCHRONIZED);
    }

    /**
     * {@code transient}を追加したビルダーを返す。
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder Transient() {
        return modifier(ModifierKind.TRANSIENT);
    }

    /**
     * {@code volatile}を追加したビルダーを返す。
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder Volatile() {
        return modifier(ModifierKind.VOLATILE);
    }

    /**
     * {@code strictfp}を追加したビルダーを返す。
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder Strictfp() {
        return modifier(ModifierKind.STRICTFP);
    }

    /**
     * 指定の修飾子を追加したビルダーを返す。
     * @param modifier 修飾子の種類
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder modifier(ModifierKind modifier) {
        if (modifier == null) {
            throw new IllegalArgumentException("modifier must not be null"); //$NON-NLS-1$
        }
        return chain(f.newModifier(modifier));
    }

    /**
     * 指定の注釈を追加したビルダーを返す。
     * @param type 注釈の型
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder annotation(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple named-type"); //$NON-NLS-1$
        }
        return annotation(f.newMarkerAnnotation((NamedType) type));
    }

    /**
     * 指定の注釈を追加したビルダーを返す。
     * @param type 注釈の型
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder annotation(java.lang.reflect.Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return annotation(Models.toType(f, type));
    }

    /**
     * 指定の注釈を追加したビルダーを返す。
     * @param type 注釈の型
     * @param value 要素の値
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder annotation(Type type, Expression value) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple named-type"); //$NON-NLS-1$
        }
        return annotation(f.newSingleElementAnnotation((NamedType) type, value));
    }

    /**
     * 指定の注釈を追加したビルダーを返す。
     * @param type 注釈の型
     * @param value 要素の値
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder annotation(java.lang.reflect.Type type, Expression value) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return annotation(Models.toType(f, type), value);
    }

    /**
     * Adds the specified annotation.
     * @param type the target annotation type
     * @param elements the element name-value pairs
     * @return this
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.5.1
     */
    public AttributeBuilder annotation(Type type, Map<? extends String, ? extends Expression> elements) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple named-type"); //$NON-NLS-1$
        }
        if (elements == null) {
            throw new IllegalArgumentException("elements must not be null"); //$NON-NLS-1$
        }
        List<AnnotationElement> elems = new ArrayList<AnnotationElement>();
        for (Map.Entry<? extends String, ? extends Expression> entry : elements.entrySet()) {
            elems.add(f.newAnnotationElement(f.newSimpleName(entry.getKey()), entry.getValue()));
        }
        return annotation(f.newNormalAnnotation((NamedType) type, elems));
    }

    /**
     * 指定の注釈を追加したビルダーを返す。
     * @param type 注釈の型
     * @param elementName 要素名
     * @param elementValue 要素の値
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder annotation(
            Type type,
            String elementName, Expression elementValue) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple named-type"); //$NON-NLS-1$
        }
        if (elementName == null) {
            throw new IllegalArgumentException("elementName must not be null"); //$NON-NLS-1$
        }
        if (elementValue == null) {
            throw new IllegalArgumentException("elementValue must not be null"); //$NON-NLS-1$
        }
        List<AnnotationElement> elements = new ArrayList<AnnotationElement>();
        elements.add(f.newAnnotationElement(
                f.newSimpleName(elementName),
                elementValue));
        return annotation(f.newNormalAnnotation((NamedType) type, elements));
    }

    /**
     * 指定の注釈を追加したビルダーを返す。
     * @param type 注釈の型
     * @param elementName1 要素名 (1)
     * @param elementValue1 要素の値 (1)
     * @param elementName2 要素名 (2)
     * @param elementValue2 要素の値 (2)
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder annotation(
            Type type,
            String elementName1, Expression elementValue1,
            String elementName2, Expression elementValue2) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple named-type"); //$NON-NLS-1$
        }
        if (elementName1 == null) {
            throw new IllegalArgumentException("elementName1 must not be null"); //$NON-NLS-1$
        }
        if (elementValue1 == null) {
            throw new IllegalArgumentException("elementValue1 must not be null"); //$NON-NLS-1$
        }
        if (elementName2 == null) {
            throw new IllegalArgumentException("elementName2 must not be null"); //$NON-NLS-2$
        }
        if (elementValue2 == null) {
            throw new IllegalArgumentException("elementValue2 must not be null"); //$NON-NLS-2$
        }
        List<AnnotationElement> elements = new ArrayList<AnnotationElement>();
        elements.add(f.newAnnotationElement(
                f.newSimpleName(elementName1), elementValue1));
        elements.add(f.newAnnotationElement(
                f.newSimpleName(elementName2), elementValue2));
        return annotation(f.newNormalAnnotation((NamedType) type, elements));
    }

    /**
     * 指定の注釈を追加したビルダーを返す。
     * @param type 注釈の型
     * @param elementName1 要素名 (1)
     * @param elementValue1 要素の値 (1)
     * @param elementName2 要素名 (2)
     * @param elementValue2 要素の値 (2)
     * @param elementName3 要素名 (3)
     * @param elementValue3 要素の値 (3)
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder annotation(
            Type type,
            String elementName1, Expression elementValue1,
            String elementName2, Expression elementValue2,
            String elementName3, Expression elementValue3) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple named-type"); //$NON-NLS-1$
        }
        if (elementName1 == null) {
            throw new IllegalArgumentException("elementName1 must not be null"); //$NON-NLS-1$
        }
        if (elementValue1 == null) {
            throw new IllegalArgumentException("elementValue1 must not be null"); //$NON-NLS-1$
        }
        if (elementName2 == null) {
            throw new IllegalArgumentException("elementName2 must not be null"); //$NON-NLS-2$
        }
        if (elementValue2 == null) {
            throw new IllegalArgumentException("elementValue2 must not be null"); //$NON-NLS-2$
        }
        if (elementName3 == null) {
            throw new IllegalArgumentException("elementName3 must not be null"); //$NON-NLS-3$
        }
        if (elementValue3 == null) {
            throw new IllegalArgumentException("elementValue3 must not be null"); //$NON-NLS-3$
        }
        List<AnnotationElement> elements = new ArrayList<AnnotationElement>();
        elements.add(f.newAnnotationElement(
                f.newSimpleName(elementName1), elementValue1));
        elements.add(f.newAnnotationElement(
                f.newSimpleName(elementName2), elementValue2));
        elements.add(f.newAnnotationElement(
                f.newSimpleName(elementName3), elementValue3));
        return annotation(f.newNormalAnnotation((NamedType) type, elements));
    }

    /**
     * 指定の注釈を追加したビルダーを返す。
     * @param type 注釈の型
     * @param elementName1 要素名 (1)
     * @param elementValue1 要素の値 (1)
     * @param elementName2 要素名 (2)
     * @param elementValue2 要素の値 (2)
     * @param elementName3 要素名 (3)
     * @param elementValue3 要素の値 (3)
     * @param elementName4 要素名 (4)
     * @param elementValue4 要素の値 (4)
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder annotation(
            Type type,
            String elementName1, Expression elementValue1,
            String elementName2, Expression elementValue2,
            String elementName3, Expression elementValue3,
            String elementName4, Expression elementValue4) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple named-type"); //$NON-NLS-1$
        }
        if (elementName1 == null) {
            throw new IllegalArgumentException("elementName1 must not be null"); //$NON-NLS-1$
        }
        if (elementValue1 == null) {
            throw new IllegalArgumentException("elementValue1 must not be null"); //$NON-NLS-1$
        }
        if (elementName2 == null) {
            throw new IllegalArgumentException("elementName2 must not be null"); //$NON-NLS-2$
        }
        if (elementValue2 == null) {
            throw new IllegalArgumentException("elementValue2 must not be null"); //$NON-NLS-2$
        }
        if (elementName3 == null) {
            throw new IllegalArgumentException("elementName3 must not be null"); //$NON-NLS-3$
        }
        if (elementValue3 == null) {
            throw new IllegalArgumentException("elementValue3 must not be null"); //$NON-NLS-3$
        }
        if (elementName4 == null) {
            throw new IllegalArgumentException("elementName4 must not be null"); //$NON-NLS-4$
        }
        if (elementValue4 == null) {
            throw new IllegalArgumentException("elementValue4 must not be null"); //$NON-NLS-4$
        }
        List<AnnotationElement> elements = new ArrayList<AnnotationElement>();
        elements.add(f.newAnnotationElement(
                f.newSimpleName(elementName1), elementValue1));
        elements.add(f.newAnnotationElement(
                f.newSimpleName(elementName2), elementValue2));
        elements.add(f.newAnnotationElement(
                f.newSimpleName(elementName3), elementValue3));
        elements.add(f.newAnnotationElement(
                f.newSimpleName(elementName4), elementValue4));
        return annotation(f.newNormalAnnotation((NamedType) type, elements));
    }

    /**
     * 指定の注釈を追加したビルダーを返す。
     * @param type 注釈の型
     * @param elementName1 要素名 (1)
     * @param elementValue1 要素の値 (1)
     * @param elementName2 要素名 (2)
     * @param elementValue2 要素の値 (2)
     * @param elementName3 要素名 (3)
     * @param elementValue3 要素の値 (3)
     * @param elementName4 要素名 (4)
     * @param elementValue4 要素の値 (4)
     * @param elementName5 要素名 (5)
     * @param elementValue5 要素の値 (5)
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder annotation(
            Type type,
            String elementName1, Expression elementValue1,
            String elementName2, Expression elementValue2,
            String elementName3, Expression elementValue3,
            String elementName4, Expression elementValue4,
            String elementName5, Expression elementValue5) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple named-type"); //$NON-NLS-1$
        }
        if (elementName1 == null) {
            throw new IllegalArgumentException("elementName1 must not be null"); //$NON-NLS-1$
        }
        if (elementValue1 == null) {
            throw new IllegalArgumentException("elementValue1 must not be null"); //$NON-NLS-1$
        }
        if (elementName2 == null) {
            throw new IllegalArgumentException("elementName2 must not be null"); //$NON-NLS-2$
        }
        if (elementValue2 == null) {
            throw new IllegalArgumentException("elementValue2 must not be null"); //$NON-NLS-2$
        }
        if (elementName3 == null) {
            throw new IllegalArgumentException("elementName3 must not be null"); //$NON-NLS-3$
        }
        if (elementValue3 == null) {
            throw new IllegalArgumentException("elementValue3 must not be null"); //$NON-NLS-3$
        }
        if (elementName4 == null) {
            throw new IllegalArgumentException("elementName4 must not be null"); //$NON-NLS-4$
        }
        if (elementValue4 == null) {
            throw new IllegalArgumentException("elementValue4 must not be null"); //$NON-NLS-4$
        }
        if (elementName5 == null) {
            throw new IllegalArgumentException("elementName5 must not be null"); //$NON-NLS-5$
        }
        if (elementValue5 == null) {
            throw new IllegalArgumentException("elementValue5 must not be null"); //$NON-NLS-5$
        }
        List<AnnotationElement> elements = new ArrayList<AnnotationElement>();
        elements.add(f.newAnnotationElement(
                f.newSimpleName(elementName1), elementValue1));
        elements.add(f.newAnnotationElement(
                f.newSimpleName(elementName2), elementValue2));
        elements.add(f.newAnnotationElement(
                f.newSimpleName(elementName3), elementValue3));
        elements.add(f.newAnnotationElement(
                f.newSimpleName(elementName4), elementValue4));
        elements.add(f.newAnnotationElement(
                f.newSimpleName(elementName5), elementValue5));
        return annotation(f.newNormalAnnotation((NamedType) type, elements));
    }

    /**
     * 指定の注釈を追加したビルダーを返す。
     * @param annotation 注釈
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public AttributeBuilder annotation(Annotation annotation) {
        if (annotation == null) {
            throw new IllegalArgumentException("annotation must not be null"); //$NON-NLS-1$
        }
        return chain(annotation);
    }

    private AttributeBuilder chain(Attribute attribute) {
        assert attribute != null;
        attributes.add(attribute);
        return this;
    }
}
