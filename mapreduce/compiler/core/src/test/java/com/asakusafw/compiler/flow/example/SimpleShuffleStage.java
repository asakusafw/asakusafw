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
package com.asakusafw.compiler.flow.example;

import com.asakusafw.compiler.flow.testing.external.Ex1MockImporterDescription;
import com.asakusafw.compiler.flow.testing.external.ExSummarizedMockExporterDescription;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.ExSummarized;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory.Summarize;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;

/**
 * A jobflow class w/ shuffle operation.
 */
@SuppressWarnings("all")
@JobFlow(name = "testing")
public class SimpleShuffleStage extends FlowDescription {

    private final In<Ex1> in;

    private final Out<ExSummarized> out;

    public SimpleShuffleStage(
            @Import(name = "ex1", description = Ex1MockImporterDescription.class) In<Ex1> in,
            @Export(name = "exs", description = ExSummarizedMockExporterDescription.class) Out<ExSummarized> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        ExOperatorFactory f = new ExOperatorFactory();
        Summarize summarized = f.summarize(in);
        out.add(summarized.out);
    }
}
