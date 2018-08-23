/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.asakusafw.dmdl.model.AstExpression;
import com.asakusafw.dmdl.model.AstModelReference;
import com.asakusafw.dmdl.model.AstPropertyDefinition;
import com.asakusafw.dmdl.model.AstRecord;
import com.asakusafw.dmdl.model.AstRecordDefinition;
import com.asakusafw.dmdl.model.AstTerm;
import com.asakusafw.dmdl.model.AstUnionExpression;
import com.asakusafw.dmdl.model.ModelDefinitionKind;
import com.asakusafw.dmdl.model.AstNode.AbstractVisitor;
import com.asakusafw.dmdl.semantics.MemberDeclaration;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.ModelSymbol;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Type;

final class PropertyDeclarationProcessor extends AbstractVisitor<ModelDeclaration, Void> {

    private final Context context;

    final List<ModelSymbol> references = new ArrayList<>();

    final List<ModelSymbol> projections = new ArrayList<>();

    private PropertyDeclarationProcessor(Context context) {
        this.context = context;
    }

    static PropertyDeclarationProcessor resolve(
            Context context, ModelDeclaration model, AstExpression<AstRecord> node) {
        PropertyDeclarationProcessor resolver = new PropertyDeclarationProcessor(context);
        node.accept(model, resolver);
        return resolver;
    }

    @Override
    public <T extends AstTerm<T>> Void visitUnionExpression(ModelDeclaration model, AstUnionExpression<T> node) {
        for (T term : node.terms) {
            term.accept(model, this);
        }
        return null;
    }

    @Override
    public Void visitModelReference(ModelDeclaration model, AstModelReference node) {
        DmdlAnalyzer.LOG.debug("processing model reference: {}", node); //$NON-NLS-1$
        ModelDeclaration decl = context.getWorld().findModelDeclaration(node.name.identifier);
        if (decl == null) {
            context.error(
                    node.name,
                    Messages.getString("DmdlAnalyzer.diagnosticMissingModel"), //$NON-NLS-1$
                    node.name.identifier);
            return null;
        }
        for (PropertyDeclaration property : decl.getDeclaredProperties()) {
            MemberDeclaration other = model.findMemberDeclaration(property.getName().identifier);
            if (other != null) {
                DmdlAnalyzer.LOG.debug("property {} is duplicated", property.getSymbol()); //$NON-NLS-1$
                if (other instanceof PropertyDeclaration
                        && property.getType().isSame(((PropertyDeclaration) other).getType())) {
                    // merge
                    continue;
                } else {
                    context.error(
                            node,
                            Messages.getString("DmdlAnalyzer.diagnosticInconsistentTypeProperty"), //$NON-NLS-1$
                            property.getName(),
                            model.getName());
                    continue;
                }
            }
            model.declareProperty(
                    node,
                    property.getName(),
                    property.getType(),
                    property.getDescription(),
                    property.getAttributes());
        }
        ModelSymbol ref = context.getWorld().createModelSymbol(node.name);
        references.add(ref);
        if (decl.getOriginalAst().kind == ModelDefinitionKind.PROJECTIVE) {
            projections.add(ref);
        }
        return null;
    }

    @Override
    public Void visitRecordDefinition(ModelDeclaration model, AstRecordDefinition node) {
        DmdlAnalyzer.LOG.debug("processing record definition: {}", node); //$NON-NLS-1$
        Set<String> sawPropertyName = new HashSet<>();
        for (AstPropertyDefinition property : node.properties) {
            // processes only normal properties
            if (property.getPropertyKind() != AstPropertyDefinition.PropertyKind.NORMAL) {
                continue;
            }
            if (sawPropertyName.contains(property.name.identifier)) {
                context.error(
                        property.name,
                        Messages.getString("DmdlAnalyzer.diagnosticDuplicatedProperty"), //$NON-NLS-1$
                        property.name.identifier);
            }
            sawPropertyName.add(property.name.identifier);
            Type type = context.resolveType(property.type);
            if (type == null) {
                context.error(
                        property.type,
                        Messages.getString("DmdlAnalyzer.diagnosticUnknownTypeProperty"), //$NON-NLS-1$
                        property.type);
                continue;
            }

            PropertyDeclaration other = model.findPropertyDeclaration(property.name.identifier);
            if (other != null) {
                DmdlAnalyzer.LOG.debug("property {} is duplicated", property.name); //$NON-NLS-1$
                if (type.equals(other.getType()) == false) {
                    context.error(
                            property.name,
                            Messages.getString(
                                    "DmdlAnalyzer.diagnosticInconsistentTypeRepordProperty"), //$NON-NLS-1$
                            property.name,
                            model.getName());
                }
                continue;
            }
            model.declareProperty(
                    property,
                    property.name,
                    type,
                    property.description,
                    property.attributes);
        }
        return null;
    }
}