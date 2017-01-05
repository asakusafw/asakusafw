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
import javax.annotation.Generated;

import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.ExSummarized;
import com.asakusafw.compiler.flow.testing.model.ExSummarized2;
import com.asakusafw.compiler.flow.testing.model.KeyConflict;
/**
 * An operator implementation class for{@link SummarizeFlow}.
 */
@Generated("OperatorImplementationClassGenerator:0.1.0") public class SummarizeFlowImpl extends SummarizeFlow {
    /**
     * Creates a new instance.
     */
    public SummarizeFlowImpl() {
        return;
    }
    @Override public ExSummarized simple(Ex1 model) {
        throw new UnsupportedOperationException("summarize operator does not have method body");
    }
    @Override public ExSummarized2 renameKey(Ex1 model) {
        throw new UnsupportedOperationException("summarize operator does not have method body");
    }
    @Override public KeyConflict keyConflict(Ex1 model) {
        throw new UnsupportedOperationException("summarize operator does not have method body");
    }
}