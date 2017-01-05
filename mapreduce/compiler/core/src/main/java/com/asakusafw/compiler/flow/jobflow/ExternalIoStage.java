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
import com.asakusafw.compiler.flow.ExternalIoDescriptionProcessor.IoContext;

/**
 * Represents a stage for I/O processes.
 * @since 0.5.1
 */
public class ExternalIoStage {

    private final String moduleName;

    private final CompiledStage compiledStage;

    private final IoContext context;

    /**
     * Creates a new instance.
     * @param moduleName the module name
     * @param compiledStage the compiled stage information
     * @param context related I/O information
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ExternalIoStage(String moduleName, CompiledStage compiledStage, IoContext context) {
        Precondition.checkMustNotBeNull(moduleName, "moduleName"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(compiledStage, "compiledStage"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
        this.moduleName = moduleName;
        this.compiledStage = compiledStage;
        this.context = context;
    }

    /**
     * Returns the module name of this I/O.
     * @return the module name
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Returns the compiled stage information.
     * @return the compiled stage information
     */
    public CompiledStage getCompiledStage() {
        return compiledStage;
    }

    /**
     * Returns the related I/O information.
     * @return the I/O context
     */
    public IoContext getContext() {
        return context;
    }
}
