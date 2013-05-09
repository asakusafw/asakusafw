/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
import com.asakusafw.compiler.flow.testing.external.Ex2MockExporterDescription;
import com.asakusafw.compiler.flow.testing.external.Ex2MockImporterDescription;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory.Branch;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory.Cogroup;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory.Update;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory;


/**
 * シャッフルを行わないジョブフロー。
 */
@SuppressWarnings("all")
@JobFlow(name = "testing")
public class ComplexStage extends FlowDescription {

    private In<Ex1> in1;

    private In<Ex2> in2;

    private Out<Ex1> out1;

    private Out<Ex2> out2;

    public ComplexStage(
            @Import(name = "ex1", description = Ex1MockImporterDescription.class) In<Ex1> in1,
            @Import(name = "ex2", description = Ex2MockImporterDescription.class) In<Ex2> in2,
            @Export(name = "ex1", description = Ex1MockExporterDescription.class) Out<Ex1> out1,
            @Export(name = "ex2", description = Ex2MockExporterDescription.class) Out<Ex2> out2) {
        this.in1 = in1;
        this.in2 = in2;
        this.out1 = out1;
        this.out2 = out2;
    }

    @Override
    protected void describe() {
        ExOperatorFactory f = new ExOperatorFactory();
        CoreOperatorFactory core = new CoreOperatorFactory();
        Update update = f.update(in1, 10);
        Branch bra = f.branch(update.out);
        Cogroup cog = f.cogroup(core.confluent(bra.yes, bra.cancel), in2);
        core.stop(bra.no);
        out1.add(cog.r1);
        out2.add(cog.r2);
    }
}
