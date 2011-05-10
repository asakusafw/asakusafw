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
package com.asakusafw.testdriver.bulkloader;

import java.io.IOException;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.testdriver.core.AbstractImporterPreparator;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.ImporterPreparator;
import com.asakusafw.vocabulary.bulkloader.BulkLoadImporterDescription;

/**
 * Implementation of {@link ImporterPreparator} for {@link BulkLoadImporterDescription}s.
 * @since 0.2.0
 */
public class BulkLoadImporterPreparator extends AbstractImporterPreparator<BulkLoadImporterDescription> {

    @Override
    public <V> void truncate(
            DataModelDefinition<V> definition,
            BulkLoadImporterDescription description) throws IOException {
        // TODO implement
        throw new IOException();
    }

    @Override
    public <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            BulkLoadImporterDescription description) throws IOException {
        // TODO implement
        throw new IOException();
    }
}
