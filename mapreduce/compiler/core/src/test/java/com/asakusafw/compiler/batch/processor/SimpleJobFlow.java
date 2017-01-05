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
package com.asakusafw.compiler.batch.processor;

import java.util.Collections;
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
 * Adds 100 to each input value.
 */
@JobFlow(name = "simple")
public class SimpleJobFlow extends FlowDescription {

    private final In<Ex1> in;

    private final Out<Ex1> out;

    /**
     * Creates a new instance.
     * @param in input
     * @param out output
     */
    public SimpleJobFlow(
            @Import(name = "x", description = Importer.class)
            In<Ex1> in,
            @Export(name = "x", description = Exporter.class)
            Out<Ex1> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        UpdateFlowFactory f = new UpdateFlowFactory();
        WithParameter op = f.withParameter(in, 100);
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
            return Collections.singleton("target/testing/SimpleJobFlow/importer/out");
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
            return "target/testing/SimpleJobFlow/exporter/out-*";
        }
    }
}
