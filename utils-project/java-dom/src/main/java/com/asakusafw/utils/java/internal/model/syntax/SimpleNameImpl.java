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
 * An implementation of {@link SimpleName}.
 */
public final class SimpleNameImpl extends ModelRoot implements SimpleName {

    private static final Set<String> RESERVED;
    static {
        // see http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.8
        Set<String> set = new HashSet<>();
        set.add("abstract"); //$NON-NLS-1$
        set.add("continue"); //$NON-NLS-1$
        set.add("for"); //$NON-NLS-1$
        set.add("new"); //$NON-NLS-1$
        set.add("switch"); //$NON-NLS-1$
        set.add("assert"); //$NON-NLS-1$
        set.add("default"); //$NON-NLS-1$
        set.add("if"); //$NON-NLS-1$
        set.add("package"); //$NON-NLS-1$
        set.add("synchronized"); //$NON-NLS-1$
        set.add("boolean"); //$NON-NLS-1$
        set.add("do"); //$NON-NLS-1$
        set.add("goto"); //$NON-NLS-1$
        set.add("private"); //$NON-NLS-1$
        set.add("this"); //$NON-NLS-1$
        set.add("break"); //$NON-NLS-1$
        set.add("double"); //$NON-NLS-1$
        set.add("implements"); //$NON-NLS-1$
        set.add("protected"); //$NON-NLS-1$
        set.add("throw"); //$NON-NLS-1$
        set.add("byte"); //$NON-NLS-1$
        set.add("else"); //$NON-NLS-1$
        set.add("import"); //$NON-NLS-1$
        set.add("public"); //$NON-NLS-1$
        set.add("throws"); //$NON-NLS-1$
        set.add("case"); //$NON-NLS-1$
        set.add("enum"); //$NON-NLS-1$
        set.add("instanceof"); //$NON-NLS-1$
        set.add("return"); //$NON-NLS-1$
        set.add("transient"); //$NON-NLS-1$
        set.add("catch"); //$NON-NLS-1$
        set.add("extends"); //$NON-NLS-1$
        set.add("int"); //$NON-NLS-1$
        set.add("short"); //$NON-NLS-1$
        set.add("try"); //$NON-NLS-1$
        set.add("char"); //$NON-NLS-1$
        set.add("final"); //$NON-NLS-1$
        set.add("interface"); //$NON-NLS-1$
        set.add("static"); //$NON-NLS-1$
        set.add("void"); //$NON-NLS-1$
        set.add("class"); //$NON-NLS-1$
        set.add("finally"); //$NON-NLS-1$
        set.add("long"); //$NON-NLS-1$
        set.add("strictfp"); //$NON-NLS-1$
        set.add("volatile"); //$NON-NLS-1$
        set.add("const"); //$NON-NLS-1$
        set.add("float"); //$NON-NLS-1$
        set.add("native"); //$NON-NLS-1$
        set.add("super"); //$NON-NLS-1$
        set.add("while"); //$NON-NLS-1$
        set.add("true"); //$NON-NLS-1$
        set.add("false"); //$NON-NLS-1$
        set.add("null"); //$NON-NLS-1$
        RESERVED = Collections.unmodifiableSet(set);
    }

    private String token;

    @Override
    public String getToken() {
        return this.token;
    }

    /**
     * Sets the identifier.
     * @param token the identifier
     * @throws IllegalArgumentException if {@code string} was {@code null}
     * @throws IllegalArgumentException if {@code string} was empty
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
        return Collections.singletonList(this);
    }

    @Override
    public String toNameString() {
        return getToken();
    }

    /**
     * Returns {@link ModelKind#SIMPLE_NAME} which represents this element kind.
     * @return {@link ModelKind#SIMPLE_NAME}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.SIMPLE_NAME;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitSimpleName(this, context);
    }
}
