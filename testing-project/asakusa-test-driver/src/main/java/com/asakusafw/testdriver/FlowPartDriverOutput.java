/**
 * Copyright 2011-2016 Asakusa Framework Team.
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

import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.testing.DirectExporterDescription;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;

/**
 * A flow output driver for testing flow-parts.
 * @since 0.2.0
 * @version 0.6.0
 * @param <T> the data model type
 */
public class FlowPartDriverOutput<T> extends FlowDriverOutput<T, FlowPartDriverOutput<T>> implements Out<T> {

    private final DirectExporterDescription exporterDescription;

    private final Out<T> out;

    /**
     * Creates a new instance.
     * @param driverContext the current test driver context
     * @param descDriver the flow description driver
     * @param name the flow output name
     * @param modelType the data model class
     */
    public FlowPartDriverOutput(TestDriverContext driverContext, FlowDescriptionDriver descDriver, String name,
            Class<T> modelType) {
        super(driverContext.getCallerClass(), driverContext.getRepository(), name, modelType);
        String exportPath = FlowPartDriverUtils.createOutputLocation(driverContext, name).toPath('/');
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

    @Override
    public FlowElementInput toInputPort() {
        return out.toInputPort();
    }
}
