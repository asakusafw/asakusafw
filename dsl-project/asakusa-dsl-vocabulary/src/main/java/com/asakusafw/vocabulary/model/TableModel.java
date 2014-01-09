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
 * テーブルから構築されたモデルを表す注釈。
 * @deprecated moved to asakusa-thundergate-vocabulary
 */
@Deprecated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TableModel {

    /**
     * 元となるテーブルの名前。
     */
    String name();

    /**
     * プロパティとして利用しているカラム名の一覧。
     */
    String[] columns() default "*";

    /**
     * 主プロパティとして利用しているカラム名の一覧。
     */
    String[] primary();

    /**
     * この注釈が付けられるモデルに必要なメソッド。
     * <p>
     * 必ずしものこのインターフェースを実装する必要はないが、下記のメソッドが存在する前提で
     * DSLの解釈がおこなれる。
     * </p>
     * @param <T> モデルオブジェクトの型
     */
    interface Interface<T> extends DataModel.Interface<T> {
        // no members
    }
}
