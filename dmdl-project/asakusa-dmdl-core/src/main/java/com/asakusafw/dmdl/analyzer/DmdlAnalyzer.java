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
package com.asakusafw.dmdl.analyzer;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.spi.AttributeDriver;
import com.asakusafw.dmdl.spi.TypeDriver;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;

/**
 * Analyzes DMDL AST and builds DMDL semantic models.
 * @since 0.2.0
 * @version 0.9.2
 */
public class DmdlAnalyzer {

    static final Logger LOG = LoggerFactory.getLogger(DmdlAnalyzer.class);

    final Context context;

    private final Graph<String> modelDependencies;

    private final DmdlAnalyzerEnhancer enhancer;

    /**
     * Creates and returns a new instance.
     * @param typeDrivers type resolvers
     * @param attributeDrivers attributed analyzers
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DmdlAnalyzer(
            Iterable<? extends TypeDriver> typeDrivers,
            Iterable<? extends AttributeDriver> attributeDrivers) {
        this(DmdlAnalyzerEnhancer.NULL, typeDrivers, attributeDrivers);
    }

    /**
     * Creates and returns a new instance.
     * @param enhancer enhances this analyzer
     * @param typeDrivers type resolvers
     * @param attributeDrivers attributed analyzers
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.7.1
     */
    public DmdlAnalyzer(
            DmdlAnalyzerEnhancer enhancer,
            Iterable<? extends TypeDriver> typeDrivers,
            Iterable<? extends AttributeDriver> attributeDrivers) {
        if (enhancer == null) {
            throw new IllegalArgumentException("enhancer must not be null"); //$NON-NLS-1$
        }
        if (typeDrivers == null) {
            throw new IllegalArgumentException("typeDrivers must not be null"); //$NON-NLS-1$
        }
        if (attributeDrivers == null) {
            throw new IllegalArgumentException("attributeDrivers must not be null"); //$NON-NLS-1$
        }
        this.context = new Context(new DmdlSemantics(), typeDrivers, attributeDrivers);
        this.modelDependencies = Graphs.newInstance();
        this.enhancer = enhancer;
    }

    /**
     * Adds a model definition to this analyzer.
     * @param definition the model definition
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void addModel(AstModelDefinition<?> definition) {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        DmdlSemantics world = context.getWorld();
        if (world.findModelDeclaration(definition.name.identifier) != null) {
            context.error(
                    definition.name,
                    Messages.getString("DmdlAnalyzer.diagnosticModelDuplicated"), //$NON-NLS-1$
                    definition.name.identifier);
        } else {
            ModelDeclarationProcessor.validate(context, definition);
            world.declareModel(
                    definition,
                    definition.name,
                    definition.description,
                    definition.attributes);
            computeDependencies(definition);
            enhancer.validateSyntax(world, definition);
        }
    }

    private void computeDependencies(AstModelDefinition<?> definition) {
        assert definition != null;
        LOG.debug("Computing dependencies: {}", definition.name); //$NON-NLS-1$
        Set<AstSimpleName> references = ModelSymbolCollector.collect(definition);
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
        verifyAttributes();
        checkDiagnostics();
        return context.getWorld();
    }

    private void checkDiagnostics() throws DmdlSemanticException {
        if (context.hasError()) {
            throw new DmdlSemanticException(
                    Messages.getString("DmdlAnalyzer.errorSemantics"), //$NON-NLS-1$
                    context.getWorld().getDiagnostics());
        }
    }

    private void resolveSymbols() {
        LOG.debug("Resolving symbols"); //$NON-NLS-1$
        Set<Set<String>> circuits = Graphs.findCircuit(modelDependencies);
        if (circuits.isEmpty() == false) {
            for (Set<String> loop : circuits) {
                for (String modelName : loop) {
                    AstSimpleName node = null;
                    ModelDeclaration md = context.getWorld().findModelDeclaration(modelName);
                    if (md != null) {
                        node = md.getName();
                    }
                    context.error(
                            node,
                            Messages.getString("DmdlAnalyzer.diagnosticCyclicDependencies"), //$NON-NLS-1$
                            modelName,
                            loop);
                }
            }
            return;
        }

        for (String name : Graphs.sortPostOrder(modelDependencies)) {
            ModelDeclaration model = context.getWorld().findModelDeclaration(name);
            if (model == null) {
                // means "model have not been declared, but is referred."
                continue;
            }
            ModelDeclarationProcessor.resolve(context, model, model.getOriginalAst());
        }
    }

    private void resolveAttributes() {
        for (ModelDeclaration model : context.getWorld().getDeclaredModels()) {
            AttributeProcessor.resolve(context, model);
        }
    }

    private void verifyAttributes() {
        for (ModelDeclaration model : context.getWorld().getDeclaredModels()) {
            AttributeProcessor.verify(context, model);
        }
    }
}
