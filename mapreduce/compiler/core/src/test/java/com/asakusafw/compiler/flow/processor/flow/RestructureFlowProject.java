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

import com.asakusafw.compiler.flow.processor.RestructureFlowProcessor;
import com.asakusafw.compiler.flow.testing.external.Ex1MockImporterDescription;
import com.asakusafw.compiler.flow.testing.external.Part1MockExporterDescription;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Part1;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Restructure;

/**
 * Test flow for {@link RestructureFlowProcessor}.
 */
@JobFlow(name = "testing")
public class RestructureFlowProject extends FlowDescription {

    private final In<Ex1> in;

    private final Out<Part1> out;

    /**
     * Creates a new instance.
     * @param in input channel
     * @param out output channel
     */
    public RestructureFlowProject(
            @Import(name = "e1", description = Ex1MockImporterDescription.class)
            In<Ex1> in,
            @Export(name = "e1", description = Part1MockExporterDescription.class)
            Out<Part1> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        CoreOperatorFactory core = new CoreOperatorFactory();
        Restructure<Part1> project = core.restructure(in, Part1.class);
        out.add(project);
    }
}
