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

import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.testing.DirectImporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription.DataSize;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;

/**
 * フロー部品のテスト入力データオブジェクト。
 * @since 0.2.0
 *
 * @param <T> モデルクラス
 */
public class FlowPartDriverInput<T> extends DriverInputBase<T> implements In<T> {

    private static final Logger LOG = LoggerFactory.getLogger(FlowPartDriverInput.class);

    /** フロー記述ドライバ。 */
    protected FlowDescriptionDriver descDriver;

    private final In<T> in;

    /**
     * コンストラクタ。
     * @param driverContext テストドライバコンテキスト。
     * @param descDriver フロー定義ドライバ。
     * @param name 入力の名前。
     * @param modelType モデルクラス。
     */
    public FlowPartDriverInput(TestDriverContext driverContext, FlowDescriptionDriver descDriver, String name,
            Class<T> modelType) {
        this.driverContext = driverContext;
        this.descDriver = descDriver;
        this.name = name;
        this.modelType = modelType;

        String importPath = FlowPartDriverUtils.createInputLocation(driverContext, name).toPath('/');
        LOG.info("Import Path=" + importPath);
        importerDescription = new DirectImporterDescription(modelType, importPath);
        in = descDriver.createIn(name, importerDescription);
    }

    /**
     * テスト実行時に使用する入力データを指定する。
     *
     * @param sourcePath 入力データのパス。
     * @return this。
     */
    public FlowPartDriverInput<T> prepare(String sourcePath) {
        LOG.info("prepare - ModelType:" + getModelType() + ", SourcePath:" + sourcePath);
        setSourceUri(sourcePath);
        return this;
    }

    /**
     * テストデータのデータサイズを指定する。
     *
     * フロー部品のテスト時にAshigel Compilerに対して最適化のヒントを与えます
     *
     * @param dataSize データサイズ
     * @return this。
     * @throws UnsupportedOperationException DirectImpoterDescription以外に対する操作が行われた
     */
    public FlowPartDriverInput<T> withDataSize(DataSize dataSize) {
        if (!(importerDescription instanceof DirectImporterDescription)) {
            throw new UnsupportedOperationException(
                    "withDataSize method is only support DirectImporterDescription but was: "
                    + importerDescription.getClass().getName());
        } else {
            ((DirectImporterDescription) importerDescription).setDataSize(dataSize);
        }
        return this;
    }

    @Override
    public FlowElementOutput toOutputPort() {
        return in.toOutputPort();
    }

}
