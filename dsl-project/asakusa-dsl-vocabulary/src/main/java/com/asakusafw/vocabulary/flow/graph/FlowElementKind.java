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
package com.asakusafw.vocabulary.flow.graph;

/**
 * {@link FlowElement}の種類。
 */
public enum FlowElementKind {

    /**
     * 入力。
     * <p>
     * この種類である{@link FlowElement}は、
     * その{@link FlowElement#getDescription() 定義記述}
     * が必ず{@link InputDescription}である。
     * </p>
     */
    INPUT,

    /**
     * 出力。
     * <p>
     * この種類である{@link FlowElement}は、
     * その{@link FlowElement#getDescription() 定義記述}
     * が必ず{@link OutputDescription}である。
     * </p>
     */
    OUTPUT,

    /**
     * 演算子。
     * <p>
     * この種類である{@link FlowElement}は、
     * その{@link FlowElement#getDescription() 定義記述}
     * が必ず{@link OperatorDescription}である。
     * </p>
     */
    OPERATOR,

    /**
     * フロー部品。
     * <p>
     * この種類である{@link FlowElement}は、
     * その{@link FlowElement#getDescription() 定義記述}
     * が必ず{@link FlowPartDescription}である。
     * </p>
     */
    FLOW_COMPONENT,

    /**
     * 疑似要素。
     */
    PSEUD,
}
