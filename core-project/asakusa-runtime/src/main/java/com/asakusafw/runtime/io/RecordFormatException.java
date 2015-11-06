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
package com.asakusafw.runtime.io;

import java.io.IOException;

/**
 * An exception that will occur for wrong record format.
 */
public class RecordFormatException extends IOException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.
     * @param message the exception message (nullable)
     */
    public RecordFormatException(String message) {
        super(message);
    }

    /**
     * Creates a new instance.
     * @param message the exception message (nullable)
     * @param cause the original cause (nullable)
     */
    public RecordFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
