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
package com.asakusafw.vocabulary.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that represents <em>joined models</em>.
 * @deprecated replaced into {@link Joined} since 0.2.0
 */
@Deprecated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JoinedModel {

    /**
     * A reference to join target model type.
     */
    ModelRef from();

    /**
     * A reference to join source model type.
     */
    ModelRef join();

    /**
     * An interface that provides skeletal information of joined models.
     * <p>
     * Each data model class may or may not implement this interface,
     * but that class must provide methods in the interface.
     * </p>
     * @param <T> joined model type
     * @param <A> join target model type
     * @param <B> join source model type
     */
    interface Interface<T, A, B> extends DataModel.Interface<T> {

        /**
         * The method name of {@link #joinFrom(Object, Object)}.
         */
        String METHOD_NAME_JOIN_FROM = "joinFrom"; //$NON-NLS-1$

        /**
         * The method name of {@link #splitInto(Object, Object)}.
         */
        String METHOD_NAME_SPLIT_INTO = "splitInto"; //$NON-NLS-1$

        /**
         * Sets properties of the joined result into this.
         * @param left the join target model object
         * @param right the join source model object
         */
        void joinFrom(A left, B right);

        /**
         * Sets properties of the joined result into original data models.
         * @param left the join target model object
         * @param right the join source model object
         */
        void splitInto(A left, B right);
    }
}
