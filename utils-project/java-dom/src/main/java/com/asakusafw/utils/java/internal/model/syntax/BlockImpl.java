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
package com.asakusafw.utils.java.internal.model.syntax;

import java.util.List;

import com.asakusafw.utils.java.model.syntax.Block;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link Block}の実装。
 */
public final class BlockImpl extends ModelRoot implements Block {

    /**
     * 文の一覧。
     */
    private List<? extends Statement> statements;

    @Override
    public List<? extends Statement> getStatements() {
        return this.statements;
    }

    /**
     * 文の一覧を設定する。
     * <p> 文が一つも宣言されない場合、引数には空を指定する。 </p>
     * @param statements
     *     文の一覧
     * @throws IllegalArgumentException
     *     {@code statements}に{@code null}が指定された場合
     */
    public void setStatements(List<? extends Statement> statements) {
        Util.notNull(statements, "statements"); //$NON-NLS-1$
        Util.notContainNull(statements, "statements"); //$NON-NLS-1$
        this.statements = Util.freeze(statements);
    }

    /**
     * この要素の種類を表す{@link ModelKind#BLOCK}を返す。
     * @return {@link ModelKind#BLOCK}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.BLOCK;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitBlock(this, context);
    }
}
