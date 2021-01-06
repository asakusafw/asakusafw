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
 * An annotation that represents <em>summarized models</em>.
 * @deprecated replaced into {@link Summarized} since 0.2.0
 */
@Deprecated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SummarizedModel {

    /**
     * A reference to summarize target model type.
     * <p>
     * Note that, ordering information will be ignored even if the {@link ModelRef} contains it.
     * </p>
     */
    ModelRef from();

    /**
     * An interface that provides skeletal information of summarized models.
     * <p>
     * Each data model class may or may not implement this interface,
     * but that class must provide methods in the interface.
     * </p>
     * @param <T> summarized model type
     * @param <O> target model type
     */
    interface Interface<T, O> extends DataModel.Interface<T> {

        /**
         * The method name of {@link #startSummarization(Object)}.
         */
        String METHOD_NAME_START_SUMMARIZATION = "startSummarization"; //$NON-NLS-1$

        /**
         * The method name of {@link #combineSummarization(Object)}.
         */
        String METHOD_NAME_COMBINE_SUMMARIZATION = "combineSummarization"; //$NON-NLS-1$

        /**
         * Initializes this object with the target model object as the first one.
         * @param original the first target object
         */
        void startSummarization(O original);

        /**
         * Summarizes the target model into this.
         * @param original the target object
         */
        void combineSummarization(T original);
    }
}
