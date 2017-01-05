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

import java.util.List;

import com.asakusafw.utils.java.model.syntax.DocBlock;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link Javadoc}.
 */
public final class JavadocImpl extends ModelRoot implements Javadoc {

    private List<? extends DocBlock> blocks;

    @Override
    public List<? extends DocBlock> getBlocks() {
        return this.blocks;
    }

    /**
     * Sets the documentation blocks.
     * @param blocks the documentation blocks
     * @throws IllegalArgumentException if {@code blocks} was {@code null}
     */
    public void setBlocks(List<? extends DocBlock> blocks) {
        Util.notNull(blocks, "blocks"); //$NON-NLS-1$
        Util.notContainNull(blocks, "blocks"); //$NON-NLS-1$
        this.blocks = Util.freeze(blocks);
    }

    /**
     * Returns {@link ModelKind#JAVADOC} which represents this element kind.
     * @return {@link ModelKind#JAVADOC}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.JAVADOC;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitJavadoc(this, context);
    }
}
