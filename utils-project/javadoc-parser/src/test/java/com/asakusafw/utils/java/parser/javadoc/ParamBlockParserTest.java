/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocSimpleName;

/**
 * Test for {@link ParamBlockParser}.
 */
public class ParamBlockParserTest extends JavadocTestRoot {

    /**
     * Test method for {@link ParamBlockParser#parse(java.lang.String, JavadocScanner)}.
     * @throws Exception If occurred
     */
    @Test
    public void testParse() throws Exception {
        {
            ParamBlockParser parser = new ParamBlockParser();
            DefaultJavadocScanner scanner = string("a");
            IrDocBlock block = parser.parse("param", scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, SIMPLE_NAME);
            assertEquals("a", ((IrDocSimpleName) fragments.get(0)).getIdentifier());
        }
        {
            ParamBlockParser parser = new ParamBlockParser();
            DefaultJavadocScanner scanner = string("a a parameter");
            IrDocBlock block = parser.parse("param", scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, SIMPLE_NAME, TEXT);
            assertEquals("a", ((IrDocSimpleName) fragments.get(0)).getIdentifier());
            assertTextEquals(" a parameter", fragments.get(1));
        }
        {
            ParamBlockParser parser = new ParamBlockParser();
            DefaultJavadocScanner scanner = string("<T> a parameter");
            IrDocBlock block = parser.parse("param", scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, TEXT, SIMPLE_NAME, TEXT, TEXT);
            assertTextEquals("<", fragments.get(0));
            assertEquals("T", ((IrDocSimpleName) fragments.get(1)).getIdentifier());
            assertTextEquals(">", fragments.get(2));
            assertTextEquals(" a parameter", fragments.get(3));
        }
        {
            ParamBlockParser parser = new ParamBlockParser();
            DefaultJavadocScanner scanner = string("<T< a parameter");
            IrDocBlock block = parser.parse("param", scanner);
            List<? extends IrDocFragment> fragments = block.getFragments();
            assertKinds(fragments, TEXT);
            assertTextEquals("<T< a parameter", fragments.get(0));
        }
    }
}
