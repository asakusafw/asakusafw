/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.utils.java.parser.javadoc;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrLocation;

/**
 * An exception which represents a documentation comment has unexpected end of comment.
 */
public class IllegalEndOfCommentException extends JavadocParseException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.
     * @param location the location
     * @param cause the original cause
     */
    public IllegalEndOfCommentException(IrLocation location, Throwable cause) {
        super(buildMessage(), location, cause);
    }

    private static String buildMessage() {
        return "Unexpected end of comment (*/)";
    }
}
