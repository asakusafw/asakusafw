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

import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBlock;

/**
 * Test for {@link AcceptableJavadocBlockParser}.
 */
public class AcceptableJavadocBlockParserTest {

    /**
     * Test method for {@link AcceptableJavadocBlockParser#canAccept(java.lang.String)}.
     */
    @Test
    public void testCanAccept() {
        AcceptableJavadocBlockParser parser = new AcceptableJavadocBlockParser("a", "b", "c") {
            @Override
            public IrDocBlock parse(String tag, JavadocScanner scanner) {
                return null;
            }
        };
        assertTrue(parser.canAccept("a"));
        assertTrue(parser.canAccept("b"));
        assertTrue(parser.canAccept("c"));
        assertFalse(parser.canAccept("d"));
        assertFalse(parser.canAccept("e"));
        assertFalse(parser.canAccept("f"));
    }

}
