/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.runtime.core;

/**
 * リソースの設定情報。
 */
public interface ResourceConfiguration {

    /**
     * 指定のキーに関連する設定情報を返す。
     * @param keyName キー名
     * @param defaultValue 設定情報が無い場合の既定値 (null可)
     * @return 指定のキーに関連する設定情報、存在しない場合は既定値
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    String get(String keyName, String defaultValue);

    /**
     * 指定のキーに関連する情報を設定する。
     * @param keyName キー名
     * @param value 設定する情報、削除する場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    void set(String keyName, String value);

    /**
     * このリソースに関連するクラスローダーを返す。
     * @return このリソースに関連するクラスローダー
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    ClassLoader getClassLoader();
}