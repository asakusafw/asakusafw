/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.asakusafw.utils.java.internal.model.util.LiteralAnalyzer;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link SimpleName}の実装。
 */
public final class SimpleNameImpl extends ModelRoot implements SimpleName {

    private static final Set<String> RESERVED;
    static {
        // see http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.8
        Set<String> set = new HashSet<String>();
        set.add("abstract");
        set.add("continue");
        set.add("for");
        set.add("new");
        set.add("switch");
        set.add("assert");
        set.add("default");
        set.add("if");
        set.add("package");
        set.add("synchronized");
        set.add("boolean");
        set.add("do");
        set.add("goto");
        set.add("private");
        set.add("this");
        set.add("break");
        set.add("double");
        set.add("implements");
        set.add("protected");
        set.add("throw");
        set.add("byte");
        set.add("else");
        set.add("import");
        set.add("public");
        set.add("throws");
        set.add("case");
        set.add("enum");
        set.add("instanceof");
        set.add("return");
        set.add("transient");
        set.add("catch");
        set.add("extends");
        set.add("int");
        set.add("short");
        set.add("try");
        set.add("char");
        set.add("final");
        set.add("interface");
        set.add("static");
        set.add("void");
        set.add("class");
        set.add("finally");
        set.add("long");
        set.add("strictfp");
        set.add("volatile");
        set.add("const");
        set.add("float");
        set.add("native");
        set.add("super");
        set.add("while");
        set.add("true");
        set.add("false");
        set.add("null");
        RESERVED = Collections.unmodifiableSet(set);
    }

    /**
     * この単純名を表現する文字列。
     */
    private String token;

    @Override
    public String getToken() {
        return this.token;
    }

    /**
     * この単純名を表現する文字列を設定する。
     * @param token
     *     この単純名を表現する文字列
     * @throws IllegalArgumentException
     *     {@code string}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code string}に空が指定された場合
     */
    public void setToken(String token) {
        Util.notNull(token, "token"); //$NON-NLS-1$
        if (token.isEmpty()) {
            throw new IllegalArgumentException("token must not be null"); //$NON-NLS-1$
        }
        if (Character.isJavaIdentifierStart(token.charAt(0)) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "string must be a valid Java identifier ({0} has invalid start)",
                    LiteralAnalyzer.stringLiteralOf(token)));
        }
        for (int i = 1, n = token.length(); i < n; i++) {
            if (Character.isJavaIdentifierPart(token.charAt(i)) == false) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "string must be a valid Java identifier ({0} has invalid part)",
                        LiteralAnalyzer.stringLiteralOf(token)));
            }
        }
        if (RESERVED.contains(token)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "string must be a valid Java identifier ({0} is a reserved word)",
                    LiteralAnalyzer.stringLiteralOf(token)));
        }
        this.token = token;
    }

    @Override
    public SimpleName getLastSegment() {
        return this;
    }

    @Override
    public List<SimpleName> toNameList() {
        return Collections.<SimpleName>singletonList(this);
    }

    @Override
    public String toNameString() {
        return getToken();
    }

    /**
     * この要素の種類を表す{@link ModelKind#SIMPLE_NAME}を返す。
     * @return {@link ModelKind#SIMPLE_NAME}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.SIMPLE_NAME;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitSimpleName(this, context);
    }
}
