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

import com.asakusafw.utils.java.model.syntax.DocBlock;
import com.asakusafw.utils.java.model.syntax.DocElement;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link DocBlock}.
 */
public final class DocBlockImpl extends ModelRoot implements DocBlock {

    private String tag;

    private List<? extends DocElement> elements;

    @Override
    public String getTag() {
        return this.tag;
    }

    /**
     * Sets the tag text.
     * @param tag the tag text
     * @throws IllegalArgumentException if {@code tag} was {@code null}
     */
    public void setTag(String tag) {
        Util.notNull(tag, "tag"); //$NON-NLS-1$
        this.tag = tag;
    }

    @Override
    public List<? extends DocElement> getElements() {
        return this.elements;
    }

    /**
     * Sets the inline elements.
     * @param elements the inline elements
     * @throws IllegalArgumentException if {@code elements} was {@code null}
     */
    public void setElements(List<? extends DocElement> elements) {
        Util.notNull(elements, "elements"); //$NON-NLS-1$
        Util.notContainNull(elements, "elements"); //$NON-NLS-1$
        this.elements = Util.freeze(elements);
    }

    /**
     * Returns {@link ModelKind#DOC_BLOCK} which represents this element kind.
     * @return {@link ModelKind#DOC_BLOCK}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.DOC_BLOCK;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitDocBlock(this, context);
    }
}
