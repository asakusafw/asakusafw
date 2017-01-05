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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.asakusafw.compiler.flow.processor.operator.UpdateFlowFactory;
import com.asakusafw.compiler.flow.processor.operator.UpdateFlowFactory.WithParameter;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.testing.TemporaryInputDescription;
import com.asakusafw.compiler.testing.TemporaryOutputDescription;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;

/**
 * Add 10 to each input.
 */
@JobFlow(name = "join")
public class JoinJobFlow extends FlowDescription {

    private final In<Ex1> in;

    private final Out<Ex1> out;

    /**
     * Creates a new instance.
     * @param in input
     * @param out output
     */
    public JoinJobFlow(
            @Import(name = "join", description = Importer.class)
            In<Ex1> in,
            @Export(name = "join", description = Exporter.class)
            Out<Ex1> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        UpdateFlowFactory f = new UpdateFlowFactory();
        WithParameter op = f.withParameter(in, 10);
        out.add(op.out);
    }

    /**
     * An importer for testing.
     */
    public static class Importer extends TemporaryInputDescription {

        @Override
        public Class<?> getModelType() {
            return Ex1.class;
        }

        @Override
        public Set<String> getPaths() {
            return new HashSet<>(Arrays.asList(
                    "target/testing/sequencefile/second/out-*",
                    "target/testing/sequencefile/side/out-*"));
        }
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
            return "target/testing/sequencefile/join/out-*";
        }
    }
}
