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

import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.MethodInvocationExpression;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link MethodInvocationExpression}の実装。
 */
public final class MethodInvocationExpressionImpl extends ModelRoot implements MethodInvocationExpression {

    /**
     * 限定式、または型限定子。
     */
    private Expression qualifier;

    /**
     * 型引数の一覧。
     */
    private List<? extends Type> typeArguments;

    /**
     * メソッドの名前。
     */
    private SimpleName name;

    /**
     * 実引数の一覧。
     */
    private List<? extends Expression> arguments;

    @Override
    public Expression getQualifier() {
        return this.qualifier;
    }

    /**
     * 限定式、または型限定子を設定する。
     * <p> 限定式が指定されない場合(単純メソッド起動)、引数には{@code null}を指定する。 </p>
     * @param qualifier
     *     限定式、または型限定子、
     *     ただし限定式が指定されない場合(単純メソッド起動)は{@code null}
     */
    public void setQualifier(Expression qualifier) {
        this.qualifier = qualifier;
    }

    @Override
    public List<? extends Type> getTypeArguments() {
        return this.typeArguments;
    }

    /**
     * 型引数の一覧を設定する。
     * <p> 型引数が一つも指定されない場合、引数には空を指定する。 </p>
     * @param typeArguments
     *     型引数の一覧
     * @throws IllegalArgumentException
     *     {@code typeArguments}に{@code null}が指定された場合
     */
    public void setTypeArguments(List<? extends Type> typeArguments) {
        Util.notNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        Util.notContainNull(typeArguments, "typeArguments"); //$NON-NLS-1$
        this.typeArguments = Util.freeze(typeArguments);
    }

    @Override
    public SimpleName getName() {
        return this.name;
    }

    /**
     * メソッドの名前を設定する。
     * @param name
     *     メソッドの名前
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    public void setName(SimpleName name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    @Override
    public List<? extends Expression> getArguments() {
        return this.arguments;
    }

    /**
     * 実引数の一覧を設定する。
     * <p> 実引数が一つも指定されない場合、引数には空を指定する。 </p>
     * @param arguments
     *     実引数の一覧
     * @throws IllegalArgumentException
     *     {@code arguments}に{@code null}が指定された場合
     */
    public void setArguments(List<? extends Expression> arguments) {
        Util.notNull(arguments, "arguments"); //$NON-NLS-1$
        Util.notContainNull(arguments, "arguments"); //$NON-NLS-1$
        this.arguments = Util.freeze(arguments);
    }

    /**
     * この要素の種類を表す{@link ModelKind#METHOD_INVOCATION_EXPRESSION}を返す。
     * @return {@link ModelKind#METHOD_INVOCATION_EXPRESSION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.METHOD_INVOCATION_EXPRESSION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitMethodInvocationExpression(this, context);
    }
}
