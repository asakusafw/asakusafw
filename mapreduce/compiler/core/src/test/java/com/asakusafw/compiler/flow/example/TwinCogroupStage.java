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

import com.asakusafw.compiler.flow.processor.operator.FoldFlowFactory;
import com.asakusafw.compiler.flow.processor.operator.FoldFlowFactory.Simple;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory.Cogroup;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.FlowPart;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Confluent;

/**
 * A jobflow class which contains two concurrent co-group operations.
 */
@SuppressWarnings("all")
@FlowPart
public class TwinCogroupStage extends FlowDescription {

    private final In<Ex1> in1;

    private final Out<Ex1> out1;

    public TwinCogroupStage(
            In<Ex1> in1,
            Out<Ex1> out1) {
        this.in1 = in1;
        this.out1 = out1;
    }

    @Override
    protected void describe() {
        ExOperatorFactory f = new ExOperatorFactory();
        CoreOperatorFactory core = new CoreOperatorFactory();
        Cogroup cog1 = f.cogroup(in1, core.empty(Ex2.class));
        Cogroup cog2 = f.cogroup(in1, core.empty(Ex2.class));
        core.stop(cog1.r2);
        core.stop(cog2.r2);

        FoldFlowFactory fff = new FoldFlowFactory();
        Confluent<Ex1> con1 = core.confluent(cog1.r1, cog2.r1);
        Simple fold = fff.simple(con1);
        out1.add(fold.out);
    }
}
