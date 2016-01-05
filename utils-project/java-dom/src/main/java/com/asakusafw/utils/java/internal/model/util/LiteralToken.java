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
package com.asakusafw.utils.java.internal.model.util;

import java.io.Serializable;

/**
 * Represents a Java literal token.
 */
public class LiteralToken implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * {@value #TOKEN_TRUE}.
     */
    public static final String TOKEN_TRUE = "true"; //$NON-NLS-1$

    /**
     * {@value #TOKEN_FALSE}.
     */
    public static final String TOKEN_FALSE = "false"; //$NON-NLS-1$

    /**
     * {@value #TOKEN_NULL}.
     */
    public static final String TOKEN_NULL = "null"; //$NON-NLS-1$

    private final String text;

    private transient LiteralTokenKind kind;

    private transient Object value;

    /**
     * Creates a new instance.
     * @param text the token image
     * @param kind the literal kind
     * @param value the literal value, or {@code null} only if the literal kind also represents {@code null}
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    LiteralToken(String text, LiteralTokenKind kind, Object value) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null"); //$NON-NLS-1$
        }
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null"); //$NON-NLS-1$
        }
        if (value == null && kind != LiteralTokenKind.NULL) {
            throw new IllegalArgumentException("value must not be null"); //$NON-NLS-1$
        }
        this.text = text.intern();
        this.kind = kind;
        this.value = value;
    }

    /**
     * Returns the literal kind.
     * @return the literal kind
     */
    public LiteralTokenKind getKind() {
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
     * Returns the value which this literal represent to.
     * If this literal is primitive value, this method will return its wrapper object.
     * @return the literal value, or {@link LiteralTokenKind#UNKNOWN}
     */
    public Object getValue() {
        return this.value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + text.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LiteralToken other = (LiteralToken) obj;
        if (text.equals(other.text) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return text;
    }

    private Object readResolve() {
        return LiteralAnalyzer.parse(text);
    }
}
