/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.compiler.testing.flow;

import com.asakusafw.compiler.flow.processor.operator.LoggingFlowFactory;
import com.asakusafw.compiler.flow.processor.operator.UpdateFlowFactory;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory;


/**
 * Fragmentが直列に並ぶようなパターン。
 */
public class DuplicateFragments extends FlowDescription {

    private In<Ex1> in1;

    private Out<Ex1> out1;

    /**
     * インスタンスを生成する。
     * @param in1 入力
     * @param out1 出力
     */
    public DuplicateFragments(In<Ex1> in1, Out<Ex1> out1) {
        this.in1 = in1;
        this.out1 = out1;
    }

    @Override
    protected void describe() {
        CoreOperatorFactory core = new CoreOperatorFactory();
        UpdateFlowFactory updates = new UpdateFlowFactory();
        LoggingFlowFactory loggings = new LoggingFlowFactory();
        UpdateFlowFactory.Simple update = updates.simple(in1);
        LoggingFlowFactory.Simple logging = loggings.simple(update.out);
        UpdateFlowFactory.Simple copy1 = updates.simple(update.out);
        UpdateFlowFactory.Simple copy2 = updates.simple(update.out);
        out1.add(copy1.out);
        out1.add(copy2.out);
        core.stop(logging.out);
    }
}
