/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.testdriver.windgate;

import java.io.IOException;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.core.BaseExporterRetriever;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.ExporterRetriever;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.vocabulary.windgate.WindGateExporterDescription;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.ResourceManipulator;
import com.asakusafw.windgate.core.resource.SourceDriver;

/**
 * Implementation of {@link ExporterRetriever} for {@link WindGateExporterDescription}s.
 * @since 0.2.2
 */
public class WindGateExporterRetriever extends BaseExporterRetriever<WindGateExporterDescription> {

    @Override
    public void truncate(
            WindGateExporterDescription description,
            TestContext context) throws IOException {
        ProcessScript<?> process = WindGateTestHelper.createProcessScript(
                description.getModelType(),
                description);
        ParameterList parameterList = new ParameterList(context.getArguments());
        ResourceManipulator manipulator =
            WindGateTestHelper.createResourceManipulator(context, description, parameterList);
        manipulator.cleanupDrain(process);
    }

    @Override
    public <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            WindGateExporterDescription description,
            TestContext context) throws IOException {
        ProcessScript<V> process = WindGateTestHelper.createProcessScript(
                definition.getModelClass(),
                description);
        ParameterList parameterList = new ParameterList(context.getArguments());
        ResourceManipulator manipulator =
            WindGateTestHelper.createResourceManipulator(context, description, parameterList);
        DrainDriver<V> driver = manipulator.createDrainForDrain(process);
        return new WindGateOutput<>(WindGateTestHelper.prepare(driver));
    }

    @Override
    public <V> DataModelSource createSource(
            DataModelDefinition<V> definition,
            WindGateExporterDescription description,
            TestContext context) throws IOException {
        ProcessScript<V> process = WindGateTestHelper.createProcessScript(
                definition.getModelClass(),
                description);
        ParameterList parameterList = new ParameterList(context.getArguments());
        ResourceManipulator manipulator =
            WindGateTestHelper.createResourceManipulator(context, description, parameterList);
        SourceDriver<V> driver = manipulator.createSourceForDrain(process);
        return new WindGateSource<>(WindGateTestHelper.prepare(driver), definition);
    }
}
