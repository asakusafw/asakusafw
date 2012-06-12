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
package com.asakusafw.dmdl.semantics.trait;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstName;
import com.asakusafw.dmdl.semantics.Trait;

/**
 * Namespace for models.
 */
public class NamespaceTrait implements Trait<NamespaceTrait> {

    private AstAttribute originalAst;

    private AstName namespace;

    /**
     * Creates and returns a new instance.
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param namespace the specified namespace
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public NamespaceTrait(AstAttribute originalAst, AstName namespace) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace must not be null"); //$NON-NLS-1$
        }
        this.originalAst = originalAst;
        this.namespace = namespace;
    }

    @Override
    public AstAttribute getOriginalAst() {
        return originalAst;
    }

    /**
     * Returns the specified namespace.
     * @return the namespace
     */
    public AstName getNamespace() {
        return namespace;
    }
}
