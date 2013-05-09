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

import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.LocalClassDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link LocalClassDeclaration}の実装。
 */
public final class LocalClassDeclarationImpl extends ModelRoot implements LocalClassDeclaration {

    /**
     * 宣言するクラス。
     */
    private ClassDeclaration declaration;

    @Override
    public ClassDeclaration getDeclaration() {
        return this.declaration;
    }

    /**
     * 宣言するクラスを設定する。
     * @param declaration
     *     宣言するクラス
     * @throws IllegalArgumentException
     *     {@code declaration}に{@code null}が指定された場合
     */
    public void setDeclaration(ClassDeclaration declaration) {
        Util.notNull(declaration, "declaration"); //$NON-NLS-1$
        this.declaration = declaration;
    }

    /**
     * この要素の種類を表す{@link ModelKind#LOCAL_CLASS_DECLARATION}を返す。
     * @return {@link ModelKind#LOCAL_CLASS_DECLARATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.LOCAL_CLASS_DECLARATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitLocalClassDeclaration(this, context);
    }
}
