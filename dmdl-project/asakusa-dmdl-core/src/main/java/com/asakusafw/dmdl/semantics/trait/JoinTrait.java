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
package com.asakusafw.dmdl.semantics.trait;

import java.util.List;

import com.asakusafw.dmdl.model.AstExpression;
import com.asakusafw.dmdl.model.AstJoin;
import com.asakusafw.dmdl.semantics.Trait;

/**
 * Join trait for joined models.
 */
public class JoinTrait implements Trait<JoinTrait> {

    private final AstExpression<AstJoin> expression;

    private final List<ReduceTerm<AstJoin>> terms;

    /**
     * Creates and returns a new instance.
     * @param expression the original AST
     * @param terms the join terms
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JoinTrait(AstExpression<AstJoin> expression, List<ReduceTerm<AstJoin>> terms) {
        if (expression == null) {
            throw new IllegalArgumentException("expression must not be null"); //$NON-NLS-1$
        }
        if (terms == null) {
            throw new IllegalArgumentException("terms must not be null"); //$NON-NLS-1$
        }
        this.expression = expression;
        this.terms = terms;
    }

    @Override
    public AstExpression<AstJoin> getOriginalAst() {
        return expression;
    }

    /**
     * Returns each join term.
     * @return the terms
     */
    public List<ReduceTerm<AstJoin>> getTerms() {
        return terms;
    }
}
