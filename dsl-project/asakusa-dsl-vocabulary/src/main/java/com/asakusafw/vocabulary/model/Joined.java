/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
 * Annotation for joined models.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Joined {

    /**
     * Each source term using join.
     */
    Term[] terms();

    /**
     * Source term.
     */
    @Target({ })
    public @interface Term {

        /**
         * The class of source model.
         */
        Class<?> source();

        /**
         * The mapping specifications from source properties to destination properties.
         */
        Mapping[] mappings();

        /**
         * Shuffling specification.
         * <p>
         * The all properties must be declared in the joined model.
         * </p>
         */
        Key shuffle();
    }

    /**
     * Property mapping.
     */
    @Target({ })
    public @interface Mapping {

        /**
         * The property name of source property in the source model.
         */
        String source();

        /**
         * The property name of destination property in the joined model.
         */
        String destination();
    }
}
