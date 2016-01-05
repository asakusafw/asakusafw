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

import java.util.List;

import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.semantics.PropertySymbol;
import com.asakusafw.dmdl.semantics.Trait;
import com.asakusafw.utils.collections.Lists;

/**
 * Trait for holding primary key.
 */
public class PrimaryKeyTrait implements Trait<PrimaryKeyTrait> {

    private final AstNode originalAst;

    private final List<PropertySymbol> properties;

    /**
     * Creates and returns a new instance.
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param properties the primary key properties
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public PrimaryKeyTrait(AstNode originalAst, List<PropertySymbol> properties) {
        this.originalAst = originalAst;
        this.properties = Lists.freeze(properties);
    }

    @Override
    public AstNode getOriginalAst() {
        return originalAst;
    }

    /**
     * Returns the primary key properties.
     * @return the properties
     */
    public List<PropertySymbol> getProperties() {
        return properties;
    }
}
