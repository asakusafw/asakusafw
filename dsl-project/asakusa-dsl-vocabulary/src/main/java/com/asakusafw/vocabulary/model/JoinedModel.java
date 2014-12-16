/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
 * 他のモデルを結合したモデルを表す注釈。
 * @deprecated replaced into {@link Joined} since 0.2.0
 */
@Deprecated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JoinedModel {

    /**
     * 結合先のモデル({@code FROM ...})への参照。
     */
    ModelRef from();

    /**
     * 結合元のモデル({@code JOIN ...})への参照。
     */
    ModelRef join();

    /**
     * この注釈が付けられるモデルに必要なメソッド。
     * <p>
     * 必ずしものこのインターフェースを実装する必要はないが、下記のメソッドが存在する前提で
     * DSLの解釈がおこなれる。
     * </p>
     * @param <T> モデルオブジェクトの型
     * @param <A> 結合されるモデルオブジェクトの型
     * @param <B> 結合するモデルオブジェクトの型
     */
    interface Interface<T, A, B> extends DataModel.Interface<T> {

        /**
         * {@link #joinFrom(Object, Object)}のメソッド名。
         */
        String METHOD_NAME_JOIN_FROM = "joinFrom"; //$NON-NLS-1$

        /**
         * {@link #splitInto(Object, Object)}のメソッド名。
         */
        String METHOD_NAME_SPLIT_INTO = "splitInto"; //$NON-NLS-1$

        /**
         * 2つのモデルオブジェクトを結合した結果を、このオブジェクトに設定する。
         * @param left 結合されるモデルのオブジェクト
         * @param right 結合するモデルのオブジェクト
         */
        void joinFrom(A left, B right);

        /**
         * この結合されたモデルを、もとの2つのモデルに分解して書き出す。
         * @param left 結合されるモデルのオブジェクト
         * @param right 結合するモデルのオブジェクト
         */
        void splitInto(A left, B right);
    }
}
