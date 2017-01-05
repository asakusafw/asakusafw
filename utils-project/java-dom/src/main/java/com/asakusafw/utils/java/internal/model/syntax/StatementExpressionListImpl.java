/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
 * An implementation of {@link StatementExpressionList}.
 */
public final class StatementExpressionListImpl extends ModelRoot implements StatementExpressionList {

    private List<? extends Expression> expressions;

    @Override
    public List<? extends Expression> getExpressions() {
        return this.expressions;
    }

    /**
     * Sets the expression list.
     * @param expressions the expression list
     * @throws IllegalArgumentException if {@code expressions} was {@code null}
     * @throws IllegalArgumentException if {@code expressions} was empty
     */
    public void setExpressions(List<? extends Expression> expressions) {
        Util.notNull(expressions, "expressions"); //$NON-NLS-1$
        Util.notContainNull(expressions, "expressions"); //$NON-NLS-1$
        Util.notEmpty(expressions, "expressions"); //$NON-NLS-1$
        this.expressions = Util.freeze(expressions);
    }

    /**
     * Returns {@link ModelKind#STATEMENT_EXPRESSION_LIST} which represents this element kind.
     * @return {@link ModelKind#STATEMENT_EXPRESSION_LIST}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.STATEMENT_EXPRESSION_LIST;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitStatementExpressionList(this, context);
    }
}
