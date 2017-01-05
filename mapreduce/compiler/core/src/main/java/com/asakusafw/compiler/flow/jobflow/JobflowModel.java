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
package com.asakusafw.compiler.flow.jobflow;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.mapreduce.OutputFormat;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.Compilable;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor;
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor.SourceInfo;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.plan.FlowBlock;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.compiler.flow.stage.StageModel;
import com.asakusafw.runtime.stage.input.TemporaryInputFormat;
import com.asakusafw.runtime.stage.output.TemporaryOutputFormat;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;

/**
 * Structural information of jobflows.
 */
public class JobflowModel extends Compilable.Trait<CompiledJobflow> {

    private final StageGraph stageGraph;

    private final String batchId;

    private final String flowId;

    private final List<Import> imports;

    private final List<Export> exports;

    private final List<Stage> stages;

    /**
     * Creates a new instance.
     * @param stageGraph the original stage graph
     * @param batchId the batch ID
     * @param flowId the flow ID
     * @param imports the import stages
     * @param exports the export stages
     * @param stages the MapReduce stages
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public JobflowModel(
            StageGraph stageGraph,
            String batchId, String flowId,
            List<Import> imports, List<Export> exports,
            List<Stage> stages) {
        Precondition.checkMustNotBeNull(stageGraph, "stageGraph"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(batchId, "batchId"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(flowId, "flowId"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(imports, "imports"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(exports, "exports"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(stages, "stages"); //$NON-NLS-1$
        this.stageGraph = stageGraph;
        this.batchId = batchId;
        this.flowId = flowId;
        this.imports = imports;
        this.exports = exports;
        this.stages = stages;
    }

    /**
     * Returns the original stage graph.
     * @return the original stage graph
     */
    public StageGraph getStageGraph() {
        return stageGraph;
    }

    /**
     * Returns the batch ID.
     * @return the batch ID
     */
    public String getBatchId() {
        return batchId;
    }

    /**
     * Returns the flow ID.
     * @return the flow ID
     */
    public String getFlowId() {
        return flowId;
    }

    /**
     * Returns information of the import stages.
     * @return the import stages
     */
    public List<Import> getImports() {
        return imports;
    }

    /**
     * Returns information of the export stages.
     * @return the export stages
     */
    public List<Export> getExports() {
        return exports;
    }

    /**
     * Returns information of the MapReduce stages.
     * @return the MapReduce stages
     */
    public List<Stage> getStages() {
        return stages;
    }

    /**
     * Returns the dependency graph of MapReduce stages.
     * @return the dependency graph
     */
    public Graph<Stage> getDependencyGraph() {
        Map<Delivery, Stage> deliveries = new HashMap<>();
        for (Stage stage : stages) {
            for (Delivery delivery : stage.getDeliveries()) {
                deliveries.put(delivery, stage);
            }
        }
        Graph<Stage> graph = Graphs.newInstance();
        for (Stage stage : stages) {
            graph.addNode(stage);
            for (Process process : stage.getProcesses()) {
                for (Source source : process.getResolvedSources()) {
                    Stage dependence = deliveries.get(source);
                    if (dependence == null) {
                        // the head stage
                        continue;
                    }
                    graph.addEdge(stage, dependence);
                }
            }
        }
        return graph;
    }

    /**
     * Structural information of MapReduce stages.
     */
    public static class Stage extends Compilable.Trait<CompiledStage> {

        private final StageModel model;

        private final List<Process> processes;

        private final List<Delivery> deliveries;

        private final Reduce reduceOrNull;

        private final Set<SideData> sideData;

        /**
         * Creates a new instance.
         * @param model the stage model
         * @param processes the processes (Map actions) in this stage
         * @param deliveries the deliveries of this stage
         * @param reduceOrNull the reducer action (nullable)
         * @param sideData the side-data for this stage
         * @throws IllegalArgumentException if some parameters are {@code null}
         */
        public Stage(
                StageModel model,
                List<Process> processes,
                List<Delivery> deliveries,
                Reduce reduceOrNull,
                Set<SideData> sideData) {
            Precondition.checkMustNotBeNull(model, "model"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(processes, "processes"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(deliveries, "deliveries"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(sideData, "sideData"); //$NON-NLS-1$
            this.model = model;
            this.processes = processes;
            this.deliveries = deliveries;
            this.reduceOrNull = reduceOrNull;
            this.sideData = sideData;
        }

        /**
         * Returns the stage number.
         * @return the stage number
         */
        public int getNumber() {
            return model.getStageBlock().getStageNumber();
        }

        /**
         * Returns the model of this stage.
         * @return the stage model
         */
        public StageModel getModel() {
            return model;
        }

        /**
         * Returns information of the Map actions in this stage.
         * @return the Map actions
         */
        public List<Process> getProcesses() {
            return processes;
        }

        /**
         * Returns information of the stage deliveries.
         * @return the stage deliveries
         */
        public List<Delivery> getDeliveries() {
            return deliveries;
        }

        /**
         * Returns information of the Reduce action.
         * @return the Reduce action, or {@code null} if this stage does not contain Reduce actions
         */
        public Reduce getReduceOrNull() {
            return reduceOrNull;
        }

        /**
         * Returns information of the side-data list for this stage.
         * @return the side-data list
         */
        public Set<SideData> getSideData() {
            return sideData;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "Stage({0})", //$NON-NLS-1$
                    String.valueOf(getNumber()));
        }
    }

    /**
     * Represents a set of configurations for Reduce actions.
     */
    public static class Reduce {

        private final Name reducerTypeName;

        private final Name combinerTypeNameOrNull;

        private final Name keyTypeName;

        private final Name valueTypeName;

        private final Name groupingComparatorTypeName;

        private final Name sortComparatorTypeName;

        private final Name partitionerTypeName;

        /**
         * Creates a new instance.
         * @param reducerTypeName the qualified class name of the reducer
         * @param combinerTypeNameOrNull the qualified class name of the reducer (nullable)
         * @param keyTypeName the qualified class name of the shuffle key
         * @param valueTypeName the qualified class name of the shuffle value
         * @param groupingComparatorTypeName the qualified class name of the grouping comparator
         * @param sortComparatorTypeName the qualified class name of the sort comparator
         * @param partitionerTypeName the qualified class name of the partitioner
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Reduce(
                Name reducerTypeName,
                Name combinerTypeNameOrNull,
                Name keyTypeName,
                Name valueTypeName,
                Name groupingComparatorTypeName,
                Name sortComparatorTypeName,
                Name partitionerTypeName) {
            Precondition.checkMustNotBeNull(reducerTypeName, "reducerTypeName"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(keyTypeName, "keyTypeName"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(valueTypeName, "valueTypeName"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(groupingComparatorTypeName, "groupingComparatorTypeName"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(sortComparatorTypeName, "sortComparatorTypeName"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(partitionerTypeName, "partitionerTypeName"); //$NON-NLS-1$
            this.reducerTypeName = reducerTypeName;
            this.combinerTypeNameOrNull = combinerTypeNameOrNull;
            this.keyTypeName = keyTypeName;
            this.valueTypeName = valueTypeName;
            this.groupingComparatorTypeName = groupingComparatorTypeName;
            this.sortComparatorTypeName = sortComparatorTypeName;
            this.partitionerTypeName = partitionerTypeName;
        }

        /**
         * Returns the qualified name of the combiner class.
         * @return the qualified class name, or {@code null} if combiner is not available
         */
        public Name getCombinerTypeNameOrNull() {
            return combinerTypeNameOrNull;
        }

        /**
         * Returns the qualified name of the reducer class.
         * @return the qualified class name
         */
        public Name getReducerTypeName() {
            return reducerTypeName;
        }

        /**
         * Returns the qualified name of the shuffle key class.
         * @return the qualified class name
         */
        public Name getKeyTypeName() {
            return keyTypeName;
        }

        /**
         * Returns the qualified name of the shuffle value class.
         * @return the qualified class name
         */
        public Name getValueTypeName() {
            return valueTypeName;
        }

        /**
         * Returns the qualified name of the grouping comparator class.
         * @return the qualified class name
         */
        public Name getGroupingComparatorTypeName() {
            return groupingComparatorTypeName;
        }

        /**
         * Returns the qualified name of the sort comparator class.
         * @return the qualified class name
         */
        public Name getSortComparatorTypeName() {
            return sortComparatorTypeName;
        }

        /**
         * Returns the qualified name of the partitioner class.
         * @return the qualified class name
         */
        public Name getPartitionerTypeName() {
            return partitionerTypeName;
        }
    }

    /**
     * An abstract super class which provides output data-sets.
     */
    public abstract static class Source {

        private final Set<FlowBlock.Output> outputs;

        /**
         * Creates a new instance.
         * @param outputs set of the corresponded output ports
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        protected Source(Set<FlowBlock.Output> outputs) {
            Precondition.checkMustNotBeNull(outputs, "outputs"); //$NON-NLS-1$
            this.outputs = outputs;
        }

        /**
         * Returns input information.
         * @return input information
         */
        public abstract SourceInfo getInputInfo();

        /**
         * Returns set of the corresponded output ports.
         * @return the corresponded output ports
         */
        public Set<FlowBlock.Output> getOutputs() {
            return outputs;
        }
    }

    /**
     * An abstract super class which accepts data-sets from {@link Source}.
     */
    public abstract static class Target {

        private final List<FlowBlock.Input> inputs;

        private Set<Source> sources;

        /**
         * Creates a new instance.
         * @param inputs set of the corresponded input ports
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public Target(List<FlowBlock.Input> inputs) {
            Precondition.checkMustNotBeNull(inputs, "inputs"); //$NON-NLS-1$
            if (inputs.isEmpty()) {
                throw new IllegalArgumentException("inputs must not be empty"); //$NON-NLS-1$
            }
            this.inputs = inputs;
        }

        /**
         * Sets opposite {@link Source} objects that provides data-sets for this target.
         * @param opposites the upstream sources
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public void resolveSources(Collection<? extends Source> opposites) {
            Precondition.checkMustNotBeNull(opposites, "opposites"); //$NON-NLS-1$
            this.sources = Sets.from(opposites);
        }

        /**
         * Returns the opposite {@link Source} objects.
         * @return the upstream sources
         * @throws IllegalStateException if they have been not set yet
         * @see #resolveSources(Collection)
         */
        public Set<Source> getResolvedSources() {
            if (sources == null) {
                throw new IllegalStateException();
            }
            return sources;
        }

        /**
         * Returns the locations where the upstream data-sets will be stored.
         * @return the upstream data-set locations
         * @throws IllegalStateException if the opposite {@link Source} objects are not set
         * @see #resolveSources(Collection)
         */
        public Set<Location> getResolvedLocations() {
            Set<Location> results = new HashSet<>();
            for (Source source : getResolvedSources()) {
                results.addAll(source.getInputInfo().getLocations());
            }
            return results;
        }

        /**
         * Returns the corresponded input ports.
         * @return the corresponded input ports
         */
        public List<FlowBlock.Input> getInputs() {
            return inputs;
        }

        /**
         * Returns the data type.
         * @return the data type
         */
        public java.lang.reflect.Type getDataType() {
            if (inputs.isEmpty()) {
                return void.class;
            }
            return inputs.get(0).getElementPort().getDescription().getDataType();
        }
    }

    /**
     * An abstract super interface for external I/O models.
     */
    public interface Processible {

        /**
         * Returns the processor for processing this external I/O operations.
         * @return the related processor
         */
        ExternalIoDescriptionProcessor getProcessor();
    }

    /**
     * Represents a set of configurations for Map actions.
     */
    public static class Process extends Target {

        private final Name mapperTypeName;

        /**
         * Creates a new instance.
         * @param inputs the corresponded input ports
         * @param mapperTypeName the qualified class name of the mapper
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Process(List<FlowBlock.Input> inputs, Name mapperTypeName) {
            super(inputs);
            Precondition.checkMustNotBeNull(mapperTypeName, "mapperTypeName"); //$NON-NLS-1$
            this.mapperTypeName = mapperTypeName;
        }

        /**
         * Returns the qualified name of the mapper class.
         * @return the qualified class name
         */
        public Name getMapperTypeName() {
            return mapperTypeName;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "Process(inputs={0}, mapper={1})", //$NON-NLS-1$
                    getInputs(),
                    getMapperTypeName());
        }
    }

    /**
     * Represents deliveries from some actions.
     */
    public static class Delivery extends Source {

        private final Set<Location> locations;

        /**
         * Creates a new instance.
         * @param outputs the corresponded output ports
         * @param locations the target output locations
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Delivery(Set<FlowBlock.Output> outputs, Set<Location> locations) {
            super(outputs);
            Precondition.checkMustNotBeNull(locations, "locations"); //$NON-NLS-1$
            this.locations = locations;
        }

        /**
         * Returns the data type.
         * @return the data type
         */
        public java.lang.reflect.Type getDataType() {
            FlowBlock.Output first = getOutputs().iterator().next();
            FlowElementOutput port = first.getElementPort();
            return port.getDescription().getDataType();
        }

        @Override
        public SourceInfo getInputInfo() {
            return new SourceInfo(locations, TemporaryInputFormat.class);
        }

        /**
         * Returns the Hadoop {@link OutputFormat} class for storing this delivery.
         * @return the Hadoop {@link OutputFormat} class
         */
        @SuppressWarnings("rawtypes")
        public Class<? extends OutputFormat> getOutputFormatType() {
            return TemporaryOutputFormat.class;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "Delivery(output={0}, locations={1})", //$NON-NLS-1$
                    getOutputs(),
                    getInputInfo().getLocations());
        }
    }

    /**
     * Represents import actions that obtains data-sets from external inputs.
     */
    public static class Import extends Source implements Processible {

        private final InputDescription description;

        private final ExternalIoDescriptionProcessor processor;

        /**
         * Creates a new instance.
         * @param description the input description
         * @param processor the external I/O processor for processing this
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Import(InputDescription description, ExternalIoDescriptionProcessor processor) {
            super(Collections.emptySet());
            Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(processor, "processor"); //$NON-NLS-1$
            this.description = description;
            this.processor = processor;
        }

        /**
         * Creates a new instance.
         * @param output the target output port
         * @param description the input description
         * @param processor the external I/O processor for processing this
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Import(
                FlowBlock.Output output, InputDescription description,
                ExternalIoDescriptionProcessor processor) {
            super(Collections.singleton(output));
            Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(processor, "processor"); //$NON-NLS-1$
            this.description = description;
            this.processor = processor;
        }

        /**
         * Returns the ID of this action.
         * @return the action ID
         */
        public String getId() {
            return description.getName();
        }

        @Override
        public SourceInfo getInputInfo() {
            return processor.getInputInfo(description);
        }

        /**
         * Returns a Hadoop {@link OutputFormat} class which generates the output data-set from this importer.
         * @return the Hadoop {@link OutputFormat} class
         */
        @SuppressWarnings("rawtypes")
        public Class<? extends OutputFormat> getOutputFormatType() {
            return TemporaryOutputFormat.class;
        }

        /**
         * Returns the input description.
         * @return the input description
         */
        public InputDescription getDescription() {
            return description;
        }

        @Override
        public ExternalIoDescriptionProcessor getProcessor() {
            return processor;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "Import(output={0}, locations={1}, description={2})", //$NON-NLS-1$
                    getOutputs(),
                    getInputInfo().getLocations(),
                    getDescription());
        }
    }

    /**
     * Represents export actions that write data-sets into external outputs.
     */
    public static class Export extends Target implements Processible {

        private final OutputDescription description;

        private final ExternalIoDescriptionProcessor processor;

        /**
         * Creates a new instance.
         * @param inputs the source input port
         * @param description the output description
         * @param processor the external I/O processor for processing this
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Export(
                List<FlowBlock.Input> inputs,
                OutputDescription description,
                ExternalIoDescriptionProcessor processor) {
            super(inputs);
            Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(processor, "processor"); //$NON-NLS-1$
            this.description = description;
            this.processor = processor;
        }

        /**
         * Returns the ID of this action.
         * @return the action ID
         */
        public String getId() {
            return description.getName();
        }

        /**
         * Returns the output description.
         * @return the output description
         */
        public OutputDescription getDescription() {
            return description;
        }

        @Override
        public ExternalIoDescriptionProcessor getProcessor() {
            return processor;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "Export(inputs={0}, description={1})", //$NON-NLS-1$
                    getInputs(),
                    getDescription());
        }
    }

    /**
     * Information of side-data.
     */
    public static class SideData {

        private final Set<Location> clusterPaths;

        private final String localName;

        /**
         * Creates a new instance.
         * @param clusterPaths the remote path of the target data
         * @param localName the unique local name
         * @throws IllegalArgumentException if parameters are {@code null}
         */
        public SideData(Set<Location> clusterPaths, String localName) {
            Precondition.checkMustNotBeNull(clusterPaths, "clusterPath"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(localName, "localName"); //$NON-NLS-1$
            this.clusterPaths = clusterPaths;
            this.localName = localName;
        }

        /**
         * Returns the remote path of the target data.
         * @return the remote path of the target data
         */
        public Set<Location> getClusterPaths() {
            return clusterPaths;
        }

        /**
         * Returns the unique local name.
         * @return the unique local name
         */
        public String getLocalName() {
            return localName;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "SideData(path={0}, name={1})", //$NON-NLS-1$
                    getClusterPaths(),
                    getLocalName());
        }
    }
}
