/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.testdriver.directio;

import java.io.IOException;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.core.BaseImporterPreparator;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.ImporterPreparator;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.vocabulary.directio.DirectFileInputDescription;

/**
 * An implementation of {@link ImporterPreparator} for {@link DirectFileInputDescription}.
 * @since 0.2.5
 */
public class DirectFileInputPreparator extends BaseImporterPreparator<DirectFileInputDescription> {

    @Override
    public void truncate(
            DirectFileInputDescription description,
            TestContext context) throws IOException {
        DirectIoTestHelper helper = new DirectIoTestHelper(context, description.getBasePath());
        helper.truncate();
    }

    @Override
    public <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            DirectFileInputDescription description,
            TestContext context) throws IOException {
        DirectIoTestHelper helper = new DirectIoTestHelper(context, description.getBasePath());
        return helper.openOutput(definition.getModelClass(), description);
    }
}
