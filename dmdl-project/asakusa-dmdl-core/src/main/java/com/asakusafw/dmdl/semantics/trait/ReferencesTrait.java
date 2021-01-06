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
package com.asakusafw.dmdl.semantics.trait;

import java.util.List;

import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.semantics.ModelSymbol;
import com.asakusafw.dmdl.semantics.Trait;

/**
 * Referred models.
 */
public class ReferencesTrait implements Trait<ReferencesTrait> {

    private final AstNode originalAst;

    private final List<ModelSymbol> references;

    /**
     * Creates and returns a new instance.
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param references each referred model
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ReferencesTrait(AstNode originalAst, List<ModelSymbol> references) {
        if (references == null) {
            throw new IllegalArgumentException("references must not be null"); //$NON-NLS-1$
        }
        this.originalAst = originalAst;
        this.references = references;
    }

    @Override
    public AstNode getOriginalAst() {
        return originalAst;
    }

    /**
     * Returns referred models.
     * @return the referred models
     */
    public List<ModelSymbol> getReferences() {
        return references;
    }
}
