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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

/**
 * Javadocに含まれる要素。
 */
public interface IrDocElement {

    /**
     * この要素の種類を返す。
     * @return この要素の種類
     */
    IrDocElementKind getKind();

    /**
     * この要素が配置されているレンジを返す。
     * 未設定の場合、この呼び出しは{@code null}を返す。
     * @return この要素が配置されているレンジ、未設定の場合は{@code null}
     */
    IrLocation getLocation();

    /**
     * この要素が配置されているレンジを設定する。
     * @param location 設定するレンジ、{@code null}を指定すると未設定になる
     */
    void setLocation(IrLocation location);

    /**
     * ビジタを受け入れ、要素の種類に合ったビジタ上のメソッドを呼び出す。
     * @param <R> 戻り値の型
     * @param <P> 引数の型
     * @param visitor ビジタ
     * @param context コンテキストオブジェクト
     * @return ビジタの実行結果
     */
    <R, P> R accept(IrDocElementVisitor<R, P> visitor, P context);
}
