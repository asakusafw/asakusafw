/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

/**
 * プロパティの情報。
 */
public @interface Property {

    /**
     * このプロパティの元になった名前。
     * <p>
     * 存在しない場合、空の文字列が指定される。
     * </p>
     */
    String name() default "";

    /**
     * このプロパティの元になったプロパティの情報({@code FROM ...})。
     * <p>
     * 存在しない場合、
     * {@link Source#declaring()}の値に{@code void.class}が指定される。
     * </p>
     */
    Source from() default @Source(declaring = void.class, name = "");

    /**
     * このプロパティの元になったプロパティの情報({@code JOIN ...})。
     * <p>
     * 存在しない場合、
     * {@link Property.Source#declaring()}の値に{@code void.class}が指定される。
     * </p>
     */
    Source join() default @Source(declaring = void.class, name = "");

    /**
     * このプロパティに適用されるべき集約関数。
     * <p>
     * 存在しない場合、
     * {@link Property.Aggregator#IDENT}が指定される。
     * </p>
     */
    Aggregator aggregator() default Aggregator.IDENT;

    /**
     * 元になったプロパティの情報。
     */
    public @interface Source {

        /**
         * 元のプロパティを宣言するクラス。
         */
        Class<?> declaring();

        /**
         * 元のプロパティの名前。
         */
        String name();
    }

    /**
     * 集約関数。
     */
    public enum Aggregator {

        /**
         * 集約なし。
         */
        IDENT,

        /**
         * 合計。
         */
        SUM,

        /**
         * カウント。
         */
        COUNT,

        /**
         * 最大値。
         */
        MAX,

        /**
         * 最小値。
         */
        MIN,
        ;
    }
}
