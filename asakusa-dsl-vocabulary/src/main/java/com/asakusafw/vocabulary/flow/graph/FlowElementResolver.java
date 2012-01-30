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

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.asakusafw.vocabulary.flow.Source;


/**
 * {@link FlowElement}の接続を解決する。
 */
public class FlowElementResolver {

    private FlowElement element;

    private Map<String, FlowElementInput> inputPorts;

    private Map<String, FlowElementOutput> outputPorts;

    /**
     * インスタンスを生成する。
     * @param description {@link FlowElement}の元になる定義記述
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FlowElementResolver(FlowElementDescription description) {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        this.inputPorts = new HashMap<String, FlowElementInput>();
        this.outputPorts = new HashMap<String, FlowElementOutput>();
        this.element = new FlowElement(description);
        for (FlowElementInput port : element.getInputPorts()) {
            inputPorts.put(port.getDescription().getName(), port);
        }
        for (FlowElementOutput port : element.getOutputPorts()) {
            outputPorts.put(port.getDescription().getName(), port);
        }
    }

    /**
     * このオブジェクトが表現するフロー要素を返す。
     * @return このオブジェクトが表現するフロー要素
     */
    public FlowElement getElement() {
        return element;
    }

    /**
     * 指定の名前を持つ入力ポートの表現を返す。
     * @param name ポートの名前
     * @return 対応するポート
     * @throws NoSuchElementException ポートが発見できない場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FlowElementInput getInput(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        FlowElementInput port = inputPorts.get(name);
        if (port == null) {
            throw new NoSuchElementException(name);
        }
        return port;
    }

    /**
     * 指定の名前を持つ出力ポートの表現を返す。
     * @param name ポートの名前
     * @return 対応するポート
     * @throws NoSuchElementException ポートが発見できない場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FlowElementOutput getOutput(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        FlowElementOutput port = outputPorts.get(name);
        if (port == null) {
            throw new NoSuchElementException(name);
        }
        return port;
    }

    /**
     * 指定の名前を持つ入力ポートと、指定のソースを結合する。
     * @param name ポートの名前
     * @param source 結合するソース
     * @return 結合結果
     * @throws NoSuchElementException ポートが発見できない場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public PortConnection resolveInput(String name, Source<?> source) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (source == null) {
            throw new IllegalArgumentException("source must not be null"); //$NON-NLS-1$
        }
        FlowElementInput port = getInput(name);
        return PortConnection.connect(source.toOutputPort(), port);
    }

    /**
     * 指定の名前を持つ出力ポートを、ソースの表現にして返す。
     * @param <T> データの種類
     * @param name ポートの名前
     * @return 対応するソースの表現
     * @throws NoSuchElementException ポートが発見できない場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public <T> Source<T> resolveOutput(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        FlowElementOutput port = getOutput(name);
        return new OutputDriver<T>(port);
    }

    /**
     * 対象要素の名前を設定する。
     * @param name 設定する名前
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     * @throws UnsupportedOperationException 対象要素が名前を変更できない要素である場合
     */
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        FlowElementDescription desc = element.getDescription();
        desc.setName(name);
    }

    /**
     * {@link FlowElementOutput}を{@link Source}として取り扱うためのドライバ。
     * @param <T> 取り扱うデータの種類
     */
    public static class OutputDriver<T> implements Source<T> {

        private FlowElementOutput outputPort;

        /**
         * インスタンスを生成する。
         * @param outputPort 対応する出力ポート
         * @throws IllegalArgumentException 引数に{@code null}が指定された場合
         */
        public OutputDriver(FlowElementOutput outputPort) {
            if (outputPort == null) {
                throw new IllegalArgumentException("outputPort must not be null"); //$NON-NLS-1$
            }
            this.outputPort = outputPort;
        }

        @Override
        public FlowElementOutput toOutputPort() {
            return outputPort;
        }
    }
}
