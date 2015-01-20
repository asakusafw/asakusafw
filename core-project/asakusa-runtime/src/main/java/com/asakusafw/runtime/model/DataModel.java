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
package com.asakusafw.runtime.model;

/**
 * Asakusaが利用する標準のデータモデルが実装すべきインターフェース。
 * <p>
 * このインターフェースにプロパティ{@code p}を追加する場合、以下のメソッドを定義する必要がある。
 * ただし、{@code p}の名前をCamelCaseの形式で表記したものを{@code <PropName>}とし、
 * {@code p}の型を{@code <PropType>}とする。
 * </p>
<ul>
<li> {@code get<PropName>Option():<PropType>} </li>
<li> {@code set<PropName>Option(<PropType>):void} </li>
</ul>
 * @param <T> 自分自身
 * @since 0.2.0
 */
public interface DataModel<T extends DataModel<T>> {

    /**
     * このオブジェクトの内容をクリアして、インスタンス生成直後と同じ状態に戻す。
     */
    void reset();

    /**
     * 指定のオブジェクトの内容を、このオブジェクトに書き出す。
     * @param other 対象のオブジェクト
     */
    void copyFrom(T other);
}
