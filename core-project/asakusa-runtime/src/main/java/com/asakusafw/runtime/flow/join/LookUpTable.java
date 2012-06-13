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
package com.asakusafw.runtime.flow.join;

import java.io.IOException;
import java.util.List;

/**
 * Joinを行うためのテーブル。
 * @param <T> 要素の種類
 */
public interface LookUpTable<T> {

    /**
     * 指定のキーに関連する要素の一覧を返す。
     * @param key 対象のキー
     * @return 関連する要素の一覧、発見できない場合には空のリスト
     * @throws IOException 値の取得に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    List<T> get(LookUpKey key) throws IOException;

    /**
     * {@link LookUpTable}を構築するためのビルダー。
     * @param <T> 要素の種類
     */
    interface Builder<T> {

        /**
         * 指定のキーに関連する要素を追加する。
         * @param key 対象のキー
         * @param value 関連する要素
         * @throws IOException 値の追加に失敗した場合
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        void add(LookUpKey key, T value) throws IOException;

        /**
         * ここまでに追加した情報を元に、テーブルを構築して返す。
         * <p>
         * このメソッドを起動した以後、このオブジェクトをそうさしてはならない。
         * </p>
         * @throws IOException テーブルの構築に失敗した場合
         * @return 構築したテーブル
         */
        LookUpTable<T> build() throws IOException;
    }
}
