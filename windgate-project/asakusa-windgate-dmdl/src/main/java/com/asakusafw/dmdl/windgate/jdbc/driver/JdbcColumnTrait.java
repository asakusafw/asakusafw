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
package com.asakusafw.dmdl.windgate.jdbc.driver;

import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.semantics.Trait;

/**
 * Trait for holding column names.
 */
public class JdbcColumnTrait implements Trait<JdbcColumnTrait> {

    private final AstNode originalAst;

    private final String columnName;

    /**
     * Creates and returns a new instance.
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param columnName the column name of the target property
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JdbcColumnTrait(AstNode originalAst, String columnName) {
        if (originalAst == null) {
            throw new IllegalArgumentException("originalAst must not be null"); //$NON-NLS-1$
        }
        if (columnName == null) {
            throw new IllegalArgumentException("columnName must not be null"); //$NON-NLS-1$
        }
        this.originalAst = originalAst;
        this.columnName = columnName;
    }

    @Override
    public AstNode getOriginalAst() {
        return originalAst;
    }

    /**
     * Returns the column name.
     * @return the column name
     */
    public String getName() {
        return columnName;
    }
}
