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

import com.asakusafw.compiler.flow.processor.BranchFlowProcessor;
import com.asakusafw.compiler.flow.processor.operator.BranchFlowFactory;
import com.asakusafw.compiler.flow.processor.operator.BranchFlowFactory.WithParameter;
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
 * test for {@link BranchFlowProcessor}.
 */
@JobFlow(name = "testing")
public class BranchFlowWithParameter extends FlowDescription {

    private In<Ex1> in1;

    private Out<Ex1> outHigh;

    private Out<Ex1> outLow;

    private Out<Ex1> outStop;

    /**
     * Creates a new instance.
     * @param in1 input
     * @param outHigh output for High
     * @param outLow output for Low
     * @param outStop output for Stop
     */
    public BranchFlowWithParameter(
            @Import(name = "e1", description = Ex1MockImporterDescription.class)
            In<Ex1> in1,
            @Export(name = "high", description = Ex1MockExporterDescription.class)
            Out<Ex1> outHigh,
            @Export(name = "low", description = Ex1MockExporterDescription.class)
            Out<Ex1> outLow,
            @Export(name = "stop", description = Ex1MockExporterDescription.class)
            Out<Ex1> outStop) {
        this.in1 = in1;
        this.outHigh = outHigh;
        this.outLow = outLow;
        this.outStop = outStop;
    }

    @Override
    protected void describe() {
        BranchFlowFactory f = new BranchFlowFactory();
        WithParameter op = f.withParameter(in1, 50);
        outHigh.add(op.high);
        outLow.add(op.low);
        outStop.add(op.stop);
    }
}
