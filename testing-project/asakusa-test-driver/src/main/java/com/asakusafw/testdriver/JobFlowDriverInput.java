/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
 * ジョブフローのテスト入力データオブジェクト。
 * @since 0.2.0
 *
 * @param <T> モデルクラス
 */
public class JobFlowDriverInput<T> extends DriverInputBase<T> {

    private static final Logger LOG = LoggerFactory.getLogger(JobFlowDriverInput.class);

    /**
     * コンストラクタ。
     *
     * @param driverContext テストドライバコンテキスト。
     * @param name 入力の名前。
     * @param modelType モデルクラス。
     */
    public JobFlowDriverInput(TestDriverContext driverContext, String name, Class<T> modelType) {
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
    public JobFlowDriverInput<T> prepare(String sourcePath) {
        LOG.info("prepare - ModelType:" + getModelType());
        setSourceUri(sourcePath);
        return this;
    }
}
