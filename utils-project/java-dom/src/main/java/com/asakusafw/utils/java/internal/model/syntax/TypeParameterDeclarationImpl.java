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

import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link TypeParameterDeclaration}の実装。
 */
public final class TypeParameterDeclarationImpl extends ModelRoot implements TypeParameterDeclaration {

    /**
     * 型引数の名前。
     */
    private SimpleName name;

    /**
     * 境界型の一覧。
     */
    private List<? extends Type> typeBounds;

    @Override
    public SimpleName getName() {
        return this.name;
    }

    /**
     * 型引数の名前を設定する。
     * @param name
     *     型引数の名前
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    public void setName(SimpleName name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    @Override
    public List<? extends Type> getTypeBounds() {
        return this.typeBounds;
    }

    /**
     * 境界型の一覧を設定する。
     * <p> 境界型が一つも指定されない場合、引数には空を指定する。 </p>
     * @param typeBounds
     *     境界型の一覧
     * @throws IllegalArgumentException
     *     {@code typeBounds}に{@code null}が指定された場合
     */
    public void setTypeBounds(List<? extends Type> typeBounds) {
        Util.notNull(typeBounds, "typeBounds"); //$NON-NLS-1$
        Util.notContainNull(typeBounds, "typeBounds"); //$NON-NLS-1$
        this.typeBounds = Util.freeze(typeBounds);
    }

    /**
     * この要素の種類を表す{@link ModelKind#TYPE_PARAMETER_DECLARATION}を返す。
     * @return {@link ModelKind#TYPE_PARAMETER_DECLARATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.TYPE_PARAMETER_DECLARATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitTypeParameterDeclaration(this, context);
    }
}
