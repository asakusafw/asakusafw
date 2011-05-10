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
package com.asakusafw.testdriver.api;

import java.net.URI;

import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.testing.DirectExporterDescription;
import com.asakusafw.compiler.testing.DirectImporterDescription;
import com.asakusafw.testdriver.core.ModelVerifier;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.Out;

/**
 * フロー部品のテスト出力データオブジェクト。
 * 
 * @param <T> モデルクラス
 */
public class FlowPartDriverOutput<T> {

    private String name;
    private Class<T> modelType;
    private ImporterDescription importerDescription;
    private ExporterDescription exporterDescription;
    private URI expected;
    private ModelVerifier<T> modelVerifier;

    private FlowDescriptionDriver flowDescriptionDriver;

    /**
     * コンストラクタ
     * 
     * @param flowDescriptionDriver flowDescriptionDriver。
     * @param name 入力の名前。
     * @param modelType モデルクラス。
     */
    public FlowPartDriverOutput(FlowDescriptionDriver flowDescriptionDriver,
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
    public FlowPartDriverOutput<T> prepare(String path) {
        // TODO verifyするつもりでこっちを呼ぶミスを犯しそうな。メソッド名を要検討。
        importerDescription = new DirectImporterDescription(modelType, path);
        return this;
    }

    /**
     * テスト結果の検証データを指定する
     * 
     * @param path 期待値データのパス。
     * @param verifier モデルに対する検証クラス
     * @return this。
     */
    public FlowPartDriverOutput<T> verify(String path, ModelVerifier<T> verifier) {
        // TODO 変換ロジックが必要？
        this.expected = URI.create(path);
        this.modelVerifier = verifier;
        exporterDescription = new DirectExporterDescription(modelType, path);
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
     * @return the expected
     */
    public URI getExpected() {
        return expected;
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
