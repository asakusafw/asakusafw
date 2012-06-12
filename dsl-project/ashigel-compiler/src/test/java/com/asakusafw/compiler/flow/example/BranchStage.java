/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory;
import com.asakusafw.compiler.flow.testing.operator.ExOperatorFactory.Branch;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.FlowPart;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory;


/**
 * Volatileのテスト。
 */
@FlowPart
public class BranchStage extends FlowDescription {

    private In<Ex1> in;

    private Out<Ex1> out1;

    private Out<Ex1> out2;

    private Out<Ex1> out3;

    /**
     * インスタンスを生成する。
     * @param in 入力
     * @param out1 出力
     * @param out2 出力
     * @param out3 出力
     */
    public BranchStage(In<Ex1> in, Out<Ex1> out1, Out<Ex1> out2, Out<Ex1> out3) {
        this.in = in;
        this.out1 = out1;
        this.out2 = out2;
        this.out3 = out3;
    }

    @Override
    protected void describe() {
        ExOperatorFactory f = new ExOperatorFactory();
        CoreOperatorFactory core = new CoreOperatorFactory();
        Branch branch = f.branch(in);
        out1.add(branch.yes);
        out2.add(branch.yes);
        out2.add(branch.no);
        out3.add(branch.no);
        core.stop(branch.cancel);
    }
}
