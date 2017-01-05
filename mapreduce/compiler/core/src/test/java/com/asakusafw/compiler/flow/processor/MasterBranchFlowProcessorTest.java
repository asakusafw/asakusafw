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
package com.asakusafw.compiler.flow.processor;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.compiler.flow.processor.flow.MasterBranchFlowSelection;
import com.asakusafw.compiler.flow.processor.flow.MasterBranchFlowSelectionWithParameter0;
import com.asakusafw.compiler.flow.processor.flow.MasterBranchFlowSelectionWithParameter1;
import com.asakusafw.compiler.flow.processor.flow.MasterBranchFlowSimple;
import com.asakusafw.compiler.flow.processor.flow.MasterBranchFlowWithParameter;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.flow.testing.model.Ex2;
import com.asakusafw.compiler.util.tester.CompilerTester;
import com.asakusafw.compiler.util.tester.CompilerTester.TestInput;
import com.asakusafw.compiler.util.tester.CompilerTester.TestOutput;
import com.asakusafw.runtime.windows.WindowsSupport;
import com.asakusafw.vocabulary.external.ImporterDescription.DataSize;

/**
 * Test for {@link MasterBranchFlowProcessor}.
 */
public class MasterBranchFlowProcessorTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    /**
     * A test helper
     */
    @Rule
    public CompilerTester tester = new CompilerTester();

    /**
     * simple case.
     * @throws Exception if exception was occurred
     */
    @Test
    public void simple() throws Exception {
        runEq(DataSize.UNKNOWN);
    }

    /**
     * test for TINY.
     * @throws Exception if exception was occurred
     */
    @Test
    public void tiny() throws Exception {
        runEq(DataSize.TINY);
    }

    private void runEq(DataSize dataSize) throws IOException {
        TestInput<Ex1> in1 = tester.input(Ex1.class, "ex1");
        TestInput<Ex2> in2 = tester.input(Ex2.class, "ex2", dataSize);
        TestOutput<Ex1> high = tester.output(Ex1.class, "high");
        TestOutput<Ex1> low = tester.output(Ex1.class, "low");
        TestOutput<Ex1> stop = tester.output(Ex1.class, "stop");

        Ex1 ex1 = new Ex1();
        Ex2 ex2 = new Ex2();

        // high
        ex1.setStringAsString("high");
        ex1.setValue(11);
        ex2.setStringAsString("high");
        ex2.setValue(20);
        in1.add(ex1);
        in2.add(ex2);

        // low
        ex1.setStringAsString("low");
        ex1.setValue(50);
        ex2.setStringAsString("low");
        ex2.setValue(-20);
        in1.add(ex1);
        in2.add(ex2);

        // stop
        ex1.setStringAsString("0-stop");
        ex1.setValue(10);
        ex2.setStringAsString("0-stop");
        ex2.setValue(-10);
        in1.add(ex1);
        in2.add(ex2);

        // missing master
        ex1.setStringAsString("1-missing");
        ex1.setValue(1000);
        ex2.setStringAsString("2-missing");
        ex2.setValue(-1);
        in1.add(ex1);
        in2.add(ex2);

        assertThat(tester.runFlow(new MasterBranchFlowSimple(
                in1.flow(), in2.flow(),
                high.flow(), low.flow(), stop.flow())), is(true));

        List<Ex1> highList = high.toList();
        List<Ex1> lowList = low.toList();
        List<Ex1> stopList = stop.toList((o1, o2) -> o1.getStringOption().compareTo(o2.getStringOption()));
        assertThat(highList.size(), is(1));
        assertThat(lowList.size(), is(1));
        assertThat(stopList.size(), is(2));

        assertThat(highList.get(0).getStringAsString(), is("high"));
        assertThat(lowList.get(0).getStringAsString(), is("low"));
        assertThat(stopList.get(0).getStringAsString(), is("0-stop"));
        assertThat(stopList.get(1).getStringAsString(), is("1-missing"));
    }

    /**
     * parameterized.
     * @throws Exception if exception was occurred
     */
    @Test
    public void withParameter() throws Exception {
        TestInput<Ex1> in1 = tester.input(Ex1.class, "ex1");
        TestInput<Ex2> in2 = tester.input(Ex2.class, "ex2");
        TestOutput<Ex1> high = tester.output(Ex1.class, "high");
        TestOutput<Ex1> low = tester.output(Ex1.class, "low");
        TestOutput<Ex1> stop = tester.output(Ex1.class, "stop");

        Ex1 ex1 = new Ex1();
        Ex2 ex2 = new Ex2();

        // high
        ex1.setStringAsString("high");
        ex1.setValue(31);
        ex2.setStringAsString("high");
        ex2.setValue(20);
        in1.add(ex1);
        in2.add(ex2);

        // low
        ex1.setStringAsString("low");
        ex1.setValue(70);
        ex2.setStringAsString("low");
        ex2.setValue(-20);
        in1.add(ex1);
        in2.add(ex2);

        // stop
        ex1.setStringAsString("0-stop");
        ex1.setValue(10);
        ex2.setStringAsString("0-stop");
        ex2.setValue(-10);
        in1.add(ex1);
        in2.add(ex2);

        // missing master
        ex1.setStringAsString("1-missing");
        ex1.setValue(1000);
        in1.add(ex1);

        assertThat(tester.runFlow(new MasterBranchFlowWithParameter(
                in1.flow(), in2.flow(),
                high.flow(), low.flow(), stop.flow())), is(true));

        List<Ex1> highList = high.toList();
        List<Ex1> lowList = low.toList();
        List<Ex1> stopList = stop.toList((o1, o2) -> o1.getStringOption().compareTo(o2.getStringOption()));
        assertThat(highList.size(), is(1));
        assertThat(lowList.size(), is(1));
        assertThat(stopList.size(), is(2));

        assertThat(highList.get(0).getStringAsString(), is("high"));
        assertThat(lowList.get(0).getStringAsString(), is("low"));
        assertThat(stopList.get(0).getStringAsString(), is("0-stop"));
        assertThat(stopList.get(1).getStringAsString(), is("1-missing"));
    }

    /**
     * w/ selector.
     * @throws Exception if exception was occurred
     */
    @Test
    public void selection() throws Exception {
        runNoEq(DataSize.UNKNOWN);
    }

    /**
     * w/ TINY data and selector.
     * @throws Exception if exception was occurred
     */
    @Test
    public void tinySelection() throws Exception {
        runNoEq(DataSize.TINY);
    }

    private void runNoEq(DataSize dataSize) throws IOException {
        TestInput<Ex1> in1 = tester.input(Ex1.class, "ex1");
        TestInput<Ex2> in2 = tester.input(Ex2.class, "ex2", dataSize);
        TestOutput<Ex1> high = tester.output(Ex1.class, "high");
        TestOutput<Ex1> low = tester.output(Ex1.class, "low");
        TestOutput<Ex1> stop = tester.output(Ex1.class, "stop");

        Ex1 ex1 = new Ex1();
        Ex2 ex2 = new Ex2();

        // high
        ex1.setStringAsString("high");
        ex1.setValue(20);
        in1.add(ex1);
        ex2.setStringAsString("high");
        ex2.setValue(0);
        in2.add(ex2);
        ex2.setValue(10);
        in2.add(ex2);
        ex2.setValue(20);
        in2.add(ex2);

        // low
        ex1.setStringAsString("low");
        ex1.setValue(10);
        in1.add(ex1);
        ex2.setStringAsString("low");
        ex2.setValue(-20);
        in2.add(ex2);
        ex2.setValue(10);
        in2.add(ex2);
        ex2.setValue(+40);
        in2.add(ex2);

        // stop
        ex1.setStringAsString("0-stop");
        ex1.setValue(0);
        in1.add(ex1);
        ex2.setStringAsString("0-stop");
        ex2.setValue(-10);
        in2.add(ex2);
        ex2.setValue(0);
        in2.add(ex2);
        ex2.setValue(+10);
        in2.add(ex2);

        // missing master
        ex1.setStringAsString("1-missing");
        ex1.setValue(1);
        in1.add(ex1);
        ex2.setStringAsString("1-missing");
        ex2.setValue(-1);
        in2.add(ex2);
        ex2.setValue(0);
        in2.add(ex2);
        ex2.setValue(2);
        in2.add(ex2);
        ex2.setValue(10);
        in2.add(ex2);

        assertThat(tester.runFlow(new MasterBranchFlowSelection(
                in1.flow(), in2.flow(),
                high.flow(), low.flow(), stop.flow())), is(true));

        List<Ex1> highList = high.toList();
        List<Ex1> lowList = low.toList();
        List<Ex1> stopList = stop.toList((o1, o2) -> o1.getStringOption().compareTo(o2.getStringOption()));
        assertThat(highList.size(), is(1));
        assertThat(lowList.size(), is(1));
        assertThat(stopList.size(), is(2));

        assertThat(highList.get(0).getStringAsString(), is("high"));
        assertThat(lowList.get(0).getStringAsString(), is("low"));
        assertThat(stopList.get(0).getStringAsString(), is("0-stop"));
        assertThat(stopList.get(1).getStringAsString(), is("1-missing"));
    }

    /**
     * w/ selector and user parameter (selector has no user parameters).
     * @throws Exception if exception was occurred
     */
    @Test
    public void selectionWithParameter0() throws Exception {
        runNoEqWithParameter0(DataSize.UNKNOWN);
    }

    /**
     * w/ TINY data, and selector and user parameter (selector has no user parameters).
     * @throws Exception if exception was occurred
     */
    @Test
    public void tinySelectionWithParameter0() throws Exception {
        runNoEqWithParameter0(DataSize.TINY);
    }

    private void runNoEqWithParameter0(DataSize dataSize) throws IOException {
        TestInput<Ex1> in1 = tester.input(Ex1.class, "ex1");
        TestInput<Ex2> in2 = tester.input(Ex2.class, "ex2", dataSize);
        TestOutput<Ex1> high = tester.output(Ex1.class, "high");
        TestOutput<Ex1> low = tester.output(Ex1.class, "low");
        TestOutput<Ex1> stop = tester.output(Ex1.class, "stop");

        Ex1 ex1 = new Ex1();
        Ex2 ex2 = new Ex2();

        // high
        ex1.setStringAsString("high");
        ex1.setValue(30);
        in1.add(ex1);
        ex2.setStringAsString("high");
        ex2.setValue(0);
        in2.add(ex2);
        ex2.setValue(15);
        in2.add(ex2);
        ex2.setValue(30);
        in2.add(ex2);

        // low
        ex1.setStringAsString("low");
        ex1.setValue(20);
        in1.add(ex1);
        ex2.setStringAsString("low");
        ex2.setValue(-20);
        in2.add(ex2);
        ex2.setValue(20);
        in2.add(ex2);
        ex2.setValue(+60);
        in2.add(ex2);

        // stop
        ex1.setStringAsString("0-stop");
        ex1.setValue(0);
        in1.add(ex1);
        ex2.setStringAsString("0-stop");
        ex2.setValue(-10);
        in2.add(ex2);
        ex2.setValue(0);
        in2.add(ex2);
        ex2.setValue(+10);
        in2.add(ex2);

        // missing master
        ex1.setStringAsString("1-missing");
        ex1.setValue(1);
        in1.add(ex1);
        ex2.setStringAsString("1-missing");
        ex2.setValue(-1);
        in2.add(ex2);
        ex2.setValue(0);
        in2.add(ex2);
        ex2.setValue(2);
        in2.add(ex2);
        ex2.setValue(10);
        in2.add(ex2);

        assertThat(tester.runFlow(new MasterBranchFlowSelectionWithParameter0(
                in1.flow(), in2.flow(),
                high.flow(), low.flow(), stop.flow())), is(true));

        List<Ex1> highList = high.toList();
        List<Ex1> lowList = low.toList();
        List<Ex1> stopList = stop.toList((o1, o2) -> o1.getStringOption().compareTo(o2.getStringOption()));
        assertThat(highList.size(), is(1));
        assertThat(lowList.size(), is(1));
        assertThat(stopList.size(), is(2));

        assertThat(highList.get(0).getStringAsString(), is("high"));
        assertThat(lowList.get(0).getStringAsString(), is("low"));
        assertThat(stopList.get(0).getStringAsString(), is("0-stop"));
        assertThat(stopList.get(1).getStringAsString(), is("1-missing"));
    }

    /**
     * w/ selector and user parameter (selector also has user parameters).
     * @throws Exception if exception was occurred
     */
    @Test
    public void selectionWithParameter1() throws Exception {
        runNoEqWithParameter1(DataSize.UNKNOWN);
    }

    /**
     * w/ TINY input, and selector and user parameter (selector also has user parameters).
     * @throws Exception if exception was occurred
     */
    @Test
    public void tinySelectionWithParameter1() throws Exception {
        runNoEqWithParameter1(DataSize.TINY);
    }

    private void runNoEqWithParameter1(DataSize dataSize) throws IOException {
        TestInput<Ex1> in1 = tester.input(Ex1.class, "ex1");
        TestInput<Ex2> in2 = tester.input(Ex2.class, "ex2", dataSize);
        TestOutput<Ex1> high = tester.output(Ex1.class, "high");
        TestOutput<Ex1> low = tester.output(Ex1.class, "low");
        TestOutput<Ex1> stop = tester.output(Ex1.class, "stop");

        Ex1 ex1 = new Ex1();
        Ex2 ex2 = new Ex2();

        // high
        ex1.setStringAsString("high");
        ex1.setValue(1);
        in1.add(ex1);
        ex2.setStringAsString("high");
        ex2.setValue(0);
        in2.add(ex2);
        ex2.setValue(25);
        in2.add(ex2);
        ex2.setValue(50);
        in2.add(ex2);

        // low
        ex1.setStringAsString("low");
        ex1.setValue(0);
        in1.add(ex1);
        ex2.setStringAsString("low");
        ex2.setValue(-20);
        in2.add(ex2);
        ex2.setValue(50);
        in2.add(ex2);
        ex2.setValue(100);
        in2.add(ex2);

        // stop
        ex1.setStringAsString("0-stop");
        ex1.setValue(-50);
        in1.add(ex1);
        ex2.setStringAsString("0-stop");
        ex2.setValue(-50);
        in2.add(ex2);
        ex2.setValue(0);
        in2.add(ex2);
        ex2.setValue(+50);
        in2.add(ex2);

        // missing master
        ex1.setStringAsString("1-missing");
        ex1.setValue(1);
        in1.add(ex1);
        ex2.setStringAsString("1-missing");
        ex2.setValue(-1);
        in2.add(ex2);
        ex2.setValue(1);
        in2.add(ex2);
        ex2.setValue(2);
        in2.add(ex2);
        ex2.setValue(0);
        in2.add(ex2);

        assertThat(tester.runFlow(new MasterBranchFlowSelectionWithParameter1(
                in1.flow(), in2.flow(),
                high.flow(), low.flow(), stop.flow())), is(true));

        List<Ex1> highList = high.toList();
        List<Ex1> lowList = low.toList();
        List<Ex1> stopList = stop.toList((o1, o2) -> o1.getStringOption().compareTo(o2.getStringOption()));
        assertThat(highList.size(), is(1));
        assertThat(lowList.size(), is(1));
        assertThat(stopList.size(), is(2));

        assertThat(highList.get(0).getStringAsString(), is("high"));
        assertThat(lowList.get(0).getStringAsString(), is("low"));
        assertThat(stopList.get(0).getStringAsString(), is("0-stop"));
        assertThat(stopList.get(1).getStringAsString(), is("1-missing"));
    }
}
