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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.Region;
import com.asakusafw.dmdl.model.*;
import com.asakusafw.dmdl.model.AstNode.AbstractVisitor;
import com.asakusafw.dmdl.semantics.Declaration;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.ModelSymbol;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.PropertyMappingKind;
import com.asakusafw.dmdl.semantics.PropertySymbol;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.trait.JoinTrait;
import com.asakusafw.dmdl.semantics.trait.MappingFactor;
import com.asakusafw.dmdl.semantics.trait.ProjectionsTrait;
import com.asakusafw.dmdl.semantics.trait.ReduceTerm;
import com.asakusafw.dmdl.semantics.trait.SummarizeTrait;
import com.asakusafw.dmdl.spi.AttributeDriver;
import com.asakusafw.dmdl.spi.TypeDriver;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;

/**
 * Analyzes DMDL AST and builds DMDL semantic models.
 */
public class DmdlAnalyzer {

    static final Logger LOG = LoggerFactory.getLogger(DmdlAnalyzer.class);

    final Context context;

    private final Graph<String> modelDependencies;

    /**
     * Creates and returns a new instance.
     * @param typeDrivers type resolvers
     * @param attributeDrivers attributed analyzers
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DmdlAnalyzer(
            Iterable<? extends TypeDriver> typeDrivers,
            Iterable<? extends AttributeDriver> attributeDrivers) {
        if (typeDrivers == null) {
            throw new IllegalArgumentException("typeDrivers must not be null"); //$NON-NLS-1$
        }
        if (attributeDrivers == null) {
            throw new IllegalArgumentException("attributeDrivers must not be null"); //$NON-NLS-1$
        }
        this.context = new Context(new DmdlSemantics(), typeDrivers, attributeDrivers);
        this.modelDependencies = Graphs.newInstance();
    }

    void report(Diagnostic diagnostic) {
        assert diagnostic != null;
        context.getWorld().report(diagnostic);
    }

    /**
     * Addes a model definition to this analyzer.
     * @param definition the model definition
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void addModel(AstModelDefinition<?> definition) {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        DmdlSemantics world = context.getWorld();
        if (world.findModelDeclaration(definition.name.identifier) != null) {
            report(new Diagnostic(
                    Diagnostic.Level.ERROR,
                    definition.name,
                    "The model \"{0}\" is duplicated.",
                    definition.name.identifier));
        } else {
            world.declareModel(
                    definition,
                    definition.name,
                    definition.description,
                    definition.attributes);
            computeDependencies(definition);
        }
    }

    private void computeDependencies(AstModelDefinition<?> definition) {
        assert definition != null;
        LOG.debug("Computing dependencies: {}", definition.name);
        Set<AstSimpleName> references = Sets.create();
        definition.expression.accept(references, ModelSymbolCollector.INSTANCE);
        modelDependencies.addNode(definition.name.identifier);
        for (AstSimpleName target : references) {
            modelDependencies.addEdge(definition.name.identifier, target.identifier);
        }
    }

    /**
     * Resolves the all added definitions and returns the root semantics model.
     * @return the analyzed semantics model
     * @throws DmdlSemanticException If registered models has some errors
     */
    public synchronized DmdlSemantics resolve() throws DmdlSemanticException {
        checkDiagnostics();
        resolveSymbols();
        checkDiagnostics();
        resolveAttributes();
        checkDiagnostics();
        return context.getWorld();
    }

    private void checkDiagnostics() throws DmdlSemanticException {
        if (context.getWorld().hasError()) {
            throw new DmdlSemanticException(
                    "DMDL analysis was stopped by error",
                    context.getWorld().getDiagnostics());
        }
    }

    private void resolveSymbols() {
        LOG.debug("Resolving symbols");
        Set<Set<String>> circuits = Graphs.findCircuit(modelDependencies);
        if (circuits.isEmpty() == false) {
            for (Set<String> loop : circuits) {
                report(new Diagnostic(
                        Level.ERROR,
                        (Region) null,
                        "A cycle exists in model dependencies: {0}",
                        loop));
            }
            return;
        }

        DmdlSemantics world = context.getWorld();
        for (String name : Graphs.sortPostOrder(modelDependencies)) {
            ModelDeclaration model = world.findModelDeclaration(name);
            if (model == null) {
                // means "model have not been declared, but is referenced."
                continue;
            }
            resolveModelSymbol(model);
        }
    }

    private void resolveModelSymbol(ModelDeclaration model) {
        assert model != null;
        AstModelDefinition<?> definition = model.getOriginalAst();
        LOG.debug("Resolving model definition: {}", definition.name);
        switch (definition.kind) {
        case RECORD:
            resolveRecord(model, definition.asRecord().expression);
            break;
        case PROJECTIVE:
            resolveRecord(model, definition.asProjective().expression);
            break;
        case JOINED:
            resolveJoined(model, definition.asJoined().expression);
            break;
        case SUMMARIZED:
            resolveSummarize(model, definition.asSummarized().expression);
            break;
        default:
            throw new AssertionError(definition.kind);
        }
    }

    private void resolveRecord(
            ModelDeclaration model,
            AstExpression<AstRecord> expression) {
        assert model != null;
        assert expression != null;
        LOG.debug("Resolving record: {}", model.getName());
        RecordExpressionResolver resolver = new RecordExpressionResolver();
        expression.accept(model, resolver);
        ProjectionsTrait projections = new ProjectionsTrait(expression, resolver.projections);
        LOG.debug("Record {} has projections: {}", model.getName(), projections.getProjections());
        model.putTrait(ProjectionsTrait.class, projections);
    }

    private void resolveJoined(
            ModelDeclaration model,
            AstExpression<AstJoin> expression) {
        assert model != null;
        assert expression != null;
        LOG.debug("Resolving joined: {}", model.getName());
        List<ReduceTerm<AstJoin>> results = Lists.create();
        for (AstJoin term : extract(expression)) {
            LOG.debug("Resolving joined term: {} -> {}", model.getName(), term.reference.name);
            ModelSymbol source = context.getWorld().createModelSymbol(term.reference.name);
            if (source.findDeclaration() == null) {
                report(new Diagnostic(
                        Level.ERROR,
                        term.reference,
                        "{0} is not defined",
                        term.reference.name));
                continue;
            }
            Map<String, PropertyDeclaration> properties = resolveJoinProperties(model, source, term);
            List<MappingFactor> mappings = resolveMapping(model, source, term.mapping);
            List<PropertySymbol> grouping = resolveGrouping(model, properties, term.grouping);
            results.add(new ReduceTerm<AstJoin>(term, source, mappings, grouping));
        }
        if (checkJoinTerms(model, results)) {
            model.putTrait(JoinTrait.class, new JoinTrait(expression, results));
        }
    }

    private Map<String, PropertyDeclaration> resolveJoinProperties(
            ModelDeclaration model,
            ModelSymbol sourceModel,
            AstJoin term) {
        assert model != null;
        assert sourceModel != null;
        assert term != null;
        LOG.debug("processing model mapping: {}", term.mapping);

        Set<String> groupingPropertyNames = Sets.create();
        if (term.grouping != null) {
            for (AstSimpleName name : term.grouping.properties) {
                groupingPropertyNames.add(name.identifier);
            }
        }
        ModelDeclaration sourceDecl = sourceModel.findDeclaration();
        assert sourceDecl != null;
        Map<String, PropertyDeclaration> results = new HashMap<String, PropertyDeclaration>();
        if (term.mapping == null) {
            for (PropertyDeclaration prop : sourceDecl.getDeclaredProperties()) {
                PropertyDeclaration declared = model.findPropertyDeclaration(prop.getName().identifier);
                if (declared != null) {
                    LOG.debug("property {} is duplicated", prop.getSymbol());
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
            Set<String> saw = Sets.create();
            for (AstPropertyMapping property : term.mapping.properties) {
                if (saw.contains(property.target.identifier)) {
                    report(new Diagnostic(
                            Level.ERROR,
                            property,
                            "Property \"{0}\" is already defined in this join term",
                            property.target.identifier));
                    continue;
                }
                saw.add(property.target.identifier);
                PropertyDeclaration sourceProp = sourceDecl.findPropertyDeclaration(property.source.identifier);
                if (sourceProp == null) {
                    report(new Diagnostic(
                            Level.ERROR,
                            sourceModel.getName(),
                            "Property \"{0}\" is not defined in \"{1}\"",
                            property.source.identifier,
                            sourceModel.getName().identifier));
                    continue;
                }
                PropertyDeclaration declared = model.findPropertyDeclaration(property.target.identifier);
                if (declared != null) {
                    LOG.debug("property {} is duplicated", property.target);
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

    private List<MappingFactor> resolveMapping(ModelDeclaration model, ModelSymbol source, AstModelMapping mapping) {
        assert model != null;
        assert source != null;
        ModelDeclaration sourceModel = source.findDeclaration();
        assert sourceModel != null;
        List<MappingFactor> results = Lists.create();
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
        Map<String, Type> typeMap = Maps.create();
        for (ReduceTerm<AstJoin> term : terms) {
            Set<String> groupingProperties = Sets.create();
            for (PropertySymbol grouping : term.getGrouping()) {
                groupingProperties.add(grouping.getName().identifier);
            }
            for (MappingFactor factor : term.getMappings()) {
                PropertySymbol target = factor.getTarget();
                Type declared = typeMap.get(target.getName().identifier);
                if (declared == null) {
                    typeMap.put(target.getName().identifier, target.findDeclaration().getType());
                } else if (groupingProperties.contains(target.getName().identifier) == false) {
                    report(new Diagnostic(
                            Level.ERROR,
                            term.getOriginalAst(),
                            "Property \"{0}\" is already defined by other join terms",
                            target.getName().identifier));
                    green = false;
                }
            }
        }
        return green;
    }

    private void resolveSummarize(
            ModelDeclaration model,
            AstExpression<AstSummarize> expression) {
        assert model != null;
        assert expression != null;
        LOG.debug("Resolving summarized: {}", model.getName());
        List<ReduceTerm<AstSummarize>> results = Lists.create();
        for (AstSummarize term : extract(expression)) {
            LOG.debug("Resolving summarized term: {} -> {}", model.getName(), term.reference.name);
            ModelSymbol source = context.getWorld().createModelSymbol(term.reference.name);
            if (source.findDeclaration() == null) {
                report(new Diagnostic(
                        Level.ERROR,
                        term.reference,
                        "{0} is not defined",
                        term.reference.name));
                continue;
            }
            Map<String, PropertyDeclaration> properties = resolveSummarizeProperties(model, source, term);
            List<MappingFactor> foldings = resolveFolding(model, source, term.folding);
            List<PropertySymbol> grouping = resolveGrouping(model, properties, term.grouping);
            results.add(new ReduceTerm<AstSummarize>(term, source, foldings, grouping));
        }
        if (checkSummarizeTerms(model, results)) {
            model.putTrait(SummarizeTrait.class, new SummarizeTrait(expression, results));
        }
    }

    private Map<String, PropertyDeclaration> resolveSummarizeProperties(
            ModelDeclaration model, ModelSymbol source, AstSummarize term) {
        assert model != null;
        assert source != null;
        assert term != null;
        LOG.debug("processing model folding: {}", term.folding);

        ModelDeclaration decl = source.findDeclaration();
        assert decl != null;
        Map<String, PropertyDeclaration> results = new HashMap<String, PropertyDeclaration>();
        for (AstPropertyFolding property : term.folding.properties) {
            PropertyDeclaration original = decl.findPropertyDeclaration(property.source.identifier);
            if (original == null) {
                report(new Diagnostic(
                        Level.ERROR,
                        source.getName(),
                        "Property \"{0}\" is not defined in \"{1}\"",
                        property.source.identifier,
                        source.getName().identifier));
                continue;
            }
            PropertyMappingKind mapping = resolveAggregateFunction(property.aggregator);
            if (mapping == null) {
                report(new Diagnostic(
                        Level.ERROR,
                        property.aggregator,
                        "Aggregate function \"{0}\" is not defined",
                        property.aggregator.toString()));
                continue;
            }
            Type resolved = original.getType().map(mapping);
            if (resolved == null) {
                report(new Diagnostic(
                        Level.ERROR,
                        property,
                        "Failed to apply \"{0}\" to \"{1} : {2}\"",
                        property.aggregator.toString(),
                        property.source.identifier,
                        original.getType()));
                continue;
            }
            PropertyDeclaration declared = model.findPropertyDeclaration(property.target.identifier);
            if (declared != null) {
                report(new Diagnostic(
                        Level.ERROR,
                        property.target,
                        "Property \"{0}\" is already defined by other summarize terms",
                        property.target.identifier));
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

    private List<MappingFactor> resolveFolding(ModelDeclaration model, ModelSymbol source, AstModelFolding folding) {
        assert model != null;
        assert source != null;
        assert folding != null;
        ModelDeclaration sourceModel = source.findDeclaration();
        assert sourceModel != null;
        List<MappingFactor> results = Lists.create();
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

    private PropertyMappingKind resolveAggregateFunction(AstName aggregator) {
        assert aggregator != null;
        String name = aggregator.toString().toUpperCase();
        try {
            return PropertyMappingKind.valueOf(name);
        } catch (Exception e) {
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
            List<PropertySymbol> results = Lists.create();
            for (AstSimpleName name : grouping.properties) {
                PropertyDeclaration property = properties.get(name.identifier);
                if (property == null) {
                    report(new Diagnostic(
                            Level.ERROR,
                            name,
                            "Property \"{0}\" is not defined",
                            name.identifier));
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
                report(new Diagnostic(
                        Level.ERROR,
                        next.getOriginalAst(),
                        "Each term in model \"{0}\" must have the same number of grouping properties",
                        model.getName()));
                return false;
            }
            List<PropertyDeclaration> nextSources = resolveGroupingSources(next);
            assert firstSources.size() == nextSources.size();
            for (int i = 0, n = firstSources.size(); i < n; i++) {
                PropertyDeclaration left = firstSources.get(i);
                PropertyDeclaration right = nextSources.get(i);
                if (left.getType().isSame(right.getType()) == false) {
                    PropertySymbol rightSymbol = next.getGrouping().get(i);
                    report(new Diagnostic(
                            Level.ERROR,
                            rightSymbol.getOriginalAst(),
                            "Type mismatch in grouping property \"{0}\"",
                            rightSymbol.getName()));
                    green = false;
                }
            }
        }
        return green;
    }

    private List<PropertyDeclaration> resolveGroupingSources(ReduceTerm<?> term) {
        assert term != null;
        Map<PropertySymbol, PropertySymbol> rmap = new HashMap<PropertySymbol, PropertySymbol>();
        for (MappingFactor entry : term.getMappings()) {
            rmap.put(entry.getTarget(), entry.getSource());
        }
        List<PropertyDeclaration> results = new ArrayList<PropertyDeclaration>();
        for (PropertySymbol prop : term.getGrouping()) {
            PropertySymbol source = rmap.get(prop);
            if (source == null) {
                source = prop;
            }
            results.add(source.findDeclaration());
        }
        return results;
    }

    private void resolveAttributes() {
        for (ModelDeclaration model : context.getWorld().getDeclaredModels()) {
            LOG.debug("Resolving attributes: {}", model.getName());
            resolveAttributes(model);
            for (PropertyDeclaration property : model.getDeclaredProperties()) {
                resolveAttributes(property);
            }
        }
    }

    private void resolveAttributes(Declaration declaration) {
        assert declaration != null;
        for (AstAttribute attribute : declaration.getAttributes()) {
            String name = attribute.name.toString();
            LOG.debug("Resolving attribute: {} -> {}", declaration.getName(), name);
            AttributeDriver driver = context.findAttributeDriver(attribute);
            if (driver == null) {
                report(new Diagnostic(
                        Level.ERROR,
                        attribute.name,
                        "Cannot apply the attribute \"{0}\"",
                        name));
                continue;
            }
            LOG.debug("Processing attribute: {} -> {}", name, driver);
            driver.process(context.getWorld(), declaration, attribute);
        }
    }

    private <T extends AstTerm<T>> List<T> extract(AstExpression<T> expression) {
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

    private class RecordExpressionResolver extends AbstractVisitor<ModelDeclaration, Void> {

        final List<ModelSymbol> projections = Lists.create();

        /**
         * Creates and returns a new instance.
         */
        RecordExpressionResolver() {
            return;
        }

        @Override
        public <T extends AstTerm<T>> Void visitUnionExpression(
                ModelDeclaration model,
                AstUnionExpression<T> node) {
            for (T term : node.terms) {
                term.accept(model, this);
            }
            return null;
        }

        @Override
        public Void visitModelReference(ModelDeclaration model, AstModelReference node) {
            LOG.debug("processing model reference: {}", node);
            ModelDeclaration decl = context.getWorld().findModelDeclaration(node.name.identifier);
            if (decl == null) {
                report(new Diagnostic(
                        Level.ERROR,
                        node.name,
                        "Model \"{0}\" is not found",
                        node.name.identifier));
                return null;
            }
            for (PropertyDeclaration property : decl.getDeclaredProperties()) {
                PropertyDeclaration other = model.findPropertyDeclaration(property.getName().identifier);
                if (other != null) {
                    LOG.debug("property {} is duplicated", property.getSymbol());
                    if (property.getType().isSame(other.getType()) == false) {
                        report(new Diagnostic(
                                Level.ERROR,
                                node,
                                "{0} is already defined in {1} with the different type",
                                property.getName(),
                                model.getName()));
                    }
                    continue;
                }
                model.declareProperty(
                        node,
                        property.getName(),
                        property.getType(),
                        property.getDescription(),
                        property.getAttributes());
            }
            if (decl.getOriginalAst().kind == ModelDefinitionKind.PROJECTIVE) {
                projections.add(context.getWorld().createModelSymbol(node.name));
            }
            return null;
        }

        @Override
        public Void visitRecordDefinition(ModelDeclaration model, AstRecordDefinition node) {
            LOG.debug("processing record definition: {}", node);
            Set<String> sawPropertyName = Sets.create();
            for (AstPropertyDefinition property : node.properties) {
                if (sawPropertyName.contains(property.name.identifier)) {
                    report(new Diagnostic(
                            Level.ERROR,
                            property.name,
                            "Property \"{0}\" is already declared",
                            property.name.identifier));
                }
                sawPropertyName.add(property.name.identifier);
                Type type = context.resolveType(property.type);
                if (type == null) {
                    report(new Diagnostic(
                            Level.ERROR,
                            property.type,
                            "Failed to resolve type \"{0}\"",
                            property.type.toString()));
                    continue;
                }

                PropertyDeclaration other = model.findPropertyDeclaration(property.name.identifier);
                if (other != null) {
                    LOG.debug("property {} is duplicated", property.name);
                    if (type.equals(other.getType()) == false) {
                        report(new Diagnostic(
                                Level.ERROR,
                                property.name,
                                "{0} is already defined in {1} with the different type",
                                property.name,
                                model.getName()));
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
}
