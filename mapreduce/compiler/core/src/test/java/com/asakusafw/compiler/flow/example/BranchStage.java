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
package com.asakusafw.compiler.flow.example;

import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory.Branch;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.FlowPart;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory;

/**
 * A test flow for branched stages.
 */
@FlowPart
public class BranchStage extends FlowDescription {

    private final In<Ex1> in;

    private final Out<Ex1> out1;

    private final Out<Ex1> out2;

    private final Out<Ex1> out3;

    /**
     * Creates a new instance.
     * @param in input
     * @param out1 output
     * @param out2 output
     * @param out3 output
     */
    public BranchStage(In<Ex1> in, Out<Ex1> out1, Out<Ex1> out2, Out<Ex1> out3) {
        this.in = in;
        this.out1 = out1;
        this.out2 = out2;
        this.out3 = out3;
    }

    @Override
    protected void describe() {
        ExOperatorFactory f = new ExOperatorFactory();
        CoreOperatorFactory core = new CoreOperatorFactory();
        Branch branch = f.branch(in);
        out1.add(branch.yes);
        out2.add(branch.yes);
        out2.add(branch.no);
        out3.add(branch.no);
        core.stop(branch.cancel);
    }
}
