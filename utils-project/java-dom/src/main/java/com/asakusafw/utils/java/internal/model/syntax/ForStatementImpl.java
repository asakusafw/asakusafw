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

import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ForInitializer;
import com.asakusafw.utils.java.model.syntax.ForStatement;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.StatementExpressionList;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link ForStatement}の実装。
 */
public final class ForStatementImpl extends ModelRoot implements ForStatement {

    /**
     * ループ初期化部。
     */
    private ForInitializer initialization;

    /**
     * ループ条件式。
     */
    private Expression condition;

    /**
     * ループ更新部。
     */
    private StatementExpressionList update;

    /**
     * ループ本体。
     */
    private Statement body;

    @Override
    public ForInitializer getInitialization() {
        return this.initialization;
    }

    /**
     * ループ初期化部を設定する。
     * <p> ループ初期化部が指定されない場合、引数には{@code null}を指定する。 </p>
     * @param initialization
     *     ループ初期化部、
     *     ただしループ初期化部が指定されない場合は{@code null}
     */
    public void setInitialization(ForInitializer initialization) {
        this.initialization = initialization;
    }

    @Override
    public Expression getCondition() {
        return this.condition;
    }

    /**
     * ループ条件式を設定する。
     * <p> ループ条件が指定されない場合、引数には{@code null}を指定する。 </p>
     * @param condition
     *     ループ条件式、
     *     ただしループ条件が指定されない場合は{@code null}
     */
    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    @Override
    public StatementExpressionList getUpdate() {
        return this.update;
    }

    /**
     * ループ更新部を設定する。
     * <p> ループ更新部が指定されない場合、引数には{@code null}を指定する。 </p>
     * @param update
     *     ループ更新部、
     *     ただしループ更新部が指定されない場合は{@code null}
     */
    public void setUpdate(StatementExpressionList update) {
        this.update = update;
    }

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

    /**
     * この要素の種類を表す{@link ModelKind#FOR_STATEMENT}を返す。
     * @return {@link ModelKind#FOR_STATEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.FOR_STATEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitForStatement(this, context);
    }
}
