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
package com.asakusafw.utils.java.parser.javadoc;

import static com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocElementKind.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBlock;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocFragment;

/**
 * Test for {@link DefaultJavadocBlockParser}.
 */
public class DefaultJavadocBlockParserTest extends JavadocTestRoot {

    /**
     * Test method for {@link DefaultJavadocBlockParser#DefaultJavadocBlockParser()}.
     */
    @Test
    public void testDefaultJavadocBlockParser() {
        DefaultJavadocBlockParser parser = new DefaultJavadocBlockParser();
        assertEquals(0, parser.getBlockParsers().size());
    }

    /**
     * Test method for {@link DefaultJavadocBlockParser#DefaultJavadocBlockParser(java.util.List)}.
     */
    @Test
    public void testDefaultJavadocBlockParserInlines() {
        MockJavadocBlockParser m1 = new MockJavadocBlockParser();
        MockJavadocBlockParser m2 = new MockJavadocBlockParser();
        MockJavadocBlockParser m3 = new MockJavadocBlockParser();

        DefaultJavadocBlockParser parser = new DefaultJavadocBlockParser(Arrays.asList(m1, m2, m3));
        List<? extends JavadocBlockParser> parsers = parser.getBlockParsers();
        assertEquals(3, parsers.size());
        assertSame(m1, parsers.get(0));
        assertSame(m2, parsers.get(1));
        assertSame(m3, parsers.get(2));
    }

    /**
     * Test method for {@link DefaultJavadocBlockParser#canAccept(java.lang.String)}.
     */
    @Test
    public void testCanAccept() {
        DefaultJavadocBlockParser parser = new DefaultJavadocBlockParser();
        assertTrue(parser.canAccept(null));
        assertTrue(parser.canAccept(""));
        assertTrue(parser.canAccept("param"));
        assertTrue(parser.canAccept("code"));
    }

    /**
     * Test method for {@link DefaultJavadocBlockParser#parse(java.lang.String, JavadocScanner)}.
     * @throws Exception If occurred
     */
    @Test
    public void testParse() throws Exception {
        MockJavadocBlockParser i1 = new MockJavadocBlockParser();
        i1.setIdentifier("i1");
        i1.setAcceptable(Pattern.compile("a"));

        DefaultJavadocBlockParser parser = new DefaultJavadocBlockParser(Arrays.asList(i1));
        {
            IrDocBlock block = parser.parse(null, string(" Hello, world!"));
            assertNull(block.getTag());
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, TEXT);
        }
        {
            IrDocBlock block = parser.parse(null, string("{@a sample}"));
            assertNull(block.getTag());
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, BLOCK);
            assertMockBlockEquals(i1, "@a", fragments.get(0));
        }
        {
            IrDocBlock block = parser.parse(null, string("Hello, {@a THIS} world!"));
            assertNull(block.getTag());
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, TEXT, BLOCK, TEXT);
            assertTextEquals("Hello, ", fragments.get(0));
            assertMockBlockEquals(i1, "@a", fragments.get(1));
            assertTextEquals(" world!", fragments.get(2));
        }
        {
            try {
                parser.parse(null, string("Hello, {@b THIS} world!"));
                fail();
            } catch (MissingJavadocBlockParserException e) {
                assertEquals("b", e.getTagName());
            }
        }
    }
}
