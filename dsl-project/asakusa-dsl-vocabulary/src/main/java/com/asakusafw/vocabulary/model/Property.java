/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
 * An annotation that represents data model properties.
 * @deprecated replaced into {@link Joined} and {@link Summarized} since 0.2.0
 */
@Deprecated
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Property {

    /**
     * The original name of the target property.
     * If the target property has no original one, this returns an empty string.
     */
    String name() default "";

    /**
     * The source property information ({@code FROM ...}).
     * If it is not defined, {@link Source#declaring()} will become {@code void.class}.
     */
    Source from() default @Source(declaring = void.class, name = "");

    /**
     * The source property information ({@code JOIN ...}).
     * If it is not defined, {@link Source#declaring()} will become {@code void.class}.
     */
    Source join() default @Source(declaring = void.class, name = "");

    /**
     * The aggregate function type should be applied into this property.
     * <p>
     * If it is not defined, this returns {@link Property.Aggregator#IDENT}.
     * </p>
     */
    Aggregator aggregator() default Aggregator.IDENT;

    /**
     * The source property information.
     */
    @interface Source {

        /**
         * The class that declares the target property.
         */
        Class<?> declaring();

        /**
         * The name of the target property.
         */
        String name();
    }

    /**
     * Represents aggregate functions.
     */
    enum Aggregator {

        /**
         * Do not aggregate.
         */
        IDENT,

        /**
         * Summation.
         */
        SUM,

        /**
         * Count.
         */
        COUNT,

        /**
         * Maximum.
         */
        MAX,

        /**
         * Minimum.
         */
        MIN,
        ;
    }
}
