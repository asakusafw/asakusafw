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
package com.asakusafw.vocabulary.flow.graph;

import java.lang.reflect.Type;
import java.text.MessageFormat;

/**
 * 入出力ポートの定義記述。
 */
public class FlowElementPortDescription {

    private String name;

    private Type dataType;

    private PortDirection direction;

    private ShuffleKey shuffleKey;

    /**
     * インスタンスを生成する。
     * @param name ポートの名称
     * @param dataType ポートが取り扱うデータの種類
     * @param direction ポートの入出力方向
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FlowElementPortDescription(String name, Type dataType, PortDirection direction) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        if (direction == null) {
            throw new IllegalArgumentException("direction must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.dataType = dataType;
        this.direction = direction;
        this.shuffleKey = null;
    }

    /**
     * シャッフルを含む入力のインスタンスを生成する。
     * @param name ポートの名称
     * @param dataType ポートが取り扱うデータの種類
     * @param shuffleKey シャッフル条件
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FlowElementPortDescription(
            String name,
            Type dataType,
            ShuffleKey shuffleKey) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        if (shuffleKey == null) {
            throw new IllegalArgumentException("shuffleKey must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.dataType = dataType;
        this.direction = PortDirection.INPUT;
        this.shuffleKey = shuffleKey;
    }

    /**
     * このポートの名前を返す。
     * @return このポートの名前
     */
    public String getName() {
        return name;
    }

    /**
     * このポートに流れるデータの種類を返す。
     * @return このポートに流れるデータの種類
     */
    public Type getDataType() {
        return dataType;
    }

    /**
     * ポートの入出力方向を返す。
     * @return ポートの入出力方向
     */
    public PortDirection getDirection() {
        return direction;
    }

    /**
     * シャッフル条件を返す。
     * @return シャッフル条件、存在しない場合は{@code null}
     */
    public ShuffleKey getShuffleKey() {
        return shuffleKey;
    }

    @Override
    public String toString() {
        if (direction == PortDirection.INPUT) {
            return MessageFormat.format(
                    "Input({0}):{1}", //$NON-NLS-1$
                    getName(),
                    getDataType());
        } else {
            return MessageFormat.format(
                    "Output({0}):{1}", //$NON-NLS-1$
                    getName(),
                    getDataType());
        }
    }
}
