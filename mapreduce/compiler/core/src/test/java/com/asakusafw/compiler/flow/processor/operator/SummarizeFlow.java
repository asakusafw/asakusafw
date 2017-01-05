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
package com.asakusafw.compiler.flow.processor.operator;

import com.asakusafw.compiler.flow.processor.SummarizeFlowProcessor;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.ExSummarized;
import com.asakusafw.compiler.flow.testing.model.ExSummarized2;
import com.asakusafw.compiler.flow.testing.model.KeyConflict;
import com.asakusafw.vocabulary.operator.Summarize;

/**
 * An operator class for testing {@link SummarizeFlowProcessor}.
 */
public abstract class SummarizeFlow {

    /**
     * simple.
     * @param model target model
     * @return result model
     */
    @Summarize
    public abstract ExSummarized simple(Ex1 model);

    /**
     * summarize w/ renaming its key.
     * @param model target model
     * @return result model
     */
    @Summarize
    public abstract ExSummarized2 renameKey(Ex1 model);

    /**
     * Grouping key is also used for other aggregation operations.
     * @param model target model
     * @return result model
     */
    @Summarize
    public abstract KeyConflict keyConflict(Ex1 model);
}
