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
package com.asakusafw.compiler.flow.processor.operator;

import javax.annotation.Generated;

import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.flow.testing.model.ExJoined;
import com.asakusafw.runtime.core.Result;

/**
 * {@link MasterJoinFlow}に関する演算子実装クラス。
 */
@Generated("OperatorImplementationClassGenerator:0.0.1")
public class MasterJoinFlowImpl extends MasterJoinFlow {
    /**
     * インスタンスを生成する。
     */
    public MasterJoinFlowImpl() {
        return;
    }

    @Override
    public void split(ExJoined joined, Result<Ex1> ex1, Result<Ex2> ex2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExJoined join(Ex1 ex1, Ex2 ex2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExJoined selection(Ex1 ex1, Ex2 ex2) {
        throw new UnsupportedOperationException();
    }
}