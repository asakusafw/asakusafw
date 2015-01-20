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
package com.asakusafw.runtime.testing;

import java.util.ArrayList;
import java.util.List;

import com.asakusafw.runtime.core.Result;


/**
 * {@link Result}のモック用実装。
 * @param <T> 取り扱うデータの型
 */
public class MockResult<T> implements Result<T> {

    private List<T> results = new ArrayList<T>();

    /**
     * インスタンスを生成して返す。
     * @param <T> 取り扱う要素の型
     * @return 生成したインスタンス
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static <T> MockResult<T> create() {
        return new MockResult<T>();
    }

    @Override
    public void add(T result) {
        T blessed = bless(result);
        results.add(blessed);
    }

    /**
     * {@link #add(Object)}に渡された結果が{@link #getResults()}のバッファに渡る前に
     * 結果の値に対して呼び出される(拡張ポイント)。
     * @param result 結果の値
     * @return {@link #getResults()}に渡す値
     */
    protected T bless(T result) {
        return result;
    }

    /**
     * これまでに追加された要素の一覧を返す。
     * @return これまでに追加された要素の一覧
     */
    public List<T> getResults() {
        return results;
    }
}
