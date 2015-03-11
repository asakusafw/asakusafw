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
package com.asakusafw.utils.java.internal.model.syntax;

import java.util.List;

import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.StatementExpressionList;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link StatementExpressionList}の実装。
 */
public final class StatementExpressionListImpl extends ModelRoot implements StatementExpressionList {

    /**
     * 式の一覧。
     */
    private List<? extends Expression> expressions;

    @Override
    public List<? extends Expression> getExpressions() {
        return this.expressions;
    }

    /**
     * 式の一覧を設定する。
     * @param expressions
     *     式の一覧
     * @throws IllegalArgumentException
     *     {@code expressions}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code expressions}に空が指定された場合
     */
    public void setExpressions(List<? extends Expression> expressions) {
        Util.notNull(expressions, "expressions"); //$NON-NLS-1$
        Util.notContainNull(expressions, "expressions"); //$NON-NLS-1$
        Util.notEmpty(expressions, "expressions"); //$NON-NLS-1$
        this.expressions = Util.freeze(expressions);
    }

    /**
     * この要素の種類を表す{@link ModelKind#STATEMENT_EXPRESSION_LIST}を返す。
     * @return {@link ModelKind#STATEMENT_EXPRESSION_LIST}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.STATEMENT_EXPRESSION_LIST;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitStatementExpressionList(this, context);
    }
}
