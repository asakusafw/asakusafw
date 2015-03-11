/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import com.asakusafw.compiler.flow.processor.MasterJoinFlowProcessor;
import com.asakusafw.compiler.flow.processor.operator.MasterJoinFlowFactory;
import com.asakusafw.compiler.flow.processor.operator.MasterJoinFlowFactory.Join;
import com.asakusafw.compiler.flow.testing.external.Ex1MockImporterDescription;
import com.asakusafw.compiler.flow.testing.external.Ex2MockExporterDescription;
import com.asakusafw.compiler.flow.testing.external.Ex2MockImporterDescription;
import com.asakusafw.compiler.flow.testing.external.ExJoinedMockExporterDescription;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.flow.testing.model.ExJoined;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;


/**
 * {@link MasterJoinFlowProcessor}のテスト。
 */
@JobFlow(name = "testing")
public class MasterJoinFlowTrivial extends FlowDescription {

    private In<Ex1> in1;

    private In<Ex2> in2;

    private Out<ExJoined> out1;

    private Out<Ex2> out2;

    /**
     * インスタンスを生成する。
     * @param in1 入力1
     * @param in2 入力2
     * @param out1 出力1
     * @param out2 出力2
     */
    public MasterJoinFlowTrivial(
            @Import(name = "e1", description = Ex1MockImporterDescription.class)
            In<Ex1> in1,
            @Import(name = "e2", description = Ex2MockImporterDescription.class)
            In<Ex2> in2,
            @Export(name = "ej", description = ExJoinedMockExporterDescription.class)
            Out<ExJoined> out1,
            @Export(name = "e2", description = Ex2MockExporterDescription.class)
            Out<Ex2> out2) {
        this.in1 = in1;
        this.in2 = in2;
        this.out1 = out1;
        this.out2 = out2;
    }

    @Override
    protected void describe() {
        MasterJoinFlowFactory f = new MasterJoinFlowFactory();
        Join join = f.join(in1, in2);
        out1.add(join.joined);
        out2.add(join.missed);
    }
}
