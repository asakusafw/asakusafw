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
package com.asakusafw.vocabulary.flow.graph;

import java.text.MessageFormat;

import com.asakusafw.vocabulary.flow.In;


/**
 * フローへの入力。
 * @param <T> 取り扱うデータの種類
 */
public final class FlowIn<T> implements In<T> {

    private InputDescription description;

    private FlowElementResolver resolver;

    /**
     * インスタンスを生成する。
     * @param description この入力の定義記述
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FlowIn(InputDescription description) {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        this.description = description;
        this.resolver = new FlowElementResolver(description);
    }

    /**
     * インスタンスを生成する。
     * @param <T> 取り扱うデータの種類
     * @param description この入力の定義記述
     * @return 生成したインスタンス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static <T> FlowIn<T> newInstance(InputDescription description) {
        return new FlowIn<T>(description);
    }

    /**
     * この入力の定義記述を返す。
     * @return この入力の定義記述
     */
    public InputDescription getDescription() {
        return description;
    }

    /**
     * この入力に対応するフロー要素を返す。
     * @return 対応するフロー要素
     */
    public FlowElement getFlowElement() {
        return resolver.getElement();
    }

    @Override
    public FlowElementOutput toOutputPort() {
        return resolver.getOutput(InputDescription.OUTPUT_PORT_NAME);
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}({1})",
                getDescription(),
                getFlowElement());
    }
}
