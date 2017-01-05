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

import static com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocTokenKind.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocToken;

/**
 * Test for {@link DefaultJavadocScanner}.
 */
public class DefaultJavadocScannerTest extends JavadocTestRoot {

    /**
     * Test method for {@link DefaultJavadocScanner#newInstance(java.lang.String)}.
     */
    @Test
    public void testNewInstanceEmpty() {
        String text = load("empty.doc.txt");
        DefaultJavadocScanner scanner = DefaultJavadocScanner.newInstance(text);

        assertEquals(SLASH, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(SLASH, scanner.nextToken().getKind());
        assertEquals(EOF, scanner.nextToken().getKind());
    }

    /**
     * Test method for {@link DefaultJavadocScanner#newInstance(java.lang.String)}.
     */
    @Test
    public void testNewInstanceSingleSpace() {
        String text = load("singlespace.doc.txt");
        DefaultJavadocScanner scanner = DefaultJavadocScanner.newInstance(text);

        assertEquals(SLASH, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(WHITE_SPACES, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(SLASH, scanner.nextToken().getKind());
        assertEquals(EOF, scanner.nextToken().getKind());
    }

    /**
     * Test method for {@link DefaultJavadocScanner#newInstance(java.lang.String)}.
     */
    @Test
    public void testNewInstance3Lines() {
        String text = load("3lines.doc.txt");
        DefaultJavadocScanner scanner = DefaultJavadocScanner.newInstance(text);

        assertEquals(SLASH, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(LINE_BREAK, scanner.nextToken().getKind());

        assertEquals(WHITE_SPACES, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(LINE_BREAK, scanner.nextToken().getKind());

        assertEquals(WHITE_SPACES, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(SLASH, scanner.nextToken().getKind());
        assertEquals(EOF, scanner.nextToken().getKind());
    }

    /**
     * Test method for {@link DefaultJavadocScanner#newInstance(java.lang.String)}.
     */
    @Test
    public void testNewInstanceSynopsis() {
        String text = load("synopsis.doc.txt");
        DefaultJavadocScanner scanner = DefaultJavadocScanner.newInstance(text);

        assertEquals(SLASH, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(LINE_BREAK, scanner.nextToken().getKind());

        assertEquals(WHITE_SPACES, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(WHITE_SPACES, scanner.nextToken().getKind());
        assertEquals(IDENTIFIER, scanner.nextToken().getKind());
        assertEquals(COMMA, scanner.nextToken().getKind());
        assertEquals(WHITE_SPACES, scanner.nextToken().getKind());
        assertEquals(IDENTIFIER, scanner.nextToken().getKind());
        assertEquals(TEXT, scanner.nextToken().getKind());
        assertEquals(LINE_BREAK, scanner.nextToken().getKind());

        assertEquals(WHITE_SPACES, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(SLASH, scanner.nextToken().getKind());
        assertEquals(EOF, scanner.nextToken().getKind());
    }

    /**
     * Test method for {@link DefaultJavadocScanner#getIndex()}.
     */
    @Test
    public void testGetIndex() {
        String text = load("synopsis.doc.txt");
        DefaultJavadocScanner scanner = DefaultJavadocScanner.newInstance(text);
        int index = 0;
        while (true) {
            assertEquals(index, scanner.getIndex());
            JavadocToken t = scanner.nextToken();
            if (t.getKind() == EOF) {
                break;
            }
            index++;
        }
        assertEquals(index, scanner.getIndex());
        scanner.nextToken();
        assertEquals(index, scanner.getIndex());
    }

    /**
     * Test method for {@link DefaultJavadocScanner#seek(int)}.
     */
    @Test
    public void testSeek() {
        String text = load("empty.doc.txt");
        DefaultJavadocScanner scanner = DefaultJavadocScanner.newInstance(text);

        assertEquals(SLASH, scanner.nextToken().getKind());
        scanner.seek(0);
        assertEquals(SLASH, scanner.nextToken().getKind());

        scanner.seek(4);
        assertEquals(SLASH, scanner.nextToken().getKind());
        assertEquals(EOF, scanner.nextToken().getKind());

        scanner.seek(1);
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
    }

    /**
     * Test method for {@link DefaultJavadocScanner#consume(int)}.
     */
    @Test
    public void testConsume() {
        String text = load("empty.doc.txt");
        DefaultJavadocScanner scanner = DefaultJavadocScanner.newInstance(text);
        assertEquals(SLASH, scanner.nextToken().getKind());
        scanner.consume(3);
        assertEquals(SLASH, scanner.nextToken().getKind());
        assertEquals(EOF, scanner.nextToken().getKind());
    }

    /**
     * Test method for {@link DefaultJavadocScanner#nextToken()}.
     */
    @Test
    public void testNextToken() {
        String text = load("empty.doc.txt");
        DefaultJavadocScanner scanner = DefaultJavadocScanner.newInstance(text);
        assertEquals(SLASH, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(ASTERISK, scanner.nextToken().getKind());
        assertEquals(SLASH, scanner.nextToken().getKind());
        assertEquals(EOF, scanner.nextToken().getKind());
        assertEquals(EOF, scanner.nextToken().getKind());
        assertEquals(EOF, scanner.nextToken().getKind());
        assertEquals(EOF, scanner.nextToken().getKind());
        assertEquals(EOF, scanner.nextToken().getKind());
    }

    /**
     * Test method for {@link DefaultJavadocScanner#lookahead(int)}.
     */
    @Test
    public void testLookahead() {
        String text = load("empty.doc.txt");
        DefaultJavadocScanner scanner = DefaultJavadocScanner.newInstance(text);
        assertEquals(SLASH, scanner.lookahead(0).getKind());
        assertEquals(ASTERISK, scanner.lookahead(1).getKind());
        assertEquals(ASTERISK, scanner.lookahead(2).getKind());
        assertEquals(ASTERISK, scanner.lookahead(3).getKind());
        assertEquals(SLASH, scanner.lookahead(4).getKind());
        assertEquals(EOF, scanner.lookahead(5).getKind());
        assertEquals(EOF, scanner.lookahead(6).getKind());
        assertEquals(EOF, scanner.lookahead(7).getKind());

        scanner.consume(3);
        assertEquals(SLASH, scanner.lookahead(-3).getKind());
        assertEquals(ASTERISK, scanner.lookahead(-2).getKind());
        assertEquals(ASTERISK, scanner.lookahead(-1).getKind());
        assertEquals(ASTERISK, scanner.lookahead(0).getKind());
        assertEquals(SLASH, scanner.lookahead(1).getKind());
        assertEquals(EOF, scanner.lookahead(2).getKind());
        assertEquals(EOF, scanner.lookahead(3).getKind());
        assertEquals(EOF, scanner.lookahead(4).getKind());
    }
}
