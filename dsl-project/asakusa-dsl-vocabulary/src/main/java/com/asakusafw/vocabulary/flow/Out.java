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
package com.asakusafw.vocabulary.flow;

/**
 * フローからの出力を表すインターフェース。
 * @param <T> 出力するデータの種類
 */
public interface Out<T> {

    /**
     * 指定の出力をフロー全体の出力として追加する。
     * @param source フロー全体の出力となるデータが流れるソース
     */
    void add(Source<T> source);
}
