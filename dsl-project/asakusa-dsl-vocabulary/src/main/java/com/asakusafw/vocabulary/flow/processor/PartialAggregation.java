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
package com.asakusafw.vocabulary.flow.processor;

import com.asakusafw.vocabulary.flow.graph.FlowElementAttribute;

/**
 * Represents strategies of aggregate operation.
 * @since 0.2.0
 */
public enum PartialAggregation implements FlowElementAttribute {

    /**
     * Partial aggregation is NOT performed.
     */
    TOTAL,

    /**
     * Uses partial aggregation.
     * With this strategy, framework API is disabled in the target operator.
     */
    PARTIAL,

    /**
     * Follows the default settings.
     */
    DEFAULT,
}
