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
package com.asakusafw.compiler.fileio.flow;

import com.asakusafw.compiler.fileio.external.Ex1MockImporterDescription;
import com.asakusafw.compiler.fileio.model.Ex1;
import com.asakusafw.compiler.fileio.operator.ExOperatorFactory;
import com.asakusafw.compiler.fileio.operator.ExOperatorFactory.Update;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;

/**
 * 単一の出力のみを行うジョブ。
 */
@JobFlow(name = "job")
public class SingleOutputJob extends FlowDescription {

    private In<Ex1> input;

    private Out<Ex1> output1;

    /**
     * インスタンスを生成する。
     * @param input 入力
     * @param output1 出力(1)
     */
    public SingleOutputJob(
            @Import(name = "input", description = Ex1MockImporterDescription.class)
            In<Ex1> input,
            @Export(name = "out1", description = Out1ExporterDesc.class)
            Out<Ex1> output1) {
        this.input = input;
        this.output1 = output1;
    }

    @Override
    protected void describe() {
        ExOperatorFactory op = new ExOperatorFactory();
        Update result = op.update(input, 100);
        output1.add(result.out);
    }
}
