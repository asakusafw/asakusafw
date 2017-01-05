/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.testdriver.core;

import java.io.IOException;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * Abstract implementation of {@link ImporterPreparator}.
 * @param <T> type of target {@link ImporterDescription}
 * @since 0.2.0
 */
public abstract class AbstractImporterPreparator<T extends ImporterDescription>
        extends BaseImporterPreparator<T> {

    /**
     * Truncates all resources which the importer will use.
     * <p>
     * If target resources do not support truncate operations,
     * this method has no effects.
     * </p>
     * @param description the description
     * @throws IOException if failed to open the target
     */
    public abstract void truncate(T description) throws IOException;

    /**
     * Redirects to {@link #truncate(ImporterDescription) this.truncate(description)}.
     * @param description the description
     * @param context the current test context
     * @throws IOException if failed to open the target
     */
    @Override
    public void truncate(T description, TestContext context) throws IOException {
        truncate(description);
    }

    /**
     * Creates a {@link ModelOutput} to prepare the resource which the importer will use.
     * @param <V> type of model
     * @param definition the data model definition
     * @param description the description
     * @return the created {@link ModelOutput}
     * @throws IOException if failed to open the target
     */
    public abstract <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            T description) throws IOException;

    /**
     * Redirects to {@link #createOutput(DataModelDefinition, ImporterDescription)
     * createOutput(definition, description)}.
     * @param <V> type of model
     * @param definition the data model definition
     * @param description the description
     * @param context the current test context
     * @return the created {@link ModelOutput}
     * @throws IOException if failed to open the target
     */
    @Override
    public <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            T description,
            TestContext context) throws IOException {
        return createOutput(definition, description);
    }
}
