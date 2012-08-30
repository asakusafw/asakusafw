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

import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldAccessExpression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link FieldAccessExpression}の実装。
 */
public final class FieldAccessExpressionImpl extends ModelRoot implements FieldAccessExpression {

    /**
     * 限定式。
     */
    private Expression qualifier;

    /**
     * フィールドの名前。
     */
    private SimpleName name;

    @Override
    public Expression getQualifier() {
        return this.qualifier;
    }

    /**
     * 限定式を設定する。
     * @param qualifier
     *     限定式
     * @throws IllegalArgumentException
     *     {@code qualifier}に{@code null}が指定された場合
     */
    public void setQualifier(Expression qualifier) {
        Util.notNull(qualifier, "qualifier"); //$NON-NLS-1$
        this.qualifier = qualifier;
    }

    @Override
    public SimpleName getName() {
        return this.name;
    }

    /**
     * フィールドの名前を設定する。
     * @param name
     *     フィールドの名前
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    public void setName(SimpleName name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    /**
     * この要素の種類を表す{@link ModelKind#FIELD_ACCESS_EXPRESSION}を返す。
     * @return {@link ModelKind#FIELD_ACCESS_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.FIELD_ACCESS_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitFieldAccessExpression(this, context);
    }
}
