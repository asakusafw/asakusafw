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
package com.asakusafw.testdriver;

import java.io.File;
import java.net.URI;
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
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.ExternalIoCommandProvider.CommandContext;
import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.DirectExporterDescription;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.DirectImporterDescription;
import com.asakusafw.compiler.testing.JobflowInfo;
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
public class FlowPartTestDriver extends TestDriverBase {

    private static final Logger LOG = LoggerFactory
            .getLogger(FlowPartTestDriver.class);

    private FlowDescriptionDriver flowDescriptionDriver = new FlowDescriptionDriver();

    private Map<String, List<String>> createInMap = new HashMap<String, List<String>>();
    private Map<String, List<String>> createOutMap = new HashMap<String, List<String>>();

    private boolean createIndividually = false;
    private boolean loadIndividually = false;

    /**
     * コンストラクタ。
     * <p>
     * 使い方：本コンストラクタは、必ずJUnitのテストメソッドから直接呼び出して下さい。
     * テストメソッド内で、ユーティリティクラスやプライベートメソッドを経由して呼び出すことは出来ません。
     * （呼び出し元のテストクラス名、テストメソッド名に基づいて入出力データを取得・生成するため）
     * </p>
     *
     * @throws RuntimeException
     *             インスタンスの生成に失敗した場合
     */
    public FlowPartTestDriver() throws RuntimeException {
        super();
    }

    /**
     * コンストラクタ。
     * <p>
     * 使い方の注意点は{@link FlowPartTestDriver#FlowPartTestDriver()}を参照。
     * </p>
     *
     * @param testDataFileList
     *            テストデータ定義シートのパスを示すFileのリスト
     * @throws RuntimeException
     *             インスタンスの生成に失敗した場合
     * @see FlowPartTestDriver#FlowPartTestDriver()
     */
    public FlowPartTestDriver(List<File> testDataFileList)
            throws RuntimeException {
        super(testDataFileList);
    }

    /**
     * フロー部品のテストを実行し、テスト結果を検証します。
     *
     * @param flowDescription
     *            フロー部品クラスのインスタンス
     * @throws Throwable
     *             テストの実行に失敗した場合
     */
    public void runTest(FlowDescription flowDescription) throws Throwable {

        // クラスタワークディレクトリ初期化
        initializeClusterDirectory(clusterWorkDir);

        // テストデータ生成ツールを実行し、Excel上のテストデータ定義からシーケンスファイルを生成し、HDFS上に配置する。
        if (createIndividually) {
            createSequenceFilesIndividually();
        } else {
            createSequenceFiles();
        }

        // フローコンパイラの実行
        String flowId = className.substring(className.lastIndexOf('.') + 1)
                + "_" + methodName;
        File compileWorkDir = new File(compileWorkBaseDir, flowId);
        if (compileWorkDir.exists()) {
            FileUtils.forceDelete(compileWorkDir);
        }

        FlowGraph flowGraph = flowDescriptionDriver
                .createFlowGraph(flowDescription);
        JobflowInfo jobflowInfo = DirectFlowCompiler.compile(flowGraph,
                "test.batch", flowId, "test.flowpart", createTempLocation(),
                compileWorkDir, Arrays.asList(new File[] { DirectFlowCompiler
                        .toLibraryPath(flowDescription.getClass()) }),
                flowDescription.getClass().getClassLoader(), options);

        CommandContext context = new CommandContext(
                System.getenv("ASAKUSA_HOME") + "/", executionId, batchArgs);

        Map<String, String> dPropMap = createHadoopProperties(context);

        TestExecutionPlan plan = createExecutionPlan(jobflowInfo, context,
                dPropMap);
        savePlan(compileWorkDir, plan);
        executePlan(plan, jobflowInfo.getPackageFile());

        // テスト結果検証ツールを実行し、Excel上の期待値とシーケンスファイル上の実際値を比較する。
        if (loadIndividually) {
            loadAndInspectSequenceFilesIndividually();
        } else {
            loadAndInspectSequenceFiles();
        }
    }

    /**
     * フロー部品の入力オブジェクトを生成します。
     *
     * @param <T>
     *            データモデルクラス
     * @param tableName
     *            テーブル名
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
     * @param <T>
     *            データモデルクラス
     * @param modelType
     *            データモデルクラス
     * @return 入力インターフェース
     */
    public <T> In<T> createIn(Class<T> modelType) {

        String tableName = testUtils.getTablenameByClass(modelType);
        addCreateIn(tableName, tableName);
        String path = createInputLocation(tableName).toPath('/');
        LOG.info("DirectImporterDescription生成:Path=" + path);
        ImporterDescription desc = new DirectImporterDescription(modelType,
                path);
        return flowDescriptionDriver.createIn(tableName, desc);
    }

    /**
     * フロー部品の入力オブジェクトを生成します。
     * <p>
     * フロー部品の入力に同一モデル（テーブル）が複数存在する場合は このメソッドを使います。
     * </p>
     *
     * @param <T>
     *            データモデルクラス
     * @param tableName
     *            テーブル名
     * @param excelFileName
     *            テストデータ定義シートファイル名(.xlsを除いた値)
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
     * @param <T>
     *            データモデルクラス
     * @param modelType
     *            データモデルクラス
     * @param excelFileName
     *            テストデータ定義シートファイル名(.xlsを除いた値)
     * @return 入力インターフェース
     */
    public <T> In<T> createIn(Class<T> modelType, String excelFileName) {

        String tableName = testUtils.getTablenameByClass(modelType);
        createIndividually = true;
        loadIndividually = true;
        addCreateIn(tableName, excelFileName);

        String path = createInputLocation(excelFileName).toPath('/');

        LOG.info("DirectImporterDescription生成:Path=" + excelFileName);

        DirectImporterDescription desc = new DirectImporterDescription(
                modelType, path);

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
     * @param <T>
     *            データモデルクラス
     * @param tableName
     *            テーブル名
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
     * @param <T>
     *            データモデルクラス
     * @param modelType
     *            データモデルクラス
     * @return 出力インターフェース
     */
    public <T> Out<T> createOut(Class<T> modelType) {

        String tableName = testUtils.getTablenameByClass(modelType);
        addCreateOut(tableName, tableName);
        String path = createOutputLocation(tableName).toPath('/');
        LOG.info("DirectExporterDescription生成:Path=" + path);
        ExporterDescription desc = new DirectExporterDescription(modelType,
                path);
        return flowDescriptionDriver.createOut(tableName, desc);
    }

    /**
     * フロー部品の出力オブジェクトを生成します。
     * <p>
     * フロー部品の出力に同一モデル（テーブル）が複数存在する場合は このメソッドを使います。
     * </p>
     *
     * @param <T>
     *            データモデルクラス
     * @param tableName
     *            テーブル名
     * @param excelFileName
     *            テストデータ定義シートファイル名(.xlsを除いた値)
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
     * @param <T>
     *            データモデルクラス
     * @param modelType
     *            データモデルクラス
     * @param excelFileName
     *            テストデータ定義シートファイル名(.xlsを除いた値)
     * @return 出力インターフェース
     */
    public <T> Out<T> createOut(Class<T> modelType, String excelFileName) {

        String tableName = testUtils.getTablenameByClass(modelType);
        loadIndividually = true;
        addCreateOut(tableName, excelFileName);

        String path = createOutputLocation(excelFileName).toPath('/');

        LOG.info("DirectExporterDescription生成:Path=" + excelFileName);

        DirectExporterDescription desc = new DirectExporterDescription(
                modelType, path);

        String outName;
        int offset = excelFileName.lastIndexOf(System
                .getProperty("file.separator"));
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

    private void createSequenceFiles() throws Throwable {
        for (String table : testUtils.getTablenames()) {
            createSequenceFile(table, table);
        }
    }

    private void createSequenceFilesIndividually() throws Throwable {

        for (Map.Entry<String, List<String>> entry : createInMap.entrySet()) {
            String tablename = entry.getKey();
            List<String> fileList = entry.getValue();
            for (String excelFileName : fileList) {
                List<File> inFileList = new ArrayList<File>();
                if (testDataDir != null) {
                    inFileList
                            .add(new File(testDataDir, excelFileName + ".xls"));
                } else {
                    for (File file : testDataFileList) {
                        if (file.getPath().endsWith(excelFileName + ".xls")) {
                            inFileList.add(file);
                            break;
                        }
                    }
                }
                testUtils = new TestUtils(inFileList);
                createSequenceFile(tablename, excelFileName);
            }
        }
    }

    private void createSequenceFile(String tablename, String excelFileName)
            throws Throwable {

        Configuration conf = new Configuration();
        FileSystem fs = null;
        SequenceFile.Writer writer = null;

        try {
            fs = FileSystem.get(conf);
            fs.setWorkingDirectory(fs.getHomeDirectory());

            URI seqFilePath = new URI(createInputSequenceFilePath(fs,
                    excelFileName));
            LOG.info("SequenceFileを作成します:Path=" + seqFilePath);

            writer = SequenceFile.createWriter(fs, conf,
                    new Path(seqFilePath.getPath()), NullWritable.class,
                    testUtils.getClassByTablename(tablename));

            testUtils.storeToSequenceFile(tablename, writer);
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (fs != null) {
                fs.close();
            }
        }
    }

    private void loadAndInspectSequenceFiles() throws Throwable {
        for (String table : testUtils.getTablenames()) {
            loadSequenceFile(table, table);
        }
        if (!testUtils.inspect()) {
            Assert.fail(testUtils.getCauseMessage());
        }
    }

    private void loadAndInspectSequenceFilesIndividually() throws Throwable {

        for (Map.Entry<String, List<String>> entry : createOutMap.entrySet()) {
            String tablename = entry.getKey();
            List<String> fileList = entry.getValue();
            for (String excelFileName : fileList) {
                List<File> outFileList = new ArrayList<File>();
                if (testDataDir != null) {
                    outFileList.add(new File(testDataDir, excelFileName
                            + ".xls"));
                } else {
                    for (File file : testDataFileList) {
                        if (file.getPath().endsWith(excelFileName + ".xls")) {
                            outFileList.add(file);
                            break;
                        }
                    }
                }
                testUtils = new TestUtils(outFileList);
                loadSequenceFile(tablename, excelFileName);
                if (!testUtils.inspect()) {
                    Assert.fail(testUtils.getCauseMessage());
                    return;
                }
            }
        }
    }

    private void loadSequenceFile(String tablename, String excelFileName)
            throws Throwable {

        Configuration conf = new Configuration();
        FileSystem fs = null;

        fs = FileSystem.get(conf);
        fs.setWorkingDirectory(fs.getHomeDirectory());
        try {
            FileStatus[] status = fs.globStatus(new Path(
                    createOutputSequenceFilePath(fs, excelFileName)));
            Path[] listedPaths = FileUtil.stat2Paths(status);
            for (Path path : listedPaths) {
                if (isSystemFile(path)) {
                    continue;
                }
                SequenceFile.Reader reader = null;
                try {
                    LOG.info("SequenceFileをロードします:Path=" + path);
                    reader = new SequenceFile.Reader(fs, path, conf);

                    testUtils.loadFromSequenceFile(tablename, reader);
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }
        } finally {
            fs.close();
        }
    }

    private boolean isSystemFile(Path path) {
        assert path != null;
        String name = path.getName();
        return name.equals(FileOutputCommitter.SUCCEEDED_FILE_NAME)
                || name.equals("_logs");
    }

    private Location createInputLocation(String tableName) {
        Location location = Location.fromPath(clusterWorkDir, '/')
                .append(executionId).append("input")
                .append(normalize(tableName));
        return location;
    }

    private Location createOutputLocation(String tableName) {
        Location location = Location.fromPath(clusterWorkDir, '/')
                .append(executionId).append("output")
                .append(normalize(tableName)).asPrefix();
        return location;
    }

    private Location createTempLocation() {
        Location location = Location.fromPath(clusterWorkDir, '/')
                .append(executionId).append("temp");
        return location;
    }

    private String createInputSequenceFilePath(FileSystem fs, String tableName) {
        Location location = createInputLocation(tableName);
        return new Path(fs.getWorkingDirectory(), location.toPath('/'))
                .toString();
    }

    private String createOutputSequenceFilePath(FileSystem fs, String tableName) {
        Location location = createOutputLocation(tableName);
        return new Path(fs.getWorkingDirectory(), location.toPath('/'))
                .toString();
    }

    private String normalize(String targetName) {
        // MultipleInputs/Outputsではアルファベットと数字だけしかつかえない
        StringBuilder buf = new StringBuilder();
        for (char c : targetName.toCharArray()) {
            // 0 はエスケープ記号に
            if ('1' <= c && c <= '9' || 'A' <= c && c <= 'Z' || 'a' <= c
                    && c <= 'z') {
                buf.append(c);
            } else if (c <= 0xff) {
                buf.append('0');
                buf.append(String.format("%02x", (int) c));
            } else {
                buf.append("0u");
                buf.append(String.format("%04x", (int) c));
            }
        }
        return buf.toString();
    }
    
/////////////////////////////////////////////////////////////////////////////////////////////////
// New Interface with asakusa-test-moderator
/////////////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    
}
