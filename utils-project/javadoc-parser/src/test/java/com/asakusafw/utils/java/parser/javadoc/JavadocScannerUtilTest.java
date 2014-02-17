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

import static com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocTokenKind.*;
import static org.junit.Assert.*;

import java.util.EnumSet;
import java.util.List;

import org.junit.Test;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocToken;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocTokenKind;

/**
 * Test for {@link JavadocScannerUtil}.
 */
public class JavadocScannerUtilTest extends JavadocTestRoot {

    /**
     * Test method for {@link JavadocScannerUtil#lookaheadTokens(JavadocScanner, int, int)}.
     */
    @Test
    public void testLookaheadTokens() {
        DefaultJavadocScanner scanner = scanner("alphabets.txt");
        {
            List<JavadocToken> tokens = JavadocScannerUtil.lookaheadTokens(scanner, 0, 0);
            assertTextSequence(tokens);
        }
        {
            List<JavadocToken> tokens = JavadocScannerUtil.lookaheadTokens(scanner, 0, 1);
            assertTextSequence(tokens, "a");
        }
        {
            List<JavadocToken> tokens = JavadocScannerUtil.lookaheadTokens(scanner, 0, 3);
            assertTextSequence(tokens, "a", " ", "b");
        }
        {
            List<JavadocToken> tokens = JavadocScannerUtil.lookaheadTokens(scanner, 4, 3);
            assertTextSequence(tokens, "c", " ", "d");
        }
        {
            List<JavadocToken> tokens = JavadocScannerUtil.lookaheadTokens(scanner, 46, 100);
            assertTextSequence(tokens, "x", " ", "y", " ", "z");
        }
    }

    /**
     * Test method for {@link JavadocScannerUtil#countWhile(java.util.Collection, JavadocScanner, int)}.
     */
    @Test
    public void testCountWhileVoid() {
        DefaultJavadocScanner scanner = scanner("kinds.txt");
        {
            EnumSet<JavadocTokenKind> set = EnumSet.noneOf(JavadocTokenKind.class);
            int count = JavadocScannerUtil.countWhile(set, scanner, 0);
            assertEquals(0, count);
        }
        {
            EnumSet<JavadocTokenKind> set = EnumSet.noneOf(JavadocTokenKind.class);
            int count = JavadocScannerUtil.countWhile(set, scanner, 3);
            assertEquals(0, count);
        }
        {
            EnumSet<JavadocTokenKind> set = EnumSet.noneOf(JavadocTokenKind.class);
            int count = JavadocScannerUtil.countWhile(set, scanner, 5);
            assertEquals(0, count);
        }
    }

    /**
     * Test method for {@link JavadocScannerUtil#countWhile(java.util.Collection, JavadocScanner, int)}.
     */
    @Test
    public void testCountWhileSingle() {
        DefaultJavadocScanner scanner = scanner("kinds.txt");
        {
            EnumSet<JavadocTokenKind> set = EnumSet.of(ASTERISK);
            int count = JavadocScannerUtil.countWhile(set, scanner, 0);
            assertEquals(3, count);
        }
        {
            EnumSet<JavadocTokenKind> set = EnumSet.of(ASTERISK);
            int count = JavadocScannerUtil.countWhile(set, scanner, 1);
            assertEquals(2, count);
        }
        {
            EnumSet<JavadocTokenKind> set = EnumSet.of(ASTERISK);
            int count = JavadocScannerUtil.countWhile(set, scanner, 2);
            assertEquals(1, count);
        }
        {
            EnumSet<JavadocTokenKind> set = EnumSet.of(ASTERISK);
            int count = JavadocScannerUtil.countWhile(set, scanner, 3);
            assertEquals(0, count);
        }
        {
            EnumSet<JavadocTokenKind> set = EnumSet.of(IDENTIFIER);
            int count = JavadocScannerUtil.countWhile(set, scanner, 0);
            assertEquals(0, count);
        }
        {
            EnumSet<JavadocTokenKind> set = EnumSet.of(IDENTIFIER);
            int count = JavadocScannerUtil.countWhile(set, scanner, 3);
            assertEquals(1, count);
        }
    }

    /**
     * Test method for {@link JavadocScannerUtil#countWhile(java.util.Collection, JavadocScanner, int)}.
     */
    @Test
    public void testCountWhileMulti() {
        DefaultJavadocScanner scanner = scanner("kinds.txt");
        {
            EnumSet<JavadocTokenKind> set = EnumSet.of(ASTERISK, IDENTIFIER);
            int count = JavadocScannerUtil.countWhile(set, scanner, 0);
            assertEquals(4, count);
        }
        {
            EnumSet<JavadocTokenKind> set = EnumSet.of(ASTERISK, IDENTIFIER, QUESTION);
            int count = JavadocScannerUtil.countWhile(set, scanner, 0);
            assertEquals(8, count);
        }
        {
            EnumSet<JavadocTokenKind> set = EnumSet.of(QUESTION, WHITE_SPACES);
            int count = JavadocScannerUtil.countWhile(set, scanner, 0);
            assertEquals(0, count);
        }
        {
            EnumSet<JavadocTokenKind> set = EnumSet.of(QUESTION, WHITE_SPACES);
            int count = JavadocScannerUtil.countWhile(set, scanner, 4);
            assertEquals(5, count);
        }
        {
            EnumSet<JavadocTokenKind> set = EnumSet.allOf(JavadocTokenKind.class);
            int count = JavadocScannerUtil.countWhile(set, scanner, 0);
            assertEquals(scanner.getTokens().size(), count);
        }
    }

    /**
     * Test method for {@link JavadocScannerUtil#countUntil(java.util.Collection, JavadocScanner, int)}.
     */
    @Test
    public void testCountUntilVoid() {
        DefaultJavadocScanner scanner = scanner("kinds.txt");
        {
            EnumSet<JavadocTokenKind> set = EnumSet.noneOf(JavadocTokenKind.class);
            int count = JavadocScannerUtil.countUntil(set, scanner, 0);
            assertEquals(scanner.getTokens().size(), count);
        }
        {
            EnumSet<JavadocTokenKind> set = EnumSet.noneOf(JavadocTokenKind.class);
            int count = JavadocScannerUtil.countUntil(set, scanner, 5);
            assertEquals(scanner.getTokens().size() - 5, count);
        }
    }

    /**
     * Test method for {@link JavadocScannerUtil#countUntil(java.util.Collection, JavadocScanner, int)}.
     */
    @Test
    public void testCountUntilSingle() {
        DefaultJavadocScanner scanner = scanner("kinds.txt");
        {
            EnumSet<JavadocTokenKind> set = EnumSet.of(ASTERISK);
            int count = JavadocScannerUtil.countUntil(set, scanner, 0);
            assertEquals(0, count);
        }
        {
            EnumSet<JavadocTokenKind> set = EnumSet.of(IDENTIFIER);
            int count = JavadocScannerUtil.countUntil(set, scanner, 0);
            assertEquals(3, count);
        }
        {
            EnumSet<JavadocTokenKind> set = EnumSet.of(QUESTION);
            int count = JavadocScannerUtil.countUntil(set, scanner, 0);
            assertEquals(4, count);
        }
        {
            EnumSet<JavadocTokenKind> set = EnumSet.of(WHITE_SPACES);
            int count = JavadocScannerUtil.countUntil(set, scanner, 0);
            assertEquals(8, count);
        }
        {
            EnumSet<JavadocTokenKind> set = EnumSet.of(AT);
            int count = JavadocScannerUtil.countUntil(set, scanner, 5);
            assertEquals(5, count);
        }
    }

    /**
     * Test method for {@link JavadocScannerUtil#countUntil(java.util.Collection, JavadocScanner, int)}.
     */
    @Test
    public void testCountUntilMulti() {
        DefaultJavadocScanner scanner = scanner("kinds.txt");
        {
            EnumSet<JavadocTokenKind> set = EnumSet.of(IDENTIFIER, AT);
            int count = JavadocScannerUtil.countUntil(set, scanner, 0);
            assertEquals(3, count);
        }
        {
            EnumSet<JavadocTokenKind> set = EnumSet.of(IDENTIFIER, AT);
            int count = JavadocScannerUtil.countUntil(set, scanner, 5);
            assertEquals(5, count);
        }
    }

    /**
     * Test method for {@link JavadocScannerUtil#countUntilBlockEnd(JavadocScanner, int)}.
     */
    @Test
    public void testCountUntilBlockEnd() {
        {
            DefaultJavadocScanner scanner = string("Hello, world!");
            int count = JavadocScannerUtil.countUntilBlockEnd(scanner, 0);
            assertEquals(EOF, scanner.lookahead(count).getKind());
        }
        {
            DefaultJavadocScanner scanner = string(
                "Hello, world!\n"
                + "@stop");
            int count = JavadocScannerUtil.countUntilBlockEnd(scanner, 0);
            assertEquals(AT, scanner.lookahead(count).getKind());
            assertEquals("stop", scanner.lookahead(count + 1).getText());
        }
        {
            DefaultJavadocScanner scanner = string(
                "\n"
                + "  * Hello, world!\n"
                + "  * @stop");
            int count = JavadocScannerUtil.countUntilBlockEnd(scanner, 0);
            assertEquals(AT, scanner.lookahead(count).getKind());
            assertEquals("stop", scanner.lookahead(count + 1).getText());
        }
        {
            DefaultJavadocScanner scanner = string(
                "\n"
                + "  * Hello, world!\n"
                + "  *             @stop");
            int count = JavadocScannerUtil.countUntilBlockEnd(scanner, 0);
            assertEquals(AT, scanner.lookahead(count).getKind());
            assertEquals("stop", scanner.lookahead(count + 1).getText());
        }
        {
            DefaultJavadocScanner scanner = string(
                "\n"
                + "  * {@code block start-->\n"
                + "  * @dummy\n"
                + "  * }\n"
                + "  * @stop");
            int count = JavadocScannerUtil.countUntilBlockEnd(scanner, 0);
            assertEquals(AT, scanner.lookahead(count).getKind());
            assertEquals("stop", scanner.lookahead(count + 1).getText());
        }
        {
            DefaultJavadocScanner scanner = string(
                "\n"
                + "  * @start\n"
                + "  * {@code block start-->\n"
                + "  * @dummy\n"
                + "  * }\n"
                + "  * @stop");
            int count = JavadocScannerUtil.countUntilBlockEnd(scanner, 6);
            assertEquals(AT, scanner.lookahead(6 + count).getKind());
            assertEquals("stop", scanner.lookahead(6 + count + 1).getText());
        }
    }

    /**
     * Test method for {@link JavadocScannerUtil#countUntilCommentEnd(JavadocScanner, boolean, int)}.
     */
    @Test
    public void testCountUntilCommentEnd() {
        {
            DefaultJavadocScanner scanner = string("*/");
            int count = JavadocScannerUtil.countUntilCommentEnd(scanner, true, 0);
            assertEquals(0, count);
        }
        {
            DefaultJavadocScanner scanner = string("*/ /* */");
            int count = JavadocScannerUtil.countUntilCommentEnd(scanner, true, 0);
            assertEquals(0, count);
        }
        {
            DefaultJavadocScanner scanner = string("*****/ /* */");
            int count = JavadocScannerUtil.countUntilCommentEnd(scanner, true, 0);
            assertEquals(4, count);
        }
        {
            DefaultJavadocScanner scanner = string("*****/ /* */");
            int count = JavadocScannerUtil.countUntilCommentEnd(scanner, false, 0);
            assertEquals(4, count);
        }
        {
            DefaultJavadocScanner scanner = string("*****+/ /* */");
            int count = JavadocScannerUtil.countUntilCommentEnd(scanner, true, 0);
            assertEquals(11, count);
        }
        {
            DefaultJavadocScanner scanner = string("*****+/ /* +/");
            int count = JavadocScannerUtil.countUntilCommentEnd(scanner, true, 0);
            assertEquals(-1, count);
        }
        {
            DefaultJavadocScanner scanner = string("*****+/ /* +/");
            int count = JavadocScannerUtil.countUntilCommentEnd(scanner, false, 0);
            assertEquals(scanner.getTokens().size(), count);
        }
    }

    /**
     * Test method for {@link JavadocScannerUtil#countUntilNextLineStart(JavadocScanner, int)}.
     */
    @Test
    public void testCountUntilNextLineStart() {
        DefaultJavadocScanner scanner = scanner("3lines-formatted.txt");
        int offset = 0;
        assertEquals("a", scanner.lookahead(offset).getText());

        offset++;
        assertEquals(LINE_BREAK, scanner.lookahead(offset).getKind());

        offset += JavadocScannerUtil.countUntilNextLineStart(scanner, offset);
        assertEquals(WHITE_SPACES, scanner.lookahead(offset).getKind());
        assertEquals("b", scanner.lookahead(offset + 1).getText());

        offset += 2;
        assertEquals(LINE_BREAK, scanner.lookahead(offset).getKind());

        offset += JavadocScannerUtil.countUntilNextLineStart(scanner, offset);
        assertEquals(WHITE_SPACES, scanner.lookahead(offset).getKind());
        assertEquals("c", scanner.lookahead(offset + 1).getText());

        offset += 2;
        assertEquals(EOF, scanner.lookahead(offset).getKind());
    }

    /**
     * Test method for {@link JavadocScannerUtil#countUntilNextPrintable(JavadocScanner, int)}.
     */
    @Test
    public void testCountUntilNextPrintableGeneral() {
        DefaultJavadocScanner scanner = scanner("alphabets.txt");
        int offset = 0;
        assertEquals("a", scanner.lookahead(offset).getText());

        offset++;
        assertEquals(WHITE_SPACES, scanner.lookahead(offset).getKind());

        offset += JavadocScannerUtil.countUntilNextPrintable(scanner, offset);
        assertEquals("b", scanner.lookahead(offset).getText());

        offset++;
        assertEquals(WHITE_SPACES, scanner.lookahead(offset).getKind());

        offset += JavadocScannerUtil.countUntilNextPrintable(scanner, offset);
        assertEquals("c", scanner.lookahead(offset).getText());
    }

    /**
     * Test method for {@link JavadocScannerUtil#countUntilNextPrintable(JavadocScanner, int)}.
     */
    @Test
    public void testCountUntilNextPrintableBeyondLines() {
        DefaultJavadocScanner scanner = scanner("3lines-formatted.txt");
        int offset = 0;
        assertEquals("a", scanner.lookahead(offset).getText());

        offset++;
        offset += JavadocScannerUtil.countUntilNextPrintable(scanner, offset);
        assertEquals("b", scanner.lookahead(offset).getText());

        offset++;
        offset += JavadocScannerUtil.countUntilNextPrintable(scanner, offset);
        assertEquals("c", scanner.lookahead(offset).getText());

        offset++;
        offset += JavadocScannerUtil.countUntilNextPrintable(scanner, offset);
        assertEquals(EOF, scanner.lookahead(offset).getKind());
    }

    /**
     * Test method for {@link JavadocScannerUtil#countUntilNextPrintable(JavadocScanner, int)}.
     */
    @Test
    public void testCountUntilNextPrintableBeyondBlankLines() {
        DefaultJavadocScanner scanner = scanner("blank-lines.txt");
        int offset = 0;
        assertEquals("a", scanner.lookahead(offset).getText());

        offset++;
        offset += JavadocScannerUtil.countUntilNextPrintable(scanner, offset);
        assertEquals("b", scanner.lookahead(offset).getText());

        offset++;
        offset += JavadocScannerUtil.countUntilNextPrintable(scanner, offset);
        assertEquals("c", scanner.lookahead(offset).getText());

        offset++;
        offset += JavadocScannerUtil.countUntilNextPrintable(scanner, offset);
        assertEquals("d", scanner.lookahead(offset).getText());

        offset++;
        offset += JavadocScannerUtil.countUntilNextPrintable(scanner, offset);
        assertEquals(EOF, scanner.lookahead(offset).getKind());
    }
}
