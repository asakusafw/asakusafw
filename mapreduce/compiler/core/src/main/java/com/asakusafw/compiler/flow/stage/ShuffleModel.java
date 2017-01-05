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
import java.util.List;

import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.Compilable;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.compiler.flow.ShuffleDescription;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;

/**
 * Represents detail of shuffle actions.
 */
public class ShuffleModel extends Compilable.Trait<CompiledShuffle> {

    private final StageBlock stageBlock;

    private final List<Segment> segments;

    /**
     * Creates a new instance.
     * @param stageBlock the target stage block
     * @param segments the shuffle segments
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public ShuffleModel(StageBlock stageBlock, List<Segment> segments) {
        Precondition.checkMustNotBeNull(stageBlock, "stageBlock"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(segments, "segments"); //$NON-NLS-1$
        this.stageBlock = stageBlock;
        this.segments = segments;
    }

    /**
     * Returns the target stage block.
     * @return the target stage block
     */
    public StageBlock getStageBlock() {
        return stageBlock;
    }

    /**
     * Returns the segments in the target shuffle operation.
     * @return the shuffle segments
     */
    public List<Segment> getSegments() {
        return segments;
    }

    /**
     * Returns a shuffle segment.
     * @param input the target input port
     * @return the shuffle segment that corresponds to the input port, or {@code null} if there is no such a segment
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public Segment findSegment(FlowElementInput input) {
        Precondition.checkMustNotBeNull(input, "input"); //$NON-NLS-1$
        for (Segment segment : getSegments()) {
            if (segment.getPort().equals(input)) {
                return segment;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Shuffle({0})", //$NON-NLS-1$
                segments);
    }

    /**
     * Represents a shuffle action for each shuffle input.
     */
    public static class Segment extends Compilable.Trait<CompiledShuffleFragment> {

        private final int elementId;

        private final int portId;

        private final ShuffleDescription description;

        private final FlowElementInput port;

        private final DataClass source;

        private final DataClass target;

        private final List<Term> terms;

        /**
         * Creates a new instance.
         * @param elementId the target element ID of this segment
         * @param portId the target port ID of this segment
         * @param description description of this segment
         * @param port the target input port
         * @param source the input type
         * @param target the output type
         * @param terms the terms of this segment
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Segment(
                int elementId, int portId,
                ShuffleDescription description,
                FlowElementInput port,
                DataClass source, DataClass target,
                List<Term> terms) {
            Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(port, "port"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(source, "source"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(target, "target"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(terms, "terms"); //$NON-NLS-1$
            this.elementId = elementId;
            this.portId = portId;
            this.description = description;
            this.port = port;
            this.source = source;
            this.target = target;
            this.terms = terms;
        }

        /**
         * Returns the target element ID.
         * @return the target element ID
         */
        public int getElementId() {
            return elementId;
        }

        /**
         * Returns the target port ID.
         * @return the target port ID
         */
        public int getPortId() {
            return portId;
        }

        /**
         * Returns a description of this segment behavior.
         * @return the segment description
         */
        public ShuffleDescription getDescription() {
            return description;
        }

        /**
         * Returns the original element port.
         * @return the original element port
         */
        public FlowElementInput getPort() {
            return port;
        }

        /**
         * Returns the input type.
         * @return the input type
         */
        public DataClass getSource() {
            return source;
        }

        /**
         * Returns the output type.
         * @return the output type
         */
        public DataClass getTarget() {
            return target;
        }

        /**
         * Returns the terms of this segment.
         * @return the terms
         */
        public List<Term> getTerms() {
            return terms;
        }

        /**
         * Returns a term of this segment.
         * @param propertyName the target property name
         * @return the term which is corresponded to the target property, or {@code null} if there is no such the term
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public Term findTerm(String propertyName) {
            Precondition.checkMustNotBeNull(propertyName, "propertyName"); //$NON-NLS-1$
            if (propertyName.trim().isEmpty()) {
                return null;
            }
            String name = JavaName.of(propertyName).toMemberName();
            for (Term term : terms) {
                if (term.getSource().getName().equals(name)) {
                    return term;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "ShuffleSegment({2})(port={0}, terms={1})", //$NON-NLS-1$
                    port,
                    terms,
                    portId);
        }
    }

    /**
     * Represents individual key properties in shuffle {@link Segment}.
     */
    public static class Term {

        private final int termId;

        private final DataClass.Property source;

        private final Arrangement arrangement;

        /**
         * Creates a new instance.
         * @param termId the serial number
         * @param source the target property
         * @param arrangement arrangement of the target property
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Term(
                int termId,
                DataClass.Property source,
                Arrangement arrangement) {
            Precondition.checkMustNotBeNull(source, "source"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(arrangement, "arrangement"); //$NON-NLS-1$
            this.termId = termId;
            this.source = source;
            this.arrangement = arrangement;
        }

        /**
         * Returns the serial number of this term in the owner {@link Segment}.
         * @return the serial number
         */
        public int getTermId() {
            return termId;
        }

        /**
         * Returns the target property.
         * @return the target property
         */
        public DataClass.Property getSource() {
            return source;
        }

        /**
         * Returns arrangement of {@link #getSource() the target property}.
         * @return arrangement of the target property
         */
        public Arrangement getArrangement() {
            return arrangement;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "{0} {1}", //$NON-NLS-1$
                    getSource().getName(),
                    getArrangement());
        }
    }

    /**
     * Represents kinds of arrangement for each property.
     */
    public enum Arrangement {

        /**
         * Only grouping by its property.
         */
        GROUPING,

        /**
         * Sort by its property with ascending order.
         */
        ASCENDING,

        /**
         * Sort by its property with descending order.
         */
        DESCENDING,
    }
}
