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
package ${package}.jobflow;

import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;
import ${package}.flowpart.ExFlowPartFactory;
import ${package}.flowpart.ExFlowPartFactory.ExFlowPart;
import ${package}.modelgen.table.model.Ex1;

/**
 * サンプル：ジョブフロークラス
 */
@JobFlow(name = "ex")
public class ExJobFlow extends FlowDescription {

    private In<Ex1> in1;
    private Out<Ex1> out1;

    /**
     * コンストラクタ
     * 
     * @param in1 ex1からの入力を示すインターフェース
     * @param out1 ex1への出力を示すインターフェース
     */            
    public ExJobFlow(
            @Import(name = "ex1", description = Ex1FromDb.class) In<Ex1> in1,
            @Export(name = "ex1", description = Ex1ToDb.class) Out<Ex1> out1) {
        this.in1 = in1;
        this.out1 = out1;
    }

    @Override
    public void describe() {
        ExFlowPartFactory ex = new ExFlowPartFactory();
        ExFlowPart comp = ex.create(in1);
        out1.add(comp.out1);
    }
}
