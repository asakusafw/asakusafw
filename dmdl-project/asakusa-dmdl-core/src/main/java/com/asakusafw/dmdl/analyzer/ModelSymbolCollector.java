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
package com.asakusafw.dmdl.analyzer;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.asakusafw.dmdl.model.AstJoin;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstModelReference;
import com.asakusafw.dmdl.model.AstNode.AbstractVisitor;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.model.AstSummarize;
import com.asakusafw.dmdl.model.AstTerm;
import com.asakusafw.dmdl.model.AstUnionExpression;

final class ModelSymbolCollector extends AbstractVisitor<Collection<AstSimpleName>, Void> {

    private ModelSymbolCollector() {
        return;
    }

    static Set<AstSimpleName> collect(AstModelDefinition<?> definition) {
        Set<AstSimpleName> results = new LinkedHashSet<>();
        definition.expression.accept(results, new ModelSymbolCollector());
        return results;
    }

    @Override
    public Void visitModelReference(Collection<AstSimpleName> context, AstModelReference node) {
        context.add(node.name);
        return null;
    }

    @Override
    public Void visitJoin(Collection<AstSimpleName> context, AstJoin node) {
        node.reference.accept(context, this);
        return null;
    }

    @Override
    public Void visitSummarize(Collection<AstSimpleName> context, AstSummarize node) {
        node.reference.accept(context, this);
        return null;
    }

    @Override
    public <T extends AstTerm<T>> Void visitUnionExpression(
            Collection<AstSimpleName> context,
            AstUnionExpression<T> node) {
        for (T child : node.terms) {
            child.accept(context, this);
        }
        return null;
    }
}
