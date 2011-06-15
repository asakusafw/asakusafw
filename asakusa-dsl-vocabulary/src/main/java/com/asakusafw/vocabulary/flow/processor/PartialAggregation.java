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
package com.asakusafw.vocabulary.flow.processor;

import com.asakusafw.vocabulary.flow.graph.FlowElementAttribute;

/**
 * 部分集約の性質。
 * @since 0.2.0
 */
public enum PartialAggregation implements FlowElementAttribute {

    /**
     * 部分集約を許さない。
     */
    TOTAL,

    /**
     * 部分集約を許す。
     * <p>
     * 部分集約を可能にした演算子の内部では、フレームワークAPIを利用できない。
     * </p>
     */
    PARTIAL,

    /**
     * デフォルトの設定に従う。
     */
    DEFAULT,
}
