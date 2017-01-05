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

import com.asakusafw.utils.java.model.syntax.Block;
import com.asakusafw.utils.java.model.syntax.CatchClause;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link CatchClause}.
 */
public final class CatchClauseImpl extends ModelRoot implements CatchClause {

    private FormalParameterDeclaration parameter;

    private Block body;

    @Override
    public FormalParameterDeclaration getParameter() {
        return this.parameter;
    }

    /**
     * Sets the exception parameter.
     * @param parameter the exception parameter
     * @throws IllegalArgumentException if {@code parameter} was {@code null}
     */
    public void setParameter(FormalParameterDeclaration parameter) {
        Util.notNull(parameter, "parameter"); //$NON-NLS-1$
        this.parameter = parameter;
    }

    @Override
    public Block getBody() {
        return this.body;
    }

    /**
     * Sets the {@code catch} block body.
     * @param body the {@code catch} block body
     * @throws IllegalArgumentException if {@code body} was {@code null}
     */
    public void setBody(Block body) {
        Util.notNull(body, "body"); //$NON-NLS-1$
        this.body = body;
    }

    /**
     * Returns {@link ModelKind#CATCH_CLAUSE} which represents this element kind.
     * @return {@link ModelKind#CATCH_CLAUSE}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.CATCH_CLAUSE;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitCatchClause(this, context);
    }
}
