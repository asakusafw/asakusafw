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

import com.asakusafw.utils.java.model.syntax.Block;
import com.asakusafw.utils.java.model.syntax.CatchClause;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.TryResource;
import com.asakusafw.utils.java.model.syntax.TryStatement;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link TryStatement}.
 * @since 0.1.0
 * @version 0.9.0
 */
public final class TryStatementImpl extends ModelRoot implements TryStatement {

    private List<? extends TryResource> resources;

    private Block tryBlock;

    private List<? extends CatchClause> catchClauses;

    private Block finallyBlock;

    @Override
    public List<? extends TryResource> getResources() {
        return resources;
    }

    /**
     * Sets the resources.
     * @param resources the resources
     * @since 0.9.0
     */
    public void setResources(List<? extends TryResource> resources) {
        Util.notNull(resources, "resources"); //$NON-NLS-1$
        Util.notContainNull(resources, "resources"); //$NON-NLS-1$
        this.resources = Util.freeze(resources);
    }

    @Override
    public Block getTryBlock() {
        return this.tryBlock;
    }

    /**
     * Sets the body block of {@code try} clause.
     * @param tryBlock the body block of {@code try} clause
     * @throws IllegalArgumentException if {@code tryBlock} was {@code null}
     */
    public void setTryBlock(Block tryBlock) {
        Util.notNull(tryBlock, "tryBlock"); //$NON-NLS-1$
        this.tryBlock = tryBlock;
    }

    @Override
    public List<? extends CatchClause> getCatchClauses() {
        return this.catchClauses;
    }

    /**
     * Sets the {@code catch} clauses.
     * @param catchClauses the {@code catch} clauses
     * @throws IllegalArgumentException if {@code catchClauses} was {@code null}
     */
    public void setCatchClauses(List<? extends CatchClause> catchClauses) {
        Util.notNull(catchClauses, "catchClauses"); //$NON-NLS-1$
        Util.notContainNull(catchClauses, "catchClauses"); //$NON-NLS-1$
        this.catchClauses = Util.freeze(catchClauses);
    }

    @Override
    public Block getFinallyBlock() {
        return this.finallyBlock;
    }

    /**
     * Sets the body block of {@code finally} clause.
     * @param finallyBlock the body block of {@code finally} clause, or {@code null} if it is not specified
     */
    public void setFinallyBlock(Block finallyBlock) {
        this.finallyBlock = finallyBlock;
    }

    /**
     * Returns {@link ModelKind#TRY_STATEMENT} which represents this element kind.
     * @return {@link ModelKind#TRY_STATEMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.TRY_STATEMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitTryStatement(this, context);
    }
}
