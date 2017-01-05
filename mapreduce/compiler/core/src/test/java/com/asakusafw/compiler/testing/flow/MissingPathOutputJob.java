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
 * Job with invalid output.
 */
@JobFlow(name = "job")
public class MissingPathOutputJob extends FlowDescription {

    private final In<Ex1> input;

    private final Out<Ex1> output;

    /**
     * Creates a new instance.
     * @param input input
     * @param output output (invalid)
     */
    public MissingPathOutputJob(
            @Import(name = "input", description = Ex1MockImporterDescription.class)
            In<Ex1> input,
            @Export(name = "output", description = MissingPathExporterDesc.class)
            Out<Ex1> output) {
        this.input = input;
        this.output = output;
    }

    @Override
    protected void describe() {
        ExOperatorFactory op = new ExOperatorFactory();
        Update result = op.update(input, 100);
        output.add(result.out);
    }
}
