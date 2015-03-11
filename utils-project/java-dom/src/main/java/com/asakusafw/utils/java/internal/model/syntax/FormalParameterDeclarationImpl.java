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

import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link FormalParameterDeclaration}の実装。
 */
public final class FormalParameterDeclarationImpl extends ModelRoot implements FormalParameterDeclaration {

    /**
     * 修飾子および注釈の一覧。
     */
    private List<? extends Attribute> modifiers;

    /**
     * 宣言する変数の型。
     */
    private Type type;

    /**
     * 可変長引数。
     */
    private boolean variableArity;

    /**
     * 仮引数の名前。
     */
    private SimpleName name;

    /**
     * 追加次元数の宣言。
     */
    private int extraDimensions;

    @Override
    public List<? extends Attribute> getModifiers() {
        return this.modifiers;
    }

    /**
     * 修飾子および注釈の一覧を設定する。
     * <p> 修飾子または注釈が一つも宣言されない場合、引数には空を指定する。 </p>
     * @param modifiers
     *     修飾子および注釈の一覧
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     */
    public void setModifiers(List<? extends Attribute> modifiers) {
        Util.notNull(modifiers, "modifiers"); //$NON-NLS-1$
        Util.notContainNull(modifiers, "modifiers"); //$NON-NLS-1$
        this.modifiers = Util.freeze(modifiers);
    }

    @Override
    public Type getType() {
        return this.type;
    }

    /**
     * 宣言する変数の型を設定する。
     * @param type
     *     宣言する変数の型
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     */
    public void setType(Type type) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        this.type = type;
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

    @Override
    public SimpleName getName() {
        return this.name;
    }

    /**
     * 仮引数の名前を設定する。
     * @param name
     *     仮引数の名前
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    public void setName(SimpleName name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    @Override
    public int getExtraDimensions() {
        return this.extraDimensions;
    }

    /**
     * 追加次元数の宣言を設定する。
     * @param extraDimensions
     *     追加次元数の宣言
     * @throws IllegalArgumentException
     *     {@code extraDimensions}に負の値が指定された場合
     */
    public void setExtraDimensions(int extraDimensions) {
        this.extraDimensions = extraDimensions;
    }

    /**
     * この要素の種類を表す{@link ModelKind#FORMAL_PARAMETER_DECLARATION}を返す。
     * @return {@link ModelKind#FORMAL_PARAMETER_DECLARATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.FORMAL_PARAMETER_DECLARATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitFormalParameterDeclaration(this, context);
    }
}
