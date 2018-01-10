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
package com.asakusafw.vocabulary.flow.util;

import static com.asakusafw.vocabulary.flow.util.PseudElementDescription.*;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;

/**
 * Provides factory methods for core operators.
 * @since 0.1.0
 * @version 0.9.0
 */
public class CoreOperatorFactory {

    /**
     * The common instance name of <em>empty operator</em>.
     */
    public static final String EMPTY_NAME = "empty"; //$NON-NLS-1$

    /**
     * The common instance name of <em>stop operator</em>.
     */
    public static final String STOP_NAME = "stop"; //$NON-NLS-1$

    /**
     * The common instance name of <em>confluent operator</em>.
     */
    public static final String CONFLUENT_NAME = "confluent"; //$NON-NLS-1$

    /**
     * The common instance name of <em>checkpoint operator</em>.
     */
    public static final String CHECKPOINT_NAME = "checkpoint"; //$NON-NLS-1$

    /**
     * The common instance name of <em>project operator</em>.
     */
    public static final String PROJECT_NAME = "project"; //$NON-NLS-1$

    /**
     * The common instance name of <em>extend operator</em>.
     */
    public static final String EXTEND_NAME = "extend"; //$NON-NLS-1$

    /**
     * The common instance name of <em>restructure operator</em>.
     */
    public static final String RESTRUCTURE_NAME = "restructure"; //$NON-NLS-1$

    /**
     * Returns a new <em>empty operator</em> instance.
     * The resulting operator acts like a dummy input which provides an empty data-sets.
     * @param <T> the data model type
     * @param type the data model type
     * @return a new instance of <em>empty operator</em>
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @see com.asakusafw.vocabulary.operator.Empty
     */
    public <T> Empty<T> empty(Class<T> type) {
        return empty((Type) type);
    }

    /**
     * Returns a new <em>empty operator</em> instance.
     * The resulting operator acts like a dummy input which provides an empty data-sets.
     * @param <T> the data model type
     * @param type the data model type
     * @return a new instance of <em>empty operator</em>
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @see com.asakusafw.vocabulary.operator.Empty
     */
    public <T> Empty<T> empty(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return new Empty<>(type);
    }

    /**
     * Returns a new fragment which will be provide an <em>empty operator</em>.
     * The resulting object will require the target (downstream) data model type.
     * @return a new fragment of <em>empty operator</em>
     * @since 0.7.3
     * @see #empty(Class)
     */
    public EmptyFragment empty() {
        return new EmptyFragment();
    }

    /**
     * Terminates the upstream source.
     * Generally, each operator output must be connected to at least one operator inputs (or flow outputs) for
     * terminating data flow. This method internally connects the upstream source to a <em>stop</em> operator input.
     * It operator will do nothing for any inputs and just drops them.
     * @param in the upstream source
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @see com.asakusafw.vocabulary.operator.Stop
     */
    public void stop(Source<?> in) {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        PseudElementDescription desc = new PseudElementDescription(
                STOP_NAME,
                getPortType(in),
                true,
                false,
                FlowBoundary.STAGE);
        FlowElementResolver resolver = new FlowElementResolver(desc);
        resolver.resolveInput(INPUT_PORT_NAME, in);
    }

    /**
     * Returns a new <em>confluent operator</em> instance.
     * The resulting operator puts the data from each upstream source together and provides them as the output.
     * @param <T> the data model type
     * @param a the upstream source (1)
     * @param b the upstream source (2)
     * @return a new instance of <em>confluent operator</em>
     * @throws IllegalArgumentException if the parameters are {@code null}
     * @see com.asakusafw.vocabulary.operator.Confluent
     */
    public <T> Confluent<T> confluent(Source<T> a, Source<T> b) {
        if (a == null) {
            throw new IllegalArgumentException("a must not be null"); //$NON-NLS-1$
        }
        if (b == null) {
            throw new IllegalArgumentException("b must not be null"); //$NON-NLS-1$
        }
        Type type = getPortType(a);
        List<Source<T>> input = new ArrayList<>();
        input.add(a);
        input.add(b);
        return new Confluent<>(type, input);
    }

    /**
     * Returns a new <em>confluent operator</em> instance.
     * The resulting operator puts the data from each upstream source together and provides them as the output.
     * @param <T> the data model type
     * @param a the upstream source (1)
     * @param b the upstream source (2)
     * @param c the upstream source (3)
     * @return a new instance of <em>confluent operator</em>
     * @throws IllegalArgumentException if the parameters are {@code null}
     * @see com.asakusafw.vocabulary.operator.Confluent
     */
    public <T> Confluent<T> confluent(Source<T> a, Source<T> b, Source<T> c) {
        if (a == null) {
            throw new IllegalArgumentException("a must not be null"); //$NON-NLS-1$
        }
        if (b == null) {
            throw new IllegalArgumentException("b must not be null"); //$NON-NLS-1$
        }
        if (c == null) {
            throw new IllegalArgumentException("b must not be null"); //$NON-NLS-1$
        }
        Type type = getPortType(a);
        List<Source<T>> input = new ArrayList<>();
        input.add(a);
        input.add(b);
        input.add(c);
        return new Confluent<>(type, input);
    }

    /**
     * Returns a new <em>confluent operator</em> instance.
     * The resulting operator puts the data from each upstream source together and provides them as the output.
     * @param <T> the data model type
     * @param a the upstream source (1)
     * @param b the upstream source (2)
     * @param c the upstream source (3)
     * @param d the upstream source (4)
     * @return a new instance of <em>confluent operator</em>
     * @throws IllegalArgumentException if the parameters are {@code null}
     * @see com.asakusafw.vocabulary.operator.Confluent
     */
    public <T> Confluent<T> confluent(Source<T> a, Source<T> b, Source<T> c, Source<T> d) {
        if (a == null) {
            throw new IllegalArgumentException("a must not be null"); //$NON-NLS-1$
        }
        if (b == null) {
            throw new IllegalArgumentException("b must not be null"); //$NON-NLS-1$
        }
        if (c == null) {
            throw new IllegalArgumentException("b must not be null"); //$NON-NLS-1$
        }
        if (d == null) {
            throw new IllegalArgumentException("d must not be null"); //$NON-NLS-1$
        }

        Type type = getPortType(a);
        List<Source<T>> input = new ArrayList<>();
        input.add(a);
        input.add(b);
        input.add(c);
        input.add(d);
        return new Confluent<>(type, input);
    }

    /**
     * Returns a new <em>confluent operator</em> instance.
     * The resulting operator puts the data from each upstream source together and provides them as the output.
     * @param <T> the data model type
     * @param inputs the upstream sources
     * @return a new instance of <em>confluent operator</em>
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @see com.asakusafw.vocabulary.operator.Confluent
     */
    public <T> Confluent<T> confluent(Iterable<? extends Source<T>> inputs) {
        if (inputs == null) {
            throw new IllegalArgumentException("inputs must not be null"); //$NON-NLS-1$
        }
        List<Source<T>> input = new ArrayList<>();
        for (Source<T> in : inputs) {
            if (in == null) {
                throw new IllegalArgumentException("inputs must not contain null"); //$NON-NLS-1$
            }
            input.add(in);
        }
        if (input.isEmpty()) {
            throw new IllegalArgumentException("inputs must not be empty"); //$NON-NLS-1$
        }
        Type type = getPortType(input.get(0));
        return new Confluent<>(type, input);
    }

    /**
     * Returns a new <em>confluent operator</em> instance.
     * The resulting operator puts the data from each upstream source together and provides them as the output.
     * @param <T> the data model type
     * @param inputs the upstream sources
     * @return a new instance of <em>confluent operator</em>
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @see com.asakusafw.vocabulary.operator.Confluent
     * @since 0.9.0
     */
    @SafeVarargs
    public final <T> Confluent<T> confluent(Source<T>... inputs) {
        if (inputs == null) {
            throw new IllegalArgumentException("inputs must not be null"); //$NON-NLS-1$
        }
        return confluent(Arrays.asList(inputs));
    }

    /**
     * Returns a new <em>checkpoint operator</em>.
     * The resulting operator will provide a restarting point for the (partial) failure.
     * Note that, this will acts as an <em>identity operator</em> in some implementations, because the checkpoint is an
     * optional operation.
     * @param <T> the data model type
     * @param in the upstream source
     * @return a new instance of <em>checkpoint operator</em>
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @see com.asakusafw.vocabulary.operator.Checkpoint
     */
    public <T> Checkpoint<T> checkpoint(Source<T> in) {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        Type type = getPortType(in);
        return new Checkpoint<>(type, in);
    }

    /**
     * Returns a new <em>project operator</em> instance.
     * The source (upstream) data type must have all properties declared in the target (downstream) data type.
     * This operator will copy such properties in the upstream data into the each resulting data.
     * If the target data model type has extra properties for the upstream data type, or if there are type incompatible
     * properties between the source and target data model, compiling this operator must be failed.
     * @param <T> the target data model type
     * @param in the upstream source
     * @param targetType the target data model class
     * @return a new instance of <em>project operator</em>
     * @throws IllegalArgumentException if the parameters are {@code null}
     * @since 0.2.0
     * @see #project(Source)
     * @see com.asakusafw.vocabulary.operator.Project
     */
    public <T> Project<T> project(Source<?> in, Class<T> targetType) {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null"); //$NON-NLS-1$
        }
        return new Project<>(in, targetType);
    }

    /**
     * Returns a new fragment which will be provide a <em>project operator</em>.
     * The resulting fragment object will require the target (downstream) data model type.
     * @param in the upstream source
     * @return a new fragment of <em>project operator</em>
     * @throws IllegalArgumentException if the parameters are {@code null}
     * @since 0.7.3
     * @see #project(Source, Class)
     */
    public ProjectFragment project(Source<?> in) {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        return new ProjectFragment(in);
    }

    /**
     * Returns a new <em>extend operator</em> instance.
     * The target (downstream) data type must have all properties declared in the source (upstream) data type.
     * This operator will copy such properties in the upstream data into the each resulting data.
     * If the target data model type does not have some properties in the upstream data type, or if there are type
     * incompatible properties between the source and target data model, compiling this operator must be failed.
     * @param <T> the target data model type
     * @param in the upstream source
     * @param targetType the target data model class
     * @return a new instance of <em>extend operator</em>
     * @throws IllegalArgumentException if the parameters are {@code null}
     * @since 0.2.0
     * @see #extend(Source)
     * @see com.asakusafw.vocabulary.operator.Extend
     */
    public <T> Extend<T> extend(Source<?> in, Class<T> targetType) {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null"); //$NON-NLS-1$
        }
        return new Extend<>(in, targetType);
    }

    /**
     * Returns a new fragment which will be provide an <em>extend operator</em>.
     * The resulting fragment object will require the target (downstream) data model type.
     * @param in the upstream source
     * @return a new fragment of <em>extend operator</em>
     * @throws IllegalArgumentException if the parameters are {@code null}
     * @since 0.7.3
     * @see #extend(Source, Class)
     */
    public ExtendFragment extend(Source<?> in) {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        return new ExtendFragment(in);
    }

    /**
     * Returns a new <em>restructure operator</em> instance.
     * The target (downstream) data type must have one or more properties declared in the source (upstream) data type.
     * This operator will copy such properties in the upstream data into the each resulting data.
     * If there are type incompatible properties between the source and target data model, compiling this operator must
     * be failed.
     * @param <T> the target data model type
     * @param in the upstream source
     * @param targetType the target data model class
     * @return a new instance of <em>restructure operator</em>
     * @throws IllegalArgumentException if the parameters are {@code null}
     * @since 0.2.1
     * @see #restructure(Source)
     * @see com.asakusafw.vocabulary.operator.Restructure
     */
    public <T> Restructure<T> restructure(Source<?> in, Class<T> targetType) {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null"); //$NON-NLS-1$
        }
        return new Restructure<>(in, targetType);
    }

    /**
     * Returns a new fragment which will be provide a <em>restructure operator</em>.
     * The resulting fragment object will require the target (downstream) data model type.
     * @param in the upstream source
     * @return a new fragment of <em>restructure operator</em>
     * @throws IllegalArgumentException if the parameters are {@code null}
     * @since 0.7.3
     * @see #restructure(Source, Class)
     */
    public RestructureFragment restructure(Source<?> in) {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        return new RestructureFragment(in);
    }

    private <T> Type getPortType(Source<T> source) {
        assert source != null;
        FlowElementOutput port = source.toOutputPort();
        Type type = port.getDescription().getDataType();
        return type;
    }

    /**
     * Represents an <em>empty operator</em> which provides empty data-sets.
     * @param <T> the data model type
     */
    public static final class Empty<T> implements Source<T> {

        /**
         * The singleton output of this operator.
         */
        public final Source<T> out;

        private final FlowElementResolver resolver;

        Empty(Type type) {
            assert type != null;
            this.out = this;
            PseudElementDescription desc = new PseudElementDescription(
                    EMPTY_NAME,
                    type,
                    false,
                    true,
                    FlowBoundary.STAGE);
            this.resolver = new FlowElementResolver(desc);
        }

        @Override
        public FlowElementOutput toOutputPort() {
            return resolver.getOutput(OUTPUT_PORT_NAME);
        }
    }

    /**
     * Represents a <em>confluent operator</em> which provides union of data-sets.
     * @param <T> the data model type
     */
    public static final class Confluent<T> implements Source<T> {

        /**
         * The singleton output of this operator.
         */
        public final Source<T> out;

        private final FlowElementResolver resolver;

        Confluent(Type type, List<Source<T>> input) {
            assert type != null;
            assert input != null;
            this.out = this;
            PseudElementDescription desc = new PseudElementDescription(
                    CONFLUENT_NAME,
                    type,
                    true,
                    true);
            resolver = new FlowElementResolver(desc);
            for (Source<T> in : input) {
                resolver.resolveInput(INPUT_PORT_NAME, in);
            }
        }

        @Override
        public FlowElementOutput toOutputPort() {
            return resolver.getOutput(OUTPUT_PORT_NAME);
        }
    }

    /**
     * Represents a <em>checkpoint operator</em> which provides restarting point for (partial) failures.
     * @param <T> the data model type
     */
    public static final class Checkpoint<T> implements Source<T> {

        /**
         * The singleton output of this operator.
         */
        public final Source<T> out;

        private final FlowElementResolver resolver;

        Checkpoint(Type type, Source<T> in) {
            assert type != null;
            assert in != null;
            this.out = this;
            PseudElementDescription desc = new PseudElementDescription(
                    CHECKPOINT_NAME,
                    type,
                    true,
                    true,
                    FlowBoundary.STAGE);
            this.resolver = new FlowElementResolver(desc);
            resolver.resolveInput(INPUT_PORT_NAME, in);
        }

        @Override
        public FlowElementOutput toOutputPort() {
            return resolver.getOutput(OUTPUT_PORT_NAME);
        }
    }

    /**
     * Represents a <em>project operator</em> which transforms data model objects into other data model types.
     * @param <T> the result data model type
     * @since 0.2.0
     */
    public static final class Project<T> implements Operator, Source<T> {

        /**
         * The singleton output of this operator.
         */
        public final Source<T> out;

        private final FlowElementResolver resolver;

        Project(Source<?> in, Class<T> targetClass) {
            assert in != null;
            assert targetClass != null;
            OperatorDescription.Builder builder =
                new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.Project.class);
            builder.declare(CoreOperatorFactory.class, CoreOperatorFactory.class, "project"); //$NON-NLS-1$
            builder.declareParameter(Source.class);
            builder.declareParameter(Class.class);
            builder.addInput(INPUT_PORT_NAME, in);
            builder.addOutput(OUTPUT_PORT_NAME, targetClass);
            this.resolver = builder.toResolver();
            this.resolver.resolveInput(INPUT_PORT_NAME, in);
            this.resolver.setName(PROJECT_NAME);
            this.out = this.resolver.resolveOutput(OUTPUT_PORT_NAME);
        }

        @Override
        public FlowElementOutput toOutputPort() {
            return resolver.getOutput(OUTPUT_PORT_NAME);
        }
    }

    /**
     * Represents a <em>extend operator</em> which transforms data model objects into other data model types.
     * @param <T> the result data model type
     * @since 0.2.0
     */
    public static final class Extend<T> implements Operator, Source<T> {

        /**
         * The singleton output of this operator.
         */
        public final Source<T> out;

        private final FlowElementResolver resolver;

        Extend(Source<?> in, Class<T> targetClass) {
            assert in != null;
            assert targetClass != null;
            OperatorDescription.Builder builder =
                new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.Extend.class);
            builder.declare(CoreOperatorFactory.class, CoreOperatorFactory.class, "extend"); //$NON-NLS-1$
            builder.declareParameter(Source.class);
            builder.declareParameter(Class.class);
            builder.addInput(INPUT_PORT_NAME, in);
            builder.addOutput(OUTPUT_PORT_NAME, targetClass);
            this.resolver = builder.toResolver();
            this.resolver.resolveInput(INPUT_PORT_NAME, in);
            this.resolver.setName(EXTEND_NAME);
            this.out = this.resolver.resolveOutput(OUTPUT_PORT_NAME);
        }

        @Override
        public FlowElementOutput toOutputPort() {
            return resolver.getOutput(OUTPUT_PORT_NAME);
        }
    }

    /**
     * Represents a <em>restructure operator</em> which transforms data model objects into other data model types.
     * @param <T> the result data model type
     * @since 0.2.1
     */
    public static final class Restructure<T> implements Operator, Source<T> {

        /**
         * The singleton output of this operator.
         */
        public final Source<T> out;

        private final FlowElementResolver resolver;

        Restructure(Source<?> in, Class<T> targetClass) {
            assert in != null;
            assert targetClass != null;
            OperatorDescription.Builder builder =
                new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.Restructure.class);
            builder.declare(CoreOperatorFactory.class, CoreOperatorFactory.class, "restructure"); //$NON-NLS-1$
            builder.declareParameter(Source.class);
            builder.declareParameter(Class.class);
            builder.addInput(INPUT_PORT_NAME, in);
            builder.addOutput(OUTPUT_PORT_NAME, targetClass);
            this.resolver = builder.toResolver();
            this.resolver.resolveInput(INPUT_PORT_NAME, in);
            this.resolver.setName(RESTRUCTURE_NAME);
            this.out = this.resolver.resolveOutput(OUTPUT_PORT_NAME);
        }

        @Override
        public FlowElementOutput toOutputPort() {
            return resolver.getOutput(OUTPUT_PORT_NAME);
        }
    }

    /**
     * An <em>empty operator</em> fragment which requires the target type.
     * @since 0.7.3
     */
    public static final class EmptyFragment {

        EmptyFragment() {
            return;
        }

        /**
         * Creates a new <em>empty operator</em> instance from the target (downstream) data model class.
         * @param <T> the target data model type
         * @param target the target data model class
         * @return the created operator instance
         */
        public <T> Empty<T> as(Class<T> target) {
            return new Empty<>(target);
        }

        /**
         * Creates a new <em>empty operator</em> instance from a source object which has the same type as the
         * target (downstream) data model type.
         * @param <T> the target data model type
         * @param target a source object which has the target data model type
         * @return the created operator instance
         */
        public <T> Empty<T> as(Source<T> target) {
            return as(classOf(target));
        }

        /**
         * Creates a new <em>empty operator</em> instance from a flow output object which has the same type as the
         * target (downstream) data model type.
         * @param <T> the target data model type
         * @param target a flow output object which has the target data model type
         * @return the created operator instance
         */
        public <T> Empty<T> as(Out<T> target) {
            return as(classOf(target));
        }
    }

    /**
     * An <em>project operator</em> fragment which requires the target type.
     * @since 0.7.3
     */
    public static final class ProjectFragment {

        private final Source<?> in;

        ProjectFragment(Source<?> in) {
            this.in = in;
        }

        /**
         * Creates a new <em>project operator</em> instance from the target (downstream) data model class.
         * @param <T> the target data model type
         * @param target the target data model class
         * @return the created operator instance
         */
        public <T> Project<T> as(Class<T> target) {
            return new Project<>(in, target);
        }

        /**
         * Creates a new <em>project operator</em> instance from a source object which has the same type as the
         * target (downstream) data model type.
         * @param <T> the target data model type
         * @param target a source object which has the target data model type
         * @return the created operator instance
         */
        public <T> Project<T> as(Source<T> target) {
            return as(classOf(target));
        }

        /**
         * Creates a new <em>project operator</em> instance from a flow output object which has the same type as the
         * target (downstream) data model type.
         * @param <T> the target data model type
         * @param target a flow output object which has the target data model type
         * @return the created operator instance
         */
        public <T> Project<T> as(Out<T> target) {
            return as(classOf(target));
        }
    }

    /**
     * An <em>extend operator</em> fragment which requires the target type.
     * @since 0.7.3
     */
    public static final class ExtendFragment {

        private final Source<?> in;

        ExtendFragment(Source<?> in) {
            this.in = in;
        }

        /**
         * Creates a new <em>extend operator</em> instance from the target (downstream) data model class.
         * @param <T> the target data model type
         * @param target the target data model class
         * @return the created operator instance
         */
        public <T> Extend<T> as(Class<T> target) {
            return new Extend<>(in, target);
        }

        /**
         * Creates a new <em>extend operator</em> instance from a source object which has the same type as the
         * target (downstream) data model type.
         * @param <T> the target data model type
         * @param target a source object which has the target data model type
         * @return the created operator instance
         */
        public <T> Extend<T> as(Source<T> target) {
            return as(classOf(target));
        }

        /**
         * Creates a new <em>extend operator</em> instance from a flow output object which has the same type as the
         * target (downstream) data model type.
         * @param <T> the target data model type
         * @param target a flow output object which has the target data model type
         * @return the created operator instance
         */
        public <T> Extend<T> as(Out<T> target) {
            return as(classOf(target));
        }
    }

    /**
     * An <em>restructure operator</em> fragment which requires the target type.
     * @since 0.7.3
     */
    public static final class RestructureFragment {

        private final Source<?> in;

        RestructureFragment(Source<?> in) {
            this.in = in;
        }

        /**
         * Creates a new <em>restructure operator</em> instance from the target (downstream) data model class.
         * @param <T> the target data model type
         * @param target the target data model class
         * @return the created operator instance
         */
        public <T> Restructure<T> as(Class<T> target) {
            return new Restructure<>(in, target);
        }

        /**
         * Creates a new <em>restructure operator</em> instance from a source object which has the same type as the
         * target (downstream) data model type.
         * @param <T> the target data model type
         * @param target a source object which has the target data model type
         * @return the created operator instance
         */
        public <T> Restructure<T> as(Source<T> target) {
            return as(classOf(target));
        }

        /**
         * Creates a new <em>restructure operator</em> instance from a flow output object which has the same type as
         * the target (downstream) data model type.
         * @param <T> the target data model type
         * @param target a flow output object which has the target data model type
         * @return the created operator instance
         */
        public <T> Restructure<T> as(Out<T> target) {
            return as(classOf(target));
        }
    }

    static <T> Class<T> classOf(Source<T> source) {
        Type type = source.toOutputPort().getDescription().getDataType();
        if ((type instanceof Class<?>) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("CoreOperatorFactory.errorRequireClass"), //$NON-NLS-1$
                    type));
        }
        @SuppressWarnings("unchecked")
        Class<T> aClass = (Class<T>) type;
        return aClass;
    }

    static <T> Class<T> classOf(Out<T> out) {
        Type type = out.toInputPort().getDescription().getDataType();
        if ((type instanceof Class<?>) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("CoreOperatorFactory.errorRequireClass"), //$NON-NLS-1$
                    type));
        }
        @SuppressWarnings("unchecked")
        Class<T> aClass = (Class<T>) type;
        return aClass;
    }
}
