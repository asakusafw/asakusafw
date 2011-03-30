/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage;

import org.apache.hadoop.mapreduce.OutputFormat;

/**
 * ステージからの出力。
 */
public class StageOutput {

    private String name;

    private Class<?> keyClass;

    private Class<?> valueClass;

    private Class<? extends OutputFormat<?, ?>> formatClass;

    /**
     * インスタンスを生成する。
     * @param name 出力を識別する名前
     * @param keyClass 出力するキーの型
     * @param valueClass 出力するデータの型
     * @param formatClass 利用するフォーマットクラス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public StageOutput(
            String name,
            Class<?> keyClass,
            Class<?> valueClass,
            Class<? extends OutputFormat> formatClass) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (keyClass == null) {
            throw new IllegalArgumentException("keyClass must not be null"); //$NON-NLS-1$
        }
        if (valueClass == null) {
            throw new IllegalArgumentException("valueClass must not be null"); //$NON-NLS-1$
        }
        if (formatClass == null) {
            throw new IllegalArgumentException("formatClass must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.formatClass = (Class<? extends OutputFormat<?, ?>>) formatClass;
    }

    /**
     * この出力を識別する名前を返す。
     * @return 出力を識別する名前
     */
    public String getName() {
        return name;
    }

    /**
     * この出力に利用するキーの型を返す。
     * @return この出力に利用するデータの型
     */
    public Class<?> getKeyClass() {
        return keyClass;
    }

    /**
     * この出力に利用するデータの型を返す。
     * @return この出力に利用するデータの型
     */
    public Class<?> getValueClass() {
        return valueClass;
    }

    /**
     * この出力に利用するフォーマットクラスを返す。
     * @return 利用するフォーマットクラス
     */
    public Class<? extends OutputFormat<?, ?>> getFormatClass() {
        return formatClass;
    }
}
