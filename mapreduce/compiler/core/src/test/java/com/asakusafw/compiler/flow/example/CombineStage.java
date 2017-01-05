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
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory.CogroupAdd;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory.FoldAdd;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.FlowPart;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;

/**
 * A jobflow class w/ combiner operations.
 */
@SuppressWarnings("all")
@FlowPart
public class CombineStage extends FlowDescription {

    private final In<Ex1> in1;

    private final Out<Ex1> out1;

    private final Out<Ex1> out2;

    public CombineStage(In<Ex1> in1, Out<Ex1> out1, Out<Ex1> out2) {
        this.in1 = in1;
        this.out1 = out1;
        this.out2 = out2;
    }

    @Override
    protected void describe() {
        ExOperatorFactory f = new ExOperatorFactory();
        FoldAdd fold = f.foldAdd(in1);
        CogroupAdd cogroup = f.cogroupAdd(in1);
        out1.add(fold.out);
        out2.add(cogroup.result);
    }
}
