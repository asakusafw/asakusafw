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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.FlowElementProcessor;
import com.asakusafw.compiler.flow.FlowElementProcessor.Kind;
import com.asakusafw.compiler.flow.plan.FlowBlock;
import com.asakusafw.compiler.flow.plan.FlowGraphUtil;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.stage.StageModel.Factor;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.stage.StageModel.MapUnit;
import com.asakusafw.compiler.flow.stage.StageModel.ReduceUnit;
import com.asakusafw.compiler.flow.stage.StageModel.ResourceFragment;
import com.asakusafw.compiler.flow.stage.StageModel.Sink;
import com.asakusafw.compiler.flow.stage.StageModel.Unit;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementKind;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;

/**
 * Analyzes Hadoop MapReduce stages.
 */
public class StageAnalyzer {

    static final Logger LOG = LoggerFactory.getLogger(StageAnalyzer.class);

    private final FlowCompilingEnvironment environment;

    private boolean sawError;

    /**
     * Creates a new instance.
     * @param environment the current environment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public StageAnalyzer(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
    }

    /**
     * Returns whether this analysis result contains any erroneous information or not.
     * @return {@code true} if this contains any erroneous information, otherwise {@code false}
     */
    public boolean hasError() {
        return sawError;
    }

    /**
     * Clears analysis errors.
     * @see #hasError()
     */
    public void clearError() {
        sawError = false;
    }

    /**
     * Analyzes the target stage block and returns its MapReduce job model.
     * @param block the target stage block
     * @param shuffle the analyzed shuffle model in the target stage (nullable)
     * @return corresponded MapReduce job model, or {@code null} if the target stage block is something wrong
     * @throws IllegalArgumentException if the {@code block} is {@code null}
     */
    public StageModel analyze(StageBlock block, ShuffleModel shuffle) {
        Precondition.checkMustNotBeNull(block, "block"); //$NON-NLS-1$
        LOG.debug("start analyzing stage: {}", block); //$NON-NLS-1$

        Context context = new Context(environment);
        List<MapUnit> mapUnits = new ArrayList<>();
        for (FlowBlock flowBlock : block.getMapBlocks()) {
            mapUnits.addAll(collectMapUnits(context, flowBlock));
        }
        mapUnits = composeMapUnits(mapUnits);

        List<ReduceUnit> reduceUnits = new ArrayList<>();
        for (FlowBlock flowBlock : block.getReduceBlocks()) {
            reduceUnits.addAll(collectReduceUnits(context, flowBlock));
        }

        List<Sink> outputs = new ArrayList<>();
        if (block.hasReduceBlocks()) {
            outputs.addAll(collectOutputs(context, reduceUnits, block.getReduceBlocks()));
        } else {
            outputs.addAll(collectOutputs(context, mapUnits, block.getMapBlocks()));
        }
        if (hasError()) {
            return null;
        }

        StageModel model = new StageModel(block, mapUnits, shuffle, reduceUnits, outputs);

        LOG.debug("finish analyzing stage: {} ({})", block, model); //$NON-NLS-1$
        return model;
    }

    private List<MapUnit> collectMapUnits(
            Context context,
            FlowBlock block) {
        assert context != null;
        assert block != null;
        LOG.debug("extracting mapper information: {}", block); //$NON-NLS-1$
        Set<FlowElement> startElements = collectFragmentStartElements(block);
        Map<FlowElement, Fragment> fragments = collectFragments(context, startElements);
        Graph<Fragment> fgraph = buildFragmentGraph(fragments);
        List<MapUnit> units = buildMapUnits(context, block, fragments, fgraph);
        return units;
    }

    private List<MapUnit> composeMapUnits(List<MapUnit> mapUnits) {
        assert mapUnits != null;
        Map<Set<FlowBlock.Output>, List<MapUnit>> sameInputs = new HashMap<>();
        for (MapUnit unit : mapUnits) {
            Set<FlowBlock.Output> sources = new HashSet<>();
            for (FlowBlock.Input input : unit.getInputs()) {
                for (FlowBlock.Connection conn : input.getConnections()) {
                    sources.add(conn.getUpstream());
                }
            }
            Maps.addToList(sameInputs, sources, unit);
        }
        List<MapUnit> results = new ArrayList<>();
        for (Map.Entry<Set<FlowBlock.Output>, List<MapUnit>> entry : sameInputs.entrySet()) {
            Set<FlowBlock.Output> sources = entry.getKey();
            List<MapUnit> group = entry.getValue();
            results.add(compose(sources, group));
        }
        return results;
    }

    private MapUnit compose(Set<FlowBlock.Output> sources, List<MapUnit> group) {
        assert sources != null;
        assert group != null;
        assert group.isEmpty() == false;
        if (group.size() == 1) {
            return group.get(0);
        }
        Set<FlowBlock.Input> sawInputs = new HashSet<>();
        List<FlowBlock.Input> inputs = new ArrayList<>();
        List<Fragment> fragments = new ArrayList<>();

        for (MapUnit unit : group) {
            fragments.addAll(unit.getFragments());
            for (FlowBlock.Input input : unit.getInputs()) {
                if (sawInputs.contains(input)) {
                    continue;
                }
                sawInputs.add(input);
                inputs.add(input);
            }
        }
        return new MapUnit(inputs, fragments);
    }

    private List<ReduceUnit> collectReduceUnits(
            Context context,
            FlowBlock block) {
        assert context != null;
        assert block != null;
        LOG.debug("extracting reducer information: {}", block); //$NON-NLS-1$
        Set<FlowElement> startElements = collectFragmentStartElements(block);
        Map<FlowElement, Fragment> fragments = collectFragments(context, startElements);
        Graph<Fragment> fgraph = buildFragmentGraph(fragments);
        List<ReduceUnit> units = buildReduceUnits(context, block, fragments, fgraph);
        return units;
    }

    private List<MapUnit> buildMapUnits(
            Context context,
            FlowBlock block,
            Map<FlowElement, Fragment> fragments,
            Graph<Fragment> fgraph) {
        assert context != null;
        assert block != null;
        assert fragments != null;
        assert fgraph != null;
        LOG.debug("extracting mapper information: {}", block); //$NON-NLS-1$
        Map<FlowBlock.Input, Graph<Fragment>> streams = new LinkedHashMap<>();
        for (FlowBlock.Input blockInput : block.getBlockInputs()) {
            FlowElementInput input = blockInput.getElementPort();
            Fragment head = fragments.get(input.getOwner());
            Graph<Fragment> subgraph = createSubgraph(head, fgraph);
            streams.put(blockInput, subgraph);
        }
        List<MapUnit> results = new ArrayList<>();
        for (Map.Entry<FlowBlock.Input, Graph<Fragment>> entry : streams.entrySet()) {
            FlowBlock.Input input = entry.getKey();
            Graph<Fragment> subgraph = entry.getValue();
            List<Fragment> body = sort(subgraph);
            for (int i = 0, n = body.size(); i < n; i++) {
                body.set(i, body.get(i));
            }
            MapUnit unit = new MapUnit(Collections.singletonList(input), body);
            results.add(unit);
        }
        return results;
    }

    private List<ReduceUnit> buildReduceUnits(
            Context context,
            FlowBlock block,
            Map<FlowElement, Fragment> fragments,
            Graph<Fragment> fgraph) {
        assert context != null;
        assert block != null;
        assert fragments != null;
        assert fgraph != null;
        Map<FlowElement, List<FlowBlock.Input>> inputGroups = new LinkedHashMap<>();
        for (FlowBlock.Input blockInput : block.getBlockInputs()) {
            FlowElement element = blockInput.getElementPort().getOwner();
            Maps.addToList(inputGroups, element, blockInput);
        }

        Map<FlowElement, Graph<Fragment>> streams = new HashMap<>();

        for (FlowElement element : inputGroups.keySet()) {
            Fragment head = fragments.get(element);
            Graph<Fragment> subgraph = createSubgraph(head, fgraph);
            streams.put(element, subgraph);
        }
        List<ReduceUnit> results = new ArrayList<>();
        for (Map.Entry<FlowElement, Graph<Fragment>> entry : streams.entrySet()) {
            FlowElement element = entry.getKey();
            Graph<Fragment> subgraph = entry.getValue();
            List<Fragment> body = sort(subgraph);
            for (int i = 0, n = body.size(); i < n; i++) {
                body.set(i, body.get(i));
            }
            List<FlowBlock.Input> inputs = inputGroups.get(element);
            ReduceUnit unit = new ReduceUnit(inputs, body);
            results.add(unit);
        }
        return results;
    }

    private List<Sink> collectOutputs(
            Context context,
            Collection<? extends Unit<?>> units,
            Collection<FlowBlock> blocks) {
        assert context != null;
        assert units != null;
        assert blocks != null;
        Set<FlowElementOutput> candidates = new HashSet<>();
        for (FlowBlock block : blocks) {
            for (FlowBlock.Output blockOutput : block.getBlockOutputs()) {
                candidates.add(blockOutput.getElementPort());
            }
        }
        Set<FlowElementOutput> outputs = new HashSet<>();
        for (Unit<?> unit : units) {
            for (Fragment fragment : unit.getFragments()) {
                for (FlowElementOutput output : fragment.getOutputPorts()) {
                    if (candidates.contains(output)) {
                        outputs.add(output);
                    }
                }
            }
        }

        Map<Set<FlowBlock.Input>, Set<FlowBlock.Output>> opposites = new HashMap<>();
        for (FlowBlock block : blocks) {
            for (FlowBlock.Output blockOutput : block.getBlockOutputs()) {
                // ignores disconnected outputs
                if (outputs.contains(blockOutput.getElementPort()) == false) {
                    continue;
                }
                // grouping outputs by having the same downstream
                Set<FlowBlock.Input> downstream = new HashSet<>();
                for (FlowBlock.Connection connection : blockOutput.getConnections()) {
                    downstream.add(connection.getDownstream());
                }
                Maps.addToSet(opposites, downstream, blockOutput);
            }
        }

        List<Sink> results = new ArrayList<>();
        for (Set<FlowBlock.Output> group : opposites.values()) {
            String name = context.names.create("result").getToken(); //$NON-NLS-1$
            results.add(new Sink(group, context.names.create(name).getToken()));
        }
        return results;
    }

    private List<Fragment> sort(Graph<Fragment> subgraph) {
        assert subgraph != null;
        Graph<Fragment> tgraph = Graphs.transpose(subgraph);
        List<Fragment> sorted = Graphs.sortPostOrder(tgraph);
        return sorted;
    }

    private Graph<Fragment> createSubgraph(Fragment head, Graph<Fragment> fgraph) {
        assert head != null;
        assert fgraph != null;
        Set<Fragment> path = Graphs.collectAllConnected(fgraph, Collections.singleton(head));
        path.add(head);
        Graph<Fragment> result = Graphs.newInstance();
        for (Fragment fragment : path) {
            result.addNode(fragment);
            for (Fragment successor : fgraph.getConnected(fragment)) {
                if (path.contains(successor)) {
                    result.addEdge(fragment, successor);
                }
            }
        }
        return result;
    }

    private Graph<Fragment> buildFragmentGraph(
            Map<FlowElement, Fragment> fragments) {
        assert fragments != null;
        Graph<Fragment> result = Graphs.newInstance();
        for (Fragment fragment : fragments.values()) {
            result.addNode(fragment);
            for (FlowElementOutput output : fragment.getOutputPorts()) {
                for (FlowElementInput next : output.getOpposites()) {
                    Fragment successor = fragments.get(next.getOwner());
                    assert successor != null;
                    result.addEdge(fragment, successor);
                }
            }
        }
        return result;
    }

    private Map<FlowElement, Fragment> collectFragments(
            Context context,
            Set<FlowElement> startElements) {
        assert context != null;
        assert startElements != null;
        Map<FlowElement, Fragment> results = new HashMap<>();
        for (FlowElement element : startElements) {
            Fragment fragment = getFragment(context, element, startElements);
            assert results.containsKey(element) == false;
            results.put(element, fragment);
        }
        return results;
    }

    private Fragment getFragment(
            Context context,
            FlowElement element,
            Set<FlowElement> startElements) {
        assert context != null;
        assert element != null;
        assert startElements != null;
        FlowElement current = element;
        List<Factor> factors = new ArrayList<>();
        List<ResourceFragment> resources = new ArrayList<>();
        while (true) {
            Factor factor = getFactor(current);
            if (factor == null) {
                break;
            }
            factors.add(factor);
            resources.addAll(getResources(current));
            if (factor.isLineEnd()) {
                break;
            }
            Set<FlowElement> successors = FlowGraphUtil.getSuccessors(current);
            if (successors.size() != 1) {
                break;
            }
            FlowElement next = successors.iterator().next();
            if (startElements.contains(next)) {
                break;
            }
            current = next;
        }
        return new Fragment(context.getNextFragmentNumber(), factors, resources);
    }

    private Set<FlowElement> collectFragmentStartElements(FlowBlock block) {
        assert block != null;

        Set<FlowElement> outputs = new HashSet<>();
        for (FlowBlock.Output blockOutput : block.getBlockOutputs()) {
            outputs.add(blockOutput.getElementPort().getOwner());
        }

        Set<FlowElement> results = new HashSet<>();
        for (FlowBlock.Input blockInput : block.getBlockInputs()) {
            results.add(blockInput.getElementPort().getOwner());
        }
        for (FlowElement element : block.getElements()) {

            // element whose upstream != 1 will be fragment head
            Set<FlowElement> predecessors = FlowGraphUtil.getPredecessors(element);
            if (predecessors.size() != 1) {
                results.add(element);
            }

            Set<FlowElement> successors = FlowGraphUtil.getSuccessors(element);
            if (successors.size() >= 2 || element.getOutputPorts().size() >= 2) {
                // successors of the element w/ 1> outputs will be fragment head
                results.addAll(successors);
            } else if (outputs.contains(element)) {
                // successors of the element which connected to block output will be fragment head
                results.addAll(successors);
            } else if (isFragmentEnd(element)) {
                // successors of the element which is tail of fragment will be fragment head
                results.addAll(successors);
            }
        }
        return results;
    }

    private boolean isFragmentEnd(FlowElement element) {
        assert element != null;
        FlowElementDescription description = element.getDescription();
        if (description.getKind() == FlowElementKind.PSEUD) {
            return false;
        }
        assert description.getKind() == FlowElementKind.OPERATOR;
        FlowElementProcessor.Repository repo = environment.getProcessors();
        FlowElementProcessor processor = repo.findProcessor(description);
        if (processor == null) {
            error(Messages.getString("StageAnalyzer.errorMissingProcessor"), description); //$NON-NLS-1$
            return false;
        }
        return processor.getKind() == Kind.LINE_END
            || processor.getKind() == Kind.RENDEZVOUS;
    }

    private Factor getFactor(FlowElement element) {
        assert element != null;
        FlowElementProcessor.Repository repo = environment.getProcessors();
        FlowElementDescription description = element.getDescription();
        if (description.getKind() == FlowElementKind.PSEUD) {
            return new Factor(element, repo.getEmptyProcessor());
        }
        FlowElementProcessor processor = repo.findProcessor(description);
        if (processor == null) {
            error(Messages.getString("StageAnalyzer.errorMissingProcessor"), description); //$NON-NLS-1$
            return new Factor(element, repo.getEmptyProcessor());
        }
        return new Factor(element, processor);
    }

    private List<ResourceFragment> getResources(FlowElement element) {
        assert element != null;
        List<FlowResourceDescription> resources = element.getDescription().getResources();
        if (resources.isEmpty()) {
            return Collections.emptyList();
        }
        List<ResourceFragment> results = new ArrayList<>();
        for (FlowResourceDescription description : resources) {
            results.add(new ResourceFragment(description));
        }
        return results;
    }

    private void error(String format, Object...args) {
        environment.error(format, args);
        sawError = true;
    }

    private static class Context {

        final NameGenerator names;

        private int fragmentSerialNumber = 0;

        Context(FlowCompilingEnvironment environment) {
            names = new NameGenerator(environment.getModelFactory());
        }

        int getNextFragmentNumber() {
            return ++fragmentSerialNumber;
        }
    }
}
