/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.compiler.fileio.flow;

import com.asakusafw.compiler.fileio.external.Ex1MockExporterDescription;
import com.asakusafw.compiler.fileio.model.Ex1;
import com.asakusafw.compiler.fileio.model.Ex2;
import com.asakusafw.compiler.fileio.operator.ExOperatorFactory;
import com.asakusafw.compiler.fileio.operator.ExOperatorFactory.Update;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory;

/**
 * Job with a normal input.
 */
@JobFlow(name = "job")
public class TinyInputJob extends FlowDescription {

    private final In<Ex2> input;

    private final Out<Ex1> output;

    /**
     * Creates a new instance.
     * @param input an input
     * @param output an output
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TinyInputJob(
            @Import(name = "input", description = TinyImporterDescription.class)
            In<Ex2> input,
            @Export(name = "out", description = Ex1MockExporterDescription.class)
            Out<Ex1> output) {
        this.input = input;
        this.output = output;
    }

    @Override
    protected void describe() {
        ExOperatorFactory op = new ExOperatorFactory();
        Update result = op.update(new CoreOperatorFactory().restructure(input, Ex1.class), 100);
        output.add(result.out);
    }
}
