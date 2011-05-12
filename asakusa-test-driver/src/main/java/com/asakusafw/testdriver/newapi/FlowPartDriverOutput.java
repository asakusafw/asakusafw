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

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.testing.DirectExporterDescription;
import com.asakusafw.compiler.testing.DirectImporterDescription;
import com.asakusafw.testdriver.FlowPartTestDriverUtils;
import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.core.ModelVerifier;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.Out;

/**
 * フロー部品のテスト出力データオブジェクト。
 * 
 * @param <T>
 *            モデルクラス
 */
public class FlowPartDriverOutput<T> {

    private static final Logger LOG = LoggerFactory
            .getLogger(FlowPartDriverOutput.class);

    private String name;
    private Class<T> modelType;
    private ImporterDescription importerDescription;
    private ExporterDescription exporterDescription;
    private URI sourceUri;
    private URI expectedUri;
    private URI verifyRuleUri;
    private ModelVerifier<T> modelVerifier;

    private FlowDescriptionDriver flowDescriptionDriver;
    private TestDriverContext driverContext;

    /**
     * コンストラクタ
     * 
     * @param driverContext
     *            テストドライバコンテキスト。
     * @param flowDescriptionDriver
     *            フロー定義ドライバ。
     * @param name
     *            入力の名前。
     * @param modelType
     *            モデルクラス。
     */
    public FlowPartDriverOutput(TestDriverContext driverContext,
            FlowDescriptionDriver flowDescriptionDriver, String name,
            Class<T> modelType) {
        this.name = name;
        this.modelType = modelType;
        this.flowDescriptionDriver = flowDescriptionDriver;
        this.driverContext = driverContext;
    }

    /**
     * テスト実行時に使用する入力データを指定する。
     * 
     * @param sourcePath
     *            入力データのパス。
     * @return this。
     */
    public FlowPartDriverOutput<T> prepare(String sourcePath) {
        return prepare(sourcePath, ":0");
    }

    /**
     * テスト実行時に使用する入力データを指定する。
     * 
     * @param sourcePath
     *            入力データのパス。
     * @param fragment
     *            フラグメント。
     * @return this。
     */
    public FlowPartDriverOutput<T> prepare(String sourcePath, String fragment) {

        LOG.info("ModelType:" + modelType);

        try {
            this.sourceUri = FlowPartTestDriverUtils
                    .toUri(sourcePath, fragment);
            LOG.info("Source URI:" + sourceUri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid sourcePath:" + sourcePath, e);
        }

        String importPath = FlowPartTestDriverUtils.createInputLocation(
                driverContext, name).toPath('/');
        LOG.info("Import Path=" + importPath);

        importerDescription = new DirectImporterDescription(modelType,
                importPath);
        return this;
    }

    /**
     * テスト結果の検証データを指定する
     * 
     * @param expectedPath
     *            期待値データのパス。
     * @param verifyRulePath
     *            検証ルールのパス。
     * @return this。
     */
    public FlowPartDriverOutput<T> verify(String expectedPath,
            String verifyRulePath) {

        LOG.info("verify - ModelType:" + modelType);
        
        try {
            this.expectedUri = FlowPartTestDriverUtils
                    .toUri(expectedPath, ":1");
            LOG.info("Expected URI:" + expectedUri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid expectedPath:" + expectedPath, e);
        }
        try {
            this.verifyRuleUri = FlowPartTestDriverUtils
                    .toUri(verifyRulePath, ":2");
            LOG.info("Verify Rule URI:" + verifyRuleUri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid verifyRulePath:" + verifyRulePath, e);
        }
        
        String exportPath = FlowPartTestDriverUtils.createOutputLocation(
                driverContext, name).toPath('/');
        LOG.info("Export Path=" + exportPath);

        exporterDescription = new DirectExporterDescription(modelType,
                exportPath);
        return this;
    }

    /**
     * テスト結果の検証データを指定する
     * 
     * @param expectedPath
     *            期待値データのパス。
     * @param verifier
     *            モデルに対する検証クラス
     * @return this。
     */
    public FlowPartDriverOutput<T> verify(String expectedPath,
            ModelVerifier<T> verifier) {
        // TODO 変換ロジックが必要？
        this.expectedUri = URI.create(expectedPath);
        this.modelVerifier = verifier;
        exporterDescription = new DirectExporterDescription(modelType,
                expectedPath);
        return this;
    }

    /**
     * フロー部品の出力オブジェクトを生成する。
     * 
     * @return 出力インターフェース。
     */
    public Out<T> createOut() {
        return flowDescriptionDriver.createOut(name, exporterDescription);
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
     * @return the importerDescription
     */
    public ImporterDescription getImporterDescription() {
        return importerDescription;
    }

    /**
     * @return the exporterDescription
     */
    public ExporterDescription getExporterDescription() {
        return exporterDescription;
    }

    /**
     * @return the modelVerifier
     */
    public ModelVerifier<T> getModelVerifier() {
        return modelVerifier;
    }

}
