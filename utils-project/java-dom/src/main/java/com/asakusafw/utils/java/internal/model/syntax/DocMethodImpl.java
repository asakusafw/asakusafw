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

import java.util.List;

import com.asakusafw.utils.java.model.syntax.DocMethod;
import com.asakusafw.utils.java.model.syntax.DocMethodParameter;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link DocMethod}の実装。
 */
public final class DocMethodImpl extends ModelRoot implements DocMethod {

    /**
     * メソッドまたはコンストラクタの宣言型。
     */
    private Type type;

    /**
     * メソッドまたはコンストラクタの名前。
     */
    private SimpleName name;

    /**
     * メソッドまたはコンストラクタの仮引数宣言の一覧。
     */
    private List<? extends DocMethodParameter> formalParameters;

    @Override
    public Type getType() {
        return this.type;
    }

    /**
     * メソッドまたはコンストラクタの宣言型を設定する。
     * <p> 宣言型が指定されない場合、引数には{@code null}を指定する。 </p>
     * @param type
     *     メソッドまたはコンストラクタの宣言型、
     *     ただし宣言型が指定されない場合は{@code null}
     */
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public SimpleName getName() {
        return this.name;
    }

    /**
     * メソッドまたはコンストラクタの名前を設定する。
     * @param name
     *     メソッドまたはコンストラクタの名前
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    public void setName(SimpleName name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    @Override
    public List<? extends DocMethodParameter> getFormalParameters() {
        return this.formalParameters;
    }

    /**
     * メソッドまたはコンストラクタの仮引数宣言の一覧を設定する。
     * <p> 仮引数が一つも宣言されない場合、引数には空を指定する。 </p>
     * @param formalParameters
     *     メソッドまたはコンストラクタの仮引数宣言の一覧
     * @throws IllegalArgumentException
     *     {@code formalParameters}に{@code null}が指定された場合
     */
    public void setFormalParameters(List<? extends DocMethodParameter> formalParameters) {
        Util.notNull(formalParameters, "formalParameters"); //$NON-NLS-1$
        Util.notContainNull(formalParameters, "formalParameters"); //$NON-NLS-1$
        this.formalParameters = Util.freeze(formalParameters);
    }

    /**
     * この要素の種類を表す{@link ModelKind#DOC_METHOD}を返す。
     * @return {@link ModelKind#DOC_METHOD}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.DOC_METHOD;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitDocMethod(this, context);
    }
}
