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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.List;

import com.asakusafw.compiler.flow.processor.CoGroupFlowProcessor;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.vocabulary.flow.processor.InputBuffer;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.operator.CoGroup;

/**
 * An operator class for testing {@link CoGroupFlowProcessor}.
 */
public abstract class CoGroupFlow {

    /**
     * returns total of values.
     * @param a1 group
     * @param r1 sink
     */
    @CoGroup
    public void op1(
            @Key(group = "string") List<Ex1> a1,
            Result<Ex1> r1) {
        withParameter(a1, r1, 0);
    }


    /**
     * returns total of values w/ using backing store.
     * @param a1 group
     * @param r1 sink
     */
    @CoGroup(inputBuffer = InputBuffer.ESCAPE)
    public void swap(
            @Key(group = "string") List<Ex1> a1,
            Result<Ex1> r1) {
        withParameter(a1, r1, 0);
    }
    /**
     * returns total of values w/ ordered.
     * @param a1 group
     * @param r1 sink
     */
    @CoGroup
    public void sorted(
            @Key(group = "string", order = "value desc") List<Ex1> a1,
            Result<Ex1> r1) {
        int current = a1.get(0).getValue();
        for (Ex1 e : a1) {
            assertThat(current, lessThanOrEqualTo(e.getValue()));
            current = e.getValue();
        }
    }

    /**
     * a1 to r2, a2 to r1.
     * @param a1 group 1
     * @param a2 group 2
     * @param r1 result 1
     * @param r2 result 2
     */
    @CoGroup
    public void op2(
            @Key(group = "string") List<Ex1> a1,
            @Key(group = "string") List<Ex2> a2,
            Result<Ex1> r1,
            Result<Ex2> r2) {
        String string = "";
        int value1 = 0;
        int value2 = 0;
        for (Ex1 e : a1) {
            value1 += e.getValue();
            string = e.getStringAsString();
        }
        for (Ex2 e : a2) {
            value2 += e.getValue();
            string = e.getStringAsString();
        }
        Ex1 re1 = new Ex1();
        Ex2 re2 = new Ex2();
        re1.setValue(value2); // exchange
        re2.setValue(value1);
        re1.setStringAsString(string);
        re2.setStringAsString(string);
        r1.add(re1);
        r2.add(re2);
    }

    /**
     * a1 to r2, a2 to r3, a3 to r1.
     * @param a1 group 1
     * @param a2 group 2
     * @param a3 group 3
     * @param r1 result 1
     * @param r2 result 2
     * @param r3 result 3
     */
    @CoGroup
    public void op3(
            @Key(group = "string") List<Ex1> a1,
            @Key(group = "string") List<Ex1> a2,
            @Key(group = "string") List<Ex1> a3,
            Result<Ex1> r1,
            Result<Ex1> r2,
            Result<Ex1> r3) {
        withParameter(a1, r2, 0);
        withParameter(a2, r3, 0);
        withParameter(a3, r1, 0);
    }

    /**
     * sum of value + parameter * size of a1.
     * @param a1 group
     * @param r1 result
     * @param parameter additional parameter
     */
    @CoGroup
    public void withParameter(
            @Key(group = "string") List<Ex1> a1,
            Result<Ex1> r1,
            int parameter) {
        String string = "";
        int value = 0;
        for (Ex1 e : a1) {
            value += e.getValue() + parameter;
            string = e.getStringAsString();
        }
        Ex1 re = new Ex1();
        re.setValue(value);
        re.setStringAsString(string);
        r1.add(re);
    }
}
