/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
 * An implementation of {@link PackageDeclaration}.
 */
public final class PackageDeclarationImpl extends ModelRoot implements PackageDeclaration {

    private Javadoc javadoc;

    private List<? extends Annotation> annotations;

    private Name name;

    @Override
    public Javadoc getJavadoc() {
        return this.javadoc;
    }

    /**
     * Sets the documentation comment.
     * @param javadoc the documentation comment, or {@code null} if it is not specified
     */
    public void setJavadoc(Javadoc javadoc) {
        this.javadoc = javadoc;
    }

    @Override
    public List<? extends Annotation> getAnnotations() {
        return this.annotations;
    }

    /**
     * Sets the annotations.
     * @param annotations the annotations
     * @throws IllegalArgumentException if {@code annotations} was {@code null}
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
     * Sets the target package name.
     * @param name the target package name
     * @throws IllegalArgumentException if {@code name} was {@code null}
     */
    public void setName(Name name) {
        Util.notNull(name, "name"); //$NON-NLS-1$
        this.name = name;
    }

    /**
     * Returns {@link ModelKind#PACKAGE_DECLARATION} which represents this element kind.
     * @return {@link ModelKind#PACKAGE_DECLARATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.PACKAGE_DECLARATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitPackageDeclaration(this, context);
    }
}
