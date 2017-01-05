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
 * A jobflow class w/ the duplicated co-group stages.
 */
@FlowPart
public class DuplicateStage extends FlowDescription {

    private final In<Ex1> in;

    private final Out<Ex1> out;

    /**
     * Creates a new instance.
     * @param in input
     * @param out output
     */
    public DuplicateStage(In<Ex1> in, Out<Ex1> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        ExOperatorFactory f = new ExOperatorFactory();
        CoreOperatorFactory core = new CoreOperatorFactory();
        Update update = f.update(in, 10);
        Cogroup cog1 = f.cogroup(update.out, core.empty(Ex2.class));
        Cogroup cog2 = f.cogroup(update.out, core.empty(Ex2.class));
        out.add(cog1.r1);
        out.add(cog2.r1);
        core.stop(cog1.r2);
        core.stop(cog2.r2);
    }
}
