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

import com.asakusafw.compiler.flow.processor.MasterBranchFlowProcessor;
import com.asakusafw.compiler.flow.processor.operator.MasterBranchFlowFactory;
import com.asakusafw.compiler.flow.processor.operator.MasterBranchFlowFactory.Simple;
import com.asakusafw.compiler.flow.testing.external.Ex1MockExporterDescription;
import com.asakusafw.compiler.flow.testing.external.Ex1MockImporterDescription;
import com.asakusafw.compiler.flow.testing.external.Ex2MockImporterDescription;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;


/**
 * test for {@link MasterBranchFlowProcessor}.
 */
@JobFlow(name = "testing")
public class MasterBranchFlowSimple extends FlowDescription {

    private In<Ex1> in1;

    private In<Ex2> in2;

    private Out<Ex1> outHigh;

    private Out<Ex1> outLow;

    private Out<Ex1> outStop;

    /**
     * Creates a new instance.
     * @param in1 input1
     * @param in2 input2
     * @param outHigh output for High
     * @param outLow output for Low
     * @param outStop output for Stop
     */
    public MasterBranchFlowSimple(
            @Import(name = "e1", description = Ex1MockImporterDescription.class)
            In<Ex1> in1,
            @Import(name = "e2", description = Ex2MockImporterDescription.class)
            In<Ex2> in2,
            @Export(name = "high", description = Ex1MockExporterDescription.class)
            Out<Ex1> outHigh,
            @Export(name = "low", description = Ex1MockExporterDescription.class)
            Out<Ex1> outLow,
            @Export(name = "stop", description = Ex1MockExporterDescription.class)
            Out<Ex1> outStop) {
        this.in1 = in1;
        this.in2 = in2;
        this.outHigh = outHigh;
        this.outLow = outLow;
        this.outStop = outStop;
    }

    @Override
    protected void describe() {
        MasterBranchFlowFactory f = new MasterBranchFlowFactory();
        Simple op = f.simple(in2, in1);
        outHigh.add(op.high);
        outLow.add(op.low);
        outStop.add(op.stop);
    }
}
