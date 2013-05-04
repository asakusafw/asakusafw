/**
 * Copyright 2013 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.operator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A meta-data for each operator.
 * Clients should not be use this annotation directly.
 * @since 0.5.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperatorInfo {

    /**
     * Kind of this operator.
     */
    Class<?> kind();

    /**
     * A list of inputs for the target operator.
     */
    Input[] input();

    /**
     * A list of outputs for the target operator.
     */
    Output[] output();

    /**
     * A list of parameters for the target operator.
     */
    Parameter[] parameter();

    /**
     * Operator input.
     * @since 0.5.0
     */
    @Target({ })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Input {

        /**
         * The factory parameter position.
         */
        int position();

        /**
         * The input name.
         */
        String name();

        /**
         * The input data model type.
         * If this input is generic, the type must be represents its upper-bound type.
         */
        Class<?> type();

        /**
         * The type variable name of the target data model.
         * If this input is not generic, this must be an empty string.
         */
        String typeVariable() default "";
    }

    /**
     * Operator output.
     * @since 0.5.0
     */
    @Target({ })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Output {

        /**
         * The output name.
         */
        String name();

        /**
         * The output data model type.
         * If this output is generic, the type must be represents its upper-bound type.
         */
        Class<?> type();

        /**
         * The type variable name of the target data model.
         * If this output is not generic, this must be an empty string.
         */
        String typeVariable() default "";
    }

    /**
     * Operator parameter.
     * @since 0.5.0
     */
    @Target({ })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Parameter {

        /**
         * The factory parameter position.
         */
        int position();

        /**
         * The parameter name.
         */
        String name();

        /**
         * The parameter type.
         */
        Class<?> type();

        /**
         * The type variable name of the target data model.
         * This is only {@link #type()} represents {@link java.lang.Class data model type}; then
         * this represents its type argument.
         * If this parameter does not represents data model type, this must be an empty string.
         */
        String typeVariable() default "";
    }
}
