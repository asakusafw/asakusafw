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
package com.asakusafw.testdriver.windgate;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceProvider;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.vocabulary.windgate.WindGateExporterDescription;
import com.asakusafw.vocabulary.windgate.WindGateImporterDescription;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.resource.ResourceManipulator;
import com.asakusafw.windgate.core.resource.SourceDriver;

/**
 * An implementation of {@link DataModelSourceProvider}
 * using {@link WindGateImporterDescription} and {@link WindGateExporterDescription}.
 * This accepts URI: {@code windgate:<fully qualified class name of Importer/Exporter description>}.
 * @since 0.2.2
 */
public class WindGateSourceProvider implements DataModelSourceProvider {

    private static final String SCHEME = "windgate"; //$NON-NLS-1$

    static final Logger LOG = LoggerFactory.getLogger(WindGateSourceProvider.class);

    @Override
    public <T> DataModelSource open(
            DataModelDefinition<T> definition,
            URI source,
            TestContext context) throws IOException {
        String scheme = source.getScheme();
        if (scheme == null || scheme.equals(SCHEME) == false) {
            LOG.debug("URI does not indicate WindGate: {}", source); //$NON-NLS-1$
            return null;
        }
        ClassLoader classLoader = context.getClassLoader();
        String rest = source.getSchemeSpecificPart();
        LOG.debug("Attempts to load {} as a class", rest); //$NON-NLS-1$
        Object instance;
        try {
            Class<?> target = classLoader.loadClass(rest);
            instance = target.newInstance();
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("WindGateSourceProvider.errorFailedToCreateDescription"), //$NON-NLS-1$
                    rest), e);
        }
        if (instance instanceof WindGateImporterDescription) {
            WindGateImporterDescription description = (WindGateImporterDescription) instance;
            ProcessScript<T> process = WindGateTestHelper.createProcessScript(
                    definition.getModelClass(),
                    description);
            ParameterList parameterList = new ParameterList(context.getArguments());
            ResourceManipulator manipulator =
                WindGateTestHelper.createResourceManipulator(context, description, parameterList);
            SourceDriver<T> driver = manipulator.createSourceForSource(process);
            return new WindGateSource<>(WindGateTestHelper.prepare(driver), definition);
        } else if (instance instanceof WindGateExporterDescription) {
            WindGateExporterDescription description = (WindGateExporterDescription) instance;
            ProcessScript<T> process = WindGateTestHelper.createProcessScript(
                    definition.getModelClass(),
                    description);
            ParameterList parameterList = new ParameterList(context.getArguments());
            ResourceManipulator manipulator =
                WindGateTestHelper.createResourceManipulator(context, description, parameterList);
            SourceDriver<T> driver = manipulator.createSourceForDrain(process);
            return new WindGateSource<>(WindGateTestHelper.prepare(driver), definition);
        } else {
            throw new IOException(MessageFormat.format(
                    Messages.getString("WindGateSourceProvider.errorInvalidDescription"), //$NON-NLS-1$
                    source,
                    WindGateImporterDescription.class.getSimpleName(),
                    WindGateExporterDescription.class.getSimpleName()));
        }
    }
}
