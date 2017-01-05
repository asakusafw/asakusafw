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
package com.asakusafw.compiler.batch;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.vocabulary.batch.Batch;
import com.asakusafw.vocabulary.batch.BatchDescription;

/**
 * Represents a model of batch class.
 */
public class BatchClass {

    private final Batch config;

    private final BatchDescription description;

    /**
     * Creates a new instance.
     * @param config the configuration of this batch
     * @param description the batch description object (must be already run)
     */
    public BatchClass(Batch config, BatchDescription description) {
        Precondition.checkMustNotBeNull(config, "config"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        this.config = config;
        this.description = description;
    }

    /**
     * Returns the configuration of this batch.
     * @return the configuration
     */
    public Batch getConfig() {
        return config;
    }

    /**
     * Returns the instance which describes this batch.
     * @return the instance which describes this batch
     */
    public BatchDescription getDescription() {
        return description;
    }
}
