package com.asakusafw.testdriver;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.asakusafw.testtools.TestUtils;

/**
 * asakusa-test-toolsが提供するAPIを使って実装されたテストドライバの基底クラス。
 *
 */
public class TestDriverTestToolsBase extends TestDriverBase {

    /** テストデータ生成・検証ツールオブジェクト。 */
    protected TestUtils testUtils;
    /** TestUtils生成時に指定するテストデータ定義シートのファイルリスト。 */
    protected List<File> testDataFileList;
    /** TestUtils生成時に指定するテストデータ定義シートのディレクトリ（testDataFileListと排他)。 */
    protected File testDataDir;

    /**
     * コンストラクタ。
     *
     * @throws RuntimeException 初期化に失敗した場合
     */
    public TestDriverTestToolsBase() {
        super(null);
    }

    /**
     * コンストラクタ。
     *
     * @param testDataFileList テストデータ定義シートのパスを示すFileのリスト
     * @throws RuntimeException 初期化に失敗した場合
     */
    public TestDriverTestToolsBase(List<File> testDataFileList) {
        super(null);
        this.testDataFileList = testDataFileList;
    }

    @Override
    protected void initialize() {
        super.initialize();

        try {
            System.setProperty("ASAKUSA_TESTTOOLS_CONF", buildProperties.getProperty("asakusa.testtools.conf"));
            System.setProperty("ASAKUSA_TEMPLATEGEN_OUTPUT_DIR", buildProperties.getProperty("asakusa.testdatasheet.output"));
            String testDataDirPath = buildProperties.getProperty("asakusa.testdriver.testdata.dir");
            if (testDataDirPath == null) {
                testDataDirPath = TestDriverBase.TESTDATA_DIR_DEFAULT;
            }
            if (testDataFileList == null) {
                testDataDir = new File(testDataDirPath + System.getProperty("file.separator")
                        + driverContext.getClassName() + System.getProperty("file.separator")
                        + driverContext.getMethodName());
                testUtils = new TestUtils(testDataDir);
            } else {
                testUtils = new TestUtils(testDataFileList);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
