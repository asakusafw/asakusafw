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

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.testing.DirectExporterDescription;
import com.asakusafw.compiler.testing.DirectImporterDescription;
import com.asakusafw.testdriver.core.ModelVerifier;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.Out;

/**
 * フロー部品のテスト出力データオブジェクト。
 * @since 0.2.0
 * 
 * @param <T> モデルクラス
 */
public class FlowPartDriverOutput<T> {

    private static final Logger LOG = LoggerFactory.getLogger(FlowPartDriverOutput.class);

    private String name;
    private Class<T> modelType;
    private ImporterDescription importerDesc;
    private ExporterDescription exporterDesc;
    private URI sourceUri;
    private URI expectedUri;
    private URI verifyRuleUri;
    private ModelVerifier<T> modelVerifier;
    private FlowDescriptionDriver descDriver;
    private TestDriverContext driverContext;

    /**
     * コンストラクタ
     * 
     * @param driverContext テストドライバコンテキスト。
     * @param descDriver フロー定義ドライバ。
     * @param name 入力の名前。
     * @param modelType モデルクラス。
     */
    public FlowPartDriverOutput(TestDriverContext driverContext, FlowDescriptionDriver descDriver, String name,
            Class<T> modelType) {
        this.name = name;
        this.modelType = modelType;
        this.descDriver = descDriver;
        this.driverContext = driverContext;
    }

    /**
     * テスト実行時に使用する入力データを指定する。
     * 
     * @param sourcePath 入力データのパス。
     * @return this。
     */
    public FlowPartDriverOutput<T> prepare(String sourcePath) {
        return prepare(sourcePath, null);
    }

    /**
     * テスト実行時に使用する入力データを指定する。
     * 
     * @param sourcePath 入力データのパス。
     * @param fragment フラグメント。
     * @return this。
     */
    public FlowPartDriverOutput<T> prepare(String sourcePath, String fragment) {

        LOG.info("ModelType:" + modelType);

        try {
            this.sourceUri = FlowPartDriverUtils.toUri(sourcePath, fragment);
            LOG.info("Source URI:" + sourceUri + ", Fragment:" + fragment);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid source URI:" + sourcePath + ", fragment:" + fragment, e);
        }

        String importPath = FlowPartDriverUtils.createInputLocation(driverContext, name).toPath('/');
        LOG.info("Import Path=" + importPath);

        importerDesc = new DirectImporterDescription(modelType, importPath);
        return this;
    }

    /**
     * テスト結果の検証データを指定する
     * 
     * @param expectedPath 期待値データのパス。
     * @param verifyRulePath 検証ルールのパス。
     * @return this。
     */
    public FlowPartDriverOutput<T> verify(String expectedPath, String verifyRulePath) {
        return verify(expectedPath, null, verifyRulePath, null);
    }

    /**
     * テスト結果の検証データを指定する
     * 
     * @param expectedPath 期待値データのパス。
     * @param expectedFlagment 期待値データのフラグメント識別子。
     * @param verifyRulePath 検証ルールのパス。
     * @param verifyRuleFlagment 検証ルールのフラグメント識別子。
     * @return this。
     */
    public FlowPartDriverOutput<T> verify(String expectedPath, String expectedFlagment, String verifyRulePath,
            String verifyRuleFlagment) {

        LOG.info("verify - ModelType:" + modelType);

        try {
            this.expectedUri = FlowPartDriverUtils.toUri(expectedPath, expectedFlagment);
            LOG.info("Expected URI:" + expectedUri + ", Fragment:" + expectedFlagment);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid expected URI. expectedPath:" + expectedPath
                    + ", expectedFlagment:" + expectedFlagment, e);
        }

        try {
            this.verifyRuleUri = FlowPartDriverUtils.toUri(verifyRulePath, verifyRuleFlagment);
            LOG.info("Verify Rule URI:" + verifyRuleUri + ", Fragment:" + verifyRuleFlagment);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid verifyRule URI. verifyRulePath:" + verifyRulePath
                    + ", verifyRuleFlagment:" + verifyRuleFlagment, e);
        }

        String exportPath = FlowPartDriverUtils.createOutputLocation(driverContext, name).toPath('/');
        LOG.info("Export Path=" + exportPath);

        exporterDesc = new DirectExporterDescription(modelType, exportPath);
        return this;
    }

    /**
     * フロー部品の出力オブジェクトを生成する。
     * 
     * @return 出力インターフェース。
     */
    public Out<T> createOut() {
        return descDriver.createOut(name, exporterDesc);
    }

    /**
     * @return the expectedUri
     */
    public URI getExpectedUri() {
        return expectedUri;
    }

    /**
     * @return the verifyRuleUri
     */
    public URI getVerifyRuleUri() {
        return verifyRuleUri;
    }

    /**
     * @return the modelType
     */
    public Class<T> getModelType() {
        return modelType;
    }

    /**
     * @return the sourceUri
     */
    public URI getSourceUri() {
        return sourceUri;
    }

    /**
     * @return the importerDesc
     */
    public ImporterDescription getImporterDescription() {
        return importerDesc;
    }

    /**
     * @return the exporterDesc
     */
    public ExporterDescription getExporterDescription() {
        return exporterDesc;
    }

    /**
     * @return the modelVerifier
     */
    public ModelVerifier<T> getModelVerifier() {
        return modelVerifier;
    }

}
