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

import com.asakusafw.utils.java.model.syntax.DoStatement;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link DoStatement}の実装。
 */
public final class DoStatementImpl extends ModelRoot implements DoStatement {

    /**
     * ループ本体。
     */
    private Statement body;

    /**
     * 条件式。
     */
    private Expression condition;

    @Override
    public Statement getBody() {
        return this.body;
    }

    /**
     * ループ本体を設定する。
     * @param body
     *     ループ本体
     * @throws IllegalArgumentException
     *     {@code body}に{@code null}が指定された場合
     */
    public void setBody(Statement body) {
        Util.notNull(body, "body"); //$NON-NLS-1$
        this.body = body;
    }

    @Override
    public Expression getCondition() {
        return this.condition;
    }

    /**
     * 条件式を設定する。
     * @param condition
     *     条件式
     * @throws IllegalArgumentException
     *     {@code condition}に{@code null}が指定された場合
     */
    public void setCondition(Expression condition) {
        Util.notNull(condition, "condition"); //$NON-NLS-1$
        this.condition = condition;
    }

    /**
     * この要素の種類を表す{@link ModelKind#DO_STATEMENT}を返す。
     * @return {@link ModelKind#DO_STATEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.DO_STATEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitDoStatement(this, context);
    }
}
