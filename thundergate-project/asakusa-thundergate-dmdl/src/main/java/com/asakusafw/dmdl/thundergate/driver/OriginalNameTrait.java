/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.dmdl.thundergate.driver;

import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.semantics.Trait;

/**
 * Trait for holding original name.
 */
public class OriginalNameTrait implements Trait<OriginalNameTrait> {

    private final AstNode originalAst;

    private final String originalName;

    /**
     * Creates and returns a new instance.
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param originalName the original name of the target declaration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public OriginalNameTrait(AstNode originalAst, String originalName) {
        this.originalAst = originalAst;
        this.originalName = originalName;
    }

    @Override
    public AstNode getOriginalAst() {
        return originalAst;
    }

    /**
     * Returns the original name.
     * @return the original name
     */
    public String getName() {
        return originalName;
    }
}
