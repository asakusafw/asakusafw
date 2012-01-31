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
package com.asakusafw.vocabulary.flow.testing;

import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.InputDescription;

/**
 * {@link In}のモック。
 * @param <T> 取り扱うデータの種類
 */
public class MockIn<T> implements In<T> {

    private FlowElementResolver resolver;

    /**
     * インスタンスを生成する。
     * @param type 入力の型
     * @param name 入力の名前
     */
    public MockIn(Class<T> type, String name) {
        InputDescription desc = new InputDescription(name, type);
        resolver = new FlowElementResolver(desc);
    }

    /**
     * インスタンスを生成する。
     * @param <T> 取り扱うデータの種類
     * @param type 入力の型
     * @param name 入力の名前
     * @return 生成したインスタンス
     */
    public static <T> MockIn<T> of(Class<T> type, String name) {
        return new MockIn<T>(type, name);
    }

    @Override
    public FlowElementOutput toOutputPort() {
        return resolver.getOutput(InputDescription.OUTPUT_PORT_NAME);
    }

    /**
     * この要素に関連するフロー要素を返す。
     * @return この要素に関連するフロー要素
     */
    public FlowElement toElement() {
        return resolver.getElement();
    }
}
