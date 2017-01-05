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

import com.asakusafw.compiler.flow.processor.ExtractFlowProcessor;
import com.asakusafw.compiler.flow.processor.operator.ExtractFlowFactory;
import com.asakusafw.compiler.flow.processor.operator.ExtractFlowFactory.WithParameter;
import com.asakusafw.compiler.flow.testing.external.Ex1MockImporterDescription;
import com.asakusafw.compiler.flow.testing.external.Ex2MockExporterDescription;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;


/**
 * test for {@link ExtractFlowProcessor}.
 */
@JobFlow(name = "testing")
public class ExtractFlowWithParameter extends FlowDescription {

    private In<Ex1> in1;

    private Out<Ex2> out1;

    /**
     * Creates a new instance.
     * @param in1 input
     * @param out1 output
     */
    public ExtractFlowWithParameter(
            @Import(name = "in", description = Ex1MockImporterDescription.class)
            In<Ex1> in1,
            @Export(name = "out1", description = Ex2MockExporterDescription.class)
            Out<Ex2> out1) {
        this.in1 = in1;
        this.out1 = out1;
    }

    @Override
    protected void describe() {
        ExtractFlowFactory f = new ExtractFlowFactory();
        WithParameter op = f.withParameter(in1, 100);
        out1.add(op.r1);
    }
}
