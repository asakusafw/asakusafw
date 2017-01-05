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
package com.asakusafw.compiler.batch;

import com.asakusafw.compiler.flow.processor.operator.UpdateFlowFactory;
import com.asakusafw.compiler.flow.processor.operator.UpdateFlowFactory.WithParameter;
import com.asakusafw.compiler.flow.testing.external.Ex1MockImporterDescription;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.testing.TemporaryOutputDescription;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;

/**
 * Add 1 to each input.
 */
@JobFlow(name = "first")
public class FirstJobFlow extends FlowDescription {

    private final In<Ex1> in;

    private final Out<Ex1> out;

    /**
     * Creates a new instance.
     * @param in input
     * @param out output
     */
    public FirstJobFlow(
            @Import(name = "first", description = Ex1MockImporterDescription.class)
            In<Ex1> in,
            @Export(name = "first", description = Exporter.class)
            Out<Ex1> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        UpdateFlowFactory f = new UpdateFlowFactory();
        WithParameter op = f.withParameter(in, 1);
        out.add(op.out);
    }

    /**
     * An exporter for testing.
     */
    public static class Exporter extends TemporaryOutputDescription {

        @Override
        public Class<?> getModelType() {
            return Ex1.class;
        }

        @Override
        public String getPathPrefix() {
            return "target/testing/sequencefile/first/out-*";
        }
    }
}
