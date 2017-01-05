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
package com.asakusafw.dmdl.model;

import java.text.MessageFormat;

import com.asakusafw.dmdl.Region;

/**
 * Represents description of definitions.
 * @since 0.2.0
 */
public class AstDescription extends AbstractAstNode {

    private final Region region;

    /**
     * The original token of this description.
     */
    public final String token;

    /**
     * Creates and returns a new instance.
     * @param region the region of this node on the enclosing script, or {@code null} if unknown
     * @param token the token that represents this description
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AstDescription(Region region, String token) {
        if (token == null) {
            throw new IllegalArgumentException("token must not be null"); //$NON-NLS-1$
        }
        this.region = region;
        this.token = token;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    /**
     * Returns the string value of this description.
     * @return the string value
     * @throws IllegalStateException if this is not a quated string
     */
    public String getText() {
        if (token.length() >= 2 && token.startsWith("\"") && token.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
            return EscapeDecoder.scan(token.substring(1, token.length() - 1));
        }
        throw new IllegalStateException(MessageFormat.format(
                "Invalid string value: {0}", //$NON-NLS-1$
                token));
    }

    @Override
    public <C, R> R accept(C context, AstNode.Visitor<C, R> visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor must not be null"); //$NON-NLS-1$
        }
        R result = visitor.visitDescription(context, this);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + token.hashCode();
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
        AstDescription other = (AstDescription) obj;
        if (!token.equals(other.token)) {
            return false;
        }
        return true;
    }
}
