/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.testdriver;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.DirectExporterDescription;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.DirectImporterDescription;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.testtools.TestUtils;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * フロー部品用のテストドライバクラス。
 */
@SuppressWarnings("deprecation")
public class FlowPartTestDriver extends TestDriverTestToolsBase {

    private static final Logger LOG = LoggerFactory.getLogger(FlowPartTestDriver.class);

    private final FlowDescriptionDriver flowDescriptionDriver = new FlowDescriptionDriver();

    private final Map<String, List<String>> createInMap = new HashMap<String, List<String>>();
    private final Map<String, List<String>> createOutMap = new HashMap<String, List<String>>();

    private boolean createIndividually = false;
    private boolean loadIndividually = false;

    /**
     * コンストラクタ。
     *
     * @throws RuntimeException インスタンスの生成に失敗した場合
     */
    public FlowPartTestDriver() {
        super();
    }

    /**
     * コンストラクタ。
     *
     * @param testDataFileList テストデータ定義シートのパスを示すFileのリスト
     * @throws RuntimeException インスタンスの生成に失敗した場合
     * @see FlowPartTestDriver#FlowPartTestDriver()
     */
    public FlowPartTestDriver(List<File> testDataFileList) {
        super(testDataFileList);
    }

    /**
     * フロー部品のテストを実行し、テスト結果を検証します。
     *
     * @param flowDescription フロー部品クラスのインスタンス
     */
    public void runTest(FlowDescription flowDescription) {

        try {
            // フローコンパイラの実行
            String batchId = "flow";
            String flowId = "part";
            File compileWorkDir = driverContext.getCompilerWorkingDirectory();
            if (compileWorkDir.exists()) {
                FileUtils.forceDelete(compileWorkDir);
            }

            FlowGraph flowGraph = flowDescriptionDriver.createFlowGraph(flowDescription);
            JobflowInfo jobflowInfo = DirectFlowCompiler.compile(flowGraph, batchId, flowId, "test.flowpart",
                    FlowPartDriverUtils.createWorkingLocation(driverContext), compileWorkDir,
                    Arrays.asList(new File[] { DirectFlowCompiler.toLibraryPath(flowDescription.getClass()) }),
                    flowDescription.getClass().getClassLoader(), driverContext.getOptions());

            JobflowExecutor executor = new JobflowExecutor(driverContext);
            driverContext.prepareCurrentJobflow(jobflowInfo);

            // クラスタワークディレクトリ初期化
            executor.cleanWorkingDirectory();

            // テストデータ生成ツールを実行し、Excel上のテストデータ定義からシーケンスファイルを生成し、HDFS上に配置する。
            if (createIndividually) {
                prepareIndividually();
            } else {
                createAllInput();
            }

            executor.runJobflow(jobflowInfo);

            // テスト結果検証ツールを実行し、Excel上の期待値とシーケンスファイル上の実際値を比較する。
            if (loadIndividually) {
                loadAndInspectResultsIndividually();
            } else {
                loadAndInspectResults();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            driverContext.cleanUpTemporaryResources();
        }
    }

    /**
     * フロー部品の入力オブジェクトを生成します。
     *
     * @param <T> データモデルクラス
     * @param tableName テーブル名
     * @return 入力インターフェース
     */
    @SuppressWarnings("unchecked")
    public <T> In<T> createIn(String tableName) {

        Class<?> modelType = testUtils.getClassByTablename(tableName);
        return (In<T>) createIn(modelType);
    }

    /**
     * フロー部品の入力オブジェクトを生成します。
     *
     * @param <T> データモデルクラス
     * @param modelType データモデルクラス
     * @return 入力インターフェース
     */
    public <T> In<T> createIn(Class<T> modelType) {

        String tableName = testUtils.getTablenameByClass(modelType);
        addCreateIn(tableName, tableName);
        String path = FlowPartDriverUtils.createInputLocation(driverContext, tableName).toPath('/');
        LOG.info("DirectImporterDescription生成:Path=" + path);
        ImporterDescription desc = new DirectImporterDescription(modelType, path);
        return flowDescriptionDriver.createIn(tableName, desc);
    }

    /**
     * フロー部品の入力オブジェクトを生成します。
     * <p>
     * フロー部品の入力に同一モデル（テーブル）が複数存在する場合は このメソッドを使います。
     * </p>
     *
     * @param <T> データモデルクラス
     * @param tableName テーブル名
     * @param excelFileName テストデータ定義シートファイル名(.xlsを除いた値)
     * @return 入力インターフェース
     */
    @SuppressWarnings("unchecked")
    public <T> In<T> createIn(String tableName, String excelFileName) {
        Class<?> modelType = testUtils.getClassByTablename(tableName);
        return (In<T>) createIn(modelType, excelFileName);
    }

    /**
     * フロー部品の入力オブジェクトを生成します。
     * <p>
     * フロー部品の入力に同一モデル（テーブル）が複数存在する場合は このメソッドを使います。
     * </p>
     *
     * @param <T> データモデルクラス
     * @param modelType データモデルクラス
     * @param excelFileName テストデータ定義シートファイル名(.xlsを除いた値)
     * @return 入力インターフェース
     */
    public <T> In<T> createIn(Class<T> modelType, String excelFileName) {

        String tableName = testUtils.getTablenameByClass(modelType);
        createIndividually = true;
        loadIndividually = true;
        addCreateIn(tableName, excelFileName);

        String path = FlowPartDriverUtils.createInputLocation(driverContext, excelFileName).toPath('/');

        LOG.info("DirectImporterDescription生成:Path=" + excelFileName);

        DirectImporterDescription desc = new DirectImporterDescription(modelType, path);

        String inName;
        int offset = excelFileName.lastIndexOf('/');
        if (offset > -1) {
            inName = excelFileName.substring(offset + 1);
        } else {
            inName = excelFileName;
        }
        return flowDescriptionDriver.createIn(inName, desc);
    }

    /**
     * フロー部品の出力オブジェクトを生成します。
     *
     * @param <T> データモデルクラス
     * @param tableName テーブル名
     * @return 出力インターフェース
     */
    @SuppressWarnings("unchecked")
    public <T> Out<T> createOut(String tableName) {
        Class<?> modelType = testUtils.getClassByTablename(tableName);
        return (Out<T>) createOut(modelType);
    }

    /**
     * フロー部品の出力オブジェクトを生成します。
     *
     * @param <T> データモデルクラス
     * @param modelType データモデルクラス
     * @return 出力インターフェース
     */
    public <T> Out<T> createOut(Class<T> modelType) {

        String tableName = testUtils.getTablenameByClass(modelType);
        addCreateOut(tableName, tableName);
        String path = FlowPartDriverUtils.createOutputLocation(driverContext, tableName).toPath('/');
        LOG.info("DirectExporterDescription生成:Path=" + path);
        ExporterDescription desc = new DirectExporterDescription(modelType, path);
        return flowDescriptionDriver.createOut(tableName, desc);
    }

    /**
     * フロー部品の出力オブジェクトを生成します。
     * <p>
     * フロー部品の出力に同一モデル（テーブル）が複数存在する場合は このメソッドを使います。
     * </p>
     *
     * @param <T> データモデルクラス
     * @param tableName テーブル名
     * @param excelFileName テストデータ定義シートファイル名(.xlsを除いた値)
     * @return 出力インターフェース
     */
    @SuppressWarnings("unchecked")
    public <T> Out<T> createOut(String tableName, String excelFileName) {

        Class<?> modelType = testUtils.getClassByTablename(tableName);
        return (Out<T>) createOut(modelType, excelFileName);

    }

    /**
     * フロー部品の出力オブジェクトを生成します。
     * <p>
     * フロー部品の出力に同一モデル（テーブル）が複数存在する場合は このメソッドを使います。
     * </p>
     *
     * @param <T> データモデルクラス
     * @param modelType データモデルクラス
     * @param excelFileName テストデータ定義シートファイル名(.xlsを除いた値)
     * @return 出力インターフェース
     */
    public <T> Out<T> createOut(Class<T> modelType, String excelFileName) {

        String tableName = testUtils.getTablenameByClass(modelType);
        loadIndividually = true;
        addCreateOut(tableName, excelFileName);

        String path = FlowPartDriverUtils.createOutputLocation(driverContext, excelFileName).toPath('/');

        LOG.info("DirectExporterDescription生成:Path=" + excelFileName);

        DirectExporterDescription desc = new DirectExporterDescription(modelType, path);

        String outName;
        int offset = excelFileName.lastIndexOf(System.getProperty("file.separator"));
        if (offset > -1) {
            outName = excelFileName.substring(offset + 1);
        } else {
            outName = excelFileName;
        }
        return flowDescriptionDriver.createOut(outName, desc);
    }

    private void addCreateIn(String tableName, String fileName) {
        List<String> fileListPerTable = createInMap.get(tableName);
        if (fileListPerTable == null) {
            fileListPerTable = new ArrayList<String>();
            createInMap.put(tableName, fileListPerTable);
        }
        fileListPerTable.add(fileName);
    }

    private void addCreateOut(String tableName, String fileName) {
        List<String> fileListPerTable = createOutMap.get(tableName);
        if (fileListPerTable == null) {
            fileListPerTable = new ArrayList<String>();
            createOutMap.put(tableName, fileListPerTable);
        }
        fileListPerTable.add(fileName);
    }

    private void createAllInput() {
        for (String table : testUtils.getTablenames()) {
            createInput(table, table);
        }
    }

    private void prepareIndividually() {
        for (Map.Entry<String, List<String>> entry : createInMap.entrySet()) {
            String tablename = entry.getKey();
            List<String> fileList = entry.getValue();
            for (String excelFileName : fileList) {
                List<File> inFileList = new ArrayList<File>();
                if (testDataDir != null) {
                    inFileList.add(new File(testDataDir, excelFileName + ".xls"));
                } else {
                    for (File file : testDataFileList) {
                        if (file.getPath().endsWith(excelFileName + ".xls")) {
                            inFileList.add(file);
                            break;
                        }
                    }
                }
                try {
                    testUtils = new TestUtils(inFileList);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                createInput(tablename, excelFileName);
            }
        }
    }

    private void createInput(String tablename, String excelFileName) {
        FileSystem fs = null;
        try {
            Configuration conf = ConfigurationFactory.getDefault().newInstance();
            fs = FileSystem.get(conf);
            URI inputPath = new URI(computeInputPath(fs, excelFileName));
            LOG.info("テストデータを配置します:Path=" + inputPath);
            testUtils.storeToTemporary(tablename, conf, new Path(inputPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadAndInspectResults() throws IOException {
        for (String table : testUtils.getTablenames()) {
            loadResult(table, table);
        }
        if (!testUtils.inspect()) {
            Assert.fail(testUtils.getCauseMessage());
        }
    }

    private void loadAndInspectResultsIndividually() throws IOException {
        for (Map.Entry<String, List<String>> entry : createOutMap.entrySet()) {
            String tablename = entry.getKey();
            List<String> fileList = entry.getValue();
            for (String excelFileName : fileList) {
                List<File> outFileList = new ArrayList<File>();
                if (testDataDir != null) {
                    outFileList.add(new File(testDataDir, excelFileName + ".xls"));
                } else {
                    for (File file : testDataFileList) {
                        if (file.getPath().endsWith(excelFileName + ".xls")) {
                            outFileList.add(file);
                            break;
                        }
                    }
                }
                try {
                    testUtils = new TestUtils(outFileList);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                loadResult(tablename, excelFileName);
                if (!testUtils.inspect()) {
                    Assert.fail(testUtils.getCauseMessage());
                    return;
                }
            }
        }
    }

    private void loadResult(String tablename, String excelFileName) throws IOException {
        Configuration conf = ConfigurationFactory.getDefault().newInstance();
        FileSystem fs = FileSystem.get(conf);
        FileStatus[] status = fs.globStatus(new Path(computeOutputPath(fs, excelFileName)));
        Path[] listedPaths = FileUtil.stat2Paths(status);
        for (Path path : listedPaths) {
            if (isSystemFile(path)) {
                continue;
            }
            LOG.info("出力結果をロードします:Path=" + path);
            testUtils.loadFromTemporary(tablename, conf, path);
        }
    }

    private boolean isSystemFile(Path path) {
        assert path != null;
        String name = path.getName();
        return name.equals(FileOutputCommitter.SUCCEEDED_FILE_NAME) || name.equals("_logs");
    }

    private String computeInputPath(FileSystem fs, String tableName) {
        Location location = FlowPartDriverUtils.createInputLocation(driverContext, tableName);
        String path = new Path(fs.getWorkingDirectory(), location.toPath('/')).toString();
        return resolvePath(path);
    }

    private String computeOutputPath(FileSystem fs, String tableName) {
        Location location = FlowPartDriverUtils.createOutputLocation(driverContext, tableName);
        String path = new Path(fs.getWorkingDirectory(), location.toPath('/')).toString();
        return resolvePath(path);
    }

    private String resolvePath(String path) {
        assert path != null;
        Map<String, String> arguments = driverContext.getArguments();
        VariableTable variables = new VariableTable();
        variables.defineVariables(arguments);
        return variables.parse(path, false);
    }
}
