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
package com.asakusafw.dmdl.semantics.trait;

import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.semantics.Element;
import com.asakusafw.dmdl.semantics.PropertyMappingKind;
import com.asakusafw.dmdl.semantics.PropertySymbol;

/**
 * Property mappings/foldings.
 */
public class MappingFactor implements Element {

    private final AstNode mapping;

    private final PropertyMappingKind kind;

    private final PropertySymbol source;

    private final PropertySymbol target;

    /**
     * Creates and returns a new instance.
     * @param originalAst the original AST, or {@code null} if this is an ad-hoc element
     * @param kind the kind of mapping function
     * @param source the source property
     * @param target the target property
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public MappingFactor(
            AstNode originalAst,
            PropertyMappingKind kind,
            PropertySymbol source,
            PropertySymbol target) {
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null"); //$NON-NLS-1$
        }
        if (source == null) {
            throw new IllegalArgumentException("source must not be null"); //$NON-NLS-1$
        }
        if (target == null) {
            throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
        }
        this.mapping = originalAst;
        this.kind = kind;
        this.source = source;
        this.target = target;
    }

    @Override
    public AstNode getOriginalAst() {
        return mapping;
    }

    /**
     * Returns the kind of mapping function.
     * @return the kind of mapping function
     */
    public PropertyMappingKind getKind() {
        return kind;
    }

    /**
     * Returns the source property.
     * @return the source property
     */
    public PropertySymbol getSource() {
        return source;
    }

    /**
     * Returns the target property.
     * @return the target property
     */
    public PropertySymbol getTarget() {
        return target;
    }
}
