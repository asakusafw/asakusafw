/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

/**
 * Test for {@link FollowsNamedTypeBlockParser}.
 */
public class FollowsNamedTypeBlockParserTest extends JavadocTestRoot {

    /**
     * Test method for {@link FollowsNamedTypeBlockParser#parse(java.lang.String, JavadocScanner)}.
     * @throws Exception If occurred
     */
    @Test
    public void testParseJust() throws Exception {
        FollowsNamedTypeBlockParser parser = new FollowsNamedTypeBlockParser("throws");
        IrDocBlock block = parser.parse("throws", string("IllegalArgumentException"));
        assertEquals("@throws", block.getTag());
        List<? extends IrDocFragment> fragments = block.getFragments();
        assertKinds(fragments, NAMED_TYPE);
        assertEquals("IllegalArgumentException", ((IrDocNamedType) fragments.get(0)).getName().asString());
    }

    /**
     * Test method for {@link FollowsNamedTypeBlockParser#parse(java.lang.String, JavadocScanner)}.
     * @throws Exception If occurred
     */
    @Test
    public void testParseTrails() throws Exception {
        FollowsNamedTypeBlockParser parser = new FollowsNamedTypeBlockParser("throws");
        IrDocBlock block = parser.parse("throws", string(" IllegalArgumentException nullpo"));
        assertEquals("@throws", block.getTag());
        List<? extends IrDocFragment> fragments = block.getFragments();
        assertKinds(fragments, NAMED_TYPE, TEXT);
        assertEquals("IllegalArgumentException", ((IrDocNamedType) fragments.get(0)).getName().asString());
        assertTextEquals(" nullpo", fragments.get(1));
    }

    /**
     * Test method for {@link FollowsNamedTypeBlockParser#parse(java.lang.String, JavadocScanner)}.
     * @throws Exception If occurred
     */
    @Test
    public void testParseMissing() throws Exception {
        FollowsNamedTypeBlockParser parser = new FollowsNamedTypeBlockParser("throws");
        IrDocBlock block = parser.parse("throws", string(" /IllegalArgumentException"));
        assertEquals("@throws", block.getTag());
        List<? extends IrDocFragment> fragments = block.getFragments();
        assertKinds(fragments, TEXT);
        assertTextEquals(" /IllegalArgumentException", fragments.get(0));
    }
}
