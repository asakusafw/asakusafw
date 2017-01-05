/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
 * An implementation of {@link ImportDeclaration}.
 */
public final class ImportDeclarationImpl extends ModelRoot implements ImportDeclaration {

    private ImportKind importKind;

    private Name name;

    @Override
    public ImportKind getImportKind() {
        return this.importKind;
    }

    /**
     * Sets the import kind.
     * @param importKind the import kind
     * @throws IllegalArgumentException if {@code importKind} was {@code null}
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
     * Sets the import target type or member name.
     * @param name the import target type or member name
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    public void setName(Name name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    /**
     * Returns {@link ModelKind#IMPORT_DECLARATION} which represents this element kind.
     * @return {@link ModelKind#IMPORT_DECLARATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.IMPORT_DECLARATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitImportDeclaration(this, context);
    }
}
