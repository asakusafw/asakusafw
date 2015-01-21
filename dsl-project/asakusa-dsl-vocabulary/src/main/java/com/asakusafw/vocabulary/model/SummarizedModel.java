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
 * 他のモデルを集約したモデルを表す注釈。
 * @deprecated replaced into {@link Summarized} since 0.2.0
 */
@Deprecated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SummarizedModel {

    /**
     * 集約先のモデルへの参照。
     * <p>
     * なお、ここにソート条件を指定した場合、それらは全て無視される。
     * </p>
     */
    ModelRef from();

    /**
     * この注釈が付けられるモデルに必要なメソッド。
     * <p>
     * 必ずしものこのインターフェースを実装する必要はないが、下記のメソッドが存在する前提で
     * DSLの解釈がおこなれる。
     * </p>
     * @param <T> モデルオブジェクトの型
     * @param <O> 集計対象のモデルオブジェクトの型
     */
    interface Interface<T, O> extends DataModel.Interface<T> {

        /**
         * {@link #startSummarization(Object)}のメソッド名。
         */
        String METHOD_NAME_START_SUMMARIZATION = "startSummarization"; //$NON-NLS-1$

        /**
         * {@link #combineSummarization(Object)}のメソッド名。
         */
        String METHOD_NAME_COMBINE_SUMMARIZATION = "combineSummarization"; //$NON-NLS-1$

        /**
         * 指定のモデルを最初の要素として、このモデルの集計結果を初期化する。
         * @param original 最初の要素となるモデル
         */
        void startSummarization(O original);

        /**
         * このモデルに、指定のモデルの集計結果を合成する。
         * @param original 合成するモデル
         */
        void combineSummarization(T original);
    }
}
