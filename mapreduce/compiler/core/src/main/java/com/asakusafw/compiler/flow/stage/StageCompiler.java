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
package com.asakusafw.compiler.flow.stage;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.plan.FlowBlock;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.stage.StageModel.ResourceFragment;
import com.asakusafw.compiler.flow.stage.StageModel.Unit;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;

/**
 * Compiles {@link StageGraph}.
 * @since 0.1.0
 * @version 0.4.0
 */
public class StageCompiler {

    static final Logger LOG = LoggerFactory.getLogger(StageCompiler.class);

    private final FlowCompilingEnvironment environment;

    private final ShuffleAnalyzer shuffleAnalyzer;
    private final StageAnalyzer mapredAnalyzer;

    private final ShuffleKeyEmitter shuffleKeyEmitter;
    private final ShuffleValueEmitter shuffleValueEmitter;
    private final ShuffleGroupingComparatorEmitter shuffleGroupingEmitter;
    private final ShuffleSortComparatorEmitter shuffleSortingEmitter;
    private final ShufflePartitionerEmitter shuffleParitioningEmitter;

    private final FlowResourceEmitter flowResourceEmitter;
    private final MapFragmentEmitter mapFragmentEmitter;
    private final ShuffleFragmentEmitter shuffleFragmentEmitter;
    private final ReduceFragmentEmitter reduceFragmentEmitter;

    private final MapperEmitter mapperEmitter;
    private final ReducerEmitter reducerEmitter;
    private final CombinerEmitter combinerEmitter;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public StageCompiler(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;

        this.shuffleAnalyzer = new ShuffleAnalyzer(environment);
        this.mapredAnalyzer = new StageAnalyzer(environment);

        this.shuffleKeyEmitter = new ShuffleKeyEmitter(environment);
        this.shuffleValueEmitter = new ShuffleValueEmitter(environment);
        this.shuffleGroupingEmitter = new ShuffleGroupingComparatorEmitter(environment);
        this.shuffleSortingEmitter = new ShuffleSortComparatorEmitter(environment);
        this.shuffleParitioningEmitter = new ShufflePartitionerEmitter(environment);

        this.flowResourceEmitter = new FlowResourceEmitter(environment);
        this.mapFragmentEmitter = new MapFragmentEmitter(environment);
        this.shuffleFragmentEmitter = new ShuffleFragmentEmitter(environment);
        this.reduceFragmentEmitter = new ReduceFragmentEmitter(environment);

        this.mapperEmitter = new MapperEmitter(environment);
        this.reducerEmitter = new ReducerEmitter(environment);
        this.combinerEmitter = new CombinerEmitter(environment);
    }

    /**
     * Compiles execution stages in the {@link StageGraph}, and returns model objects of the compiled stages.
     * @param graph the target stage graph
     * @return the compiled model objects of each stage
     * @throws IOException if error was occurred while compiling the target element
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public List<StageModel> compile(StageGraph graph) throws IOException {
        Precondition.checkMustNotBeNull(graph, "graph"); //$NON-NLS-1$
        LOG.debug("compiling stages in stage graph: {}", //$NON-NLS-1$
                graph.getInput().getSource().getDescription().getName());

        Map<FlowResourceDescription, CompiledType> resourceMap = compileResources(graph);
        List<StageModel> results = new ArrayList<>();
        for (StageBlock block : graph.getStages()) {
            StageModel model = compileStage(block, resourceMap);
            results.add(model);
        }
        if (environment.hasError()) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("StageCompiler.errorFailedToCompileResource"), //$NON-NLS-1$
                    environment.getErrorMessage()));
        }
        return results;
    }

    private StageModel compileStage(
            StageBlock block,
            Map<FlowResourceDescription, CompiledType> resourceMap) throws IOException {
        assert block != null;
        assert resourceMap != null;
        LOG.debug("compiling stage block: {}", block); //$NON-NLS-1$
        StageModel model = analyze(block);
        blessResources(model, resourceMap);
        compileShuffle(model);
        compileFragments(model);
        compileUnits(model);
        return model;
    }

    private void compileUnits(StageModel model) throws IOException {
        assert model != null;
        for (StageModel.MapUnit unit : model.getMapUnits()) {
            CompiledType compiled = mapperEmitter.emit(model, unit);
            unit.setCompiled(compiled);
        }
        if (model.getReduceUnits().isEmpty() == false) {
            CompiledType compiledReducer = reducerEmitter.emit(model);
            CompiledType compiledCombiner = combinerEmitter.emit(model);
            CompiledReduce compiled = new CompiledReduce(compiledReducer, compiledCombiner);
            for (StageModel.ReduceUnit unit : model.getReduceUnits()) {
                unit.setCompiled(compiled);
            }
        }
    }

    private void compileFragments(StageModel model) throws IOException {
        assert model != null;
        StageBlock block = model.getStageBlock();
        for (StageModel.MapUnit unit : model.getMapUnits()) {
            for (StageModel.Fragment fragment : unit.getFragments()) {
                if (fragment.isCompiled()) {
                    continue;
                }
                CompiledType compiled = mapFragmentEmitter.emit(fragment, block);
                fragment.setCompiled(compiled);
            }
        }

        ShuffleModel shuffle = model.getShuffleModel();
        if (shuffle == null) {
            return;
        }
        Name keyTypeName = shuffle.getCompiled().getKeyTypeName();
        Name valueTypeName = shuffle.getCompiled().getValueTypeName();
        for (ShuffleModel.Segment segment : shuffle.getSegments()) {
            CompiledShuffleFragment fragment = shuffleFragmentEmitter.emit(
                    segment,
                    keyTypeName,
                    valueTypeName,
                    block);
            segment.setCompiled(fragment);
        }

        for (StageModel.ReduceUnit unit : model.getReduceUnits()) {
            for (StageModel.Fragment fragment : unit.getFragments()) {
                if (fragment.isCompiled()) {
                    continue;
                }
                CompiledType compiled;
                if (fragment.isRendezvous()) {
                    compiled = reduceFragmentEmitter.emit(fragment, shuffle, block);
                } else {
                    compiled = mapFragmentEmitter.emit(fragment, block);
                }
                fragment.setCompiled(compiled);
            }
        }
    }

    private StageModel analyze(StageBlock block) throws IOException {
        ShuffleModel shuffle = shuffleAnalyzer.analyze(block);
        if (shuffleAnalyzer.hasError()) {
            shuffleAnalyzer.clearError();
            throw new IOException(Messages.getString("StageCompiler.errorFailedToCompileShuffle")); //$NON-NLS-1$
        }
        StageModel model = mapredAnalyzer.analyze(block, shuffle);
        if (mapredAnalyzer.hasError()) {
            mapredAnalyzer.clearError();
            throw new IOException(Messages.getString("StageCompiler.errorFailedToCompileBlock")); //$NON-NLS-1$
        }
        return model;
    }

    private void compileShuffle(StageModel model) throws IOException {
        assert model != null;
        ShuffleModel shuffle = model.getShuffleModel();
        if (shuffle == null) {
            return;
        }
        Name keyTypeName = shuffleKeyEmitter.emit(shuffle);
        Name valueTypeName = shuffleValueEmitter.emit(shuffle);
        Name groupComparatorTypeName = shuffleGroupingEmitter.emit(shuffle, keyTypeName);
        Name sortComparatorTypeName = shuffleSortingEmitter.emit(shuffle, keyTypeName);
        Name partitionerTypeName = shuffleParitioningEmitter.emit(shuffle, keyTypeName, valueTypeName);
        CompiledShuffle compiled = new CompiledShuffle(
                keyTypeName,
                valueTypeName,
                groupComparatorTypeName,
                sortComparatorTypeName,
                partitionerTypeName);
        shuffle.setCompiled(compiled);
    }

    private Map<FlowResourceDescription, CompiledType> compileResources(
            StageGraph graph) throws IOException {
        assert graph != null;
        Set<FlowResourceDescription> resources = collectResources(graph);
        return flowResourceEmitter.emit(resources);
    }

    private Set<FlowResourceDescription> collectResources(StageGraph graph) {
        assert graph != null;
        Set<FlowResourceDescription> resources = new HashSet<>();
        for (StageBlock stage : graph.getStages()) {
            List<FlowBlock> blocks = new ArrayList<>();
            blocks.addAll(stage.getMapBlocks());
            blocks.addAll(stage.getReduceBlocks());
            for (FlowBlock block : blocks) {
                for (FlowElement element : block.getElements()) {
                    for (FlowResourceDescription resource : element.getDescription().getResources()) {
                        resources.add(resource);
                    }
                }
            }
        }
        return resources;
    }

    private void blessResources(
            StageModel model,
            Map<FlowResourceDescription, CompiledType> resourceMap) {
        assert model != null;
        assert resourceMap != null;
        List<ResourceFragment> resources = new ArrayList<>();
        List<Unit<?>> units = new ArrayList<>();
        units.addAll(model.getMapUnits());
        units.addAll(model.getReduceUnits());
        for (Unit<?> unit : units) {
            for (Fragment fragment : unit.getFragments()) {
                resources.addAll(fragment.getResources());
            }
        }
        Set<FlowResourceDescription> saw = new HashSet<>();
        for (ResourceFragment fragment : resources) {
            if (fragment.isCompiled()) {
                continue;
            }
            CompiledType resolved = resourceMap.get(fragment.getDescription());
            if (resolved == null) {
                if (saw.contains(fragment.getDescription()) == false) {
                    environment.error(
                            Messages.getString("StageCompiler.errorUnresolvedResource"), //$NON-NLS-1$
                            fragment.getDescription());
                    saw.add(fragment.getDescription());
                }
                continue;
            }
            fragment.setCompiled(resolved);
        }
    }
}
