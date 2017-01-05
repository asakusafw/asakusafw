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
package com.asakusafw.compiler.flow.processor.flow;

import com.asakusafw.compiler.flow.processor.CoGroupFlowProcessor;
import com.asakusafw.compiler.flow.processor.operator.CoGroupFlowFactory;
import com.asakusafw.compiler.flow.processor.operator.CoGroupFlowFactory.Swap;
import com.asakusafw.compiler.flow.testing.external.Ex1MockExporterDescription;
import com.asakusafw.compiler.flow.testing.external.Ex1MockImporterDescription;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;


/**
 * test for {@link CoGroupFlowProcessor}.
 */
@JobFlow(name = "testing")
public class CoGroupFlowSwap extends FlowDescription {

    private final In<Ex1> in1;

    private final Out<Ex1> out1;

    /**
     * Creates a new instance.
     * @param in1 input
     * @param out1 output
     */
    public CoGroupFlowSwap(
            @Import(name = "e1", description = Ex1MockImporterDescription.class)
            In<Ex1> in1,
            @Export(name = "e1", description = Ex1MockExporterDescription.class)
            Out<Ex1> out1) {
        this.in1 = in1;
        this.out1 = out1;
    }

    @Override
    protected void describe() {
        CoGroupFlowFactory f = new CoGroupFlowFactory();
        Swap op = f.swap(in1);
        out1.add(op.r1);
    }
}
