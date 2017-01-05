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
package com.asakusafw.compiler.testing.flow;

import com.asakusafw.compiler.flow.processor.operator.FoldFlowFactory;
import com.asakusafw.compiler.flow.processor.operator.UpdateFlowFactory;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;

/**
 * A flow which has rendezvous - map fragments.
 */
public class StraightRendezvousFragments extends FlowDescription {

    private final In<Ex1> in1;

    private final Out<Ex1> out1;

    /**
     * Creates a new instance.
     * @param in1 the input
     * @param out1 the output
     */
    public StraightRendezvousFragments(In<Ex1> in1, Out<Ex1> out1) {
        this.in1 = in1;
        this.out1 = out1;
    }

    @Override
    protected void describe() {
        FoldFlowFactory folds = new FoldFlowFactory();
        UpdateFlowFactory updates = new UpdateFlowFactory();
        FoldFlowFactory.Simple fold = folds.simple(in1);
        UpdateFlowFactory.Simple update = updates.simple(fold.out);
        out1.add(update.out);
    }
}
