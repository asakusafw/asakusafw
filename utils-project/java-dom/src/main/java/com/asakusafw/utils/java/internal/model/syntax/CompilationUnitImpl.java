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

import com.asakusafw.utils.java.model.syntax.Comment;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.ImportDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.PackageDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link CompilationUnit}の実装。
 */
public final class CompilationUnitImpl extends ModelRoot implements CompilationUnit {

    /**
     * パッケージ宣言。
     */
    private PackageDeclaration packageDeclaration;

    /**
     * このコンパイル単位で宣言されるインポート宣言の一覧。
     */
    private List<? extends ImportDeclaration> importDeclarations;

    /**
     * このコンパイル単位で宣言される型の一覧。
     */
    private List<? extends TypeDeclaration> typeDeclarations;

    /**
     * このコンパイル単位に記述されたコメントの一覧。
     */
    private List<? extends Comment> comments;

    @Override
    public PackageDeclaration getPackageDeclaration() {
        return this.packageDeclaration;
    }

    /**
     * パッケージ宣言を設定する。
     * <p> 無名パッケージ上に存在するコンパイル単位を表現する場合、引数には{@code null}を指定する。 </p>
     * @param packageDeclaration
     *     パッケージ宣言、
     *     ただし無名パッケージ上に存在するコンパイル単位を表現する場合は{@code null}
     */
    public void setPackageDeclaration(PackageDeclaration packageDeclaration) {
        this.packageDeclaration = packageDeclaration;
    }

    @Override
    public List<? extends ImportDeclaration> getImportDeclarations() {
        return this.importDeclarations;
    }

    /**
     * このコンパイル単位で宣言されるインポート宣言の一覧を設定する。
     * <p> インポート宣言が一つも宣言されない場合、引数には空を指定する。 </p>
     * @param importDeclarations
     *     このコンパイル単位で宣言されるインポート宣言の一覧
     * @throws IllegalArgumentException
     *     {@code importDeclarations}に{@code null}が指定された場合
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
     * このコンパイル単位で宣言される型の一覧を設定する。
     * <p> 型が一つも宣言されない場合、引数には空を指定する。 </p>
     * @param typeDeclarations
     *     このコンパイル単位で宣言される型の一覧
     * @throws IllegalArgumentException
     *     {@code typeDeclarations}に{@code null}が指定された場合
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
     * このコンパイル単位に記述されたコメントの一覧を設定する。
     * <p> コメントが一つも記述されない場合、引数には空を指定する。 </p>
     * @param comments
     *     このコンパイル単位に記述されたコメントの一覧
     * @throws IllegalArgumentException
     *     {@code comments}に{@code null}が指定された場合
     */
    public void setComments(List<? extends Comment> comments) {
        Util.notNull(comments, "comments"); //$NON-NLS-1$
        Util.notContainNull(comments, "comments"); //$NON-NLS-1$
        this.comments = Util.freeze(comments);
    }

    /**
     * この要素の種類を表す{@link ModelKind#COMPILATION_UNIT}を返す。
     * @return {@link ModelKind#COMPILATION_UNIT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.COMPILATION_UNIT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitCompilationUnit(this, context);
    }
}
