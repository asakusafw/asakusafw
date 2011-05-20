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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.testing.DirectExporterDescription;
import com.asakusafw.compiler.testing.DirectImporterDescription;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.Source;

/**
 * フロー部品のテスト出力データオブジェクト。
 * @since 0.2.0
 * 
 * @param <T> モデルクラス
 */
public class FlowPartDriverOutput<T> extends DriverOutputBase<T> implements Out<T> {

    private static final Logger LOG = LoggerFactory.getLogger(FlowPartDriverOutput.class);

    /** フロー記述ドライバ */
    protected FlowDescriptionDriver descDriver;

    private Out<T> out;

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
        this.driverContext = driverContext;
        this.descDriver = descDriver;
        this.name = name;
        this.modelType = modelType;

        String exportPath = FlowPartDriverUtils.createOutputLocation(driverContext, name).toPath('/');
        LOG.info("Export Path=" + exportPath);
        exporterDescription = new DirectExporterDescription(modelType, exportPath);
        out = descDriver.createOut(name, exporterDescription);
    }

    /**
     * テスト実行時に使用する入力データを指定する。
     * 
     * @param sourcePath 入力データのパス。
     * @return this。
     */
    public FlowPartDriverOutput<T> prepare(String sourcePath) {
        LOG.info("prepare - ModelType:" + getModelType());
        setSourceUri(sourcePath);

        String importPath = FlowPartDriverUtils.createInputLocation(driverContext, name).toPath('/');
        LOG.info("Import Path=" + importPath);
        importerDescription = new DirectImporterDescription(modelType, importPath);
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

        LOG.info("verify - ModelType:" + modelType);
        setExpectedUri(expectedPath);
        setVerifyRuleUri(verifyRulePath);
        return this;
    }

    @Override
    public void add(Source<T> source) {
        out.add(source);
    }

}
