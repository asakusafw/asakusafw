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

import com.asakusafw.vocabulary.batch.WorkDescription;

/**
 * An abstract super interface of processor for {@link WorkDescription}.
 * @param <T> the target {@link WorkDescription}
 */
public interface WorkDescriptionProcessor<T extends WorkDescription>
        extends BatchCompilingEnvironment.Initializable {

    /**
     * Returns the target class of this processor.
     * @return the target class
     */
    Class<T> getTargetType();

    /**
     * Processes the target description object.
     * @param description the target description object
     * @return the processed result
     * @throws IOException if failed to process the target description
     */
    Object process(T description) throws IOException;
}
