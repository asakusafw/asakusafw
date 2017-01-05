/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
 * An operator class for testing {@link ExtractFlowProcessor}.
 */
public abstract class ExtractFlow {

    /**
     * value of input + 1.
     * @param a1 input
     * @param r1 result
     */
    @Extract
    public void op1(
            Ex1 a1,
            Result<Ex1> r1) {
        withParameterEx1(a1, r1, 1);
    }

    /**
     * value of input + 1 to r1, value of input + 2 to r2.
     * @param a1 input
     * @param r1 result 1
     * @param r2 result 2
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
     * value of input + N to rN.
     * @param a1 input
     * @param r1 result 1
     * @param r2 result 2
     * @param r3 result 3
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
     * value of input + parameter.
     * @param a1 input
     * @param r1 result
     * @param parameter additional parameter
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
