/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import com.asakusafw.utils.java.model.syntax.Block;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link Block}.
 */
public final class BlockImpl extends ModelRoot implements Block {

    private List<? extends Statement> statements;

    @Override
    public List<? extends Statement> getStatements() {
        return this.statements;
    }

    /**
     * Sets the element statements.
     * @param statements the element statements
     * @throws IllegalArgumentException if {@code statements} was {@code null}
     */
    public void setStatements(List<? extends Statement> statements) {
        Util.notNull(statements, "statements"); //$NON-NLS-1$
        Util.notContainNull(statements, "statements"); //$NON-NLS-1$
        this.statements = Util.freeze(statements);
    }

    /**
     * Returns {@link ModelKind#BLOCK} which represents this element kind.
     * @return {@link ModelKind#BLOCK}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.BLOCK;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitBlock(this, context);
    }
}
