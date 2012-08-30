/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.dmdl.analyzer;

import java.util.Collections;
import java.util.List;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.utils.collections.Lists;

/**
 * DMDL Semantic Error.
 */
public class DmdlSemanticException extends Exception {

    private static final long serialVersionUID = 1L;

    private final List<Diagnostic> diagnostics;

    /**
     * Creates and returns a new instance.
     * @param message the message of this error
     * @param diagnostics the raised diagnostics
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DmdlSemanticException(
            String message,
            List<Diagnostic> diagnostics) {
        super(message);
        if (diagnostics == null) {
            throw new IllegalArgumentException("diagnostics must not be null"); //$NON-NLS-1$
        }
        this.diagnostics = Lists.from(diagnostics);
    }

    /**
     * Returns semantic diagnostics of current error.
     * @return diagnostics
     */
    public List<Diagnostic> getDiagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }
}
