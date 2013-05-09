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
package com.asakusafw.utils.java.parser.javadoc;

import static com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocElementKind.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrBasicTypeKind;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBasicType;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBlock;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocField;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocFragment;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocMethod;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocMethodParameter;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocNamedType;

/**
 * Test for {@link FollowsReferenceBlockParser}.
 */
public class FollowsReferenceBlockParserTest extends JavadocTestRoot {

    /**
     * Test method for {@link FollowsReferenceBlockParser#parse(java.lang.String, JavadocScanner)}.
     * @throws Exception If occurred
     */
    @Test
    public void testParseType() throws Exception {
        {
            FollowsReferenceBlockParser parser = new FollowsReferenceBlockParser();
            DefaultJavadocScanner scanner = string("A");
            IrDocBlock block = parser.parse(null, scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, NAMED_TYPE);
            assertEquals("A", ((IrDocNamedType) fragments.get(0)).getName().asString());
        }
        {
            FollowsReferenceBlockParser parser = new FollowsReferenceBlockParser();
            DefaultJavadocScanner scanner = string("hoge.foo.Bar");
            IrDocBlock block = parser.parse(null, scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, NAMED_TYPE);
            assertEquals("hoge.foo.Bar", ((IrDocNamedType) fragments.get(0)).getName().asString());
        }
        {
            FollowsReferenceBlockParser parser = new FollowsReferenceBlockParser();
            DefaultJavadocScanner scanner = string(
                "\n"
                + "    * hoge.foo.Bar");
            IrDocBlock block = parser.parse(null, scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, NAMED_TYPE);
            assertEquals("hoge.foo.Bar", ((IrDocNamedType) fragments.get(0)).getName().asString());
        }
        {
            FollowsReferenceBlockParser parser = new FollowsReferenceBlockParser();
            DefaultJavadocScanner scanner = string(
                "\n"
                + "    * hoge.foo.Bar?");
            IrDocBlock block = parser.parse(null, scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, TEXT);
            assertTextEquals("hoge.foo.Bar?", fragments.get(0));
        }
        {
            FollowsReferenceBlockParser parser = new FollowsReferenceBlockParser();
            DefaultJavadocScanner scanner = string(
                " hoge.foo.Bar This is a pen.");
            IrDocBlock block = parser.parse(null, scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, NAMED_TYPE, TEXT);
            assertEquals("hoge.foo.Bar", ((IrDocNamedType) fragments.get(0)).getName().asString());
            assertTextEquals(" This is a pen.", fragments.get(1));
        }
    }

    /**
     * Test method for {@link FollowsReferenceBlockParser#parse(java.lang.String, JavadocScanner)}.
     * @throws Exception If occurred
     */
    @Test
    public void testParseField() throws Exception {
        {
            FollowsReferenceBlockParser parser = new FollowsReferenceBlockParser();
            DefaultJavadocScanner scanner = string("#field");
            IrDocBlock block = parser.parse(null, scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, FIELD);
            IrDocField field = (IrDocField) fragments.get(0);
            assertNull(field.getDeclaringType());
            assertEquals("field", field.getName().getIdentifier());
        }
        {
            FollowsReferenceBlockParser parser = new FollowsReferenceBlockParser();
            DefaultJavadocScanner scanner = string("A#field");
            IrDocBlock block = parser.parse(null, scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, FIELD);
            IrDocField field = (IrDocField) fragments.get(0);
            assertEquals("A", field.getDeclaringType().getName().asString());
            assertEquals("field", field.getName().getIdentifier());
        }
        {
            FollowsReferenceBlockParser parser = new FollowsReferenceBlockParser();
            DefaultJavadocScanner scanner = string("hoge.foo.Bar#CONST");
            IrDocBlock block = parser.parse(null, scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, FIELD);
            IrDocField field = (IrDocField) fragments.get(0);
            assertEquals("hoge.foo.Bar", field.getDeclaringType().getName().asString());
            assertEquals("CONST", field.getName().getIdentifier());
        }
        {
            FollowsReferenceBlockParser parser = new FollowsReferenceBlockParser();
            DefaultJavadocScanner scanner = string("\n  * hoge.foo.Bar#CONST");
            IrDocBlock block = parser.parse(null, scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, FIELD);
            IrDocField field = (IrDocField) fragments.get(0);
            assertEquals("hoge.foo.Bar", field.getDeclaringType().getName().asString());
            assertEquals("CONST", field.getName().getIdentifier());
        }
    }

    /**
     * Test method for {@link FollowsReferenceBlockParser#parse(java.lang.String, JavadocScanner)}.
     * @throws Exception If occurred
     */
    @Test
    public void testParseMethod() throws Exception {
        {
            FollowsReferenceBlockParser parser = new FollowsReferenceBlockParser();
            DefaultJavadocScanner scanner = string("#method()");
            IrDocBlock block = parser.parse(null, scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, METHOD);
            IrDocMethod method = (IrDocMethod) fragments.get(0);
            assertNull(method.getDeclaringType());
            assertEquals("method", method.getName().getIdentifier());
            assertEquals(0, method.getParameters().size());
        }
        {
            FollowsReferenceBlockParser parser = new FollowsReferenceBlockParser();
            DefaultJavadocScanner scanner = string("#method(int)");
            IrDocBlock block = parser.parse(null, scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, METHOD);
            IrDocMethod method = (IrDocMethod) fragments.get(0);
            assertNull(method.getDeclaringType());
            assertEquals("method", method.getName().getIdentifier());
            assertEquals(1, method.getParameters().size());
            {
                IrDocMethodParameter param = method.getParameters().get(0);
                assertEquals(BASIC_TYPE, param.getType().getKind());
                assertEquals(IrBasicTypeKind.INT, ((IrDocBasicType) param.getType()).getTypeKind());
            }
        }
    }
}
