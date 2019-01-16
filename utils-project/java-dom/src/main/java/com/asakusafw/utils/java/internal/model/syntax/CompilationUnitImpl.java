/**
 * Copyright 2011-2019 Asakusa Framework Team.
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

import com.asakusafw.utils.java.model.syntax.Comment;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.ImportDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.PackageDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link CompilationUnit}.
 */
public final class CompilationUnitImpl extends ModelRoot implements CompilationUnit {

    private PackageDeclaration packageDeclaration;

    private List<? extends ImportDeclaration> importDeclarations;

    private List<? extends TypeDeclaration> typeDeclarations;

    private List<? extends Comment> comments;

    @Override
    public PackageDeclaration getPackageDeclaration() {
        return this.packageDeclaration;
    }

    /**
     * Sets the package declaration.
     * @param packageDeclaration the package declaration, or {@code null} for the default package
     */
    public void setPackageDeclaration(PackageDeclaration packageDeclaration) {
        this.packageDeclaration = packageDeclaration;
    }

    @Override
    public List<? extends ImportDeclaration> getImportDeclarations() {
        return this.importDeclarations;
    }

    /**
     * Sets the import declarations.
     * @param importDeclarations the import declarations
     * @throws IllegalArgumentException if {@code importDeclarations} was {@code null}
     */
    public void setImportDeclarations(List<? extends ImportDeclaration> importDeclarations) {
        Util.notNull(importDeclarations, "importDeclarations"); //$NON-NLS-1$
        Util.notContainNull(importDeclarations, "importDeclarations"); //$NON-NLS-1$
        this.importDeclarations = Util.freeze(importDeclarations);
    }

    @Override
    public List<? extends TypeDeclaration> getTypeDeclarations() {
        return this.typeDeclarations;
    }

    /**
     * Sets the type declarations.
     * @param typeDeclarations the type declarations
     * @throws IllegalArgumentException if {@code typeDeclarations} was {@code null}
     */
    public void setTypeDeclarations(List<? extends TypeDeclaration> typeDeclarations) {
        Util.notNull(typeDeclarations, "typeDeclarations"); //$NON-NLS-1$
        Util.notContainNull(typeDeclarations, "typeDeclarations"); //$NON-NLS-1$
        this.typeDeclarations = Util.freeze(typeDeclarations);
    }

    @Override
    public List<? extends Comment> getComments() {
        return this.comments;
    }

    /**
     * Sets the comments.
     * @param comments the comments
     * @throws IllegalArgumentException if {@code comments} was {@code null}
     */
    public void setComments(List<? extends Comment> comments) {
        Util.notNull(comments, "comments"); //$NON-NLS-1$
        Util.notContainNull(comments, "comments"); //$NON-NLS-1$
        this.comments = Util.freeze(comments);
    }

    /**
     * Returns {@link ModelKind#COMPILATION_UNIT} which represents this element kind.
     * @return {@link ModelKind#COMPILATION_UNIT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.COMPILATION_UNIT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitCompilationUnit(this, context);
    }
}
