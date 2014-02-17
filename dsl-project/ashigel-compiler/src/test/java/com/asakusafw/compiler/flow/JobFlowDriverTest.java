/**
 * Copyright 2011-2014 Asakusa Framework Team.
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

import com.asakusafw.compiler.flow.example.*;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowIn;
import com.asakusafw.vocabulary.flow.graph.FlowOut;

/**
 * Test for {@link JobFlowDriver}.
 */
public class JobFlowDriverTest {

    /**
     * 単純な例。
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
     * トップレベルでないクラス。
     */
    @Test
    public void Class_NotTopLevel() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(TopLevelJobFlow.Inner.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * publicでないクラス。
     */
    @Test
    public void Class_NotPublic() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(NotPublicJobFlow.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * 抽象クラス。
     */
    @Test
    public void Class_Abstract() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(AbstractJobFlow.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * 注釈がない。
     */
    @Test
    public void Class_NotAnnotated() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(NotAnnotatedJobFlow.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * 公開コンストラクタがない。
     */
    @Test
    public void Constructor_None() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(NoPublicConstructors.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * 公開コンストラクタが多い。
     */
    @Test
    public void Constructor_Multi() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(MultiPublicConstructor.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * 入出力に関連しない余計なパラメーター。
     */
    @Test
    public void Constructor_InvalidParameter() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(InvalidParameter.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * 型が指定されていない入力。
     */
    @Test
    public void Input_NotTyped() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(NotTypedInput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * 不正な注釈を持つ入力。
     */
    @Test
    public void Input_WithInvalidAnnotation() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(WithExportInput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * Empty name for input.
     */
    @Test
    public void Input_WithEmptyInputName() {
        JobFlowDriver driver = JobFlowDriver.analyze(WithEmptyInputName.class);
        assertThat(driver.hasError(), is(true));
        assertThat(driver.getDiagnostics().size(), greaterThan(0));
    }

    /**
     * 入力ポート名が不正。
     */
    @Test
    public void Input_WithInvalidInputName() {
        JobFlowDriver driver = JobFlowDriver.analyze(WithInvalidInputName.class);
        assertThat(driver.hasError(), is(true));
        assertThat(driver.getDiagnostics().size(), greaterThan(0));
    }

    /**
     * 必要な注釈がない入力。
     */
    @Test
    public void Input_WithoudMandatoryAnnotation() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(NoImportInput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * 注釈の型が不一致。
     */
    @Test
    public void Input_InconsistentAnnotation() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(InconsistentImportInput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * インスタンス化できないインポーター記述。
     */
    @Test
    public void Input_AbstractDescription() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(WithAbstractImportInput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * 型の指定が無いインポーター記述。
     */
    @Test
    public void Input_InvalidTypeDescription() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(WithMissTypedInput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * 型が指定されていない出力。
     */
    @Test
    public void Output_NotTyped() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(NotTypedOutput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * 不正な注釈を持つ出力。
     */
    @Test
    public void Output_WithInvalidAnnotation() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(WithImportOutput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * Empty name for output.
     */
    @Test
    public void Output_WithEmptyOutputName() {
        JobFlowDriver driver = JobFlowDriver.analyze(WithEmptyOutputName.class);
        assertThat(driver.hasError(), is(true));
        assertThat(driver.getDiagnostics().size(), greaterThan(0));
    }

    /**
     * 出力ポート名が不正。
     */
    @Test
    public void Output_WithInvalidOutputName() {
        JobFlowDriver driver = JobFlowDriver.analyze(WithInvalidOutputName.class);
        assertThat(driver.hasError(), is(true));
        assertThat(driver.getDiagnostics().size(), greaterThan(0));
    }

    /**
     * 必要な注釈がない出力。
     */
    @Test
    public void Output_WithoudMandatoryAnnotation() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(NoExportOutput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * 注釈の型が不一致。
     */
    @Test
    public void Output_InconsistentAnnotation() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(InconsistentExportOutput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * インスタンス化できないエクスポーター記述。
     */
    @Test
    public void Output_InvalidDescription() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(WithAbstractExportOutput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * 型の指定が無いエクスポーター記述。
     */
    @Test
    public void Output_InvalidTypeDescription() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(WithMissTypedOutput.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * インスタンス化に失敗する。
     */
    @Test
    public void InstantiateFailure() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(InstantiateFailJobFlow.class);
        assertThat(analyzed.hasError(), is(true));
    }

    /**
     * 結線に失敗する。
     */
    @Test
    public void DescribeFailure() {
        JobFlowDriver analyzed = JobFlowDriver.analyze(DescribeFailJobFlow.class);
        assertThat(analyzed.hasError(), is(true));
    }
}
