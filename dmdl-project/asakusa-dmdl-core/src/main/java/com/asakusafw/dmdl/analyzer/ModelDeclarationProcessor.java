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
package com.asakusafw.dmdl.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.model.AstExpression;
import com.asakusafw.dmdl.model.AstGrouping;
import com.asakusafw.dmdl.model.AstJoin;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstModelFolding;
import com.asakusafw.dmdl.model.AstModelMapping;
import com.asakusafw.dmdl.model.AstName;
import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.model.AstPropertyFolding;
import com.asakusafw.dmdl.model.AstPropertyMapping;
import com.asakusafw.dmdl.model.AstRecord;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.model.AstSummarize;
import com.asakusafw.dmdl.model.AstTerm;
import com.asakusafw.dmdl.model.AstUnionExpression;
import com.asakusafw.dmdl.model.ModelDefinitionKind;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.ModelSymbol;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.PropertyMappingKind;
import com.asakusafw.dmdl.semantics.PropertyReferenceDeclaration;
import com.asakusafw.dmdl.semantics.PropertySymbol;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.trait.JoinTrait;
import com.asakusafw.dmdl.semantics.trait.MappingFactor;
import com.asakusafw.dmdl.semantics.trait.ProjectionsTrait;
import com.asakusafw.dmdl.semantics.trait.ReduceTerm;
import com.asakusafw.dmdl.semantics.trait.ReferencesTrait;
import com.asakusafw.dmdl.semantics.trait.SummarizeTrait;

final class ModelDeclarationProcessor {

    static final Logger LOG = LoggerFactory.getLogger(ModelDeclarationProcessor.class);

    private final Context context;

    ModelDeclarationProcessor(Context context) {
        this.context = context;
    }

    static void validate(Context context, AstModelDefinition<?> node) {
        MemberDeclarationProcessor.validate(context, node);
    }

    static void resolve(Context context, ModelDeclaration model, AstModelDefinition<?> node) {
        new ModelDeclarationProcessor(context).resolve(model, node);
    }

    private void resolve(ModelDeclaration model, AstModelDefinition<?> node) {
        assert model != null;
        LOG.debug("resolving model definition: {}", node.name); //$NON-NLS-1$
        switch (node.kind) {
        case RECORD:
            resolveRecord(model, node.asRecord());
            break;
        case PROJECTIVE:
            resolveRecord(model, node.asProjective());
            break;
        case JOINED:
            resolveJoined(model, node.asJoined());
            break;
        case SUMMARIZED:
            resolveSummarize(model, node.asSummarized());
            break;
        default:
            throw new AssertionError(node.kind);
        }
    }

    private void resolveRecord(ModelDeclaration model, AstModelDefinition<AstRecord> node) {
        assert model != null;
        assert node != null;
        LOG.debug("resolving record: {}", model.getName()); //$NON-NLS-1$
        PropertyDeclarationProcessor refs = PropertyDeclarationProcessor.resolve(context, model, node.expression);
        LOG.debug("record {} has references: {}", model.getName(), refs.references); //$NON-NLS-1$
        LOG.debug("record {} has projections: {}", model.getName(), refs.projections); //$NON-NLS-1$
        model.putTrait(ReferencesTrait.class, new ReferencesTrait(node.expression, refs.references));
        model.putTrait(ProjectionsTrait.class, new ProjectionsTrait(node.expression, refs.projections));

        if (context.hasError()) {
            return;
        }
        PropertyReferenceDeclarationProcessor.resolve(context, model, node.expression);

        if (node.kind == ModelDefinitionKind.RECORD) {
            if (context.hasError()) {
                return;
            }
            for (PropertyReferenceDeclaration ref : model.getDeclaredPropertyReferences()) {
                if (ref.getReference().isStub()) {
                    AstNode orig = ref.getOriginalAst();
                    if (orig == null) {
                        orig = node.name;
                    }
                    context.error(
                            orig,
                            Messages.getString("ModelDeclarationProcessor.diagnosticMissingReferenceBody"), //$NON-NLS-1$
                            ref.getOwner().getName().identifier,
                            ref.getName().identifier);
                }
            }
        }
    }

    private void resolveJoined(ModelDeclaration model, AstModelDefinition<AstJoin> node) {
        assert model != null;
        assert node != null;
        LOG.debug("resolving joined: {}", model.getName()); //$NON-NLS-1$
        List<ReduceTerm<AstJoin>> results = new ArrayList<>();
        List<AstJoin> terms = extract(node.expression);
        if (terms.size() >= 3) {
            context.error(
                    node.expression,
                    Messages.getString("DmdlAnalyzer.diagnosticTooManyJoinTerms")); //$NON-NLS-1$
        }
        for (AstJoin term : terms) {
            LOG.debug("resolving joined term: {} -> {}", model.getName(), term.reference.name); //$NON-NLS-1$
            ModelSymbol source = context.getWorld().createModelSymbol(term.reference.name);
            if (source.findDeclaration() == null) {
                context.error(
                        term.reference,
                        Messages.getString("DmdlAnalyzer.diagnosticMissingJoinModel"), //$NON-NLS-1$
                        term.reference.name);
                continue;
            }
            Map<String, PropertyDeclaration> properties = resolveJoinProperties(model, source, term);
            List<MappingFactor> mappings = resolveMapping(model, source, term.mapping);
            List<PropertySymbol> grouping = resolveGrouping(model, properties, term.grouping);
            results.add(new ReduceTerm<>(term, source, mappings, grouping));
        }
        if (context.hasError() == false && checkJoinTerms(model, results)) {
            model.putTrait(JoinTrait.class, new JoinTrait(node.expression, results));
        }

        // NOTE: joined models don't inherit any property references
    }

    private Map<String, PropertyDeclaration> resolveJoinProperties(
            ModelDeclaration model,
            ModelSymbol sourceModel,
            AstJoin term) {
        assert model != null;
        assert sourceModel != null;
        assert term != null;
        LOG.debug("processing model mapping: {}", term.mapping); //$NON-NLS-1$

        ModelDeclaration sourceDecl = sourceModel.findDeclaration();
        assert sourceDecl != null;
        Map<String, PropertyDeclaration> results = new HashMap<>();
        if (term.mapping == null) {
            for (PropertyDeclaration prop : sourceDecl.getDeclaredProperties()) {
                PropertyDeclaration declared = model.findPropertyDeclaration(prop.getName().identifier);
                if (declared != null) {
                    LOG.debug("property {} is duplicated", prop.getSymbol()); //$NON-NLS-1$
                    results.put(declared.getName().identifier, declared);
                } else {
                    declared = model.declareProperty(
                            sourceModel.getName(),
                            prop.getName(),
                            prop.getType(),
                            prop.getDescription(),
                            prop.getAttributes());
                    results.put(declared.getName().identifier, declared);
                }
            }
        } else {
            Set<String> saw = new HashSet<>();
            for (AstPropertyMapping property : term.mapping.properties) {
                if (saw.contains(property.target.identifier)) {
                    context.error(
                            property,
                            Messages.getString("DmdlAnalyzer.diagnosticDuplicatedJoinMappingProperty"), //$NON-NLS-1$
                            property.target.identifier);
                    continue;
                }
                saw.add(property.target.identifier);
                PropertyDeclaration sourceProp = sourceDecl.findPropertyDeclaration(property.source.identifier);
                if (sourceProp == null) {
                    context.error(
                            sourceModel.getName(),
                            Messages.getString("DmdlAnalyzer.diagnosticMissingJoinProperty"), //$NON-NLS-1$
                            property.source.identifier,
                            sourceModel.getName().identifier);
                    continue;
                }
                PropertyDeclaration declared = model.findPropertyDeclaration(property.target.identifier);
                if (declared != null) {
                    LOG.debug("property {} is duplicated", property.target); //$NON-NLS-1$
                    results.put(declared.getName().identifier, declared);
                } else {
                    declared = model.declareProperty(
                            property,
                            property.target,
                            sourceProp.getType(),
                            property.description,
                            property.attributes);
                    results.put(declared.getName().identifier, declared);
                }
            }
        }
        return results;
    }

    private static List<MappingFactor> resolveMapping(
            ModelDeclaration model, ModelSymbol source, AstModelMapping mapping) {
        assert model != null;
        assert source != null;
        ModelDeclaration sourceModel = source.findDeclaration();
        assert sourceModel != null;
        List<MappingFactor> results = new ArrayList<>();
        if (mapping == null) {
            for (PropertyDeclaration property : sourceModel.getDeclaredProperties()) {
                PropertyDeclaration targetProperty = model.findPropertyDeclaration(property.getName().identifier);
                if (targetProperty != null) {
                    results.add(new MappingFactor(
                            source.getName(),
                            PropertyMappingKind.ANY,
                            source.createPropertySymbol(property.getName()),
                            targetProperty.getSymbol()));
                }
            }
        } else {
            for (AstPropertyMapping propertyMapping : mapping.properties) {
                PropertyDeclaration targetProperty = model.findPropertyDeclaration(propertyMapping.target.identifier);
                if (targetProperty != null) {
                    results.add(new MappingFactor(
                            source.getName(),
                            PropertyMappingKind.ANY,
                            source.createPropertySymbol(propertyMapping.source),
                            targetProperty.getSymbol()));
                }
            }
        }
        return results;
    }

    private boolean checkJoinTerms(ModelDeclaration model, List<ReduceTerm<AstJoin>> terms) {
        assert model != null;
        assert terms != null;
        if (checkGrouping(model, terms) == false) {
            return false;
        }
        boolean green = true;
        Map<String, Type> typeMap = new HashMap<>();
        for (ReduceTerm<AstJoin> term : terms) {
            Set<String> groupingProperties = new HashSet<>();
            for (PropertySymbol grouping : term.getGrouping()) {
                groupingProperties.add(grouping.getName().identifier);
            }
            for (MappingFactor factor : term.getMappings()) {
                PropertySymbol target = factor.getTarget();
                Type declared = typeMap.get(target.getName().identifier);
                if (declared == null) {
                    typeMap.put(target.getName().identifier, target.findDeclaration().getType());
                } else if (groupingProperties.contains(target.getName().identifier) == false) {
                    context.error(
                            term.getOriginalAst(),
                            Messages.getString("DmdlAnalyzer.diagnosticDuplicatedJoinGroupingProperty"), //$NON-NLS-1$
                            target.getName().identifier);
                    green = false;
                }
            }
        }
        return green;
    }

    private void resolveSummarize(ModelDeclaration model, AstModelDefinition<AstSummarize> node) {
        assert model != null;
        assert node != null;
        LOG.debug("resolving summarized: {}", model.getName()); //$NON-NLS-1$
        List<ReduceTerm<AstSummarize>> results = new ArrayList<>();
        for (AstSummarize term : extract(node.expression)) {
            LOG.debug("resolving summarized term: {} -> {}", model.getName(), term.reference.name); //$NON-NLS-1$
            ModelSymbol source = context.getWorld().createModelSymbol(term.reference.name);
            if (source.findDeclaration() == null) {
                context.error(
                        term.reference,
                        Messages.getString("DmdlAnalyzer.diagnosticMissingSummarizeModel"), //$NON-NLS-1$
                        term.reference.name);
                continue;
            }
            Map<String, PropertyDeclaration> properties = resolveSummarizeProperties(model, source, term);
            List<MappingFactor> foldings = resolveFolding(model, source, term.folding);
            List<PropertySymbol> grouping = resolveGrouping(model, properties, term.grouping);
            results.add(new ReduceTerm<>(term, source, foldings, grouping));
        }
        if (checkSummarizeTerms(model, results)) {
            model.putTrait(SummarizeTrait.class, new SummarizeTrait(node.expression, results));
        }

        // NOTE: summarized models don't inherit any property references
    }

    private Map<String, PropertyDeclaration> resolveSummarizeProperties(
            ModelDeclaration model, ModelSymbol source, AstSummarize term) {
        assert model != null;
        assert source != null;
        assert term != null;
        LOG.debug("processing model folding: {}", term.folding); //$NON-NLS-1$

        ModelDeclaration decl = source.findDeclaration();
        assert decl != null;
        Map<String, PropertyDeclaration> results = new HashMap<>();
        for (AstPropertyFolding property : term.folding.properties) {
            PropertyDeclaration original = decl.findPropertyDeclaration(property.source.identifier);
            if (original == null) {
                context.error(
                        source.getName(),
                        Messages.getString("DmdlAnalyzer.diagnosticMissingSummarizeFoldingProperty"), //$NON-NLS-1$
                        property.source.identifier,
                        source.getName().identifier);
                continue;
            }
            PropertyMappingKind mapping = resolveAggregateFunction(property.aggregator);
            if (mapping == null) {
                context.error(
                        property.aggregator,
                        Messages.getString("DmdlAnalyzer.diagnosticMissingSummarizeAggregateFunction"), //$NON-NLS-1$
                        property.aggregator.toString());
                continue;
            }
            Type resolved = original.getType().map(mapping);
            if (resolved == null) {
                context.error(
                        property,
                        Messages.getString(
                                "DmdlAnalyzer.diagnosticInconsistentSummarizeAggregateFunction"), //$NON-NLS-1$
                        property.aggregator.toString(),
                        property.source.identifier,
                        original.getType());
                continue;
            }
            PropertyDeclaration declared = model.findPropertyDeclaration(property.target.identifier);
            if (declared != null) {
                context.error(
                        property.target,
                        Messages.getString("DmdlAnalyzer.diagnosticDuplicatedSummarizeProperty"), //$NON-NLS-1$
                        property.target.identifier);
                continue;
            }
            PropertyDeclaration result = model.declareProperty(
                    property,
                    property.target,
                    resolved,
                    property.description,
                    property.attributes);
            results.put(result.getName().identifier, result);
        }
        return results;
    }

    private static List<MappingFactor> resolveFolding(
            ModelDeclaration model, ModelSymbol source, AstModelFolding folding) {
        assert model != null;
        assert source != null;
        assert folding != null;
        ModelDeclaration sourceModel = source.findDeclaration();
        assert sourceModel != null;
        List<MappingFactor> results = new ArrayList<>();
        for (AstPropertyFolding propertyFolding : folding.properties) {
            PropertyDeclaration targetProperty = model.findPropertyDeclaration(propertyFolding.target.identifier);
            PropertyMappingKind mapping = resolveAggregateFunction(propertyFolding.aggregator);
            if (targetProperty != null && mapping != null) {
                results.add(new MappingFactor(
                        source.getName(),
                        mapping,
                        source.createPropertySymbol(propertyFolding.source),
                        targetProperty.getSymbol()));
            }
        }
        return results;
    }

    private boolean checkSummarizeTerms(ModelDeclaration model, List<ReduceTerm<AstSummarize>> terms) {
        assert model != null;
        assert terms != null;
        if (checkGrouping(model, terms) == false) {
            return false;
        }
        // always single term currently
        return terms.size() == 1;
    }

    private static PropertyMappingKind resolveAggregateFunction(AstName aggregator) {
        assert aggregator != null;
        String name = aggregator.toString().toUpperCase();
        try {
            return PropertyMappingKind.valueOf(name);
        } catch (Exception e) {
            LOG.debug("invalid property mapping kind: {}", name, e); //$NON-NLS-1$
            return null;
        }
    }

    private List<PropertySymbol> resolveGrouping(
            ModelDeclaration model,
            Map<String, PropertyDeclaration> properties,
            AstGrouping grouping) {
        assert model != null;
        if (grouping == null) {
            return Collections.emptyList();
        } else {
            List<PropertySymbol> results = new ArrayList<>();
            for (AstSimpleName name : grouping.properties) {
                PropertyDeclaration property = properties.get(name.identifier);
                if (property == null) {
                    context.error(
                            name,
                            Messages.getString("DmdlAnalyzer.diagnosticMissingSummarizeGroupingProperty"), //$NON-NLS-1$
                            name.identifier);
                    continue;
                }
                results.add(model.createPropertySymbol(name));
            }
            return results;
        }
    }

    private <T extends AstTerm<T>> boolean checkGrouping(ModelDeclaration model, List<ReduceTerm<T>> terms) {
        assert model != null;
        assert terms != null;
        Iterator<ReduceTerm<T>> iter = terms.iterator();
        if (iter.hasNext() == false) {
            return false;
        }

        boolean green = true;
        ReduceTerm<T> first = iter.next();
        List<PropertyDeclaration> firstSources = resolveGroupingSources(first);
        while (iter.hasNext()) {
            ReduceTerm<T> next = iter.next();
            if (first.getGrouping().size() != next.getGrouping().size()) {
                context.error(
                        next.getOriginalAst(),
                        Messages.getString("DmdlAnalyzer.diagnosticInconsistentNumberGroupingProperties"), //$NON-NLS-1$
                        model.getName());
                return false;
            }
            List<PropertyDeclaration> nextSources = resolveGroupingSources(next);
            assert firstSources.size() == nextSources.size();
            for (int i = 0, n = firstSources.size(); i < n; i++) {
                PropertyDeclaration left = firstSources.get(i);
                PropertyDeclaration right = nextSources.get(i);
                if (left.getType().isSame(right.getType()) == false) {
                    PropertySymbol rightSymbol = next.getGrouping().get(i);
                    context.error(
                            rightSymbol.getOriginalAst(),
                            Messages.getString("DmdlAnalyzer.diagnosticInconsistentTypeGroupingProperty"), //$NON-NLS-1$
                            rightSymbol.getName());
                    green = false;
                }
            }
        }
        return green;
    }

    private static List<PropertyDeclaration> resolveGroupingSources(ReduceTerm<?> term) {
        assert term != null;
        Map<PropertySymbol, PropertySymbol> rmap = new HashMap<>();
        for (MappingFactor entry : term.getMappings()) {
            rmap.put(entry.getTarget(), entry.getSource());
        }
        List<PropertyDeclaration> results = new ArrayList<>();
        for (PropertySymbol prop : term.getGrouping()) {
            PropertySymbol source = rmap.get(prop);
            if (source == null) {
                source = prop;
            }
            results.add(source.findDeclaration());
        }
        return results;
    }

    private static <T extends AstTerm<T>> List<T> extract(AstExpression<T> expression) {
        if (expression instanceof AstTerm<?>) {
            AstTerm<T> term = (AstTerm<T>) expression;
            return Collections.singletonList(term.getUnit());
        } else if (expression instanceof AstUnionExpression<?>) {
            AstUnionExpression<T> union = (AstUnionExpression<T>) expression;
            return union.terms;
        } else {
            throw new AssertionError(expression);
        }
    }
}
