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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

import java.io.Serializable;

/**
 * Represents a token in Java documentation comments.
 */
public final class JavadocToken implements Serializable {

    private static final long serialVersionUID = 1L;

    private final JavadocTokenKind kind;
    private final String text;
    private final int start;

    /**
     * Creates a new instance.
     * @param kind the token kind
     * @param text the token image
     * @param start the starting position
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public JavadocToken(JavadocTokenKind kind, String text, int start) {
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null"); //$NON-NLS-1$
        }
        if (text == null) {
            throw new IllegalArgumentException("text must not be null"); //$NON-NLS-1$
        }
        this.kind = kind;
        this.text = text;
        this.start = start;
    }

    /**
     * Returns the token kind.
     * @return the token kind
     */
    public JavadocTokenKind getKind() {
        return this.kind;
    }

    /**
     * Returns the token image.
     * @return the token image
     */
    public String getText() {
        return this.text;
    }

    /**
     * Returns the starting position (0-origin).
     * @return the starting position
     */
    public int getStartPosition() {
        return this.start;
    }

    /**
     * Returns the location of this token.
     * @return the location of this token
     */
    public IrLocation getLocation() {
        return new IrLocation(getStartPosition(), getText().length());
    }

    @Override
    public String toString() {
        return getText();
    }
}
