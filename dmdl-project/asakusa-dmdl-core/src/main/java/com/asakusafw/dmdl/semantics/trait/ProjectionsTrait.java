/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.semantics.ModelSymbol;
import com.asakusafw.dmdl.semantics.Trait;

/**
 * Projections in use.
 */
public class ProjectionsTrait implements Trait<ProjectionsTrait> {

    private final AstNode originalAst;

    private final List<ModelSymbol> projectives;

    /**
     * Creates and returns a new instance.
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param projectives each projective model in references
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ProjectionsTrait(AstNode originalAst, List<ModelSymbol> projectives) {
        if (projectives == null) {
            throw new IllegalArgumentException("projectives must not be null"); //$NON-NLS-1$
        }
        this.originalAst = originalAst;
        this.projectives = projectives;
    }

    @Override
    public AstNode getOriginalAst() {
        return originalAst;
    }

    /**
     * Returns owned projective models.
     * @return the projectives
     */
    public List<ModelSymbol> getProjections() {
        return projectives;
    }
}
