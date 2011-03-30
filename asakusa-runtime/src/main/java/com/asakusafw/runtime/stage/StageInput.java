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

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * ステージへの入力。
 */
public class StageInput {

    private Class<? extends Mapper<?, ?, ?, ?>> mapperClass;

    private String pathString;

    private Class<? extends InputFormat<?, ?>> formatClass;

    /**
     * インスタンスを生成する。
     * @param pathString 入力のパス文字列 (変数を含む)
     * @param formatClass 入力をキーと値の列に変換するフォーマットクラス
     * @param mapperClass 入力を処理するマッパークラス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public StageInput(
            String pathString,
            Class<? extends InputFormat> formatClass,
            Class<? extends Mapper> mapperClass) {
        if (pathString == null) {
            throw new IllegalArgumentException("pathString must not be null"); //$NON-NLS-1$
        }
        if (formatClass == null) {
            throw new IllegalArgumentException("formatClass must not be null"); //$NON-NLS-1$
        }
        if (mapperClass == null) {
            throw new IllegalArgumentException("mapperClass must not be null"); //$NON-NLS-1$
        }
        this.pathString = pathString;
        this.formatClass = (Class<? extends InputFormat<?, ?>>) formatClass;
        this.mapperClass = (Class<? extends Mapper<?, ?, ?, ?>>) mapperClass;
    }

    /**
     * 入力のパス文字列を返す。
     * @return 入力のパス文字列
     */
    public String getPathString() {
        return pathString;
    }

    /**
     * 入力をキーと値の列に変換するフォーマットクラスを返す。
     * @return 入力をキーと値の列に変換するフォーマットクラス
     */
    public Class<? extends InputFormat<?, ?>> getFormatClass() {
        return formatClass;
    }

    /**
     * 入力を処理するマッパークラスを返す。
     * @return 入力を処理するマッパークラス
     */
    public Class<? extends Mapper<?, ?, ?, ?>> getMapperClass() {
        return mapperClass;
    }
}
