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
package com.asakusafw.compiler.flow.processor.operator;

import com.asakusafw.compiler.flow.processor.ExtractFlowProcessor;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.vocabulary.operator.Extract;

/**
 * {@link ExtractFlowProcessor}に対するテスト演算子。
 */
public abstract class ExtractFlow {

    /**
     * valueを加算する。
     * @param a1 入力
     * @param r1 value + 1
     */
    @Extract
    public void op1(
            Ex1 a1,
            Result<Ex1> r1) {
        withParameterEx1(a1, r1, 1);
    }

    /**
     * valueを加算する。
     * @param a1 入力
     * @param r1 value + 1
     * @param r2 value + 2
     */
    @Extract
    public void op2(
            Ex1 a1,
            Result<Ex1> r1,
            Result<Ex2> r2) {
        withParameterEx1(a1, r1, 1);
        withParameter(a1, r2, 2);
    }

    /**
     * valueを加算する。
     * @param a1 入力
     * @param r1 value + 1
     * @param r2 value + 2
     * @param r3 value + 3
     */
    @Extract
    public void op3(
            Ex1 a1,
            Result<Ex1> r1,
            Result<Ex2> r2,
            Result<Ex1> r3) {
        withParameterEx1(a1, r1, 1);
        withParameter(a1, r2, 2);
        withParameterEx1(a1, r3, 3);
    }

    /**
     * valueを加算する。
     * @param a1 入力
     * @param r1 value + param
     * @param parameter パラメーター
     */
    @Extract
    public void withParameter(
            Ex1 a1,
            Result<Ex2> r1,
            int parameter) {
        Ex2 copy = new Ex2();
        copy.setSidOption(a1.getSidOption());
        copy.setStringOption(a1.getStringOption());
        copy.setValue(a1.getValue() + parameter);
        r1.add(copy);
    }

    private void withParameterEx1(
            Ex1 a1,
            Result<Ex1> r1,
            int parameter) {
        Ex1 copy = new Ex1();
        copy.setSidOption(a1.getSidOption());
        copy.setStringOption(a1.getStringOption());
        copy.setValue(a1.getValue() + parameter);
        r1.add(copy);
    }
}
