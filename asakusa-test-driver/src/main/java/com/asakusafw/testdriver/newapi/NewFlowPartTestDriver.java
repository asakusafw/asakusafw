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
package com.asakusafw.testdriver.newapi;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.testdriver.TestDriverBase;
import com.asakusafw.testdriver.TestExecutionPlan;
import com.asakusafw.testdriver.core.DataModelAdapter;
import com.asakusafw.testdriver.core.ExporterRetriever;
import com.asakusafw.testdriver.core.ImporterPreparator;
import com.asakusafw.testdriver.core.SourceProvider;
import com.asakusafw.testdriver.core.TestInputPreparator;
import com.asakusafw.testdriver.core.TestResultInspector;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.testdriver.core.VerifyRuleProvider;
import com.asakusafw.testdriver.excel.ExcelSheetRuleProvider;
import com.asakusafw.testdriver.excel.ExcelSheetSourceProvider;
import com.asakusafw.testdriver.file.FileExporterRetriever;
import com.asakusafw.testdriver.file.FileImporterPreparator;
import com.asakusafw.testdriver.model.DefaultDataModelAdapter;
import com.asakusafw.testdriver.model.DefaultDataModelDefinition;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * フロー部品用のテストドライバクラス。
 */
public class NewFlowPartTestDriver extends TestDriverBase {

    private List<FlowPartDriverInput<?>> inputs = new LinkedList<FlowPartDriverInput<?>>();
    private List<FlowPartDriverOutput<?>> outputs = new LinkedList<FlowPartDriverOutput<?>>();
    
    private FlowDescriptionDriver flowDescriptionDriver = new FlowDescriptionDriver();
    
    /**
     * テスト入力データを指定する。
     * 
     * @param <T> ModelType。
     * @param name 入力データ名。テストドライバに指定する入力データ間で一意の名前を指定する。
     * @param modelType ModelType。
     * @return テスト入力データオブジェクト。
     */
    public <T> FlowPartDriverInput<T> input(String name, Class<T> modelType) {
        FlowPartDriverInput<T> input = new FlowPartDriverInput<T>(
                driverContext, flowDescriptionDriver, name, modelType);
        inputs.add(input);
        return input;
    }
    
    /**
     * テスト結果の出力データ（期待値データ）を指定する。
     * 
     * @param <T> ModelType。
     * @param name 出力データ名。テストドライバに指定する出力データ間で一意の名前を指定する。
     * @param modelType ModelType。
     * @return テスト入力データオブジェクト。
     */
    public <T> FlowPartDriverOutput<T> output(String name, Class<T> modelType) {
        FlowPartDriverOutput<T> output = new FlowPartDriverOutput<T>(
                driverContext, flowDescriptionDriver, name, modelType);
        outputs.add(output);
        return output;
    }
    
    /**
     * フロー部品のテストを実行し、テスト結果を検証する。
     *
     * @param flowDescription フロー部品クラスのインスタンス
     * @throws IOException 
     */
    public void runTest(FlowDescription flowDescription) throws IOException {

        // クラスタワークディレクトリ初期化
        initializeClusterDirectory(driverContext.getClusterWorkDir()); 

        // TODO Excel上のテストデータ定義からシーケンスファイルを生成し、HDFS上に配置する。
        // TODO Sourceプロバイダ、ImporterPreparatorの切り替え
        // TestInputPrepatatorの生成
        DataModelAdapter adapter = new DefaultDataModelAdapter();
        SourceProvider source = new ExcelSheetSourceProvider();
        ImporterPreparator targets = new FileImporterPreparator();
        
        for (FlowPartDriverInput<?> input : inputs) {
            TestInputPreparator preparator = new TestInputPreparator(adapter, source, targets);
            preparator.prepare(input.getModelType(), input.getImporterDescription(), input.getSourceUri());
        }
        
        // フローコンパイラの実行
        String flowId = driverContext.getClassName().substring(driverContext.getClassName().lastIndexOf('.') + 1)
                + "_" + driverContext.getMethodName();
        File compileWorkDir = new File(driverContext.getCompileWorkBaseDir(), flowId);
        if (compileWorkDir.exists()) {
            FileUtils.forceDelete(compileWorkDir);
        }

        FlowGraph flowGraph = flowDescriptionDriver
                .createFlowGraph(flowDescription);
        JobflowInfo jobflowInfo = DirectFlowCompiler.compile(flowGraph,
                "test.batch", flowId, "test.flowpart", createTempLocation(),
                compileWorkDir, Arrays.asList(new File[] { DirectFlowCompiler
                        .toLibraryPath(flowDescription.getClass()) }),
                flowDescription.getClass().getClassLoader(), driverContext.getOptions());

        CommandContext context = new CommandContext(
                System.getenv("ASAKUSA_HOME") + "/", driverContext.getExecutionId(), driverContext.getBatchArgs());

        Map<String, String> dPropMap = createHadoopProperties(context);

        TestExecutionPlan plan = createExecutionPlan(jobflowInfo, context,
                dPropMap);
        savePlan(compileWorkDir, plan);
        executePlan(plan, jobflowInfo.getPackageFile());
        
        // TODO Excel上の期待値とシーケンスファイル上の実際値を比較する。
        // TODO ExporterRetriever、Importerプリパレータの切り替え
        
        VerifyRuleProvider provider = new ExcelSheetRuleProvider();
        ExporterRetriever retriever = new FileExporterRetriever();
        TestResultInspector inspector = new TestResultInspector(
                adapter, source, provider, retriever);
        VerifyContext verifyContext = new VerifyContext();
        
        for (FlowPartDriverOutput<?> output : outputs) {
            // TODO ModelVerifierの切り替え
            inspector.inspect(
                    output.getModelType(),
                    output.getExporterDescription(),
                    output.getExpectedUri(),
                    new ExcelSheetRuleProvider().get(
                            new DefaultDataModelDefinition(output.getModelType()),
                            new VerifyContext(),
                            output.getVerifyRuleUri()));
        }
        
    }
    
    private Location createTempLocation() {
        Location location = Location.fromPath(driverContext.getClusterWorkDir(), '/')
                .append(driverContext.getExecutionId()).append("temp");
        return location;
    }
    

}
