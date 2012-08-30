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

import java.util.List;

import org.junit.Test;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBlock;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocComment;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocFragment;

/**
 * Test for {@link JavadocParser}.
 */
public class JavadocParserTest extends JavadocTestRoot {

    /**
     * Test method for {@link JavadocParser#parse(JavadocScanner)}.
     * @throws Exception If occurred
     */
    @Test
    public void testParse() throws Exception {
        JavadocParserBuilder builder = new JavadocParserBuilder();
        JavadocParser parser = builder.build();
        {
            DefaultJavadocScanner scanner = string("/***/");
            IrDocComment doc = parser.parse(scanner);
            List<? extends IrDocBlock> blocks = doc.getBlocks();
            assertEquals(0, blocks.size());
        }
        {
            DefaultJavadocScanner scanner = string("/** a */");
            IrDocComment doc = parser.parse(scanner);
            List<? extends IrDocBlock> blocks = doc.getBlocks();
            assertEquals(1, blocks.size());
        }
        {
            DefaultJavadocScanner scanner = string(
                "/**\n"
                + " * a\n"
                + " * @tag\n"
                + " */");
            IrDocComment doc = parser.parse(scanner);
            List<? extends IrDocBlock> blocks = doc.getBlocks();
            assertEquals(2, blocks.size());
            assertNull(blocks.get(0).getTag());
            assertEquals("@tag", blocks.get(1).getTag());
        }
        {
            DefaultJavadocScanner scanner = string(
                "/**\n"
                + " * a\n"
                + " * @tag1\n"
                + " * @tag2\n"
                + " */");
            IrDocComment doc = parser.parse(scanner);
            List<? extends IrDocBlock> blocks = doc.getBlocks();
            assertEquals(3, blocks.size());
            assertNull(blocks.get(0).getTag());
            assertEquals("@tag1", blocks.get(1).getTag());
            assertEquals("@tag2", blocks.get(2).getTag());
        }
        {
            DefaultJavadocScanner scanner = string(
                "/**\n"
                + " * a\n"
                + " * @tag\n"
                + " *");
            try {
                parser.parse(scanner);
                fail();
            } catch (IllegalDocCommentFormatException e) {
                // ok.
            }
        }
        {
            DefaultJavadocScanner scanner = string(
                "**\n"
                + " * a\n"
                + " * @tag\n"
                + " */");
            try {
                parser.parse(scanner);
                fail();
            } catch (IllegalDocCommentFormatException e) {
                // ok.
            }
        }
        {
            DefaultJavadocScanner scanner = string(
                "/*\n"
                + " * a\n"
                + " * @tag\n"
                + " */");
            try {
                parser.parse(scanner);
                fail();
            } catch (IllegalDocCommentFormatException e) {
                // ok.
            }
        }
        {
            DefaultJavadocScanner scanner = string(
                "/**\n"
                + " * a\n"
                + " * @tag\n"
                + " /");
            try {
                parser.parse(scanner);
                fail();
            } catch (IllegalDocCommentFormatException e) {
                // ok.
            }
        }
        {
            DefaultJavadocScanner scanner = string(
                "/**\n"
                + "***a\n"
                + "***@tag\n"
                + "**/");
            parser.parse(scanner);
            // ok
        }
    }

    /**
     * Test method for {@link JavadocParser#parse(JavadocScanner)}.
     * @throws Exception If occurred
     */
    @Test
    public void testParseDetails() throws Exception {
        JavadocParserBuilder builder = new JavadocParserBuilder();
        builder.addSpecialStandAloneBlockParser(new ParamBlockParser());
        builder.addSpecialStandAloneBlockParser(new FollowsNamedTypeBlockParser("throws"));
        builder.addSpecialInlineBlockParser(new FollowsReferenceBlockParser("link"));

        JavadocParser parser = builder.build();
        {
            DefaultJavadocScanner scanner = string(
                "/** */");
            IrDocComment doc = parser.parse(scanner);
            List<? extends IrDocBlock> blocks = doc.getBlocks();
            assertEquals(0, blocks.size());
        }
        {
            DefaultJavadocScanner scanner = string(
                "/**\n"
                + " * Hello, this is {@code world}!\n"
                + " * testtest\n"
                + " * @param a {@link String}\n"
                + " * @throws hoge.foo.Bar If occurred\n"
                + " */");
            IrDocComment doc = parser.parse(scanner);
            List<? extends IrDocBlock> blocks = doc.getBlocks();
            assertEquals(3, blocks.size());
            {
                IrDocBlock b = blocks.get(0);
                assertNull(b.getTag());
                List<? extends IrDocFragment> fragments = b.getFragments();
                assertKinds(fragments, TEXT, BLOCK, TEXT, TEXT);
            }
            {
                IrDocBlock b = blocks.get(1);
                assertEquals("@param", b.getTag());
                List<? extends IrDocFragment> fragments = b.getFragments();
                assertKinds(fragments, SIMPLE_NAME, TEXT, BLOCK);
                IrDocBlock inner = (IrDocBlock) fragments.get(2);
                assertKinds(inner.getFragments(), NAMED_TYPE);
            }
            {
                IrDocBlock b = blocks.get(2);
                assertEquals("@throws", b.getTag());
                List<? extends IrDocFragment> fragments = b.getFragments();
                assertKinds(fragments, NAMED_TYPE, TEXT);
            }
        }
    }
}
