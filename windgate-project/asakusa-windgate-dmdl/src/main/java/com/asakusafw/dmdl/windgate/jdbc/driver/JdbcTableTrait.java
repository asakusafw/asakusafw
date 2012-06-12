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
package com.asakusafw.dmdl.windgate.jdbc.driver;

import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.semantics.Trait;

/**
 * Trait for holding table information.
 */
public class JdbcTableTrait implements Trait<JdbcTableTrait> {

    private final AstNode originalAst;

    private final String tableName;

    /**
     * Creates and returns a new instance.
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param tableName the table name of the target data model
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JdbcTableTrait(AstNode originalAst, String tableName) {
        if (originalAst == null) {
            throw new IllegalArgumentException("originalAst must not be null"); //$NON-NLS-1$
        }
        if (tableName == null) {
            throw new IllegalArgumentException("tableName must not be null"); //$NON-NLS-1$
        }
        this.originalAst = originalAst;
        this.tableName = tableName;
    }

    @Override
    public AstNode getOriginalAst() {
        return originalAst;
    }

    /**
     * Returns the table name.
     * @return the table name
     */
    public String getName() {
        return tableName;
    }
}
