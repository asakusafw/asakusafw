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
package com.asakusafw.compiler.flow.processor.operator;
import javax.annotation.Generated;

import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
/**
 * {@link MasterCheckFlow}に関する演算子実装クラス。
 */
@Generated("OperatorImplementationClassGenerator:0.0.1") public class MasterCheckFlowImpl extends MasterCheckFlow {
    /**
     * インスタンスを生成する。
     */
    public MasterCheckFlowImpl() {
        return;
    }
    @Override public boolean simple(Ex2 master, Ex1 model) {
        throw new UnsupportedOperationException("マスタ確認演算子は組み込みの方法で処理されます");
    }
    @Override public boolean selection(Ex2 master, Ex1 model) {
        throw new UnsupportedOperationException("マスタ確認演算子は組み込みの方法で処理されます");
    }
}