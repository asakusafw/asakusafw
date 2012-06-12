/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.utils.java.internal.model.syntax;

import java.text.MessageFormat;

import com.asakusafw.utils.java.internal.model.util.LiteralAnalyzer;
import com.asakusafw.utils.java.internal.model.util.LiteralToken;
import com.asakusafw.utils.java.model.syntax.Literal;
import com.asakusafw.utils.java.model.syntax.LiteralKind;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link Literal}の実装。
 */
public final class LiteralImpl extends ModelRoot implements Literal {

    /**
     * このリテラルを構成する字句。
     */
    private String token;

    private LiteralKind literalKind;

    @Override
    public String getToken() {
        return this.token;
    }

    /**
     * このリテラルを構成する字句を設定する。
     * @param token
     *     このリテラルを構成する字句
     * @throws IllegalArgumentException
     *     {@code token}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code token}に空が指定された場合
     */
    public void setToken(String token) {
        Util.notNull(token, "token"); //$NON-NLS-1$
        LiteralKind kind = computeLiteralKind(token);
        if (kind == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                "Invalid literal token: {0}", //$NON-NLS-1$
                LiteralAnalyzer.stringLiteralOf(token)));
        }
        this.token = token;
        this.literalKind = kind;
    }

    private static LiteralKind computeLiteralKind(String tokenString) {
        LiteralToken token = LiteralAnalyzer.parse(tokenString);
        switch (token.getKind()) {
        case INT: return LiteralKind.INT;
        case LONG: return LiteralKind.LONG;
        case FLOAT: return LiteralKind.FLOAT;
        case DOUBLE: return LiteralKind.DOUBLE;
        case CHAR: return LiteralKind.CHAR;
        case STRING: return LiteralKind.STRING;
        case BOOLEAN: return LiteralKind.BOOLEAN;
        case NULL: return LiteralKind.NULL;
        default:
            return null;
        }
    }

    @Override
    public LiteralKind getLiteralKind() {
        return literalKind;
    }

    /**
     * この要素の種類を表す{@link ModelKind#LITERAL}を返す。
     * @return {@link ModelKind#LITERAL}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.LITERAL;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitLiteral(this, context);
    }
}
