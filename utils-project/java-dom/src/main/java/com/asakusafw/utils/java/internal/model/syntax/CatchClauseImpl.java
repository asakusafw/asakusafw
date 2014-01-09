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

import com.asakusafw.utils.java.model.syntax.Block;
import com.asakusafw.utils.java.model.syntax.CatchClause;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link CatchClause}の実装。
 */
public final class CatchClauseImpl extends ModelRoot implements CatchClause {

    /**
     * 例外仮引数。
     */
    private FormalParameterDeclaration parameter;

    /**
     * {@code catch}ブロック。
     */
    private Block body;

    @Override
    public FormalParameterDeclaration getParameter() {
        return this.parameter;
    }

    /**
     * 例外仮引数を設定する。
     * @param parameter
     *     例外仮引数
     * @throws IllegalArgumentException
     *     {@code parameter}に{@code null}が指定された場合
     */
    public void setParameter(FormalParameterDeclaration parameter) {
        Util.notNull(parameter, "parameter"); //$NON-NLS-1$
        this.parameter = parameter;
    }

    @Override
    public Block getBody() {
        return this.body;
    }

    /**
     * {@code catch}ブロックを設定する。
     * @param body
     *     {@code catch}ブロック
     * @throws IllegalArgumentException
     *     {@code body}に{@code null}が指定された場合
     */
    public void setBody(Block body) {
        Util.notNull(body, "body"); //$NON-NLS-1$
        this.body = body;
    }

    /**
     * この要素の種類を表す{@link ModelKind#CATCH_CLAUSE}を返す。
     * @return {@link ModelKind#CATCH_CLAUSE}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.CATCH_CLAUSE;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitCatchClause(this, context);
    }
}
