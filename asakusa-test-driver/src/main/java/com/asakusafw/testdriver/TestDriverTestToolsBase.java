package com.asakusafw.testdriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testtools.TestUtils;

/**
 * asakusa-test-toolsが提供するAPIを使って実装されたテストドライバの基底クラス。
 *
 */
public class TestDriverTestToolsBase extends TestDriverBase {

    static final Logger LOG = LoggerFactory.getLogger(TestDriverTestToolsBase.class);

    private static final String BUILD_PROPERTIES_FILE = "build.properties";

    /** テストデータ格納先のデフォルト値 */
    protected static final String TESTDATA_DIR_DEFAULT = "src/test/data/excel";

    /** テストデータ生成・検証ツールオブジェクト。 */
    protected TestUtils testUtils;
    /** TestUtils生成時に指定するテストデータ定義シートのファイルリスト。 */
    protected List<File> testDataFileList;
    /** TestUtils生成時に指定するテストデータ定義シートのディレクトリ（testDataFileListと排他)。 */
    protected File testDataDir;

    /** build.properties */
    protected Properties buildProperties;

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
        initialize();
    }

    private void initialize() {
        try {

            File buildPropertiesFile = new File(BUILD_PROPERTIES_FILE);
            if (buildPropertiesFile.exists()) {
                LOG.info("ビルド設定情報をロードしています: {}", buildPropertiesFile);
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(buildPropertiesFile);
                    buildProperties = new Properties();
                    buildProperties.load(fis);
                    System.setProperty("ASAKUSA_MODELGEN_PACKAGE", buildProperties.getProperty("asakusa.modelgen.package"));
                    System.setProperty("ASAKUSA_MODELGEN_OUTPUT", buildProperties.getProperty("asakusa.modelgen.output"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    IOUtils.closeQuietly(fis);
                }
            } else {
                LOG.info("ビルド設定情報が存在しないため、スキップします: {}", BUILD_PROPERTIES_FILE);
            }
            System.setProperty("ASAKUSA_TESTTOOLS_CONF", buildProperties.getProperty("asakusa.jdbc.conf"));
            System.setProperty("ASAKUSA_TEMPLATEGEN_OUTPUT_DIR", buildProperties.getProperty("asakusa.testdatasheet.output"));
            String testDataDirPath = buildProperties.getProperty("asakusa.testdriver.testdata.dir");
            if (testDataDirPath == null) {
                testDataDirPath = TESTDATA_DIR_DEFAULT;
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
