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

import static com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocTokenKind.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for {@link DefaultJavadocTokenStream}.
 */
public class DefaultJavadocTokenStreamTest extends JavadocTestRoot {

    /**
     * Test method for {@link DefaultJavadocTokenStream#peek()}.
     */
    @Test
    public void testPeek() {
        DefaultJavadocTokenStream stream = stream("alphabets.txt");

        assertEquals("a", stream.peek().getText());
        assertEquals("a", stream.peek().getText());
        assertEquals("a", stream.peek().getText());

        stream.nextToken();

        assertEquals("b", stream.peek().getText());
        assertEquals("b", stream.peek().getText());
        assertEquals("b", stream.peek().getText());
    }

    /**
     * Test method for {@link DefaultJavadocTokenStream#lookahead(int)}.
     */
    @Test
    public void testLookahead() {
        DefaultJavadocTokenStream stream = stream("alphabets.txt");

        assertEquals("a", stream.lookahead(0).getText());
        assertEquals("a", stream.lookahead(0).getText());
        assertEquals("b", stream.lookahead(2).getText());
        assertEquals("c", stream.lookahead(4).getText());
        assertEquals("d", stream.lookahead(6).getText());
        assertEquals("e", stream.lookahead(8).getText());

        stream.nextToken();
        assertEquals("b", stream.lookahead(1).getText());
        assertEquals("c", stream.lookahead(3).getText());
        assertEquals("d", stream.lookahead(5).getText());
    }

    /**
     * Test method for {@link DefaultJavadocTokenStream#nextToken()}.
     */
    @Test
    public void testNextTokenSimple() {
        DefaultJavadocTokenStream stream = stream("alphabets.txt");
        for (char c = 'a'; c <= 'z'; c++) {
            assertEquals(String.valueOf(c), stream.nextToken().getText());
        }
        assertEquals(EOF, stream.nextToken().getKind());
        assertEquals(EOF, stream.nextToken().getKind());
    }

    /**
     * Test method for {@link DefaultJavadocTokenStream#nextToken()}.
     */
    @Test
    public void testNextTokenLines() {
        DefaultJavadocTokenStream stream = stream("blank-lines.txt");
        assertEquals("a", stream.nextToken().getText());
        assertEquals("b", stream.nextToken().getText());
        assertEquals("c", stream.nextToken().getText());
        assertEquals("d", stream.nextToken().getText());
        assertEquals(EOF, stream.nextToken().getKind());
        assertEquals(EOF, stream.nextToken().getKind());
    }

    /**
     * Test method for {@link DefaultJavadocTokenStream#mark()}.
     */
    @Test
    public void testMark() {
        DefaultJavadocTokenStream stream = stream("blank-lines.txt");
        stream.mark();
        assertEquals("a", stream.nextToken().getText());
        stream.mark();
        assertEquals("b", stream.nextToken().getText());
        stream.mark();
        assertEquals("c", stream.nextToken().getText());
        stream.mark();
        assertEquals("d", stream.nextToken().getText());

        stream.rewind();
        assertEquals("d", stream.nextToken().getText());
        stream.rewind();
        assertEquals("c", stream.nextToken().getText());
        stream.rewind();
        assertEquals("b", stream.nextToken().getText());
        stream.rewind();
        assertEquals("a", stream.nextToken().getText());
    }

    /**
     * Test method for {@link DefaultJavadocTokenStream#rewind()}.
     */
    @Test
    public void testRewind() {
        DefaultJavadocTokenStream stream = stream("blank-lines.txt");
        stream.mark();
        assertEquals("a", stream.nextToken().getText());
        stream.mark();
        assertEquals("b", stream.nextToken().getText());
        stream.rewind();
        assertEquals("b", stream.nextToken().getText());
        stream.rewind();
        assertEquals("a", stream.nextToken().getText());
        try {
            stream.rewind();
            fail();
        } catch (IllegalStateException e) {
            // ok.
        }
    }

    /**
     * Test method for {@link DefaultJavadocTokenStream#discard()}.
     */
    @Test
    public void testDiscard() {
        DefaultJavadocTokenStream stream = stream("blank-lines.txt");
        stream.mark();
        assertEquals("a", stream.nextToken().getText());
        stream.mark();
        assertEquals("b", stream.nextToken().getText());
        stream.mark();
        assertEquals("c", stream.nextToken().getText());

        stream.discard();
        assertEquals("d", stream.nextToken().getText());
        stream.rewind();
        assertEquals("b", stream.nextToken().getText());
        stream.discard();
        assertEquals("c", stream.nextToken().getText());
        try {
            stream.discard();
            fail();
        } catch (IllegalStateException e) {
            // ok.
        }
    }
}
