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
package com.asakusafw.compiler.flow;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.compiler.flow.example.AbstractJobFlow;
import com.asakusafw.compiler.flow.example.DescribeFailJobFlow;
import com.asakusafw.compiler.flow.example.InconsistentExportOutput;
import com.asakusafw.compiler.flow.example.InconsistentImportInput;
import com.asakusafw.compiler.flow.example.InstantiateFailJobFlow;
import com.asakusafw.compiler.flow.example.InvalidParameter;
import com.asakusafw.compiler.flow.example.MultiPublicConstructor;
import com.asakusafw.compiler.flow.example.NoExportOutput;
import com.asakusafw.compiler.flow.example.NoImportInput;
import com.asakusafw.compiler.flow.example.NoPublicConstructors;
import com.asakusafw.compiler.flow.example.NotAnnotatedJobFlow;
import com.asakusafw.compiler.flow.example.NotTypedInput;
import com.asakusafw.compiler.flow.example.NotTypedOutput;
import com.asakusafw.compiler.flow.example.SimpleJobFlow;
import com.asakusafw.compiler.flow.example.TopLevelJobFlow;
import com.asakusafw.compiler.flow.example.WithAbstractExportOutput;
import com.asakusafw.compiler.flow.example.WithAbstractImportInput;
import com.asakusafw.compiler.flow.example.WithEmptyInputName;
import com.asakusafw.compiler.flow.example.WithEmptyOutputName;
import com.asakusafw.compiler.flow.example.WithExportInput;
import com.asakusafw.compiler.flow.example.WithImportOutput;
import com.asakusafw.compiler.flow.example.WithInvalidInputName;
import com.asakusafw.compiler.flow.example.WithInvalidOutputName;
import com.asakusafw.compiler.flow.example.WithMissTypedInput;
import com.asakusafw.compiler.flow.example.WithMissTypedOutput;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowIn;
import com.asakusafw.vocabulary.flow.graph.FlowOut;

/**
 * Test for {@link JobFlowDriver}.
 */
public class JobFlowDriverTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(SimpleJobFlow.class);
        assertThat(analyzed.hasError(), is(false));
        JobFlowClass jf = analyzed.getJobFlowClass();

        FlowGraph graph = jf.getGraph();
        assertThat(graph.getDescription(), is((Object) SimpleJobFlow.class));

        assertThat(graph.getFlowInputs().size(), is(1));
        assertThat(graph.getFlowOutputs().size(), is(1));

        FlowIn<?> in = graph.getFlowInputs().get(0);
        assertThat(in.getDescription().getName(), is("hoge"));

        FlowOut<?> out = graph.getFlowOutputs().get(0);
        assertThat(out.getDescription().getName(), is("hoge"));

        assertThat(in.toOutputPort().getConnected(), is(out.toInputPort().getConnected()));
    }

    /**
     * not top-level class.
     */
    @Test
    public void Class_NotTopLevel() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(TopLevelJobFlow.Inner.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * not public class.
     */
    @Test
    public void Class_NotPublic() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(NotPublicJobFlow.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * abstract class.
     */
    @Test
    public void Class_Abstract() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(AbstractJobFlow.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * class w/o annotations.
     */
    @Test
    public void Class_NotAnnotated() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(NotAnnotatedJobFlow.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * class w/o public constructors.
     */
    @Test
    public void Constructor_None() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(NoPublicConstructors.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * class w/ extra public constructors.
     */
    @Test
    public void Constructor_Multi() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(MultiPublicConstructor.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * constructor w/ extra parameters.
     */
    @Test
    public void Constructor_InvalidParameter() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(InvalidParameter.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * raw input.
     */
    @Test
    public void Input_NotTyped() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(NotTypedInput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * input w/ invalid annotation.
     */
    @Test
    public void Input_WithInvalidAnnotation() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(WithExportInput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * empty name for input.
     */
    @Test
    public void Input_WithEmptyInputName() {
        JobFlowDriver driver = JobFlowDriver.analyze(WithEmptyInputName.class);
        assertThat(driver.hasError(), is(true));
        assertThat(driver.getDiagnostics().size(), greaterThan(0));
    }

    /**
     * input w/ invalid name.
     */
    @Test
    public void Input_WithInvalidInputName() {
        JobFlowDriver driver = JobFlowDriver.analyze(WithInvalidInputName.class);
        assertThat(driver.hasError(), is(true));
        assertThat(driver.getDiagnostics().size(), greaterThan(0));
    }

    /**
     * input w/o annotation.
     */
    @Test
    public void Input_WithoudMandatoryAnnotation() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(NoImportInput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * input w/ export annotation.
     */
    @Test
    public void Input_InconsistentAnnotation() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(InconsistentImportInput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * input w/ importer which cannot be instantiated.
     */
    @Test
    public void Input_AbstractDescription() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(WithAbstractImportInput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * input w/ importer which cannot provide its data type.
     */
    @Test
    public void Input_InvalidTypeDescription() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(WithMissTypedInput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * output w/ data type info.
     */
    @Test
    public void Output_NotTyped() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(NotTypedOutput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * output w/ invalid annotation.
     */
    @Test
    public void Output_WithInvalidAnnotation() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(WithImportOutput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * empty name for output.
     */
    @Test
    public void Output_WithEmptyOutputName() {
        JobFlowDriver driver = JobFlowDriver.analyze(WithEmptyOutputName.class);
        assertThat(driver.hasError(), is(true));
        assertThat(driver.getDiagnostics().size(), greaterThan(0));
    }

    /**
     * output w/ invalid name.
     */
    @Test
    public void Output_WithInvalidOutputName() {
        JobFlowDriver driver = JobFlowDriver.analyze(WithInvalidOutputName.class);
        assertThat(driver.hasError(), is(true));
        assertThat(driver.getDiagnostics().size(), greaterThan(0));
    }

    /**
     * output w/o annotation.
     */
    @Test
    public void Output_WithoudMandatoryAnnotation() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(NoExportOutput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * output w/ import annotation.
     */
    @Test
    public void Output_InconsistentAnnotation() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(InconsistentExportOutput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * output w/ exporter which cannot be instantiated.
     */
    @Test
    public void Output_InvalidDescription() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(WithAbstractExportOutput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * output w/ exporter which cannot provide its data type.
     */
    @Test
    public void Output_InvalidTypeDescription() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(WithMissTypedOutput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * constructor raises an exception.
     */
    @Test
    public void InstantiateFailure() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(InstantiateFailJobFlow.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * describe method raises an exception.
     */
    @Test
    public void DescribeFailure() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(DescribeFailJobFlow.class);
        assertThat(analyzed.hasError(), is(true));
    }
}
