/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import com.asakusafw.utils.java.model.syntax.ImportDeclaration;
import com.asakusafw.utils.java.model.syntax.ImportKind;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link ImportDeclaration}の実装。
 */
public final class ImportDeclarationImpl extends ModelRoot implements ImportDeclaration {

    /**
     * インポートの種類。
     */
    private ImportKind importKind;

    /**
     * インポートする型およびメンバの名前。
     */
    private Name name;

    @Override
    public ImportKind getImportKind() {
        return this.importKind;
    }

    /**
     * インポートの種類を設定する。
     * @param importKind
     *     インポートの種類
     * @throws IllegalArgumentException
     *     {@code importKind}に{@code null}が指定された場合
     */
    public void setImportKind(ImportKind importKind) {
        Util.notNull(importKind, "importKind"); //$NON-NLS-1$
        this.importKind = importKind;
    }

    @Override
    public Name getName() {
        return this.name;
    }

    /**
     * インポートする型およびメンバの名前を設定する。
     * @param name
     *     インポートする型およびメンバの名前
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    public void setName(Name name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    /**
     * この要素の種類を表す{@link ModelKind#IMPORT_DECLARATION}を返す。
     * @return {@link ModelKind#IMPORT_DECLARATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.IMPORT_DECLARATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitImportDeclaration(this, context);
    }
}
