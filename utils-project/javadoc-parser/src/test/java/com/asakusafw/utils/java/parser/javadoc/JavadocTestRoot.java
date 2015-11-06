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
 * Test root for Javadoc parsers.
 */
public class JavadocTestRoot {

    /**
     * Loads the target resource as UTF-8 text, and returns its contents.
     * The line-break characters will be replaced with single {@code U+000a}.
     * @param name the target resource name
     * @return the contents
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
        } catch(IOException e) {
            throw new AssertionError(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
    }

    /**
     * Returns a scanner for the string.
     * @param text the target string
     * @return the scanner
     */
    public static DefaultJavadocScanner string(String text) {
        Assert.assertNotNull(text);
        return DefaultJavadocScanner.newInstance(text);
    }

    /**
     * Returns a scanner for the target resource.
     * @param name the target resource name
     * @return the scanner
     */
    public static DefaultJavadocScanner scanner(String name) {
        String resource = load(name);
        return string(resource);
    }

    /**
     * Returns a token stream for the target resource.
     * @param name the target resource name
     * @return the scanner
     */
    public static DefaultJavadocTokenStream stream(String name) {
        return new DefaultJavadocTokenStream(scanner(name));
    }

    /**
     * Checks the target tokens have the specified images, or raises an error.
     * @param tokens the target tokens
     * @param expected the expected token images
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
     * Concatenates the target token images.
     * @param tokens the target tokens
     * @return the concatenated images
     */
    public static String toString(List<? extends JavadocToken> tokens) {
        StringBuilder buf = new StringBuilder();
        for (JavadocToken t: tokens) {
            buf.append(t.getText());
        }
        return buf.toString();
    }

    /**
     * Checks the target elements are the specified element kinds, or raises an error.
     * @param elements the target elements
     * @param expected the expected element kinds
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
     * Checks the target fragment represents just a text and it have the specified string, or raises an error.
     * @param content the expected text
     * @param fragment the target fragment
     */
    public static void assertTextEquals(String content, IrDocFragment fragment) {
        Assert.assertEquals(IrDocElementKind.TEXT, fragment.getKind());
        Assert.assertEquals(content, ((IrDocText) fragment).getContent());
    }

    /**
     * Checks the target fragment represents a block and it have the specified tag, or raises an error.
     * @param parser the parser which was generated the target fragment
     * @param tag the expected tag
     * @param fragment the target fragment
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
