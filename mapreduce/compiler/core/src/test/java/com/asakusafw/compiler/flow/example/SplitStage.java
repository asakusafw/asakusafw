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
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory.Cogroup;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory.Update;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.FlowPart;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory;


/**
 * Splits mappers and then unify them.
 */
@SuppressWarnings("all")
@FlowPart
public class SplitStage extends FlowDescription {

    private final In<Ex1> in1;

    private final Out<Ex1> out1;

    private final Out<Ex1> out2;

    public SplitStage(
            In<Ex1> in1,
            Out<Ex1> out1,
            Out<Ex1> out2) {
        this.in1 = in1;
        this.out1 = out1;
        this.out2 = out2;
    }

    @Override
    protected void describe() {
        ExOperatorFactory f = new ExOperatorFactory();
        CoreOperatorFactory core = new CoreOperatorFactory();
        Update update0 = f.update(in1, 1);
        Update update1 = f.update(core.checkpoint(update0.out), 2);
        Cogroup cog1 = f.cogroup(update1.out, core.empty(Ex2.class));
        core.stop(cog1.r2);
        Cogroup cog2 = f.cogroup(update1.out, core.empty(Ex2.class));
        core.stop(cog2.r2);
        out1.add(cog1.r1);
        out2.add(cog2.r1);
    }
}
