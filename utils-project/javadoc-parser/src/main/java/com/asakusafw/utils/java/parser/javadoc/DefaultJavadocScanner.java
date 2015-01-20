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
package com.asakusafw.utils.java.parser.javadoc;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.asakusafw.utils.java.internal.model.util.JavaEscape;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocToken;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocTokenKind;

/**
 * {@link JavadocToken}を取り扱うスキャナ。
 */
public class DefaultJavadocScanner implements JavadocScanner {

    private int index;
    private List<JavadocToken> tokens;
    private JavadocToken eof;

    /**
     * インスタンスを生成する。
     * @param tokens トークンの一覧
     * @param successorStartsAt 後続する文字の位置
     */
    public DefaultJavadocScanner(List<JavadocToken> tokens, int successorStartsAt) {
        super();
        this.index = 0;
        this.tokens = tokens;
        this.eof = eof(successorStartsAt);
    }

    /**
     * インスタンスを生成する。
     * @param text 対象の文字列
     * @return 生成したインスタンス
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static DefaultJavadocScanner newInstance(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text"); //$NON-NLS-1$
        }
        JavadocTokenizer tokenizer = new JavadocTokenizer(new StringReader(text));
        try {
            while (tokenizer.yylex() != -1) {
                // do nothing
            }
        } catch (IOException e) {
            throw (AssertionError) new AssertionError(tokenizer.getStore()).initCause(e);
        }
        ArrayList<JavadocToken> list = new ArrayList<JavadocToken>(tokenizer.getStore());
        list.trimToSize();
        return new DefaultJavadocScanner(list, text.length());
    }

    private static JavadocToken eof(int offset) {
        return new JavadocToken(JavadocTokenKind.EOF, "", offset); //$NON-NLS-1$
    }

    @Override
    public List<JavadocToken> getTokens() {
        return Collections.unmodifiableList(tokens);
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public void seek(int position) {
        setIndex(position);
    }

    @Override
    public void consume(int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        setIndex(index + count);
    }

    @Override
    public JavadocToken nextToken() {
        int position = lookahead0(0);
        JavadocToken token = token(position);
        setIndex(position + 1);
        return token;
    }

    @Override
    public JavadocToken lookahead(int offset) {
        int position = lookahead0(offset);
        JavadocToken token = token(position);
        return token;
    }

    private void setIndex(int position) {
        if (position > tokens.size()) {
            index = tokens.size();
        } else {
            index = position;
        }
    }

    private int lookahead0(int i) {
        int pos = index + i;
        checkPositive(index, i);
        int size = tokens.size();
        if (pos >= size) {
            return size;
        } else {
            return pos;
        }
    }

    private JavadocToken token(int position) {
        if (position == tokens.size()) {
            return eof;
        } else {
            return tokens.get(position);
        }
    }

    private void checkPositive(int base, int offset) {
        if (base + offset < 0) {
            throw new IllegalArgumentException(MessageFormat.format(
                "{0}@[{1} {2} {3}]", //$NON-NLS-1$
                tokens,
                base,
                offset >= 0 ? '+' : '-',
                offset));
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append('[');
        for (int i = 0, n = Math.min(index, tokens.size()); i < n; i++) {
            buf.append('"');
            buf.append(escape(tokens.get(i).getText()));
            buf.append('"');
            buf.append(", "); //$NON-NLS-1$
        }
        buf.append('>');
        for (int i = index, n = tokens.size(); i < n; i++) {
            buf.append('"');
            buf.append(escape(tokens.get(i).getText()));
            buf.append('"');
            buf.append(", "); //$NON-NLS-1$
        }
        int len = buf.length();
        if (index != tokens.size() && len >= 2) {
            buf.delete(len - 2, len);
        }
        buf.append(']');
        return buf.toString();
    }

    private String escape(String s) {
        return JavaEscape.escape(s, false, false);
    }
}
