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
package com.asakusafw.runtime.stage;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * ステージへの入力。
 * @since 0.1.0
 * @version 0.2.5
 */
public class StageInput {

    private final String pathString;

    private final Class<? extends InputFormat<?, ?>> formatClass;

    private final Class<? extends Mapper<?, ?, ?, ?>> mapperClass;

    private final Map<String, String> attributes;

    /**
     * Creates a new instance without any attributes.
     * @param pathString path to the input (may includes variables)
     * @param formatClass input format class
     * @param mapperClass mapper class to process input data
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    @SuppressWarnings({ "rawtypes" })
    public StageInput(
            String pathString,
            Class<? extends InputFormat> formatClass,
            Class<? extends Mapper> mapperClass) {
        this(pathString, formatClass, mapperClass, Collections.<String, String>emptyMap());
    }

    /**
     * Creates a new instance.
     * @param pathString path to the input (may includes variables)
     * @param formatClass input format class
     * @param mapperClass mapper class to process input data
     * @param attributes input attributes
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.5
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public StageInput(
            String pathString,
            Class<? extends InputFormat> formatClass,
            Class<? extends Mapper> mapperClass,
            Map<String, String> attributes) {
        if (pathString == null) {
            throw new IllegalArgumentException("pathString must not be null"); //$NON-NLS-1$
        }
        if (formatClass == null) {
            throw new IllegalArgumentException("formatClass must not be null"); //$NON-NLS-1$
        }
        if (mapperClass == null) {
            throw new IllegalArgumentException("mapperClass must not be null"); //$NON-NLS-1$
        }
        if (attributes == null) {
            throw new IllegalArgumentException("attributes must not be null"); //$NON-NLS-1$
        }
        this.pathString = pathString;
        this.formatClass = (Class<? extends InputFormat<?, ?>>) formatClass;
        this.mapperClass = (Class<? extends Mapper<?, ?, ?, ?>>) mapperClass;
        this.attributes = Collections.unmodifiableMap(new TreeMap<String, String>(attributes));
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

    /**
     * Returns the input attributes.
     * @return the attributes
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
