/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBlock;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocFragment;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocSimpleName;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocText;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrLocation;

/**
 * Test for {@link JavadocBlockParser}.
 */
public class JavadocBlockParserTest extends JavadocTestRoot {

    /**
     * Test method for {@link JavadocBlockParser#newBlock(java.lang.String, java.util.List)}.
     */
    @Test
    public void testNewBlock() {
        MockJavadocBlockParser parser = new MockJavadocBlockParser();
        {
            IrDocBlock block = parser.newBlock(null, Collections.emptyList());
            assertNull(block.getTag());
            assertEquals(0, block.getFragments().size());
        }
        {
            IrDocText f0 = new IrDocText("Hello, world!");
            IrDocBlock block = parser.newBlock("code", Arrays.asList(f0));
            assertEquals("@code", block.getTag());
            assertEquals(1, block.getFragments().size());
            assertEquals(f0, block.getFragments().get(0));
        }
        {
            IrDocSimpleName f0 = new IrDocSimpleName("arg0");
            IrDocText f1 = new IrDocText("Hello!");
            IrDocText f2 = new IrDocText("This is text");
            IrDocBlock block = parser.newBlock("param", Arrays.asList(f0, f1, f2));
            assertEquals("@param", block.getTag());
            assertEquals(3, block.getFragments().size());
            assertEquals(f0, block.getFragments().get(0));
            assertEquals(f1, block.getFragments().get(1));
            assertEquals(f2, block.getFragments().get(2));
        }
    }

    /**
     * Test method for {@link JavadocBlockParser#parseBlock(JavadocBlockInfo)}.
     * @throws Exception If occurred
     */
    @Test
    public void testParseBlock() throws Exception {
        MockJavadocBlockParser i1 = new MockJavadocBlockParser();
        i1.setAcceptable(Pattern.compile("a"));
        i1.setIdentifier("i1");
        MockJavadocBlockParser i2 = new MockJavadocBlockParser();
        i2.setAcceptable(Pattern.compile("b"));
        i2.setIdentifier("i2");
        MockJavadocBlockParser i3 = new MockJavadocBlockParser();
        i3.setAcceptable(Pattern.compile("c|d"));
        i3.setIdentifier("i3");
        MockJavadocBlockParser i4 = new MockJavadocBlockParser();
        i4.setAcceptable(Pattern.compile("<SYNOPSIS>"));
        i4.setIdentifier("i4");

        MockJavadocBlockParser parser = new MockJavadocBlockParser(i1, i2, i3, i4);
        {
            JavadocBlockInfo block = new JavadocBlockInfo("a", string(""), new IrLocation(0, 1));
            IrDocBlock parsed = parser.parseBlock(block);
            assertEquals(new IrLocation(0, 1), parsed.getLocation());
            List<? extends IrDocFragment> fragments = parsed.getFragments();
            assertKinds(fragments, TEXT);
            assertTextEquals("i1", fragments.get(0));
        }
        {
            JavadocBlockInfo block = new JavadocBlockInfo("b", string(""), new IrLocation(0, 1));
            IrDocBlock parsed = parser.parseBlock(block);
            assertEquals(new IrLocation(0, 1), parsed.getLocation());
            List<? extends IrDocFragment> fragments = parsed.getFragments();
            assertKinds(fragments, TEXT);
            assertTextEquals("i2", fragments.get(0));
        }
        {
            JavadocBlockInfo block = new JavadocBlockInfo("c", string(""), new IrLocation(0, 1));
            IrDocBlock parsed = parser.parseBlock(block);
            assertEquals(new IrLocation(0, 1), parsed.getLocation());
            List<? extends IrDocFragment> fragments = parsed.getFragments();
            assertKinds(fragments, TEXT);
            assertTextEquals("i3", fragments.get(0));
        }
        {
            JavadocBlockInfo block = new JavadocBlockInfo("d", string(""), new IrLocation(0, 1));
            IrDocBlock parsed = parser.parseBlock(block);
            assertEquals(new IrLocation(0, 1), parsed.getLocation());
            List<? extends IrDocFragment> fragments = parsed.getFragments();
            assertKinds(fragments, TEXT);
            assertTextEquals("i3", fragments.get(0));
        }
        {
            JavadocBlockInfo block = new JavadocBlockInfo("e", string(""), new IrLocation(0, 1));
            try {
                parser.parseBlock(block);
                fail();
            } catch (MissingJavadocBlockParserException e) {
                // ok.
                assertEquals("e", e.getTagName());
            }
        }
        {
            JavadocBlockInfo block = new JavadocBlockInfo(null, string(""), new IrLocation(0, 1));
            IrDocBlock parsed = parser.parseBlock(block);
            assertEquals(new IrLocation(0, 1), parsed.getLocation());
            List<? extends IrDocFragment> fragments = parsed.getFragments();
            assertKinds(fragments, TEXT);
            assertTextEquals("i4", fragments.get(0));
        }
    }

    /**
     * Test method for {@link JavadocBlockParser#fetchRestFragments(JavadocScanner)}.
     * @throws Exception If occurred
     */
    @Test
    public void testFetchRestFragments() throws Exception {
        MockJavadocBlockParser inline = new MockJavadocBlockParser();
        inline.setIdentifier("INLINE");
        MockJavadocBlockParser parser = new MockJavadocBlockParser(inline);
        {
            DefaultJavadocScanner scanner = string("");
            List<IrDocFragment> fragments = parser.fetchRestFragments(scanner);
            assertEquals(0, fragments.size());
        }
        {
            DefaultJavadocScanner scanner = string("Single line");
            List<IrDocFragment> fragments = parser.fetchRestFragments(scanner);
            assertKinds(fragments, TEXT);
            assertTextEquals("Single line", fragments.get(0));
        }
        {
            DefaultJavadocScanner scanner = string(
                "Multi\n"
                + "Lines");
            List<IrDocFragment> fragments = parser.fetchRestFragments(scanner);
            assertKinds(fragments, TEXT, TEXT);
            assertTextEquals("Multi", fragments.get(0));
            assertTextEquals("Lines", fragments.get(1));
        }
        {
            DefaultJavadocScanner scanner = string(
                "Formatted\n"
                + "     * Text\n"
                + "     * \n"
                + "     * Sequence\n");
            List<IrDocFragment> fragments = parser.fetchRestFragments(scanner);
            assertKinds(fragments, TEXT, TEXT, TEXT);
            assertTextEquals("Formatted", fragments.get(0));
            assertTextEquals("Text", fragments.get(1));
            assertTextEquals("Sequence", fragments.get(2));
        }
        {
            DefaultJavadocScanner scanner = string("{@code hello}");
            List<IrDocFragment> fragments = parser.fetchRestFragments(scanner);
            assertKinds(fragments, BLOCK);
            {
                IrDocBlock block = (IrDocBlock) fragments.get(0);
                assertEquals(0, block.getLocation().getStartPosition());
                assertEquals("{@code hello}".length(), block.getLocation().getLength());
                assertEquals("@code", block.getTag());
                assertMockBlockEquals(inline, "@code", block);
            }
        }
        {
            DefaultJavadocScanner scanner = string("{@code hello}{@code world!}");
            List<IrDocFragment> fragments = parser.fetchRestFragments(scanner);
            assertKinds(fragments, BLOCK, BLOCK);
            {
                IrDocBlock block = (IrDocBlock) fragments.get(0);
                assertEquals(0, block.getLocation().getStartPosition());
                assertEquals("{@code hello}".length(), block.getLocation().getLength());
                assertEquals("@code", block.getTag());
                assertMockBlockEquals(inline, "@code", block);
            }
            {
                IrDocBlock block = (IrDocBlock) fragments.get(1);
                assertEquals("{@code hello}".length(), block.getLocation().getStartPosition());
                assertEquals("{@code world!}".length(), block.getLocation().getLength());
                assertMockBlockEquals(inline, "@code", block);
            }
        }
        {
            DefaultJavadocScanner scanner = string(
                "This returns {@code null} {@code value}\n"
                + "    * {@link Object}");
            List<IrDocFragment> fragments = parser.fetchRestFragments(scanner);
            assertKinds(fragments, TEXT, BLOCK, TEXT, BLOCK, BLOCK);
            assertTextEquals("This returns ", fragments.get(0));
            assertMockBlockEquals(inline, "@code", fragments.get(1));
            assertTextEquals(" ", fragments.get(2));
            assertMockBlockEquals(inline, "@code", fragments.get(3));
            assertMockBlockEquals(inline, "@link", fragments.get(4));
        }
    }
}
