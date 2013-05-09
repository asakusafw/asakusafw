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
package com.asakusafw.utils.java.model.syntax;

/**
 * ワイルドカードが持つ型境界の種類。
 */
public enum WildcardBoundKind {

    /**
     * バウンドを持たない。
     */
    UNBOUNDED(""), //$NON-NLS-1$

    /**
     * 上界を持つ。
     */
    UPPER_BOUNDED("extends"), //$NON-NLS-1$

    /**
     * 下界を持つ。
     */
    LOWER_BOUNDED("super"), //$NON-NLS-1$
    ;

    private final String representation;

    /**
     * インスタンスを生成する。
     * @param representation 表記
     */
    private WildcardBoundKind(String representation) {
        assert representation != null;
        this.representation = representation;
    }

    /**
     * この要素の表記を返す。
     * @return この要素の表記
     */
    public String getRepresentation() {
        return representation;
    }

    /**
     * この種類を正規化する。
     * {@link #UNBOUNDED}は{@link #UPPER_BOUNDED}に変換され、
     * それ以外は自身を返す。
     * @return 正規化した定数
     */
    public WildcardBoundKind normalize() {
        return this == UNBOUNDED ? UPPER_BOUNDED : this;
    }
}
