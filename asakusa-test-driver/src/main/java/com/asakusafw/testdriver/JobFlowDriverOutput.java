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

import com.asakusafw.testdriver.core.DataModelSinkFactory;
import com.asakusafw.testdriver.core.DifferenceSinkFactory;
import com.asakusafw.testdriver.core.ModelVerifier;

/**
 * ジョブフローのテスト出力データオブジェクト。
 * @since 0.2.0
 *
 * @param <T> モデルクラス
 */

public class JobFlowDriverOutput<T> extends DriverOutputBase<T> {

    private static final Logger LOG = LoggerFactory.getLogger(JobFlowDriverOutput.class);

    /**
     * コンストラクタ
     *
     * @param driverContext テストドライバコンテキスト。
     * @param name 入力の名前。
     * @param modelType モデルクラス。
     */
    public JobFlowDriverOutput(TestDriverContext driverContext, String name, Class<T> modelType) {
        this.driverContext = driverContext;
        this.name = name;
        this.modelType = modelType;
    }

    /**
     * テスト実行時に使用する入力データを指定する。
     *
     * @param sourcePath 入力データのパス。
     * @return this。
     */
    public JobFlowDriverOutput<T> prepare(String sourcePath) {

        LOG.info("prepare - ModelType:" + getModelType());
        setSourceUri(sourcePath);
        return this;
    }

    /**
     * テスト結果の検証データを指定する
     *
     * @param expectedPath 期待値データのパス。
     * @param verifyRulePath 検証ルールのパス。
     * @return this。
     */
    public JobFlowDriverOutput<T> verify(String expectedPath, String verifyRulePath) {

        LOG.info("verify - ModelType:" + modelType);
        setExpectedUri(expectedPath);
        setVerifyRuleUri(verifyRulePath);
        return this;
    }

    /**
     * テスト結果の検証データを指定する
     *
     * @param expectedPath 期待値データのパス
     * @param modelVerifier 検証ルール
     * @return this。
     */
    public JobFlowDriverOutput<T> verify(String expectedPath, ModelVerifier<? super T> modelVerifier) {
        LOG.info("verify - ModelType:" + modelType);
        setExpectedUri(expectedPath);
        setModelVerifier(modelVerifier);
        return this;
    }

    /**
     * テスト結果のデータを受け取るオブジェクトのファクトリを指定する (experimental)。
     * @param factory テスト結果のデータを受け取るオブジェクトのファクトリ
     * @return this
     */
    public JobFlowDriverOutput<T> dumpActual(DataModelSinkFactory factory) {
        LOG.info("dump actual: {}", factory);
        setResultSink(factory);
        return this;
    }

    /**
     * テスト結果の差異を受け取るオブジェクトのファクトリを指定する (experimental)。
     * @param factory テスト結果の差異を受け取るオブジェクトのファクトリ
     * @return this
     */
    public JobFlowDriverOutput<T> dumpDifference(DifferenceSinkFactory factory) {
        LOG.info("dump difference: {}", factory);
        setDifferenceSink(factory);
        return this;
    }
}
