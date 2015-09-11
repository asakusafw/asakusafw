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
package com.asakusafw.vocabulary.batch;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a Unit-of-Work in batch.
 */
public final class Work {

    private final BatchDescription declaring;

    private final WorkDescription description;

    private final List<Work> dependencies;

    /**
     * Creates a new instance.
     * @param declaring the declaring batch class
     * @param description the target description of this work
     * @param dependencies the dependencies of this work in the batch
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    public Work(
            BatchDescription declaring,
            WorkDescription description,
            List<Work> dependencies) {
        if (declaring == null) {
            throw new IllegalArgumentException("declaring must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        if (dependencies == null) {
            throw new IllegalArgumentException("dependencies must not be null"); //$NON-NLS-1$
        }
        this.declaring = declaring;
        this.description = description;
        this.dependencies = Collections.unmodifiableList(
                new ArrayList<Work>(dependencies));
    }

    /**
     * Returns the batch which declaring this work.
     * @return the declaring batch
     */
    public BatchDescription getDeclaring() {
        return declaring;
    }

    /**
     * Returns the description of this work.
     * @return the description
     */
    public WorkDescription getDescription() {
        return description;
    }

    /**
     * Returns the dependencies of this work.
     * @return the dependencies
     */
    public List<Work> getDependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}'{'description={1}, declaring={2}, dependencies={3}'}'", //$NON-NLS-1$
                getClass().getSimpleName(),
                description,
                declaring.getClass().getName(),
                dependencies);
    }
}
