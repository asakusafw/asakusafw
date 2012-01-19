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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.mapreduce.OutputFormat;

/**
 * ステージからの出力。
 * @since 0.1.0
 * @version 0.2.5
 */
public class StageOutput {

    private final String name;

    private final Class<?> keyClass;

    private final Class<?> valueClass;

    private final Class<? extends OutputFormat<?, ?>> formatClass;

    private final Map<String, String> attributes;
    /**
     * Creates a new instance without any attributes.
     * @param name target output name
     * @param keyClass output key type
     * @param valueClass output value type
     * @param formatClass input format class
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    @SuppressWarnings({ "rawtypes" })
    public StageOutput(
            String name,
            Class<?> keyClass,
            Class<?> valueClass,
            Class<? extends OutputFormat> formatClass) {
        this(name, keyClass, valueClass, formatClass, Collections.<String, String>emptyMap());
    }

    /**
     * Creates a new instance.
     * @param name target output name
     * @param keyClass output key type
     * @param valueClass output value type
     * @param formatClass input format class
     * @param attributes input attributes
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.5
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public StageOutput(
            String name,
            Class<?> keyClass,
            Class<?> valueClass,
            Class<? extends OutputFormat> formatClass,
            Map<String, String> attributes) {
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
        if (attributes == null) {
            throw new IllegalArgumentException("attributes must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.keyClass = keyClass;
        this.valueClass = valueClass;
        this.formatClass = (Class<? extends OutputFormat<?, ?>>) formatClass;
        this.attributes = Collections.unmodifiableMap(new TreeMap<String, String>(attributes));
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

    /**
     * Returns the output attributes.
     * @return the attributes
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
