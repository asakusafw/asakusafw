/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import com.asakusafw.utils.java.model.syntax.DocMethodParameter;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link DocMethodParameter}の実装。
 */
public final class DocMethodParameterImpl extends ModelRoot implements DocMethodParameter {

    /**
     * 仮引数の型。
     */
    private Type type;

    /**
     * 仮引数の名前。
     */
    private SimpleName name;

    /**
     * 可変長引数。
     */
    private boolean variableArity;

    @Override
    public Type getType() {
        return this.type;
    }

    /**
     * 仮引数の型を設定する。
     * @param type
     *     仮引数の型
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     */
    public void setType(Type type) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        this.type = type;
    }

    @Override
    public SimpleName getName() {
        return this.name;
    }

    /**
     * 仮引数の名前を設定する。
     * <p> 仮引数の名前が省略される場合、引数には{@code null}を指定する。 </p>
     * @param name
     *     仮引数の名前、
     *     ただし仮引数の名前が省略される場合は{@code null}
     */
    public void setName(SimpleName name) {
        this.name = name;
    }

    @Override
    public boolean isVariableArity() {
        return this.variableArity;
    }

    /**
     * 可変長引数である場合に{@code true}を設定する。
     * <p> そうでない場合、{@code false}を設定する。 </p>
     * @param variableArity
     *     可変長引数
     */
    public void setVariableArity(boolean variableArity) {
        this.variableArity = variableArity;
    }

    /**
     * この要素の種類を表す{@link ModelKind#DOC_METHOD_PARAMETER}を返す。
     * @return {@link ModelKind#DOC_METHOD_PARAMETER}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.DOC_METHOD_PARAMETER;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitDocMethodParameter(this, context);
    }
}
