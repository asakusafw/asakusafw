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
package com.asakusafw.utils.java.model.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import com.asakusafw.utils.java.model.syntax.DocBlock;
import com.asakusafw.utils.java.model.syntax.DocElement;
import com.asakusafw.utils.java.model.syntax.DocMethodParameter;
import com.asakusafw.utils.java.model.syntax.DocText;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;

/**
 * Javadocを構築するビルダー。
 * <p>
 * このクラスのオブジェクトは、自身を破壊的に変更してJavadocを構築する。
 * 特定の状態のビルダーを再利用する場合、{@link #copy()}を利用すること。
 * </p>
 */
public class JavadocBuilder {

    private static final Pattern ESCAPE = Pattern.compile("@"); //$NON-NLS-1$

    private ModelFactory f;

    private List<DocBlock> blocks;

    private String currentTag;

    private List<DocElement> elements;

    /**
     * インスタンスを生成する。
     * @param factory モデルの構築に利用するファクトリ
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder(ModelFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        this.f = factory;
        this.blocks = new ArrayList<DocBlock>();
        this.currentTag = ""; // overview //$NON-NLS-1$
        this.elements = new ArrayList<DocElement>();
    }

    /**
     * 現在のビルダーと同等の内容を持つビルダーを新しく作成して返す。
     * @return コピーしたビルダー
     */
    public JavadocBuilder copy() {
        JavadocBuilder copy = new JavadocBuilder(f);
        copy.blocks = new ArrayList<DocBlock>(blocks);
        copy.currentTag = currentTag;
        copy.elements = new ArrayList<DocElement>(elements);
        return copy;
    }

    /**
     * ここまでに構築した内容を{@link Javadoc}の形式に変換して返す。
     * @return 変換結果
     */
    public Javadoc toJavadoc() {
        flushBlock(""); //$NON-NLS-1$
        return f.newJavadoc(blocks);
    }

    /**
     * 指定のタグを利用してブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param tag 開始するタグ (先頭の<code>&#64;は省略してもよい</code>)
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder block(String tag) {
        if (tag == null) {
            throw new IllegalArgumentException("tag must not be null"); //$NON-NLS-1$
        }
        if (tag.startsWith("@")) { //$NON-NLS-1$
            flushBlock(tag);
        } else {
            flushBlock("@" + tag); //$NON-NLS-1$
        }
        return this;
    }

    /**
     * 指定のインライン要素を挿入する。
     * @param element 対象の要素
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder inline(DocElement element) {
        if (element == null) {
            throw new IllegalArgumentException("element must not be null"); //$NON-NLS-1$
        }
        elements.add(element);
        return this;
    }

    /**
     * 指定のインライン要素を挿入する。
     * @param elems 対象の要素
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder inline(List<? extends DocElement> elems) {
        if (elems == null) {
            throw new IllegalArgumentException("elems must not be null"); //$NON-NLS-1$
        }
        elements.addAll(elems);
        return this;
    }

    /**
     * 指定の名前のパラメーターに関する<code>&#64;param</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param name パラメータの名前
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder param(String name) {
        return param(f.newSimpleName(name));
    }

    /**
     * 指定の名前のパラメーターに関する<code>&#64;param</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param name パラメータの名前
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder param(SimpleName name) {
        block("@param"); //$NON-NLS-1$
        elements.add(name);
        return this;
    }

    /**
     * 指定の名前の型パラメーターに関する<code>&#64;param</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param name パラメータの名前
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder typeParam(String name) {
        return typeParam(f.newSimpleName(name));
    }

    /**
     * 指定の名前の型パラメーターに関する<code>&#64;param</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param name パラメータの名前
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder typeParam(SimpleName name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        block("@param"); //$NON-NLS-1$
        elements.add(f.newDocText("<")); //$NON-NLS-1$
        elements.add(name);
        elements.add(f.newDocText(">")); //$NON-NLS-1$
        return this;
    }

    /**
     * 指定の型変数に対応する型パラメーターに関する<code>&#64;param</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param typeVariable 型変数
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder typeParam(Type typeVariable) {
        if (typeVariable == null) {
            throw new IllegalArgumentException("typeVariable must not be null"); //$NON-NLS-1$
        }
        if (typeVariable.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("typeVariable must be a simple name-type");
        }
        NamedType named = (NamedType) typeVariable;
        if (named.getModelKind() != ModelKind.SIMPLE_NAME) {
            throw new IllegalArgumentException("typeVariable must have a simple name");
        }

        return typeParam((SimpleName) named.getName());
    }

    /**
     * <code>&#64;return</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @return 続きの操作を行うビルダー
     */
    public JavadocBuilder returns() {
        block("@return"); //$NON-NLS-1$
        return this;
    }

    /**
     * 指定の名前の型に関する<code>&#64;throw</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param type 例外の型
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder exception(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple name-type");
        }
        block("@throws"); //$NON-NLS-1$
        elements.add(((NamedType) type).getName());
        return this;
    }

    /**
     * 指定の名前の型に関する<code>&#64;see</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param type 型
     * @return 続きの操作を行うビルダー
     */
    public JavadocBuilder seeType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple name-type");
        }
        return see(((NamedType) type).getName());
    }

    /**
     * 指定のフィールドに関する<code>&#64;see</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param name フィールドの単純名
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder seeField(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return seeField(null, f.newSimpleName(name));
    }

    /**
     * 指定のフィールドに関する<code>&#64;see</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param type フィールドの型
     * @param name フィールドの単純名
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder seeField(Type type, String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return seeField(type, f.newSimpleName(name));
    }

    /**
     * 指定のフィールドに関する<code>&#64;see</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param name フィールドの単純名
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder seeField(SimpleName name) {
        return seeField(null, name);
    }

    /**
     * 指定のフィールドに関する<code>&#64;see</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param type フィールドの型
     * @param name フィールドの単純名
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder seeField(Type type, SimpleName name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return see(f.newDocField(type, name));
    }

    /**
     * 指定のメソッドに関する<code>&#64;see</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param name メソッドの単純名
     * @param parameterTypes メソッドの引数型一覧
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder seeMethod(
            String name,
            Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return seeMethod(
                null,
                f.newSimpleName(name),
                Arrays.asList(parameterTypes));
    }

    /**
     * 指定のメソッドに関する<code>&#64;see</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param name メソッドの単純名
     * @param parameterTypes メソッドの引数型一覧
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder seeMethod(
            String name,
            List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return seeMethod(null, f.newSimpleName(name), parameterTypes);
    }

    /**
     * 指定のメソッドに関する<code>&#64;see</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param name メソッドの単純名
     * @param parameterTypes メソッドの引数型一覧
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder seeMethod(
            SimpleName name,
            Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return seeMethod(null, name, Arrays.asList(parameterTypes));
    }

    /**
     * 指定のメソッドに関する<code>&#64;see</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param name メソッドの単純名
     * @param parameterTypes メソッドの引数型一覧
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder seeMethod(
            SimpleName name,
            List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return seeMethod(null, name, parameterTypes);
    }

    /**
     * 指定のメソッドに関する<code>&#64;see</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param type メソッドの型
     * @param name メソッドの単純名
     * @param parameterTypes メソッドの引数型一覧
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder seeMethod(
            Type type,
            String name,
            Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return seeMethod(
                type,
                f.newSimpleName(name),
                Arrays.asList(parameterTypes));
    }

    /**
     * 指定のメソッドに関する<code>&#64;see</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param type メソッドの型
     * @param name メソッドの単純名
     * @param parameterTypes メソッドの引数型一覧
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder seeMethod(
            Type type,
            String name,
            List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return seeMethod(type, f.newSimpleName(name), parameterTypes);
    }

    /**
     * 指定のメソッドに関する<code>&#64;see</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param type メソッドの型
     * @param name メソッドの単純名
     * @param parameterTypes メソッドの引数型一覧
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder seeMethod(
            Type type,
            SimpleName name,
            Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return seeMethod(type, name, Arrays.asList(parameterTypes));
    }

    /**
     * 指定のメソッドに関する<code>&#64;see</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param type メソッドの型
     * @param name メソッドの単純名
     * @param parameterTypes メソッドの引数型一覧
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder seeMethod(
            Type type,
            SimpleName name,
            List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        List<DocMethodParameter> parameters = new ArrayList<DocMethodParameter>();
        for (Type parameterType : parameterTypes) {
            parameters.add(f.newDocMethodParameter(parameterType, null, false));
        }
        return see(f.newDocMethod(type, name, parameters));
    }

    /**
     * 指定の要素に関する<code>&#64;see</code>タグブロックを開始する。
     * <p>
     * 直前までのブロックはこの操作によって終了する。
     * </p>
     * @param element 対象の要素
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder see(DocElement element) {
        if (element == null) {
            throw new IllegalArgumentException("element must not be null"); //$NON-NLS-1$
        }
        block("@see"); //$NON-NLS-1$
        elements.add(element);
        return this;
    }

    /**
     * 指定の内容のテキストを挿入する。
     * @param pattern {@link MessageFormat#format(String, Object...)}形式のパターン
     * @param arguments {@link MessageFormat#format(String, Object...)}形式の引数
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder text(String pattern, Object... arguments) {
        elements.add(escape(pattern, arguments));
        return this;
    }

    /**
     * 指定の内容の<code>&#64;code</code>インラインブロックを挿入する。
     * @param pattern {@link MessageFormat#format(String, Object...)}形式のパターン
     * @param arguments {@link MessageFormat#format(String, Object...)}形式の引数
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder code(String pattern, Object... arguments) {
        elements.add(f.newDocBlock(
                "@code", //$NON-NLS-1$
                Collections.singletonList(escape(pattern, arguments))));
        return this;
    }

    /**
     * 指定の名前の型に関する<code>&#64;link</code>インラインブロックを挿入する。
     * @param type 型
     * @return 続きの操作を行うビルダー
     */
    public JavadocBuilder linkType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalArgumentException("type must be a simple name-type");
        }
        return link(((NamedType) type).getName());
    }

    /**
     * 指定のフィールドに関する<code>&#64;link</code>インラインブロックを挿入する。
     * @param name フィールドの単純名
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder linkField(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return linkField(null, f.newSimpleName(name));
    }

    /**
     * 指定のフィールドに関する<code>&#64;link</code>インラインブロックを挿入する。
     * @param type フィールドの型
     * @param name フィールドの単純名
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder linkField(Type type, String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return linkField(type, f.newSimpleName(name));
    }

    /**
     * 指定のフィールドに関する<code>&#64;link</code>インラインブロックを挿入する。
     * @param name フィールドの単純名
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder linkField(SimpleName name) {
        return linkField(null, name);
    }

    /**
     * 指定のフィールドに関する<code>&#64;link</code>インラインブロックを挿入する。
     * @param type フィールドの型
     * @param name フィールドの単純名
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder linkField(Type type, SimpleName name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return link(f.newDocField(type, name));
    }

    /**
     * 指定のメソッドに関する<code>&#64;link</code>インラインブロックを挿入する。
     * @param name メソッドの単純名
     * @param parameterTypes メソッドの引数型一覧
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder linkMethod(
            String name,
            Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return linkMethod(
                null,
                f.newSimpleName(name),
                Arrays.asList(parameterTypes));
    }

    /**
     * 指定のメソッドに関する<code>&#64;link</code>インラインブロックを挿入する。
     * @param name メソッドの単純名
     * @param parameterTypes メソッドの引数型一覧
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder linkMethod(
            String name,
            List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return linkMethod(null, f.newSimpleName(name), parameterTypes);
    }

    /**
     * 指定のメソッドに関する<code>&#64;link</code>インラインブロックを挿入する。
     * @param name メソッドの単純名
     * @param parameterTypes メソッドの引数型一覧
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder linkMethod(
            SimpleName name,
            Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return linkMethod(null, name, Arrays.asList(parameterTypes));
    }

    /**
     * 指定のメソッドに関する<code>&#64;link</code>インラインブロックを挿入する。
     * @param name メソッドの単純名
     * @param parameterTypes メソッドの引数型一覧
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder linkMethod(
            SimpleName name,
            List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return linkMethod(null, name, parameterTypes);
    }

    /**
     * 指定のメソッドに関する<code>&#64;link</code>インラインブロックを挿入する。
     * @param type メソッドの型
     * @param name メソッドの単純名
     * @param parameterTypes メソッドの引数型一覧
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder linkMethod(
            Type type,
            String name,
            Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return linkMethod(
                type,
                f.newSimpleName(name),
                Arrays.asList(parameterTypes));
    }

    /**
     * 指定のメソッドに関する<code>&#64;link</code>インラインブロックを挿入する。
     * @param type メソッドの型
     * @param name メソッドの単純名
     * @param parameterTypes メソッドの引数型一覧
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder linkMethod(
            Type type,
            String name,
            List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return linkMethod(type, f.newSimpleName(name), parameterTypes);
    }

    /**
     * 指定のメソッドに関する<code>&#64;link</code>インラインブロックを挿入する。
     * @param type メソッドの型
     * @param name メソッドの単純名
     * @param parameterTypes メソッドの引数型一覧
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder linkMethod(
            Type type,
            SimpleName name,
            Type... parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        return linkMethod(type, name, Arrays.asList(parameterTypes));
    }

    /**
     * 指定のメソッドに関する<code>&#64;link</code>インラインブロックを挿入する。
     * @param type メソッドの型
     * @param name メソッドの単純名
     * @param parameterTypes メソッドの引数型一覧
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder linkMethod(
            Type type,
            SimpleName name,
            List<? extends Type> parameterTypes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (parameterTypes == null) {
            throw new IllegalArgumentException(
                "parameterTypes must not be null"); //$NON-NLS-1$
        }
        List<DocMethodParameter> parameters = new ArrayList<DocMethodParameter>();
        for (Type parameterType : parameterTypes) {
            parameters.add(f.newDocMethodParameter(parameterType, null, false));
        }
        return link(f.newDocMethod(type, name, parameters));
    }

    /**
     * 指定の要素に関する<code>&#64;link</code>インラインブロックを挿入する。
     * @param element 対象の要素
     * @return 続きの操作を行うビルダー
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocBuilder link(DocElement element) {
        if (element == null) {
            throw new IllegalArgumentException("element must not be null"); //$NON-NLS-1$
        }
        elements.add(f.newDocBlock(
            "@link", //$NON-NLS-1$
            Collections.singletonList(element)));
        return this;
    }

    private DocText escape(String pattern, Object... arguments) {
        String text = MessageFormat.format(pattern, arguments);
        String escaped = ESCAPE.matcher(text).replaceAll("&#64;"); //$NON-NLS-1$
        return f.newDocText(escaped);
    }

    private void flushBlock(String nextTag) {
        if (currentTag.length() >= 0 || elements.isEmpty() == false) {
            blocks.add(f.newDocBlock(currentTag, elements));
            elements.clear();
        }
        this.currentTag = nextTag;
    }
}
