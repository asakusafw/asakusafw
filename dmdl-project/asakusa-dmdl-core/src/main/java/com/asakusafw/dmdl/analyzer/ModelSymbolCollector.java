/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import com.asakusafw.dmdl.model.AstExpression;
import com.asakusafw.dmdl.model.AstJoin;
import com.asakusafw.dmdl.model.AstModelReference;
import com.asakusafw.dmdl.model.AstNode.AbstractVisitor;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.model.AstSummarize;
import com.asakusafw.dmdl.model.AstTerm;
import com.asakusafw.dmdl.model.AstUnionExpression;

/**
 * Collects appreared model symbols in {@link AstExpression}.
 */
public class ModelSymbolCollector extends AbstractVisitor<Collection<AstSimpleName>, Void> {

    /**
     * The singleton instance.
     */
    public static final ModelSymbolCollector INSTANCE = new ModelSymbolCollector();

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
