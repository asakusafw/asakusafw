/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.vocabulary.flow.graph.ShuffleKey;
import com.asakusafw.utils.java.model.syntax.DocElement;

/**
 * 演算子メソッドが利用する変数。
 */
public class OperatorPortDeclaration {

    private Kind kind;

    private List<DocElement> documentation;

    private String name;

    private PortTypeDescription type;

    private Integer position;

    private ShuffleKey shuffleKey;

    /**
     * インスタンスを生成する。
     * @param kind 変数の種類
     * @param documentation 変数に対するドキュメンテーション
     * @param name 変数の名前
     * @param type 変数の型
     * @param position パラメーターの位置、パラメーターで宣言されていない場合は{@code null}
     * @param shuffleKey シャッフル条件、存在しない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public OperatorPortDeclaration(
            Kind kind,
            List<? extends DocElement> documentation,
            String name,
            PortTypeDescription type,
            Integer position,
            ShuffleKey shuffleKey) {
        Precondition.checkMustNotBeNull(kind, "kind"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(documentation, "documentation"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        this.kind = kind;
        this.documentation = Collections.unmodifiableList(new ArrayList<DocElement>(documentation));
        this.name = name;
        this.type = type;
        this.position = position;
        this.shuffleKey = shuffleKey;
    }

    /**
     * このポートのパラメーターでの位置を返す。
     * @return このポートのパラメーターでの位置、パラメーターから導出されていない場合は{@code null}
     */
    public Integer getParameterPosition() {
        return position;
    }

    /**
     * 変数の種類を返す。
     * @return 変数の種類
     */
    public Kind getKind() {
        return this.kind;
    }

    /**
     * 変数に対するドキュメンテーションを返す。
     * @return 変数に対するドキュメンテーション、存在しない場合は{@code null}
     */
    public List<DocElement> getDocumentation() {
        return this.documentation;
    }

    /**
     * 変数の名前を返す。
     * @return 変数の名前
     */
    public String getName() {
        return this.name;
    }

    /**
     * ポートの型を返す。
     * @return ポートの型
     */
    public PortTypeDescription getType() {
        return this.type;
    }

    /**
     * 変数に関連するキー情報を返す。
     * @return 変数に関連するキー情報、存在しない場合は{@code null}
     */
    public ShuffleKey getShuffleKey() {
        return shuffleKey;
    }

    /**
     * 変数の種類。
     */
    public enum Kind {

        /**
         * 入力。
         */
        INPUT,

        /**
         * 出力。
         */
        OUTPUT,

        /**
         * 定数の定義。
         */
        CONSTANT,
    }
}
