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
            System.setProperty("ASAKUSA_TESTTOOLS_CONF", buildProperties.getProperty("asakusa.testtools.conf"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
