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
package com.asakusafw.compiler.testing.flow;

import com.asakusafw.compiler.flow.testing.external.Ex1MockImporterDescription;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory.Update;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;

/**
 * Job which output multiple file sets.
 */
@JobFlow(name = "job")
public class MultipleOutputJob extends FlowDescription {

    private final In<Ex1> input;

    private final Out<Ex1> output1;

    private final Out<Ex1> output2;

    private final Out<Ex1> output3;

    private final Out<Ex1> output4;

    /**
     * Creates a new instance.
     * @param input input
     * @param output1 output (1)
     * @param output2 output (2)
     * @param output3 output (3)
     * @param output4 output (4)
     */
    public MultipleOutputJob(
            @Import(name = "input", description = Ex1MockImporterDescription.class)
            In<Ex1> input,
            @Export(name = "out1", description = Out1ExporterDesc.class)
            Out<Ex1> output1,
            @Export(name = "out2", description = Out2ExporterDesc.class)
            Out<Ex1> output2,
            @Export(name = "out3", description = Out3ExporterDesc.class)
            Out<Ex1> output3,
            @Export(name = "out4", description = Out4ExporterDesc.class)
            Out<Ex1> output4) {
        this.input = input;
        this.output1 = output1;
        this.output2 = output2;
        this.output3 = output3;
        this.output4 = output4;
    }

    @Override
    protected void describe() {
        ExOperatorFactory op = new ExOperatorFactory();

        Update result1 = op.update(input, 100);
        output1.add(result1.out);

        Update result2 = op.update(input, 200);
        output2.add(result2.out);

        Update result3 = op.update(input, 300);
        output3.add(result3.out);

        Update result4 = op.update(input, 400);
        output4.add(result4.out);
    }
}
