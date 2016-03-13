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
package com.asakusafw.testdriver.compiler;

import java.util.Objects;

/**
 * Represents a token in command.
 * @since 0.8.0
 */
public class CommandToken {

    /**
     * Represents a token of batch ID.
     */
    public static final CommandToken BATCH_ID = new CommandToken(TokenKind.BATCH_ID);

    /**
     * Represents a token of flow ID.
     */
    public static final CommandToken FLOW_ID = new CommandToken(TokenKind.FLOW_ID);

    /**
     * Represents a token of execution ID.
     */
    public static final CommandToken EXECUTION_ID = new CommandToken(TokenKind.EXECUTION_ID);

    /**
     * Represents a token of batch arguments.
     */
    public static final CommandToken BATCH_ARGUMENTS = new CommandToken(TokenKind.BATCH_ARGUMENTS);

    private final TokenKind tokenKind;

    private final String image;

    /**
     * Creates a new instance.
     * @param image the token image
     * @see #BATCH_ID
     * @see #FLOW_ID
     * @see #EXECUTION_ID
     * @see #BATCH_ARGUMENTS
     */
    public CommandToken(String image) {
        this.tokenKind = TokenKind.TEXT;
        this.image = image;
    }

    private CommandToken(TokenKind tokenKind) {
        this.tokenKind = tokenKind;
        this.image = null;
    }

    /**
     * Returns a plain text command token.
     * @param image the token image
     * @return the created instance
     */
    public static CommandToken of(String image) {
        return new CommandToken(image);
    }

    /**
     * Returns the token kind.
     * @return the token kind
     */
    public TokenKind getTokenKind() {
        return tokenKind;
    }

    /**
     * Returns the token image (for only text tokens).
     * @return the token image, or {@code null} if this token does not represent a text token
     */
    public String getImage() {
        return image;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(tokenKind);
        result = prime * result + Objects.hashCode(image);
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
        CommandToken other = (CommandToken) obj;
        if (!Objects.equals(tokenKind, other.tokenKind)) {
            return false;
        }
        if (!Objects.equals(image, other.image)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (image == null) {
            return String.format("$%s", tokenKind); //$NON-NLS-1$
        } else {
            return String.format("'%s'", image); //$NON-NLS-1$
        }
    }

    /**
     * Represents a token.
     */
    @SuppressWarnings("hiding")
    public static enum TokenKind {

        /**
         * immediate text.
         */
        TEXT,

        /**
         * batch ID.
         */
        BATCH_ID,

        /**
         * flow ID.
         */
        FLOW_ID,

        /**
         * execution ID.
         */
        EXECUTION_ID,

        /**
         * serialized batch arguments.
         */
        BATCH_ARGUMENTS,
    }
}
