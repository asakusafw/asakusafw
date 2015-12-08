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
package com.asakusafw.testdriver.directio.api;

import java.io.IOException;
import java.text.MessageFormat;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.OperatorTestEnvironment;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.directio.DirectIoTestHelper;
import com.asakusafw.vocabulary.directio.DirectFileInputDescription;

/**
 * Operator testing API for Direct I/O.
 * @since 0.7.3
 */
final class DirectIoOperatorTester extends DirectIoTester {

    private final OperatorTestEnvironment environment;

    DirectIoOperatorTester(OperatorTestEnvironment environment) {
        this.environment = environment;
    }

    @Override
    protected DirectIoResource resource(DirectFileInputDescription description) {
        return new Resource(environment, description);
    }

    private static final class Resource extends DirectIoResource {

        private final OperatorTestEnvironment environment;

        public Resource(OperatorTestEnvironment environment, DirectFileInputDescription description) {
            super(environment, description);
            this.environment = environment;
        }

        @Override
        public void prepare(DataModelSourceFactory factory) {
            prepare0(factory, description.getModelType());
        }

        private <T> void prepare0(DataModelSourceFactory factory, Class<T> type) {
            try {
                DataModelDefinition<T> definition = getTestTools().toDataModelDefinition(type);
                DirectIoTestHelper helper = new DirectIoTestHelper(
                        environment.getTestContext(),
                        description.getBasePath(),
                        environment.getConfiguration());
                helper.truncate(description.getResourcePattern());
                try (ModelOutput<T> output = helper.openOutput(type, description);
                        DataModelSource source = factory.createSource(definition, environment.getTestContext())) {
                    while (true) {
                        DataModelReflection r = source.next();
                        if (r == null) {
                            break;
                        }
                        T object = definition.toObject(r);
                        output.write(object);
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException(MessageFormat.format(
                        Messages.getString("DirectIoOperatorTester.errorFailedToInitialize"), //$NON-NLS-1$
                        description), e);
            }
        }
    }
}
