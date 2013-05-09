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

import com.asakusafw.utils.java.model.syntax.ClassBody;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link ClassBody}の実装。
 */
public final class ClassBodyImpl extends ModelRoot implements ClassBody {

    /**
     * メンバの一覧。
     */
    private List<? extends TypeBodyDeclaration> bodyDeclarations;

    @Override
    public List<? extends TypeBodyDeclaration> getBodyDeclarations() {
        return this.bodyDeclarations;
    }

    /**
     * メンバの一覧を設定する。
     * <p> メンバが一つも宣言されない場合、引数には空を指定する。 </p>
     * @param bodyDeclarations
     *     メンバの一覧
     * @throws IllegalArgumentException
     *     {@code bodyDeclarations}に{@code null}が指定された場合
     */
    public void setBodyDeclarations(List<? extends TypeBodyDeclaration> bodyDeclarations) {
        Util.notNull(bodyDeclarations, "bodyDeclarations"); //$NON-NLS-1$
        Util.notContainNull(bodyDeclarations, "bodyDeclarations"); //$NON-NLS-1$
        this.bodyDeclarations = Util.freeze(bodyDeclarations);
    }

    /**
     * この要素の種類を表す{@link ModelKind#CLASS_BODY}を返す。
     * @return {@link ModelKind#CLASS_BODY}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.CLASS_BODY;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitClassBody(this, context);
    }
}
