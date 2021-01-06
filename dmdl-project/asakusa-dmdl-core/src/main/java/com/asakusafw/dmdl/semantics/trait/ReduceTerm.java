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

import com.asakusafw.dmdl.model.AstTerm;
import com.asakusafw.dmdl.semantics.Element;
import com.asakusafw.dmdl.semantics.ModelSymbol;
import com.asakusafw.dmdl.semantics.PropertySymbol;
import com.asakusafw.utils.collections.Lists;

/**
 * Reducing terms.
 * @param <T> the type of terms
 */
public class ReduceTerm<T extends AstTerm<T>> implements Element {

    private final T term;

    private final ModelSymbol source;

    private final List<MappingFactor> mappings;

    private final List<PropertySymbol> grouping;

    /**
     * Creates and returns a new instance.
     * @param term the original AST
     * @param source the join source
     * @param mappings mapping/folding factors
     * @param grouping the grouping properties
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ReduceTerm(T term, ModelSymbol source, List<MappingFactor> mappings, List<PropertySymbol> grouping) {
        if (term == null) {
            throw new IllegalArgumentException("term must not be null"); //$NON-NLS-1$
        }
        if (source == null) {
            throw new IllegalArgumentException("source must not be null"); //$NON-NLS-1$
        }
        if (grouping == null) {
            throw new IllegalArgumentException("grouping must not be null"); //$NON-NLS-1$
        }
        this.term = term;
        this.source = source;
        this.mappings = Lists.freeze(mappings);
        this.grouping = Lists.freeze(grouping);
    }

    @Override
    public T getOriginalAst() {
        return term;
    }

    /**
     * Returns a symbol of source model.
     * @return the source model
     */
    public ModelSymbol getSource() {
        return source;
    }

    /**
     * Returns mapping/folding factors in this term.
     * @return mapping/folding factors
     */
    public List<MappingFactor> getMappings() {
        return mappings;
    }

    /**
     * Returns symbols of grouping properties.
     * @return the grouping properties
     */
    public List<PropertySymbol> getGrouping() {
        return grouping;
    }
}
