/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
 * An exception which represents a comment is malformed.
 */
public class IllegalDocCommentFormatException extends JavadocParseException {

    private static final long serialVersionUID = 1L;

    private final boolean head;

    /**
     * Creates a new instance.
     * @param head {@code true} if missing comment header, or {@code false} if missing comment footer
     * @param location the location
     * @param cause the exception cause
     */
    public IllegalDocCommentFormatException(boolean head, IrLocation location, Throwable cause) {
        super(buildMessage(head), location, cause);
        this.head = head;
    }

    /**
     * Returns whether this is occurred by missing comment header or not.
     * @return {@code true} if this is occurred by missing comment header, otherwise {@code false}
     * @see #isMissingTail()
     */
    public boolean isMissingHead() {
        return head;
    }

    /**
     * Returns whether this is occurred by missing comment footer or not.
     * @return {@code true} if this is occurred by missing comment footer, otherwise {@code false}
     */
    public boolean isMissingTail() {
        return !isMissingHead();
    }

    private static String buildMessage(boolean head) {
        if (head) {
            return Messages.getString("IllegalDocCommentFormatException.messageMissingHeader"); //$NON-NLS-1$
        } else {
            return Messages.getString("IllegalDocCommentFormatException.messageMissingFooter"); //$NON-NLS-1$
        }
    }
}
