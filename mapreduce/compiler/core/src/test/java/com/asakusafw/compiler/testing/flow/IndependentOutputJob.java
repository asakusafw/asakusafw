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
 * Job which output nested file sets.
 */
@JobFlow(name = "job")
public class IndependentOutputJob extends FlowDescription {

    private final In<Ex1> input;

    private final Out<Ex1> output;

    private final Out<Ex1> independent;

    /**
     * Creates a new instance.
     * @param input input
     * @param output output
     * @param independent output to independent directory
     */
    public IndependentOutputJob(
            @Import(name = "input", description = Ex1MockImporterDescription.class)
            In<Ex1> input,
            @Export(name = "output", description = Out1ExporterDesc.class)
            Out<Ex1> output,
            @Export(name = "independent", description = IndependentOutExporterDesc.class)
            Out<Ex1> independent) {
        this.input = input;
        this.output = output;
        this.independent = independent;
    }

    @Override
    protected void describe() {
        ExOperatorFactory op = new ExOperatorFactory();

        Update result1 = op.update(input, 100);
        output.add(result1.out);

        Update result2 = op.update(input, 200);
        independent.add(result2.out);
    }
}
