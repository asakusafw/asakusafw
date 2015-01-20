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

import com.asakusafw.utils.java.model.syntax.DocField;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link DocField}の実装。
 */
public final class DocFieldImpl extends ModelRoot implements DocField {

    /**
     * フィールドを宣言した型。
     */
    private Type type;

    /**
     * フィールドの名称。
     */
    private SimpleName name;

    @Override
    public Type getType() {
        return this.type;
    }

    /**
     * フィールドを宣言した型を設定する。
     * <p> 宣言型が指定されない場合、引数には{@code null}を指定する。 </p>
     * @param type
     *     フィールドを宣言した型、
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
     * フィールドの名称を設定する。
     * @param name
     *     フィールドの名称
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    public void setName(SimpleName name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    /**
     * この要素の種類を表す{@link ModelKind#DOC_FIELD}を返す。
     * @return {@link ModelKind#DOC_FIELD}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.DOC_FIELD;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitDocField(this, context);
    }
}
