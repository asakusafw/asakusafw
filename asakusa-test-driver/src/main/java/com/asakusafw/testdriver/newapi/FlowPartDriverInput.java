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

import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.testing.DirectImporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.In;

/**
 * フロー部品のテスト入力データオブジェクト。
 * 
 * @param <T> モデルクラス
 */
public class FlowPartDriverInput<T> {

    private String name;
    private Class<T> modelType;
    private ImporterDescription importerDescription;

    private FlowDescriptionDriver flowDescriptionDriver;

    /**
     * コンストラクタ
     * 
     * @param flowDescriptionDriver flowDescriptionDriver。
     * @param name 入力の名前。
     * @param modelType モデルクラス。
     */
    public FlowPartDriverInput(FlowDescriptionDriver flowDescriptionDriver,
            String name, Class<T> modelType) {
        this.name = name;
        this.modelType = modelType;
        this.flowDescriptionDriver = flowDescriptionDriver;
    }

    /**
     * テスト実行時に使用する入力データを指定する。
     * 
     * @param path 入力データのパス。
     * @return this。
     */
    public FlowPartDriverInput<T> prepare(String path) {
        importerDescription = new DirectImporterDescription(modelType, path);
        return this;
    }

    /**
     * フロー部品の入力オブジェクトを生成する。
     * 
     * @return 入力インターフェース。
     */
    public In<T> createIn() {
        return flowDescriptionDriver.createIn(name, importerDescription);
    }
    
    /**
     * @return the modelType
     */
    public Class<T> getModelType() {
        return modelType;
    }

    /**
     * @return the importerDescription
     */
    public ImporterDescription getImporterDescription() {
        return importerDescription;
    }

}
