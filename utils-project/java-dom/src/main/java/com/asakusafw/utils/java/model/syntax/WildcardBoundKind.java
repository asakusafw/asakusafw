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
package com.asakusafw.utils.java.model.syntax;

/**
 * Represents a kind of wildcard type bounds.
 */
public enum WildcardBoundKind {

    /**
     * Unbound.
     */
    UNBOUNDED(""), //$NON-NLS-1$

    /**
     * With upper bound types.
     */
    UPPER_BOUNDED("extends"), //$NON-NLS-1$

    /**
     * With lower bound types.
     */
    LOWER_BOUNDED("super"), //$NON-NLS-1$
    ;

    private final String representation;

    /**
     * Creates a new instance.
     * @param representation the keyword
     */
    private WildcardBoundKind(String representation) {
        assert representation != null;
        this.representation = representation;
    }

    /**
     * Returns the keyword.
     * @return the keyword, or an empty string for unbound kind
     */
    public String getRepresentation() {
        return representation;
    }

    /**
     * Normalizes this bound kind.
     * Returns {@link #UPPER_BOUNDED} if this kind is {@link #UNBOUNDED}, or otherwise returns itself.
     * @return the normalized kind
     */
    public WildcardBoundKind normalize() {
        return this == UNBOUNDED ? UPPER_BOUNDED : this;
    }
}
