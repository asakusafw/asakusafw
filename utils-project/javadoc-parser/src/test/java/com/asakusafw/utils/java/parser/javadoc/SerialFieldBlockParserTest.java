/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocFragment;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocNamedType;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocSimpleName;

/**
 * Test for {@link SerialFieldBlockParser}.
 */
public class SerialFieldBlockParserTest extends JavadocTestRoot {

    /**
     * Test method for {@link SerialFieldBlockParser#parse(java.lang.String, JavadocScanner)}.
     * @throws Exception If occurred
     */
    @Test
    public void testParse() throws Exception {
        SerialFieldBlockParser parser = new SerialFieldBlockParser();
        {
            DefaultJavadocScanner scanner = string("field int");
            IrDocBlock block = parser.parse("serialField", scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, SIMPLE_NAME, BASIC_TYPE);
            assertEquals("field", ((IrDocSimpleName) fragments.get(0)).asString());
        }
        {
            DefaultJavadocScanner scanner = string("field java.lang.String");
            IrDocBlock block = parser.parse("serialField", scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, SIMPLE_NAME, NAMED_TYPE);
            assertEquals("field", ((IrDocSimpleName) fragments.get(0)).asString());
            assertEquals("java.lang.String", ((IrDocNamedType) fragments.get(1)).getName().asString());
        }
        {
            DefaultJavadocScanner scanner = string("field java.lang.String Hello, world!");
            IrDocBlock block = parser.parse("serialField", scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, SIMPLE_NAME, NAMED_TYPE, TEXT);
            assertEquals("field", ((IrDocSimpleName) fragments.get(0)).asString());
            assertEquals("java.lang.String", ((IrDocNamedType) fragments.get(1)).getName().asString());
            assertTextEquals(" Hello, world!", fragments.get(2));
        }
        {
            DefaultJavadocScanner scanner = string("java.lang.String Hello, world!");
            IrDocBlock block = parser.parse("serialField", scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, TEXT);
            assertTextEquals("java.lang.String Hello, world!", fragments.get(0));
        }
    }
}
