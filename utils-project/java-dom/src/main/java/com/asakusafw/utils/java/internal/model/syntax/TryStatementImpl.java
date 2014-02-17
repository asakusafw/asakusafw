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
package com.asakusafw.utils.java.internal.model.syntax;

import java.util.List;

import com.asakusafw.utils.java.model.syntax.Block;
import com.asakusafw.utils.java.model.syntax.CatchClause;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.TryStatement;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link TryStatement}の実装。
 */
public final class TryStatementImpl extends ModelRoot implements TryStatement {

    /**
     * {@code try}節。
     */
    private Block tryBlock;

    /**
     * {@code catch}節の一覧。
     */
    private List<? extends CatchClause> catchClauses;

    /**
     * {@code finally}節。
     */
    private Block finallyBlock;

    @Override
    public Block getTryBlock() {
        return this.tryBlock;
    }

    /**
     * {@code try}節を設定する。
     * @param tryBlock
     *     {@code try}節
     * @throws IllegalArgumentException
     *     {@code tryBlock}に{@code null}が指定された場合
     */
    public void setTryBlock(Block tryBlock) {
        Util.notNull(tryBlock, "tryBlock"); //$NON-NLS-1$
        this.tryBlock = tryBlock;
    }

    @Override
    public List<? extends CatchClause> getCatchClauses() {
        return this.catchClauses;
    }

    /**
     * {@code catch}節の一覧を設定する。
     * <p> {@code catch}節が一つも指定されない場合、引数には空を指定する。 </p>
     * @param catchClauses
     *     {@code catch}節の一覧
     * @throws IllegalArgumentException
     *     {@code catchClauses}に{@code null}が指定された場合
     */
    public void setCatchClauses(List<? extends CatchClause> catchClauses) {
        Util.notNull(catchClauses, "catchClauses"); //$NON-NLS-1$
        Util.notContainNull(catchClauses, "catchClauses"); //$NON-NLS-1$
        this.catchClauses = Util.freeze(catchClauses);
    }

    @Override
    public Block getFinallyBlock() {
        return this.finallyBlock;
    }

    /**
     * {@code finally}節を設定する。
     * <p> {@code finally}節が指定されない場合、引数には{@code null}を指定する。 </p>
     * @param finallyBlock
     *     {@code finally}節、
     *     ただし{@code finally}節が指定されない場合は{@code null}
     */
    public void setFinallyBlock(Block finallyBlock) {
        this.finallyBlock = finallyBlock;
    }

    /**
     * この要素の種類を表す{@link ModelKind#TRY_STATEMENT}を返す。
     * @return {@link ModelKind#TRY_STATEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.TRY_STATEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitTryStatement(this, context);
    }
}
