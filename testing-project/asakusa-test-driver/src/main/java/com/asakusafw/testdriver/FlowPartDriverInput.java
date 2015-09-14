/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

// TODO i18n
/**
 * A flow input driver for testing flow-parts.
 * @since 0.2.0
 * @version 0.6.0
 * @param <T> the data model type
 */
public class FlowPartDriverInput<T> extends FlowDriverInput<T, FlowPartDriverInput<T>> implements In<T> {

    static final Logger LOG = LoggerFactory.getLogger(FlowPartDriverInput.class);

    private final DirectImporterDescription importerDescription;

    private final In<T> in;

    /**
     * Creates a new instance.
     * @param driverContext the current test driver context
     * @param descDriver the flow description driver
     * @param name the flow input name
     * @param modelType the data model class
     */
    public FlowPartDriverInput(TestDriverContext driverContext, FlowDescriptionDriver descDriver, String name,
            Class<T> modelType) {
        super(driverContext.getCallerClass(), driverContext.getRepository(), name, modelType);
        String importPath = FlowPartDriverUtils.createInputLocation(driverContext, name).toPath('/');
        this.importerDescription = new DirectImporterDescription(modelType, importPath);
        this.in = descDriver.createIn(name, importerDescription);
    }

    @Override
    protected FlowPartDriverInput<T> getThis() {
        return this;
    }

    DirectImporterDescription getImporterDescription() {
        return importerDescription;
    }

    /**
     * テストデータのデータサイズを指定する。
     * @param dataSize データサイズ
     * @return this
     */
    public FlowPartDriverInput<T> withDataSize(DataSize dataSize) {
        importerDescription.setDataSize(dataSize);
        return this;
    }

    @Override
    public FlowElementOutput toOutputPort() {
        return in.toOutputPort();
    }
}
