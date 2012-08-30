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

import com.asakusafw.utils.java.model.syntax.LabeledStatement;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link LabeledStatement}の実装。
 */
public final class LabeledStatementImpl extends ModelRoot implements LabeledStatement {

    /**
     * ラベルの名前。
     */
    private SimpleName label;

    /**
     * 対象の文。
     */
    private Statement body;

    @Override
    public SimpleName getLabel() {
        return this.label;
    }

    /**
     * ラベルの名前を設定する。
     * @param label
     *     ラベルの名前
     * @throws IllegalArgumentException
     *     {@code label}に{@code null}が指定された場合
     */
    public void setLabel(SimpleName label) {
        Util.notNull(label, "label"); //$NON-NLS-1$
        this.label = label;
    }

    @Override
    public Statement getBody() {
        return this.body;
    }

    /**
     * 対象の文を設定する。
     * @param body
     *     対象の文
     * @throws IllegalArgumentException
     *     {@code body}に{@code null}が指定された場合
     */
    public void setBody(Statement body) {
        Util.notNull(body, "body"); //$NON-NLS-1$
        this.body = body;
    }

    /**
     * この要素の種類を表す{@link ModelKind#LABELED_STATEMENT}を返す。
     * @return {@link ModelKind#LABELED_STATEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.LABELED_STATEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitLabeledStatement(this, context);
    }
}
