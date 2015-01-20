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

import java.text.MessageFormat;

import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.Source;


/**
 * フローからの出力。
 * @param <T> 取り扱うデータの種類
 */
public final class FlowOut<T> implements Out<T> {

    private OutputDescription description;

    private FlowElementResolver resolver;

    /**
     * インスタンスを生成する。
     * @param description この出力の定義記述
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FlowOut(OutputDescription description) {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        this.description = description;
        this.resolver = new FlowElementResolver(description);
    }

    /**
     * インスタンスを生成する。
     * @param <T> 取り扱うデータの種類
     * @param description この出力の定義記述
     * @return 生成したインスタンス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static <T> FlowOut<T> newInstance(OutputDescription description) {
        return new FlowOut<T>(description);
    }

    /**
     * この出力の定義記述を返す。
     * @return この出力の定義記述
     */
    public OutputDescription getDescription() {
        return description;
    }

    /**
     * この出力に対応するフロー要素を返す。
     * @return 対応するフロー要素
     */
    public FlowElement getFlowElement() {
        return resolver.getElement();
    }

    @Override
    public void add(Source<T> source) {
        PortConnection.connect(source.toOutputPort(), this.toInputPort());
    }

    /**
     * この要素のポートとしての表現を返す。
     * @return この要素のポートとしての表現
     */
    public FlowElementInput toInputPort() {
        return resolver.getInput(OutputDescription.INPUT_PORT_NAME);
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}({1})",
                getDescription(),
                getFlowElement());
    }
}
