/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.runtime.core.context;

/**
 * Represents application is inconsistent between local and remote.
 * @since 0.4.0
 */
public class InconsistentApplicationException extends IllegalStateException {

    private static final long serialVersionUID = 8732513817965656444L;

    /**
     * Creates a new instance.
     * @param message message (nullable)
     */
    public InconsistentApplicationException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     * @param message message (nullable)
     * @param cause reason of this exception
     */
    public InconsistentApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
