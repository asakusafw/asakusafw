/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.dmdl.java.analyzer;

import java.util.List;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.java.emitter.NameConstants;
import com.asakusafw.dmdl.model.AstJoin;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstNode.AbstractVisitor;
import com.asakusafw.dmdl.model.AstPropertyDefinition;
import com.asakusafw.dmdl.model.AstPropertyFolding;
import com.asakusafw.dmdl.model.AstPropertyMapping;
import com.asakusafw.dmdl.model.AstRecordDefinition;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.model.AstSummarize;
import com.asakusafw.dmdl.model.AstTerm;
import com.asakusafw.dmdl.model.AstUnionExpression;
import com.asakusafw.dmdl.semantics.DmdlSemantics;

/**
 * Validates syntax for Java data model generation.
 * @since 0.7.1
 */
class ExtraSyntaxValidator extends AbstractVisitor<AstModelDefinition<?>, Void> {

    private final DmdlSemantics root;

    /**
     * Creates a new instance.
     * @param root the root semantics
     */
    ExtraSyntaxValidator(DmdlSemantics root) {
        this.root = root;
    }

    @Override
    public <T extends AstTerm<T>> Void visitModelDefinition(
            AstModelDefinition<?> context, AstModelDefinition<T> node) {
        node.expression.accept(node, this);
        return null;
    }

    @Override
    public <T extends AstTerm<T>> Void visitUnionExpression(
            AstModelDefinition<?> context, AstUnionExpression<T> node) {
        for (T term : node.terms) {
            term.accept(context, this);
        }
        return null;
    }

    @Override
    public Void visitJoin(AstModelDefinition<?> context, AstJoin node) {
        if (node.mapping != null) {
            for (AstPropertyMapping property : node.mapping.properties) {
                validatePropertyName(context, property.target);
            }
        }
        return null;
    }

    @Override
    public Void visitSummarize(AstModelDefinition<?> context, AstSummarize node) {
        for (AstPropertyFolding property : node.folding.properties) {
            validatePropertyName(context, property.target);
        }
        return null;
    }

    @Override
    public Void visitRecordDefinition(AstModelDefinition<?> context, AstRecordDefinition node) {
        for (AstPropertyDefinition property : node.properties) {
            validatePropertyName(context, property.name);
        }
        return null;
    }

    private void validatePropertyName(AstModelDefinition<?> context, AstSimpleName name) {
        List<String> words = name.getWordList();
        String last = words.get(words.size() - 1);
        if (last.equals(NameConstants.PROPERTY_GETTER_SUFFIX)) {
            root.report(new Diagnostic(Diagnostic.Level.ERROR, name,
                    Messages.getString("ExtraSyntaxValidator.diagnosticInvalidPropertyNameSuffix"), //$NON-NLS-1$
                    NameConstants.PROPERTY_GETTER_SUFFIX,
                    context.name.identifier,
                    name.identifier));
        }
    }
}
