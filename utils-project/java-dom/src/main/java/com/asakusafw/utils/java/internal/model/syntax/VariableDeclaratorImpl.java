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
package com.asakusafw.utils.java.internal.model.syntax;

import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.VariableDeclarator;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link VariableDeclarator}の実装。
 */
public final class VariableDeclaratorImpl extends ModelRoot implements VariableDeclarator {

    /**
     * 変数の名前。
     */
    private SimpleName name;

    /**
     * 追加次元数の宣言。
     */
    private int extraDimensions;

    /**
     * 初期化式。
     */
    private Expression initializer;

    @Override
    public SimpleName getName() {
        return this.name;
    }

    /**
     * 変数の名前を設定する。
     * @param name
     *     変数の名前
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    public void setName(SimpleName name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    @Override
    public int getExtraDimensions() {
        return this.extraDimensions;
    }

    /**
     * 追加次元数の宣言を設定する。
     * @param extraDimensions
     *     追加次元数の宣言
     * @throws IllegalArgumentException
     *     {@code extraDimensions}に負の値が指定された場合
     */
    public void setExtraDimensions(int extraDimensions) {
        this.extraDimensions = extraDimensions;
    }

    @Override
    public Expression getInitializer() {
        return this.initializer;
    }

    /**
     * 初期化式を設定する。
     * <p> 初期化式が指定されない場合、引数には{@code null}を指定する。 </p>
     * @param initializer
     *     初期化式、
     *     ただし初期化式が指定されない場合は{@code null}
     */
    public void setInitializer(Expression initializer) {
        this.initializer = initializer;
    }

    /**
     * この要素の種類を表す{@link ModelKind#VARIABLE_DECLARATOR}を返す。
     * @return {@link ModelKind#VARIABLE_DECLARATOR}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.VARIABLE_DECLARATOR;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitVariableDeclarator(this, context);
    }
}
