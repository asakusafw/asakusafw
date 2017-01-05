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

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import com.asakusafw.vocabulary.batch.WorkDescription;

/**
 * An abstract interface of processing {@link Workflow}.
 */
public interface WorkflowProcessor extends BatchCompilingEnvironment.Initializable {

    /**
     * Returns the target {@link WorkDescriptionProcessor}s for this.
     * @return the target {@link WorkDescriptionProcessor}s
     */
    Collection<Class<? extends WorkDescriptionProcessor<?>>> getDescriptionProcessors();

    /**
     * Processes the target {@link Workflow}.
     * @param workflow the target {@link Workflow}
     * @throws IOException if failed to process the target workflow
     */
    void process(Workflow workflow) throws IOException;

    /**
     * A repository for {@link WorkflowProcessor}s.
     */
    interface Repository extends BatchCompilingEnvironment.Initializable {

        /**
         * Returns the available workflow processors for the target descriptions.
         * @param descriptions the target descriptions
         * @return the available {@link WorkflowProcessor}s
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        Set<WorkflowProcessor> findWorkflowProcessors(Set<? extends WorkDescription> descriptions);

        /**
         * Returns the work description processor for the target object.
         * @param workDescription the target {@link WorkDescription}
         * @return the corresponded {@link WorkDescriptionProcessor}, or {@code null}
         *     if there is no suitable processors
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        WorkDescriptionProcessor<?> findDescriptionProcessor(WorkDescription workDescription);
    }
}
