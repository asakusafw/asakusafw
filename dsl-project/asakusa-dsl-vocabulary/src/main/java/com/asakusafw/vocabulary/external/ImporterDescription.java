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
package com.asakusafw.vocabulary.external;

/**
 * インポーターの処理内容を記述するインターフェース。
 * <p>
 * このインターフェースを実装するクラスは次のようなクラスである必要がある。
 * </p>
 * <ul>
 * <li> {@code public}で宣言されている </li>
 * <li> {@code abstract}で宣言されていない </li>
 * <li> 型引数が宣言されていない </li>
 * <li> 明示的なコンストラクターが宣言されていない </li>
 * </ul>
 * TODO i18n
 */
public interface ImporterDescription {

    /**
     * インポーターが対象とするモデルオブジェクトの型を表すクラスを返す。
     * @return インポーターが対象とするモデルオブジェクトの型を表すクラス
     */
    Class<?> getModelType();

    /**
     * このインポート処理によって取得するおおよそのデータサイズを返す。
     * @return おおよそのデータサイズ
     */
    DataSize getDataSize();

    /**
     * インポートするおおよそのデータサイズ。
     */
    public enum DataSize {

        /**
         * 不明。
         */
        UNKNOWN,

        /**
         * データサイズがおよそ10メガバイト未満。
         */
        TINY,

        /**
         * データサイズがおよそ10メガバイト以上、200メガバイト未満。
         */
        SMALL,

        /**
         * データサイズがおよそ200メガバイト以上。
         */
        LARGE,
    }
}
