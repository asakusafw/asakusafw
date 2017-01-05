/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.flow;

import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;

/**
 * Represents an abstract data source for downstream operators and flow outputs.
 * @param <T> the data model type
 */
public interface Source<T> {

    /**
     * Returns the internal port representation of this source element.
     * Application developers should not use this method directly.
     * @return the internal port representation
     */
    FlowElementOutput toOutputPort();
}
