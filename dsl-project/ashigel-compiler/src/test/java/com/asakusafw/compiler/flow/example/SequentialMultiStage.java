/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import com.asakusafw.compiler.flow.processor.operator.CoGroupFlowFactory;
import com.asakusafw.compiler.flow.processor.operator.CoGroupFlowFactory.Op1;
import com.asakusafw.compiler.flow.testing.external.Ex1MockExporterDescription;
import com.asakusafw.compiler.flow.testing.external.Ex1MockImporterDescription;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;


/**
 * シーケンシャルにマルチステージになるジョブフロー。
 */
@SuppressWarnings("all")
@JobFlow(name = "testing")
public class SequentialMultiStage extends FlowDescription {

    private In<Ex1> in;

    private Out<Ex1> out;

    public SequentialMultiStage(
            @Import(name = "ex1", description = Ex1MockImporterDescription.class) In<Ex1> in,
            @Export(name = "ex1", description = Ex1MockExporterDescription.class) Out<Ex1> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        CoGroupFlowFactory f = new CoGroupFlowFactory();
        Op1 st1 = f.op1(in);
        Op1 st2 = f.op1(st1.r1);
        out.add(st2.r1);
    }
}
