/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
import com.asakusafw.vocabulary.external.ExporterDescription;

/**
 * Retrieves test results from suitable {@link ExporterDescription}.
 * <em>
 * Attention:
 * Currently this interface is not stable.
 * Please inherit {@link AbstractExporterRetriever} or {@link BaseExporterRetriever} instead.
 * </em>
 * <p>
 * Adding {@link ExporterDescription} test moderators, clients can implement this
 * and put the class name in
 * {@code META-INF/services/com.asakusafw.testdriver.core.ExporterRetriever}.
 * </p>
 * @param <T> type of target {@link ExporterDescription}
 * @version 0.2.2
 * @since 0.2.0
 */
public interface ExporterRetriever<T extends ExporterDescription> {

    /**
     * Returns the class of target {@link ExporterDescription}.
     * @return the class
     */
    Class<T> getDescriptionClass();

    /**
     * Truncates all resources which the exporter will use.
     * <p>
     * If target resources do not support truncate operations,
     * this method has no effects.
     * </p>
     * @param description the description
     * @param context the current test context
     * @throws IOException if failed to open the target
     * @since 0.2.2
     */
    void truncate(T description, TestContext context) throws IOException;

    /**
     * Creates a {@link ModelOutput} to prepare what the exporter will use.
     * @param <V> type of model
     * @param definition the data model definition
     * @param description the description
     * @param context the current test context
     * @return the created {@link ModelOutput}
     * @throws IOException if failed to open the target
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.2
     */
    <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            T description,
            TestContext context) throws IOException;

    /**
     * Creates a {@link DataModelSource} to retrieve what the target exporter had created.
     * @param <V> type of model
     * @param definition the data model definition
     * @param description the description
     * @param context the current test context
     * @return the created {@link DataModelSource}
     * @throws IOException if failed to open the target
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.2
     */
    <V> DataModelSource createSource(
            DataModelDefinition<V> definition,
            T description,
            TestContext context) throws IOException;
}
