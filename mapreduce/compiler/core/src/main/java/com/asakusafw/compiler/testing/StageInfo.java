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
package com.asakusafw.compiler.testing;

import com.asakusafw.compiler.common.Precondition;

/**
 * Structural information of stages in the main phase.
 */
public class StageInfo {

    private final String className;

    /**
     * Creates a new instance.
     * @param className the qualified class name of the stage client
     * @throws IllegalArgumentException the parameter is {@code null}
     */
    public StageInfo(String className) {
        Precondition.checkMustNotBeNull(className, "className"); //$NON-NLS-1$
        this.className = className;
    }

    /**
     * Returns the stage client class name.
     * @return the stage client class name
     */
    public String getClassName() {
        return className;
    }
}
