/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.PackageDeclaration;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link PackageDeclaration}の実装。
 */
public final class PackageDeclarationImpl extends ModelRoot implements PackageDeclaration {

    /**
     * ドキュメンテーションコメント。
     */
    private Javadoc javadoc;

    /**
     * 注釈の一覧。
     */
    private List<? extends Annotation> annotations;

    /**
     * 宣言するパッケージの名称。
     */
    private Name name;

    @Override
    public Javadoc getJavadoc() {
        return this.javadoc;
    }

    /**
     * ドキュメンテーションコメントを設定する。
     * <p> ドキュメンテーションコメントが存在しない場合、引数には{@code null}を指定する。 </p>
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     */
    public void setJavadoc(Javadoc javadoc) {
        this.javadoc = javadoc;
    }

    @Override
    public List<? extends Annotation> getAnnotations() {
        return this.annotations;
    }

    /**
     * 注釈の一覧を設定する。
     * <p> 注釈が存在しない場合、引数には空を指定する。 </p>
     * @param annotations
     *     注釈の一覧
     * @throws IllegalArgumentException
     *     {@code annotations}に{@code null}が指定された場合
     */
    public void setAnnotations(List<? extends Annotation> annotations) {
        Util.notNull(annotations, "annotations"); //$NON-NLS-1$
        Util.notContainNull(annotations, "annotations"); //$NON-NLS-1$
        this.annotations = Util.freeze(annotations);
    }

    @Override
    public Name getName() {
        return this.name;
    }

    /**
     * 宣言するパッケージの名称を設定する。
     * @param name
     *     宣言するパッケージの名称
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    public void setName(Name name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    /**
     * この要素の種類を表す{@link ModelKind#PACKAGE_DECLARATION}を返す。
     * @return {@link ModelKind#PACKAGE_DECLARATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.PACKAGE_DECLARATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitPackageDeclaration(this, context);
    }
}
