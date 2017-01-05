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
package com.asakusafw.compiler.flow.jobflow;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.java.model.syntax.Name;

/**
 * Compiled information of a jobflow stage in the main phase.
 */
public class CompiledStage {

    private final Name qualifiedName;

    private final String stageId;

    /**
     * Creates a new instance.
     * @param qualifiedName the stage client class name
     * @param stageId the stage ID
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public CompiledStage(Name qualifiedName, String stageId) {
        Precondition.checkMustNotBeNull(qualifiedName, "qualifiedName"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(stageId, "stageId"); //$NON-NLS-1$
        this.qualifiedName = qualifiedName;
        this.stageId = stageId;
    }

    /**
     * Returns the qualified name of the stage client class.
     * @return the qualified name of the stage client class
     */
    public Name getQualifiedName() {
        return qualifiedName;
    }

    /**
     * Returns the stage ID.
     * @return the stage ID
     */
    public String getStageId() {
        return stageId;
    }
}
