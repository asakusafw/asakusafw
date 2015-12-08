/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Checkpoint;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Confluent;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Empty;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.EmptyFragment;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Extend;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.ExtendFragment;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Project;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.ProjectFragment;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Restructure;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.RestructureFragment;

/**
 * Provides factory methods for core operators.
 * @since 0.2.6
 * @version 0.7.3
 */
public final class CoreOperators {

    private static final CoreOperatorFactory FACTORY = new CoreOperatorFactory();

    private CoreOperators() {
        return;
    }

    /**
     * Returns a new <em>empty operator</em> instance.
     * The resulting operator acts like a dummy input which provides an empty data-sets.
     * @param <T> the data model type
     * @param type the data model type
     * @return a new instance of <em>empty operator</em>
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @see CoreOperatorFactory#empty(Class)
     */
    public static <T> Empty<T> empty(Class<T> type) {
        return FACTORY.empty(type);
    }

    /**
     * Returns a new fragment which will be provide an <em>empty operator</em>.
     * The resulting object will require the target (downstream) data model type.
     * @return a new fragment of <em>empty operator</em>
     * @since 0.7.3
     * @see CoreOperatorFactory#empty()
     */
    public static EmptyFragment empty() {
        return FACTORY.empty();
    }

    /**
     * Terminates the upstream source.
     * Generally, operator outputs must be connected to at least one operator inputs for terminating data flow.
     * This method internally connects the upstream source to a <em>stop</em> operator input.
     * It operator will do nothing for any inputs and just drops them.
     * @param in the upstream source
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @see CoreOperatorFactory#stop(Source)
     */
    public static void stop(Source<?> in) {
        FACTORY.stop(in);
    }

    /**
     * Returns a new <em>confluent operator</em> instance.
     * The resulting operator puts the data from each upstream source together and provides them as the output.
     * @param <T> the data model type
     * @param a the upstream source (1)
     * @param b the upstream source (2)
     * @return a new instance of <em>confluent operator</em>
     * @throws IllegalArgumentException if the parameters are {@code null}
     * @see CoreOperatorFactory#confluent(Source, Source)
     */
    public static <T> Confluent<T> confluent(Source<T> a, Source<T> b) {
        return FACTORY.confluent(a, b);
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
     * @see CoreOperatorFactory#confluent(Source, Source, Source)
     */
    public static <T> Confluent<T> confluent(Source<T> a, Source<T> b, Source<T> c) {
        return FACTORY.confluent(a, b, c);
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
     * @see CoreOperatorFactory#confluent(Source, Source, Source, Source)
     */
    public static <T> Confluent<T> confluent(Source<T> a, Source<T> b, Source<T> c, Source<T> d) {
        return FACTORY.confluent(a, b, c, d);
    }

    /**
     * Returns a new <em>confluent operator</em> instance.
     * The resulting operator puts the data from each upstream source together and provides them as the output.
     * @param <T> the data model type
     * @param inputs the upstream sources
     * @return a new instance of <em>confluent operator</em>
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @see CoreOperatorFactory#confluent(Iterable)
     */
    public static <T> Confluent<T> confluent(Iterable<? extends Source<T>> inputs) {
        return FACTORY.confluent(inputs);
    }

    /**
     * Returns a new <em>checkpoint operator</em>.
     * The resulting operator will provide a restarting point for the (partial) failure.
     * @param <T> the data model type
     * @param in the upstream source
     * @return a new instance of <em>checkpoint operator</em>
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @see CoreOperatorFactory#checkpoint(Source)
     */
    public static <T> Checkpoint<T> checkpoint(Source<T> in) {
        return FACTORY.checkpoint(in);
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
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see CoreOperatorFactory#project(Source, Class)
     */
    public static <T> Project<T> project(Source<?> in, Class<T> targetType) {
        return FACTORY.project(in, targetType);
    }

    /**
     * Returns a new fragment which will be provide a <em>project operator</em>.
     * The resulting fragment object will require the target (downstream) data model type.
     * @param in the upstream source
     * @return a new fragment of <em>project operator</em>
     * @throws IllegalArgumentException if the parameters are {@code null}
     * @since 0.7.3
     * @see CoreOperatorFactory#project(Source)
     */
    public static ProjectFragment project(Source<?> in) {
        return FACTORY.project(in);
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
     * @see CoreOperatorFactory#extend(Source, Class)
     */
    public static <T> Extend<T> extend(Source<?> in, Class<T> targetType) {
        return FACTORY.extend(in, targetType);
    }

    /**
     * Returns a new fragment which will be provide an <em>extend operator</em>.
     * The resulting fragment object will require the target (downstream) data model type.
     * @param in the upstream source
     * @return a new fragment of <em>extend operator</em>
     * @throws IllegalArgumentException if the parameters are {@code null}
     * @since 0.7.3
     * @see CoreOperatorFactory#extend(Source)
     */
    public static ExtendFragment extend(Source<?> in) {
        return FACTORY.extend(in);
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
     * @see CoreOperatorFactory#restructure(Source, Class)
     */
    public static <T> Restructure<T> restructure(Source<?> in, Class<T> targetType) {
        return FACTORY.restructure(in, targetType);
    }

    /**
     * Returns a new fragment which will be provide a <em>restructure operator</em>.
     * The resulting fragment object will require the target (downstream) data model type.
     * @param in the upstream source
     * @return a new fragment of <em>restructure operator</em>
     * @throws IllegalArgumentException if the parameters are {@code null}
     * @since 0.7.3
     * @see CoreOperatorFactory#restructure(Source)
     */
    public static RestructureFragment restructure(Source<?> in) {
        return FACTORY.restructure(in);
    }
}
