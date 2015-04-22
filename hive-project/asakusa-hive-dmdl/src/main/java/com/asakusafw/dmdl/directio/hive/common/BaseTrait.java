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
package com.asakusafw.dmdl.directio.hive.common;

import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.semantics.Trait;

/**
 * The common base trait.
 * @param <T> kind of trait
 * @since 0.7.0
 */
public class BaseTrait<T extends BaseTrait<T>> implements Trait<T> {

    private volatile AstNode originalAst;

    @Override
    public AstNode getOriginalAst() {
        return originalAst;
    }

    /**
     * Sets the original AST.
     * @param ast the AST
     * @param overwrite {@code true} to overwrite previous AST, or {@code false} to set only if it is absent
     */
    public void setOriginalAst(AstNode ast, boolean overwrite) {
        if (overwrite || this.originalAst == null) {
            this.originalAst = ast;
        }
    }
}