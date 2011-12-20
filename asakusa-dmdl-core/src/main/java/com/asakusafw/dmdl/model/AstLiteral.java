/**
 * Copyright 2011 Asakusa Framework Team.
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;

import com.asakusafw.dmdl.Region;

/**
 * Represents literals.
 * @since 0.2.0
 */
public class AstLiteral extends AbstractAstNode implements AstAttributeValue {

    private static final char[] ASCII_SPECIAL_ESCAPE = new char[128];
    static {
        ASCII_SPECIAL_ESCAPE['\b'] = 'b';
        ASCII_SPECIAL_ESCAPE['\t'] = 't';
        ASCII_SPECIAL_ESCAPE['\n'] = 'n';
        ASCII_SPECIAL_ESCAPE['\f'] = 'f';
        ASCII_SPECIAL_ESCAPE['\r'] = 'r';
        ASCII_SPECIAL_ESCAPE['\\'] = '\\';
        ASCII_SPECIAL_ESCAPE['\"'] = '\"';
    }

    private final Region region;

    /**
     * The (original) token of this literal.
     */
    public final String token;

    /**
     * The kind of this literal.
     */
    public final LiteralKind kind;

    /**
     * Creates a new instance.
     * @param region the region of this node on the enclosing script, or {@code null} if unknown
     * @param token the token of this literal
     * @param kind the kind of this literal
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AstLiteral(Region region, String token, LiteralKind kind) {
        if (token == null) {
            throw new IllegalArgumentException("token must not be null"); //$NON-NLS-1$
        }
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null"); //$NON-NLS-1$
        }
        this.region = region;
        this.token = token;
        this.kind = kind;
    }

    /**
     * Converts the string as the token of DMDL string literal.
     * @param string target string
     * @return the corresponded DMDL string literal
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static String quote(String string) {
        if (string == null) {
            throw new IllegalArgumentException("string must not be null"); //$NON-NLS-1$
        }
        StringBuilder buf = new StringBuilder();
        buf.append('"');
        for (char c : string.toCharArray()) {
            if (c <= 0x7f && ASCII_SPECIAL_ESCAPE[c] != 0) {
                buf.append('\\');
                buf.append(ASCII_SPECIAL_ESCAPE[c]);
            } else if (Character.isISOControl(c) || Character.isDefined(c) == false) {
                buf.append(String.format("\\u%04x", (int) c)); //$NON-NLS-1$
            } else {
                buf.append(c);
            }
        }
        buf.append('"');
        return buf.toString();
    }

    /**
     * Returns the string value of this literal.
     * @return the string value
     * @throws IllegalStateException if this is not a string literal
     */
    public String toStringValue() {
        checkKind(LiteralKind.STRING);
        if (token.length() >= 2 && token.startsWith("\"") && token.endsWith("\"")) {
            return EscapeDecoder.scan(token.substring(1, token.length() - 1));
        }
        throw new IllegalStateException(MessageFormat.format(
                "Invalid string value: {0}",
                token));
    }

    /**
     * Returns the integer value of this literal.
     * @return the integer value
     * @throws IllegalStateException if this is not a integer literal
     */
    public BigInteger toIntegerValue() {
        checkKind(LiteralKind.INTEGER);
        return new BigInteger(token);
    }

    /**
     * Returns the decimal value of this literal.
     * @return the decimal value
     * @throws IllegalStateException if this is not a decimal literal
     */
    public BigDecimal toDecimalValue() {
        checkKind(LiteralKind.DECIMAL);
        return new BigDecimal(token);
    }

    /**
     * Returns the boolean value of this literal.
     * @return the boolean value
     * @throws IllegalStateException if this is not a boolean literal
     */
    public boolean toBooleanValue() {
        checkKind(LiteralKind.BOOLEAN);
        return token.equals("TRUE");
    }

    private void checkKind(LiteralKind expected) {
        assert expected != null;
        if (kind != expected) {
            throw new IllegalStateException(MessageFormat.format(
                    "Inconsistent literal kind: {0}",
                    token));
        }
    }

    @Override
    public Region getRegion() {
        return region;
    }

    /**
     * Returns the token of this literal.
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * Returns the kind of this literal.
     * @return the literal kind
     */
    public LiteralKind getKind() {
        return kind;
    }

    @Override
    public <C, R> R accept(C context, AstNode.Visitor<C, R> visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor must not be null"); //$NON-NLS-1$
        }
        R result = visitor.visitLiteral(context, this);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + kind.hashCode();
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
        AstLiteral other = (AstLiteral) obj;
        if (kind != other.kind) {
            return false;
        }
        if (!token.equals(other.token)) {
            return false;
        }
        return true;
    }
}
