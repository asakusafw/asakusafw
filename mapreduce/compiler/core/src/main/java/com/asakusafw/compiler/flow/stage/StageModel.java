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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.Compilable;
import com.asakusafw.compiler.flow.FlowElementProcessor;
import com.asakusafw.compiler.flow.FlowElementProcessor.Kind;
import com.asakusafw.compiler.flow.RendezvousProcessor;
import com.asakusafw.compiler.flow.plan.FlowBlock;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.flow.Rendezvous;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.InputDescription;

/**
 * Represents detail of MapReduce job stages.
 * @see ShuffleModel
 */
public class StageModel {

    private final StageBlock stageBlock;

    private final List<MapUnit> mapUnits;

    private final ShuffleModel shuffleModel;

    private final List<ReduceUnit> reduceUnits;

    private final List<Sink> sinks;

    /**
     * Creates a new instance.
     * @param stageBlock the target stage block
     * @param mapUnits the map units in the target stage
     * @param shuffleModel the shuffle model of the target stage (nullable)
     * @param reduceUnits the reduce units in the target stage
     * @param sinks the outputs in the target stage
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public StageModel(
            StageBlock stageBlock,
            List<MapUnit> mapUnits,
            ShuffleModel shuffleModel,
            List<ReduceUnit> reduceUnits,
            List<Sink> sinks) {
        Precondition.checkMustNotBeNull(stageBlock, "stageBlock"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(mapUnits, "mapUnits"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(reduceUnits, "reduceUnits"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(sinks, "sinks"); //$NON-NLS-1$
        this.stageBlock = stageBlock;
        this.shuffleModel = shuffleModel;
        int unitSerial = 1;
        for (MapUnit unit : mapUnits) {
            unit.renumberUnit(unitSerial++);
        }
        for (ReduceUnit unit : reduceUnits) {
            unit.renumberUnit(unitSerial++);
        }
        this.mapUnits = mapUnits;
        this.reduceUnits = reduceUnits;
        this.sinks = sinks;
    }

    /**
     * Returns the target stage block.
     * @return the target stage block
     */
    public StageBlock getStageBlock() {
        return stageBlock;
    }

    /**
     * Returns the map units of this stage.
     * @return the map units
     */
    public List<MapUnit> getMapUnits() {
        return mapUnits;
    }

    /**
     * Returns the shuffle model of this stage.
     * @return the shuffle model, or {@code null} if this stage does not require shuffle operations
     */
    public ShuffleModel getShuffleModel() {
        return shuffleModel;
    }

    /**
     * Returns the reduce units of this stage.
     * @return the reduce units, or an empty list if this stage does not require reduce operations
     */
    public List<ReduceUnit> getReduceUnits() {
        return reduceUnits;
    }

    /**
     * Returns input descriptions which will be used for external resources in this stage.
     * @return input description of the external resources
     */
    public Set<InputDescription> getSideDataInputs() {
        Set<ResourceFragment> resources = new HashSet<>();
        List<Unit<?>> units = new ArrayList<>();
        units.addAll(getMapUnits());
        units.addAll(getReduceUnits());
        for (Unit<?> unit : units) {
            for (Fragment fragment : unit.getFragments()) {
                resources.addAll(fragment.getResources());
            }
        }
        Set<InputDescription> results = new HashSet<>();
        for (ResourceFragment resource : resources) {
            results.addAll(resource.getDescription().getSideDataInputs());
        }
        return results;
    }

    /**
     * Returns the outputs of this stage.
     * @return the outputs
     */
    public List<Sink> getStageResults() {
        return sinks;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Stage(map={0}, shuffle={1}, reduce={2})", //$NON-NLS-1$
                getMapUnits(),
                getShuffleModel(),
                getReduceUnits());
    }

    /**
     * An abstract super class of map/reduce operation units.
     * @param <T> the compiled model type
     */
    public abstract static class Unit<T> extends Compilable.Trait<T> {

        private final List<FlowBlock.Input> inputs;

        private final List<Fragment> fragments;

        private int serialNumber = -1;

        /**
         * Creates a new instance.
         * @param inputs the input ports of this operation unit
         * @param fragments the operation fragments
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Unit(List<FlowBlock.Input> inputs, List<Fragment> fragments) {
            Precondition.checkMustNotBeNull(inputs, "inputs"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(fragments, "fragments"); //$NON-NLS-1$
            this.inputs = inputs;
            this.fragments = fragments;
        }

        /**
         * Returns the serial number of this stage.
         * @return the stage number
         * @throws IllegalStateException if the stage number has been not set
         */
        public int getSerialNumber() {
            if (serialNumber < 0) {
                throw new IllegalStateException();
            }
            return serialNumber;
        }

        /**
         * Returns the input ports of this unit.
         * @return the input ports
         */
        public List<FlowBlock.Input> getInputs() {
            return inputs;
        }

        /**
         * Returns the operation fragments.
         * @return the operation fragments
         */
        public List<Fragment> getFragments() {
            return fragments;
        }

        boolean hasSerialNumber() {
            return serialNumber >= 0;
        }

        void renumberUnit(int serial) {
            this.serialNumber = serial;
        }
    }

    /**
     * Represents a map operation unit.
     */
    public static class MapUnit extends Unit<CompiledType> {

        /**
         * Creates a new instance.
         * @param inputs the input ports of this operation unit
         * @param fragments the operation fragments
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public MapUnit(List<FlowBlock.Input> inputs, List<Fragment> fragments) {
            super(inputs, fragments);
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "MapUnit({2})'{'inputs={0}, fragments={1}'}'", //$NON-NLS-1$
                    getInputs(),
                    getFragments(),
                    hasSerialNumber() ? String.valueOf(getSerialNumber()) : "?"); //$NON-NLS-1$
        }
    }

    /**
     * Represents a reduce operation unit.
     */
    public static class ReduceUnit extends Unit<CompiledReduce> {

        /**
         * Creates a new instance.
         * @param inputs the input ports of this operation unit
         * @param fragments the operation fragments
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public ReduceUnit(List<FlowBlock.Input> inputs, List<Fragment> fragments) {
            super(inputs, fragments);
        }

        /**
         * Returns whether this unit allows combine operations or not.
         * @return {@code true} if this unit allows combine operations, otherwise {@code false}
         */
        public boolean canCombine() {
            List<Fragment> fragments = getFragments();
            if (fragments.isEmpty()) {
                return false;
            }
            Fragment headFragment = fragments.get(0);
            return headFragment.canCombine();
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "ReduceUnit({2})'{'inputs={0}, fragments={1}'}'", //$NON-NLS-1$
                    getInputs(),
                    getFragments(),
                    hasSerialNumber() ? String.valueOf(getSerialNumber()) : "?"); //$NON-NLS-1$
        }
    }

    /**
     * Represents an operation fragment.
     */
    public static class Fragment extends Compilable.Trait<CompiledType> {

        private final int serialNumber;

        private final List<Factor> factors;

        private final List<ResourceFragment> resources;

        /**
         * Creates a new instance.
         * @param serialNumber the serial number
         * @param factors operation factors in this fragment
         * @param resources external resources in this fragment
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Fragment(int serialNumber, List<Factor> factors, List<ResourceFragment> resources) {
            Precondition.checkMustNotBeNull(factors, "factors"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(resources, "resources"); //$NON-NLS-1$
            if (factors.isEmpty()) {
                throw new IllegalArgumentException();
            }
            Factor first = factors.get(0);
            if (first.getElement().getInputPorts().size() != 1 && first.isRendezvous() == false) {
                throw new IllegalArgumentException();
            }
            if (factors.size() >= 2 && first.isRendezvous()) {
                throw new IllegalArgumentException();
            }
            this.serialNumber = serialNumber;
            this.factors = Lists.from(factors);
            this.resources = resources;
        }

        /**
         * Returns the serial number of this fragment.
         * @return the serial number
         */
        public int getSerialNumber() {
            return serialNumber;
        }

        /**
         * Returns whether this fragment allows combine operations or not.
         * @return {@code true} if this fragment allows combine operations, otherwise {@code false}
         */
        public boolean canCombine() {
            if (isRendezvous() == false) {
                return false;
            }
            Factor first = factors.get(0);
            assert first.isRendezvous();
            RendezvousProcessor processor = (RendezvousProcessor) first.getProcessor();
            return processor.isPartial(first.getElement().getDescription());
        }

        /**
         * Returns the operation factors in this fragment.
         * @return the operation factors
         */
        public List<Factor> getFactors() {
            return factors;
        }

        /**
         * Returns the original input ports.
         * @return the original input ports
         */
        public List<FlowElementInput> getInputPorts() {
            if (factors.isEmpty()) {
                return Collections.emptyList();
            }
            Factor first = factors.get(0);
            return first.getElement().getInputPorts();
        }

        /**
         * Returns the original output ports.
         * @return the original output ports
         */
        public List<FlowElementOutput> getOutputPorts() {
            if (factors.isEmpty()) {
                return Collections.emptyList();
            }
            Factor last = factors.get(factors.size() - 1);
            return last.getElement().getOutputPorts();
        }

        /**
         * Returns the external resources used in this fragment.
         * @return the external resources
         */
        public List<ResourceFragment> getResources() {
            return resources;
        }

        /**
         * Returns whether this is a rendezvous fragment or not.
         * If so, this fragment will be compiled into {@link Rendezvous}, otherwise {@link Result}.
         * @return {@code true} if this is a rendezvous fragment, otherwise {@code false}
         */
        public boolean isRendezvous() {
            if (factors.isEmpty()) {
                return false;
            }
            Factor first = factors.get(0);
            return first.isRendezvous();
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "Fragment{0}", //$NON-NLS-1$
                    getInputPorts());
        }
    }

    /**
     * Represents an operation factor.
     * This holds only one {@link FlowElement}.
     */
    public static class Factor {

        private final FlowElement element;

        private final FlowElementProcessor processor;

        /**
         * Creates a new instance.
         * @param element the corresponding flow element
         * @param processor the processor that can process the {@code element}
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Factor(FlowElement element, FlowElementProcessor processor) {
            Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(processor, "processor"); //$NON-NLS-1$
            this.element = element;
            this.processor = processor;
        }

        /**
         * Returns whether this operation is kind of rendezvous or not.
         * @return {@code true} if this operation is kind of rendezvous, otherwise {@code false}
         */
        public boolean isRendezvous() {
            return processor.getKind() == Kind.RENDEZVOUS;
        }

        /**
         * Returns whether this operation must be put on the end of {@link Fragment} or not.
         * @return {@code true} if this operation must be put on the end of {@link Fragment}, otherwise {@code false}
         */
        public boolean isLineEnd() {
            return processor.getKind() == Kind.LINE_END;
        }

        /**
         * Returns the corresponding flow element.
         * @return the corresponding flow element
         */
        public FlowElement getElement() {
            return element;
        }

        /**
         * Returns the processor which can process {@link #getElement() the holding flow element}.
         * @return the suitable flow processor
         */
        public FlowElementProcessor getProcessor() {
            return processor;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "Factor({0})", //$NON-NLS-1$
                    element);
        }
    }

    /**
     * Represents an external resources.
     */
    public static class ResourceFragment extends Compilable.Trait<CompiledType> {

        private final FlowResourceDescription description;

        /**
         * Creates a new instance.
         * @param description the target description
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public ResourceFragment(FlowResourceDescription description) {
            Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
            this.description = description;
        }

        /**
         * Returns the description of this external resource.
         * @return the description of this external resource
         */
        public FlowResourceDescription getDescription() {
            return description;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + description.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ResourceFragment other = (ResourceFragment) obj;
            if (description.equals(other.description) == false) {
                return false;
            }
            return true;
        }
    }

    /**
     * Represents an output of MapReduce stage.
     */
    public static class Sink {

        private final Set<FlowBlock.Output> outputs;

        private final String name;

        /**
         * Creates a new instance.
         * @param outputs the original output ports
         * @param name the output name
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Sink(Set<FlowBlock.Output> outputs, String name) {
            Precondition.checkMustNotBeNull(outputs, "outputs"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
            this.outputs = outputs;
            this.name = name;
        }

        /**
         * Returns the original output ports.
         * @return the original output ports
         */
        public Set<FlowBlock.Output> getOutputs() {
            return outputs;
        }

        /**
         * Returns the data model type of this output.
         * @return the data model type
         */
        public java.lang.reflect.Type getType() {
            return outputs.iterator().next().getElementPort().getDescription().getDataType();
        }

        /**
         * Returns the name of this output.
         * @return the name
         */
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Sink({0})", getName()); //$NON-NLS-1$
        }
    }
}
