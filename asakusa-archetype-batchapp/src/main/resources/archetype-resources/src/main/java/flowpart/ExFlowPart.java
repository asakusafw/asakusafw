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
package ${package}.flowpart;

import ${package}.modelgen.table.model.Ex1;
import ${package}.modelgen.table.model.Ex2;
import ${package}.operator.ExOperatorFactory;
import ${package}.operator.ExOperatorFactory.Cogroup;
import ${package}.operator.ExOperatorFactory.Update;
import com.asakusafw.vocabulary.flow.FlowPart;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory;

/**
 * 単純なフロー部品。
 */
@FlowPart
public class ExFlowPart extends FlowDescription {

    private In<Ex1> in1;

    private Out<Ex1> out1;

    /**
     * フロー部品の入出力を規定するコンストラクタ。
     * @param in1 フロー部品への入力
     * @param out1 フロー部品からの出力
     */
    public ExFlowPart(In<Ex1> in1, Out<Ex1> out1) {
        this.in1 = in1;
        this.out1 = out1;
    }

    /**
     * フロー構造。
     */
    @Override
    protected void describe() {
        ExOperatorFactory f = new ExOperatorFactory();
        CoreOperatorFactory core = new CoreOperatorFactory();
        Update update = f.update(in1, 10);
        Cogroup cog = f.cogroup(update.out, core.empty(Ex2.class));
        out1.add(cog.r1);
        core.stop(cog.r2);
    }
}
