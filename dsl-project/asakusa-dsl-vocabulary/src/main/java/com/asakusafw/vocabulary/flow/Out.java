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
package com.asakusafw.vocabulary.flow;

import com.asakusafw.vocabulary.flow.graph.FlowElementInput;

/**
 * フローからの出力を表すインターフェース。
 * @param <T> 出力するデータの種類
 * @since 0.1.0
 * @version 0.7.3
 */
public interface Out<T> {

    /**
     * 指定の出力をフロー全体の出力として追加する。
     * @param source フロー全体の出力となるデータが流れるソース
     */
    void add(Source<T> source);

    /**
     * この要素のポートとしての表現を返す。
     * <p>
     * DSL利用者はこのクラスのメソッドを直接利用すべきでない。
     * </p>
     * @return この要素のポートとしての表現
     */
    FlowElementInput toInputPort();
}
