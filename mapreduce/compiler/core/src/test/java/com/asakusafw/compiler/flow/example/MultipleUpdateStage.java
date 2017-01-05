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

import com.asakusafw.compiler.flow.testing.external.Ex1MockExporterDescription;
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
 * A jobflow class w/ multiple sequential update operators.
 */
@SuppressWarnings("all")
@JobFlow(name = "testing")
public class MultipleUpdateStage extends FlowDescription {

    private final In<Ex1> in;

    private final Out<Ex1> out;

    public MultipleUpdateStage(
            @Import(name = "ex1", description = Ex1MockImporterDescription.class) In<Ex1> in,
            @Export(name = "ex1", description = Ex1MockExporterDescription.class) Out<Ex1> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        ExOperatorFactory f = new ExOperatorFactory();
        Update u1 = f.update(in, 100);
        Update u2 = f.update(u1.out, 200);
        Update u3 = f.update(u2.out, 300);
        out.add(u3.out);
    }
}
