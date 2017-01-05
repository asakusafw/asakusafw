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
package com.asakusafw.compiler.operator;

import javax.tools.Diagnostic;

/**
 * An exception while compiling operator DSL programs.
 * @since 0.1.0
 * @version 0.7.0
 */
public class OperatorCompilerException extends RuntimeException {

    private static final long serialVersionUID = 2L;

    private static final Diagnostic.Kind DEFAULT_KIND = Diagnostic.Kind.OTHER;

    private final Diagnostic.Kind kind;

    /**
     * Creates a new instance.
     * @param message the exception message (nullable)
     */
    public OperatorCompilerException(String message) {
        this(DEFAULT_KIND, message);
    }

    /**
     * Creates a new instance.
     * @param message the exception message (nullable)
     * @param cause the original cause (nullable)
     */
    public OperatorCompilerException(String message, Throwable cause) {
        this(DEFAULT_KIND, message, cause);
    }

    /**
     * Creates a new instance.
     * @param kind the diagnostics kind
     * @param message the exception message (nullable)
     * @since 0.7.0
     */
    public OperatorCompilerException(Diagnostic.Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    /**
     * Creates a new instance.
     * @param kind the diagnostics kind
     * @param message the exception message (nullable)
     * @param cause the original cause (nullable)
     * @since 0.7.0
     */
    public OperatorCompilerException(Diagnostic.Kind kind, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
    }

    /**
     * Returns the diagnostics kind of this exception.
     * @return the diagnostics kind of this exception, or {@code null} if it was not defined
     * @since 0.7.0
     */
    public Diagnostic.Kind getKind() {
        return kind;
    }
}
