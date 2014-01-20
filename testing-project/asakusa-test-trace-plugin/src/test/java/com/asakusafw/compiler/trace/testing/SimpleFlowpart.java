/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.compiler.trace.testing;

import com.asakusafw.compiler.trace.testing.SimpleOperatorFactory.Line;
import com.asakusafw.compiler.trace.testing.dmdl.model.Model;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.FlowPart;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;

/**
 * Simple flowpart for testing.
 */
@FlowPart
public class SimpleFlowpart extends FlowDescription {

    final In<Model> in;

    final Out<Model> out;

    /**
     * Creates a new instance.
     * @param in input
     * @param out output
     */
    public SimpleFlowpart(In<Model> in, Out<Model> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        SimpleOperatorFactory f = new SimpleOperatorFactory();
        Line line = f.line(in);
        out.add(line.out);
    }
}
