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
        return prepare(sourcePath, null);
    }

    /**
     * テスト実行時に使用する入力データを指定する。
     * 
     * @param sourcePath 入力データのパス。
     * @param fragment フラグメント。
     * @return this。
     */
    public JobFlowDriverOutput<T> prepare(String sourcePath, String fragment) {

        LOG.info("prepare - ModelType:" + getModelType());
        setSourceUri(sourcePath, fragment);
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
    public JobFlowDriverOutput<T> verify(String expectedPath, String expectedFlagment, String verifyRulePath,
            String verifyRuleFlagment) {

        LOG.info("verify - ModelType:" + modelType);
        setExpectedUri(expectedPath, expectedFlagment);
        setVerifyRuleUri(verifyRulePath, verifyRuleFlagment);
        return this;
    }    
    
}
