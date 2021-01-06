/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for summarized models.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Summarized {

    /**
     * Each source term using summarization.
     */
    Term term();

    /**
     * Source term.
     */
    @Target({ })
    @interface Term {

        /**
         * The class of source model.
         */
        Class<?> source();

        /**
         * The folding specifications from source properties to destination properties.
         */
        Folding[] foldings();

        /**
         * Shuffling specification.
         * <p>
         * The all properties must be declared in the summarized model.
         * </p>
         */
        Key shuffle();
    }

    /**
     * Property folding.
     */
    @Target({ })
    @interface Folding {

        /**
         * The aggregator how aggregate source properties into the destination property.
         */
        Aggregator aggregator();

        /**
         * The property name of source property in the source model.
         */
        String source();

        /**
         * The property name of destination property in the joined model.
         */
        String destination();
    }

    /**
     * Aggregator functions.
     */
    enum Aggregator {

        /**
         * Any of element in bag.
         */
        ANY,

        /**
         * Sum of element in bag.
         * This throws {@link NullPointerException} if {@code null} exists.
         */
        SUM,

        /**
         * Cardinality of element in bag.
         */
        COUNT,

        /**
         * Maximum of element in bag.
         * This throws {@link NullPointerException} if {@code null} exists.
         */
        MAX,

        /**
         * Minimum of element in bag.
         * This throws {@link NullPointerException} if {@code null} exists.
         */
        MIN,
    }
}
