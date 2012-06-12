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
package com.asakusafw.utils.java.parser.javadoc;

import static com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocElementKind.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import com.asakusafw.utils.java.internal.model.util.JavaEscape;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBlock;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocElement;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocElementKind;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocFragment;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocText;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocToken;

/**
 * Javadoc関連のテスト。
 */
public class JavadocTestRoot {

    /**
     * 指定の名前のリソースをUTF-8テキストとして読み出し、内容を返す。
     * すべての改行文字は、単一の{@code U+000a}に置換される。
     * @param name リソースの名前
     * @return リソースの内容
     */
    public static String load(String name) {
        InputStream in = JavadocTestRoot.class.getResourceAsStream(name);
        Assert.assertNotNull(name, in);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[256];
            while (true) {
                int read = in.read(buf);
                if (read == -1) {
                    break;
                }
                out.write(buf, 0, read);
            }
            String content = new String(out.toByteArray(), "UTF-8"); //$NON-NLS-1$
            content = content.replaceAll("\r\n|\r", "\n");
            return content;
        }
        catch(IOException e) {
            throw new AssertionError(e);
        }
        finally {
            try {
                in.close();
            }
            catch (IOException e) {
                throw new AssertionError(e);
            }
        }
    }

    /**
     * 指定の文字列をトーカナイズして返す。
     * @param text 対象の文字列
     * @return トークンの列
     */
    public static DefaultJavadocScanner string(String text) {
        Assert.assertNotNull(text);
        return DefaultJavadocScanner.newInstance(text);
    }

    /**
     * 指定の名前のリソースをUTF-8テキストとして読み出し、トーカナイズして返す。
     * @param name リソースの名前
     * @return リソースの内容
     */
    public static DefaultJavadocScanner scanner(String name) {
        String resource = load(name);
        return string(resource);
    }

    /**
     * 指定の名前のリソースをUTF-8テキストとして読み出し、トークン列を返す。
     * @param name リソースの名前
     * @return リソースの内容
     */
    public static DefaultJavadocTokenStream stream(String name) {
        return new DefaultJavadocTokenStream(scanner(name));
    }

    /**
     * 指定のトークン列であることを表明する。
     * @param tokens 対象のトークン列
     * @param expected 表明する正解
     */
    public static void assertTextSequence(List<? extends JavadocToken> tokens, String...expected) {
        if (tokens.size() != expected.length) {
            Assert.fail(MessageFormat.format(
                "count: {0} {1}", //$NON-NLS-1$
                format(expected),
                format(tokens)));
        }
        for (int i = 0, n = tokens.size(); i < n; i++) {
            if (!tokens.get(i).getText().equals(expected[i])) {
                Assert.fail(MessageFormat.format(
                    "{0}: {1} {2}", //$NON-NLS-1$
                    i,
                    format(expected[i]),
                    format(tokens.get(i).getText())));
            }
        }
    }

    /**
     * 指定のトークン列を連結させた文字列を返す。
     * @param tokens 対象のトークン列
     * @return 連結させた文字列
     */
    public static String toString(List<? extends JavadocToken> tokens) {
        StringBuilder buf = new StringBuilder();
        for (JavadocToken t: tokens) {
            buf.append(t.getText());
        }
        return buf.toString();
    }

    /**
     * 指定の種類の要素列であることを表明する。
     * @param elements 対象の要素列
     * @param expected 表明する正解
     */
    public static void assertKinds(List<? extends IrDocElement> elements, IrDocElementKind...expected) {
        if (elements.size() != expected.length) {
            Assert.fail(MessageFormat.format(
                "count: {0} {1}", //$NON-NLS-1$
                Arrays.deepToString(expected),
                elements));
        }
        for (int i = 0, n = elements.size(); i < n; i++) {
            if (!elements.get(i).getKind().equals(expected[i])) {
                Assert.fail(MessageFormat.format(
                    "{0}: {1} {2}", //$NON-NLS-1$
                    i,
                    expected[i],
                    elements.get(i)));
            }
        }
    }

    /**
     * 指定の断片がテキストで、かつ指定の内容を保持していることを表明する。
     * @param content 表明するテキスト
     * @param fragment 対象の断片
     */
    public static void assertTextEquals(String content, IrDocFragment fragment) {
        Assert.assertEquals(IrDocElementKind.TEXT, fragment.getKind());
        Assert.assertEquals(content, ((IrDocText) fragment).getContent());
    }

    /**
     * 指定の断片がブロックで、かつ指定の内容を保持していることを表明する。
     * @param parser このブロックを解析したパーサ
     * @param tag タグ名
     * @param fragment 対象の断片
     */
    public static void assertMockBlockEquals(MockJavadocBlockParser parser, String tag, IrDocFragment fragment) {
        assertEquals(BLOCK, fragment.getKind());
        IrDocBlock block = (IrDocBlock) fragment;
        assertEquals(tag, block.getTag());
        List<? extends IrDocFragment> inlines = block.getFragments();
        assertKinds(inlines, TEXT);
        assertEquals(parser.getIdentifier(), ((IrDocText) inlines.get(0)).getContent());
    }

    private static String format(String string) {
        return '"' + JavaEscape.escape(string, false, false) + '"';
    }

    private static List<String> format(String...list) {
        List<String> formatted = new ArrayList<String>(list.length);
        for (String s: list) {
            formatted.add(format(s));
        }
        return formatted;
    }

    private static List<String> format(List<? extends JavadocToken> list) {
        List<String> formatted = new ArrayList<String>(list.size());
        for (JavadocToken t: list) {
            formatted.add(format(t.getText()));
        }
        return formatted;
    }
}
