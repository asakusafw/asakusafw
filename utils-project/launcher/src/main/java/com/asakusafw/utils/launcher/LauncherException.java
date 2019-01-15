/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.utils.launcher;

/**
 * Represents an error while launching applications.
 */
public class LauncherException extends RuntimeException {

    private static final long serialVersionUID = -5566847104874631897L;

    /**
     * Creates a new instance.
     */
    public LauncherException() {
        super();
    }

    /**
     * Creates a new instance.
     * @param message the message
     */
    public LauncherException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     * @param message the message
     * @param cause the cause of this exception
     */
    public LauncherException(String message, Throwable cause) {
        super(message, cause);
    }
}
