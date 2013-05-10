/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

/**
 * {@link IrDocElement}を走査するビジタ。
 * @param <R> 戻り値の型
 * @param <P> 引数の型
 */
public abstract class IrDocElementVisitor<R, P> {

    /**
     * {@link IrDocComment}の{@link IrDocElement#accept(IrDocElementVisitor, Object)}から呼び出される。
     * @param elem ビジタを受け入れた要素
     * @param context コンテキストオブジェクト
     * @return 処理結果
     */
    public R visitComment(IrDocComment elem, P context) {
        return null;
    }

    /**
     * {@link IrDocBlock}の{@link IrDocElement#accept(IrDocElementVisitor, Object)}から呼び出される。
     * @param elem ビジタを受け入れた要素
     * @param context コンテキストオブジェクト
     * @return 処理結果
     */
    public R visitBlock(IrDocBlock elem, P context) {
        return null;
    }

    /**
     * {@link IrDocSimpleName}の{@link IrDocElement#accept(IrDocElementVisitor, Object)}から呼び出される。
     * @param elem ビジタを受け入れた要素
     * @param context コンテキストオブジェクト
     * @return 処理結果
     */
    public R visitSimpleName(IrDocSimpleName elem, P context) {
        return null;
    }

    /**
     * {@link IrDocQualifiedName}の{@link IrDocElement#accept(IrDocElementVisitor, Object)}から呼び出される。
     * @param elem ビジタを受け入れた要素
     * @param context コンテキストオブジェクト
     * @return 処理結果
     */
    public R visitQualifiedName(IrDocQualifiedName elem, P context) {
        return null;
    }

    /**
     * {@link IrDocField}の{@link IrDocElement#accept(IrDocElementVisitor, Object)}から呼び出される。
     * @param elem ビジタを受け入れた要素
     * @param context コンテキストオブジェクト
     * @return 処理結果
     */
    public R visitField(IrDocField elem, P context) {
        return null;
    }

    /**
     * {@link IrDocMethod}の{@link IrDocElement#accept(IrDocElementVisitor, Object)}から呼び出される。
     * @param elem ビジタを受け入れた要素
     * @param context コンテキストオブジェクト
     * @return 処理結果
     */
    public R visitMethod(IrDocMethod elem, P context) {
        return null;
    }

    /**
     * {@link IrDocText}の{@link IrDocElement#accept(IrDocElementVisitor, Object)}から呼び出される。
     * @param elem ビジタを受け入れた要素
     * @param context コンテキストオブジェクト
     * @return 処理結果
     */
    public R visitText(IrDocText elem, P context) {
        return null;
    }

    /**
     * {@link IrDocMethodParameter}の{@link IrDocElement#accept(IrDocElementVisitor, Object)}から呼び出される。
     * @param elem ビジタを受け入れた要素
     * @param context コンテキストオブジェクト
     * @return 処理結果
     */
    public R visitMethodParameter(IrDocMethodParameter elem, P context) {
        return null;
    }

    /**
     * {@link IrDocBasicType}の{@link IrDocElement#accept(IrDocElementVisitor, Object)}から呼び出される。
     * @param elem ビジタを受け入れた要素
     * @param context コンテキストオブジェクト
     * @return 処理結果
     */
    public R visitBasicType(IrDocBasicType elem, P context) {
        return null;
    }

    /**
     * {@link IrDocNamedType}の{@link IrDocElement#accept(IrDocElementVisitor, Object)}から呼び出される。
     * @param elem ビジタを受け入れた要素
     * @param context コンテキストオブジェクト
     * @return 処理結果
     */
    public R visitNamedType(IrDocNamedType elem, P context) {
        return null;
    }

    /**
     * {@link IrDocArrayType}の{@link IrDocElement#accept(IrDocElementVisitor, Object)}から呼び出される。
     * @param elem ビジタを受け入れた要素
     * @param context コンテキストオブジェクト
     * @return 処理結果
     */
    public R visitArrayType(IrDocArrayType elem, P context) {
        return null;
    }
}
