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

import com.asakusafw.compiler.flow.processor.ExtendFlowProcessor;
import com.asakusafw.compiler.flow.testing.external.Ex1MockExporterDescription;
import com.asakusafw.compiler.flow.testing.external.Part1MockImporterDescription;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Part1;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Extend;

/**
 * Test flow for {@link ExtendFlowProcessor}.
 */
@JobFlow(name = "testing")
public class ExtendFlowSimple extends FlowDescription {

    private final In<Part1> in;

    private final Out<Ex1> out;

    /**
     * Creates a new instance.
     * @param in input channel
     * @param out output channel
     */
    public ExtendFlowSimple(
            @Import(name = "e1", description = Part1MockImporterDescription.class)
            In<Part1> in,
            @Export(name = "e1", description = Ex1MockExporterDescription.class)
            Out<Ex1> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        CoreOperatorFactory core = new CoreOperatorFactory();
        Extend<Ex1> project = core.extend(in, Ex1.class);
        out.add(project);
    }
}
