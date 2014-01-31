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
import com.asakusafw.compiler.testing.DirectExporterDescription;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.Source;

/**
 * フロー部品のテスト出力データオブジェクト。
 * @since 0.2.0
 * @version 0.6.0
 * @param <T> モデルクラス
 */
public class FlowPartDriverOutput<T> extends FlowDriverOutput<T, FlowPartDriverOutput<T>> implements Out<T> {

    private static final Logger LOG = LoggerFactory.getLogger(FlowPartDriverOutput.class);

    /** フロー記述ドライバ 。*/
    protected FlowDescriptionDriver descDriver;

    private final DirectExporterDescription exporterDescription;

    private final Out<T> out;

    /**
     * コンストラクタ。
     *
     * @param driverContext テストドライバコンテキスト。
     * @param descDriver フロー定義ドライバ。
     * @param name 入力の名前。
     * @param modelType モデルクラス。
     */
    public FlowPartDriverOutput(TestDriverContext driverContext, FlowDescriptionDriver descDriver, String name,
            Class<T> modelType) {
        super(driverContext.getCallerClass(), driverContext.getRepository(), name, modelType);
        this.descDriver = descDriver;

        String exportPath = FlowPartDriverUtils.createOutputLocation(driverContext, name).toPath('/');
        LOG.info("Export Path=" + exportPath);
        this.exporterDescription = new DirectExporterDescription(modelType, exportPath);
        this.out = descDriver.createOut(name, exporterDescription);
    }

    DirectExporterDescription getExporterDescription() {
        return exporterDescription;
    }

    @Override
    protected FlowPartDriverOutput<T> getThis() {
        return this;
    }

    @Override
    public void add(Source<T> upstream) {
        out.add(upstream);
    }
}
