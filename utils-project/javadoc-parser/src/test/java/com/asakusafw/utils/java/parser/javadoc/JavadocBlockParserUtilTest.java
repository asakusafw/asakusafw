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

import static com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocTokenKind.*;
import static org.junit.Assert.*;

import java.util.EnumSet;

import org.junit.Test;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrBasicTypeKind;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocArrayType;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBasicType;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocElement;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocElementKind;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocField;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocFragment;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocMethod;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocMethodParameter;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocName;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocNamedType;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocSimpleName;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocText;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocType;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrLocation;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocToken;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocTokenKind;

/**
 * Test for {@link JavadocBlockParserUtil}.
 */
public class JavadocBlockParserUtilTest extends JavadocTestRoot {

    /**
     * Test method for {@link JavadocBlockParserUtil#setLocation(IrDocElement, JavadocToken, JavadocToken)}.
     */
    @Test
    public void testSetLocationToken() {
        {
            IrDocElement elem = new IrDocText("text");
            JavadocToken start = new JavadocToken(JavadocTokenKind.IDENTIFIER, "id", 10);
            JavadocToken stop = new JavadocToken(JavadocTokenKind.IDENTIFIER, "id", 20);
            JavadocBlockParserUtil.setLocation(elem, start, stop);
            assertEquals(10, elem.getLocation().getStartPosition());
            assertEquals(12, elem.getLocation().getLength());
        }
        {
            IrDocElement elem = new IrDocText("text");
            JavadocToken start = new JavadocToken(JavadocTokenKind.IDENTIFIER, "id", 10);
            JavadocBlockParserUtil.setLocation(elem, start, start);
            assertEquals(10, elem.getLocation().getStartPosition());
            assertEquals(2, elem.getLocation().getLength());
        }
    }

    /**
     * Test method for {@link JavadocBlockParserUtil#setLocation(IrDocElement, IrLocation, IrLocation)}.
     */
    @Test
    public void testSetLocationLocation() {
        {
            IrDocElement elem = new IrDocText("text");
            IrLocation start = new IrLocation(10, 2);
            IrLocation stop = new IrLocation(20, 2);
            JavadocBlockParserUtil.setLocation(elem, start, stop);
            assertEquals(10, elem.getLocation().getStartPosition());
            assertEquals(12, elem.getLocation().getLength());
        }
        {
            IrDocElement elem = new IrDocText("text");
            IrLocation start = new IrLocation(10, 2);
            JavadocBlockParserUtil.setLocation(elem, start, start);
            assertEquals(10, elem.getLocation().getStartPosition());
            assertEquals(2, elem.getLocation().getLength());
        }
        {
            IrDocElement elem = new IrDocText("text");
            IrLocation fragment = new IrLocation(10, 2);
            JavadocBlockParserUtil.setLocation(elem, null, fragment);
            assertNull(elem.getLocation());
        }
        {
            IrDocElement elem = new IrDocText("text");
            IrLocation fragment = new IrLocation(10, 2);
            JavadocBlockParserUtil.setLocation(elem, fragment, null);
            assertNull(elem.getLocation());
        }
    }

    /**
     * Test method for {@link JavadocBlockParserUtil#fetchText(JavadocScanner, boolean, boolean)}.
     */
    @Test
    public void testFetchText() {
        DefaultJavadocScanner scanner = scanner("testFetchText.txt");
        {
            int offset = scanner.lookahead(0).getStartPosition();
            IrDocText text = JavadocBlockParserUtil.fetchText(scanner, false, false);
            assertEquals("onlyIdentifier", text.getContent());
            assertNotNull(text.getLocation());
            assertEquals(offset, text.getLocation().getStartPosition());
            assertEquals(text.getContent().length(), text.getLocation().getLength());

            assertEquals(JavadocTokenKind.LINE_BREAK, scanner.lookahead(0).getKind());
            scanner.consume(JavadocScannerUtil.countUntilNextLineStart(scanner, 0));
        }
        {
            int offset = scanner.lookahead(0).getStartPosition();
            IrDocText text = JavadocBlockParserUtil.fetchText(scanner, false, false);
            assertEquals("Identifier with space", text.getContent());
            assertNotNull(text.getLocation());
            assertEquals(offset, text.getLocation().getStartPosition());
            assertEquals(text.getContent().length(), text.getLocation().getLength());

            assertEquals(JavadocTokenKind.LINE_BREAK, scanner.lookahead(0).getKind());
            scanner.consume(JavadocScannerUtil.countUntilNextLineStart(scanner, 0));
        }
        {
            IrDocText text = JavadocBlockParserUtil.fetchText(scanner, false, false);
            assertNull(text);
            scanner.consume(JavadocScannerUtil.countUntilNextLineStart(scanner, 0));
        }
        {
            int offset = scanner.lookahead(0).getStartPosition();
            IrDocText text = JavadocBlockParserUtil.fetchText(scanner, false, false);
            assertEquals("head ", text.getContent());
            assertNotNull(text.getLocation());
            assertEquals(offset, text.getLocation().getStartPosition());
            assertEquals(text.getContent().length(), text.getLocation().getLength());

            assertEquals(JavadocTokenKind.LEFT_BRACE, scanner.lookahead(0).getKind());
            scanner.consume(JavadocScannerUtil.countUntil(EnumSet.of(JavadocTokenKind.RIGHT_BRACE), scanner, 0) + 1);

            offset = scanner.lookahead(0).getStartPosition();
            text = JavadocBlockParserUtil.fetchText(scanner, false, false);
            assertEquals(" tail", text.getContent());
            assertNotNull(text.getLocation());
            assertEquals(offset, text.getLocation().getStartPosition());
            assertEquals(text.getContent().length(), text.getLocation().getLength());

            assertEquals(JavadocTokenKind.LINE_BREAK, scanner.lookahead(0).getKind());
            scanner.consume(JavadocScannerUtil.countUntilNextLineStart(scanner, 0));
        }
        {
            int offset = scanner.lookahead(0).getStartPosition();
            IrDocText text = JavadocBlockParserUtil.fetchText(scanner, false, false);
            assertEquals("   <- 3spaces ->   ", text.getContent());
            assertNotNull(text.getLocation());
            assertEquals(offset, text.getLocation().getStartPosition());
            assertEquals(text.getContent().length(), text.getLocation().getLength());

            assertEquals(JavadocTokenKind.LINE_BREAK, scanner.lookahead(0).getKind());
            scanner.consume(JavadocScannerUtil.countUntilNextLineStart(scanner, 0));
        }
        {
            int offset = scanner.lookahead(0).getStartPosition();
            IrDocText text = JavadocBlockParserUtil.fetchText(scanner, true, false);
            assertEquals("<- 3spaces ->   ", text.getContent());
            assertNotNull(text.getLocation());
            assertEquals(offset + 3, text.getLocation().getStartPosition());
            assertEquals(text.getContent().length(), text.getLocation().getLength());

            assertEquals(JavadocTokenKind.LINE_BREAK, scanner.lookahead(0).getKind());
            scanner.consume(JavadocScannerUtil.countUntilNextLineStart(scanner, 0));
        }
        {
            int offset = scanner.lookahead(0).getStartPosition();
            IrDocText text = JavadocBlockParserUtil.fetchText(scanner, false, true);
            assertEquals("   <- 3spaces ->", text.getContent());
            assertNotNull(text.getLocation());
            assertEquals(offset, text.getLocation().getStartPosition());
            assertEquals(text.getContent().length(), text.getLocation().getLength());

            assertEquals(JavadocTokenKind.LINE_BREAK, scanner.lookahead(0).getKind());
            scanner.consume(JavadocScannerUtil.countUntilNextLineStart(scanner, 0));
        }
        {
            int offset = scanner.lookahead(0).getStartPosition();
            IrDocText text = JavadocBlockParserUtil.fetchText(scanner, true, true);
            assertEquals("<- 3spaces ->", text.getContent());
            assertNotNull(text.getLocation());
            assertEquals(offset + 3, text.getLocation().getStartPosition());
            assertEquals(text.getContent().length(), text.getLocation().getLength());

            assertEquals(JavadocTokenKind.LINE_BREAK, scanner.lookahead(0).getKind());
            scanner.consume(JavadocScannerUtil.countUntilNextLineStart(scanner, 0));
        }
        {
            IrDocText text = JavadocBlockParserUtil.fetchText(scanner, false, false);
            assertNull(text);
        }
    }

    /**
     * Test method for {@link JavadocBlockParserUtil#fetchBlockInfo(JavadocScanner)}.
     */
    @Test
    public void testFetchBlockInfoEmpty() {
        DefaultJavadocScanner scanner = scanner("empty-inline.txt");
        JavadocBlockInfo block = JavadocBlockParserUtil.fetchBlockInfo(scanner);
        assertEquals(JavadocTokenKind.EOF, scanner.lookahead(0).getKind());
        assertEquals("code", block.getTagName());
        assertEquals(0, block.getLocation().getStartPosition());
        assertEquals(scanner.lookahead(0).getStartPosition(), block.getLocation().getLength());

        JavadocScanner bScanner = block.getBlockScanner();
        assertEquals(JavadocTokenKind.EOF, bScanner.lookahead(0).getKind());
    }

    /**
     * Test method for {@link JavadocBlockParserUtil#fetchBlockInfo(JavadocScanner)}.
     */
    @Test
    public void testFetchBlockInfoSimple() {
        DefaultJavadocScanner scanner = scanner("simple-inline.txt");
        JavadocBlockInfo block = JavadocBlockParserUtil.fetchBlockInfo(scanner);
        assertEquals(JavadocTokenKind.EOF, scanner.lookahead(0).getKind());
        assertEquals("code", block.getTagName());
        assertEquals(0, block.getLocation().getStartPosition());
        assertEquals(scanner.lookahead(0).getStartPosition(), block.getLocation().getLength());

        JavadocScanner bScanner = block.getBlockScanner();
        IrDocText inline = JavadocBlockParserUtil.fetchText(bScanner, false, false);
        assertEquals(" Hello, world!", inline.getContent());
        assertEquals(JavadocTokenKind.EOF, bScanner.lookahead(0).getKind());
    }

    /**
     * Test method for {@link JavadocBlockParserUtil#fetchBlockInfo(JavadocScanner)}.
     */
    @Test
    public void testFetchBlockInfoBroken() {
        DefaultJavadocScanner scanner = scanner("broken-inline.txt");
        JavadocBlockInfo block = JavadocBlockParserUtil.fetchBlockInfo(scanner);
        assertEquals(JavadocTokenKind.EOF, scanner.lookahead(0).getKind());
        assertEquals("code", block.getTagName());
        assertEquals(0, block.getLocation().getStartPosition());
        assertEquals(scanner.lookahead(0).getStartPosition(), block.getLocation().getLength());

        JavadocScanner bScanner = block.getBlockScanner();
        IrDocText inline = JavadocBlockParserUtil.fetchText(bScanner, false, false);
        assertEquals(" Hello, world! EOF->", inline.getContent());
        assertEquals(JavadocTokenKind.EOF, bScanner.lookahead(0).getKind());
    }

    /**
     * Test method for {@link JavadocBlockParserUtil#fetchSimpleName(JavadocScanner, java.util.Set)}.
     */
    @Test
    public void testFetchSimpleName() {
        {
            DefaultJavadocScanner scanner = string("java.lang.String");
            IrDocSimpleName elem = JavadocBlockParserUtil.fetchSimpleName(scanner, null);
            assertNotNull(elem);
            assertEquals("java", elem.getIdentifier());
            assertEquals(0, elem.getLocation().getStartPosition());
            assertEquals("java".length(), elem.getLocation().getLength());
            assertEquals(DOT, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string(".String");
            IrDocSimpleName elem = JavadocBlockParserUtil.fetchSimpleName(scanner, null);
            assertNull(elem);
            assertEquals(DOT, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("java.lang.String");
            IrDocSimpleName elem = JavadocBlockParserUtil.fetchSimpleName(scanner, EnumSet.of(WHITE_SPACES));
            assertNull(elem);
            assertEquals(IDENTIFIER, scanner.lookahead(0).getKind());
        }
    }

    /**
     * Test method for {@link JavadocBlockParserUtil#fetchName(JavadocScanner, java.util.Set)}.
     */
    @Test
    public void testFetchName() {
        {
            DefaultJavadocScanner scanner = string("java.lang.String#length()");
            IrDocName elem = JavadocBlockParserUtil.fetchName(scanner, null);
            assertNotNull(elem);
            assertEquals("java.lang.String", elem.asString());
            assertSameLocation(0, "java.lang.String".length(), elem.getLocation());

            assertEquals(IrDocElementKind.QUALIFIED_NAME, elem.getKind());
            assertEquals(3, elem.asSimpleNameList().size());
            assertSameLocation("".length(), "java".length(), elem.asSimpleNameList().get(0).getLocation());
            assertSameLocation("java.".length(), "lang".length(), elem.asSimpleNameList().get(1).getLocation());
            assertSameLocation("java.lang.".length(), "String".length(), elem.asSimpleNameList().get(2).getLocation());
            assertEquals(SHARP, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string(".java.lang.String");
            IrDocName elem = JavadocBlockParserUtil.fetchName(scanner, null);
            assertNull(elem);
            assertEquals(DOT, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("java.lang.String#length()");
            IrDocName elem = JavadocBlockParserUtil.fetchName(scanner, EnumSet.of(WHITE_SPACES));
            assertNull(elem);
            assertEquals(IDENTIFIER, scanner.lookahead(0).getKind());
        }
    }

    /**
     * Test method for {@link JavadocBlockParserUtil#fetchBasicType(JavadocScanner, java.util.Set)}.
     */
    @Test
    public void testFetchBasicType() {
        {
            DefaultJavadocScanner scanner = string("int");
            IrDocBasicType elem = JavadocBlockParserUtil.fetchBasicType(scanner, null);
            assertNotNull(elem);
            assertEquals(IrBasicTypeKind.INT, elem.getTypeKind());
            assertSameLocation(0, "int".length(), elem.getLocation());
            assertEquals(EOF, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("void");
            IrDocBasicType elem = JavadocBlockParserUtil.fetchBasicType(scanner, null);
            assertNotNull(elem);
            assertEquals(IrBasicTypeKind.VOID, elem.getTypeKind());
            assertSameLocation(0, "void".length(), elem.getLocation());
            assertEquals(EOF, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("int[]");
            IrDocBasicType elem = JavadocBlockParserUtil.fetchBasicType(scanner, null);
            assertNotNull(elem);
            assertEquals(IrBasicTypeKind.INT, elem.getTypeKind());
            assertSameLocation(0, "int".length(), elem.getLocation());
            assertEquals(LEFT_BRACKET, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("#int[]");
            IrDocBasicType elem = JavadocBlockParserUtil.fetchBasicType(scanner, null);
            assertNull(elem);
            assertEquals(SHARP, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("int[]");
            IrDocBasicType elem = JavadocBlockParserUtil.fetchBasicType(scanner, EnumSet.of(WHITE_SPACES));
            assertNull(elem);
            assertEquals(IDENTIFIER, scanner.lookahead(0).getKind());
        }
    }

    /**
     * Test method for {@link JavadocBlockParserUtil#fetchPrimitiveType(JavadocScanner, java.util.Set)}.
     */
    @Test
    public void testFetchPrimitiveType() {
        {
            DefaultJavadocScanner scanner = string("int");
            IrDocBasicType elem = JavadocBlockParserUtil.fetchPrimitiveType(scanner, null);
            assertNotNull(elem);
            assertEquals(IrBasicTypeKind.INT, elem.getTypeKind());
            assertSameLocation(0, "int".length(), elem.getLocation());
            assertEquals(EOF, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("void");
            IrDocBasicType elem = JavadocBlockParserUtil.fetchPrimitiveType(scanner, null);
            assertNull(elem);
            assertEquals(IDENTIFIER, scanner.lookahead(0).getKind());
        }
    }

    /**
     * Test method for {@link JavadocBlockParserUtil#fetchNamedType(JavadocScanner, java.util.Set)}.
     */
    @Test
    public void testFetchNamedType() {
        {
            DefaultJavadocScanner scanner = string("String#length()");
            IrDocNamedType elem = JavadocBlockParserUtil.fetchNamedType(scanner, null);
            assertNotNull(elem);
            assertEquals("String", elem.getName().asString());
            assertSameLocation(0, "String".length(), elem.getLocation());
            assertEquals(SHARP, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("java.lang.String#length()");
            IrDocNamedType elem = JavadocBlockParserUtil.fetchNamedType(scanner, null);
            assertNotNull(elem);
            assertEquals("java.lang.String", elem.getName().asString());
            assertSameLocation(0, "java.lang.String".length(), elem.getLocation());
            assertEquals(SHARP, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("/String#length()");
            IrDocNamedType elem = JavadocBlockParserUtil.fetchNamedType(scanner, null);
            assertNull(elem);
            assertEquals(SLASH, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("String#length()");
            IrDocNamedType elem = JavadocBlockParserUtil.fetchNamedType(scanner, EnumSet.of(WHITE_SPACES));
            assertNull(elem);
            assertEquals(IDENTIFIER, scanner.lookahead(0).getKind());
        }
    }

    /**
     * Test method for {@link JavadocBlockParserUtil#fetchType(JavadocScanner, java.util.Set)}.
     */
    @Test
    public void testFetchType() {
        {
            DefaultJavadocScanner scanner = string("double");
            IrDocType type = JavadocBlockParserUtil.fetchType(scanner, null);
            assertNotNull(type);

            assertEquals(IrDocElementKind.BASIC_TYPE, type.getKind());
            IrDocBasicType elem = (IrDocBasicType) type;

            assertEquals(IrBasicTypeKind.DOUBLE, elem.getTypeKind());
            assertSameLocation(0, "double".length(), elem.getLocation());
            assertEquals(EOF, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("float[]");
            IrDocType type = JavadocBlockParserUtil.fetchType(scanner, null);
            assertNotNull(type);

            assertEquals(IrDocElementKind.ARRAY_TYPE, type.getKind());
            IrDocArrayType array = (IrDocArrayType) type;
            assertSameLocation(0, "float[]".length(), array.getLocation());

            IrDocBasicType elem = (IrDocBasicType) array.getComponentType();
            assertEquals(IrBasicTypeKind.FLOAT, elem.getTypeKind());
            assertSameLocation(0, "float".length(), elem.getLocation());
            assertEquals(EOF, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("char[][]");
            IrDocType type = JavadocBlockParserUtil.fetchType(scanner, null);
            assertNotNull(type);

            assertEquals(IrDocElementKind.ARRAY_TYPE, type.getKind());
            IrDocArrayType array = (IrDocArrayType) type;
            assertSameLocation(0, "char[][]".length(), array.getLocation());

            IrDocType component = array.getComponentType();
            assertEquals(IrDocElementKind.ARRAY_TYPE, component.getKind());
            IrDocArrayType array2 = (IrDocArrayType) component;
            assertSameLocation(0, "char[]".length(), array2.getLocation());

            IrDocBasicType elem = (IrDocBasicType) array2.getComponentType();
            assertEquals(IrBasicTypeKind.CHAR, elem.getTypeKind());
            assertSameLocation(0, "char".length(), elem.getLocation());
            assertEquals(EOF, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("java.lang.String");
            IrDocType type = JavadocBlockParserUtil.fetchType(scanner, null);
            assertNotNull(type);
            assertEquals(IrDocElementKind.NAMED_TYPE, type.getKind());
            IrDocNamedType elem = (IrDocNamedType) type;
            assertEquals("java.lang.String", elem.getName().asString());
            assertSameLocation(0, "java.lang.String".length(), elem.getLocation());
            assertEquals(EOF, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("java.lang.String[]");
            IrDocType type = JavadocBlockParserUtil.fetchType(scanner, null);
            assertNotNull(type);

            assertEquals(IrDocElementKind.ARRAY_TYPE, type.getKind());
            IrDocArrayType array = (IrDocArrayType) type;
            assertSameLocation(0, "java.lang.String[]".length(), array.getLocation());

            IrDocNamedType elem = (IrDocNamedType) array.getComponentType();
            assertEquals("java.lang.String", elem.getName().asString());
            assertSameLocation(0, "java.lang.String".length(), elem.getLocation());

            assertEquals(EOF, scanner.lookahead(0).getKind());
        }
    }

    /**
     * Test method for {@link JavadocBlockParserUtil#fetchField(JavadocScanner, java.util.Set)}.
     */
    @Test
    public void testFetchField() {
        {
            DefaultJavadocScanner scanner = string("Math#PI{");
            IrDocField elem = JavadocBlockParserUtil.fetchField(scanner, null);
            assertNotNull(elem);

            assertEquals("Math", elem.getDeclaringType().getName().asString());
            assertEquals("PI", elem.getName().getIdentifier());
            assertSameLocation(0, "Math#PI".length(), elem.getLocation());

            assertEquals(LEFT_BRACE, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("#PI{");
            IrDocField elem = JavadocBlockParserUtil.fetchField(scanner, null);
            assertNotNull(elem);

            assertNull(elem.getDeclaringType());
            assertEquals("PI", elem.getName().getIdentifier());
            assertSameLocation(0, "#PI".length(), elem.getLocation());

            assertEquals(LEFT_BRACE, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("/Math#PI");
            IrDocField elem = JavadocBlockParserUtil.fetchField(scanner, null);
            assertNull(elem);
            assertEquals(0, scanner.getIndex());
        }
        {
            DefaultJavadocScanner scanner = string("Math+#PI");
            IrDocField elem = JavadocBlockParserUtil.fetchField(scanner, null);
            assertNull(elem);
            assertEquals(0, scanner.getIndex());
        }
        {
            DefaultJavadocScanner scanner = string("Math#+PI");
            IrDocField elem = JavadocBlockParserUtil.fetchField(scanner, null);
            assertNull(elem);
            assertEquals(0, scanner.getIndex());
        }
        {
            DefaultJavadocScanner scanner = string("Math#PI{");
            IrDocField elem = JavadocBlockParserUtil.fetchField(scanner, EnumSet.of(WHITE_SPACES));
            assertNull(elem);
            assertEquals(0, scanner.getIndex());
        }
    }

    /**
     * Test method for {@link JavadocBlockParserUtil#fetchMethod(JavadocScanner, java.util.Set)}.
     */
    @Test
    public void testFetchMethod() {
        {
            DefaultJavadocScanner scanner = string("String#length(){");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, null);
            assertNotNull(elem);
            assertEquals("String", elem.getDeclaringType().getName().asString());
            assertEquals("length", elem.getName().asString());
            assertEquals(0, elem.getParameters().size());
            assertEquals(LEFT_BRACE, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("#length(){");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, null);
            assertNotNull(elem);
            assertNull(elem.getDeclaringType());
            assertEquals("length", elem.getName().asString());
            assertEquals(0, elem.getParameters().size());
            assertEquals(LEFT_BRACE, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("Math#abs(int)");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, null);
            assertNotNull(elem);
            assertEquals("Math", elem.getDeclaringType().getName().asString());
            assertEquals("abs", elem.getName().asString());
            assertEquals(1, elem.getParameters().size());
            {
                IrDocMethodParameter param = elem.getParameters().get(0);
                assertEquals(IrDocElementKind.BASIC_TYPE, param.getType().getKind());
                assertEquals(IrBasicTypeKind.INT, ((IrDocBasicType) param.getType()).getTypeKind());
                assertNull(param.getName());
            }
            assertEquals(EOF, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("Math#abs(int number)");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, null);
            assertNotNull(elem);
            assertEquals("Math", elem.getDeclaringType().getName().asString());
            assertEquals("abs", elem.getName().asString());
            assertEquals(1, elem.getParameters().size());
            {
                IrDocMethodParameter param = elem.getParameters().get(0);
                assertEquals(IrDocElementKind.BASIC_TYPE, param.getType().getKind());
                assertEquals(IrBasicTypeKind.INT, ((IrDocBasicType) param.getType()).getTypeKind());
                assertFalse(param.isVariableArity());
                assertNotNull(param.getName());
                assertEquals("number", param.getName().getIdentifier());
            }
            assertEquals(EOF, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("Arrays#asList(Object...)");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, null);
            assertNotNull(elem);
            assertEquals("Arrays", elem.getDeclaringType().getName().asString());
            assertEquals("asList", elem.getName().asString());
            assertEquals(1, elem.getParameters().size());
            {
                IrDocMethodParameter param = elem.getParameters().get(0);
                assertEquals(IrDocElementKind.NAMED_TYPE, param.getType().getKind());
                assertEquals("Object", ((IrDocNamedType) param.getType()).getName().asString());
                assertTrue(param.isVariableArity());
                assertNull(param.getName());
            }
            assertEquals(EOF, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("Arrays#asList(Object...elems)");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, null);
            assertNotNull(elem);
            assertEquals("Arrays", elem.getDeclaringType().getName().asString());
            assertEquals("asList", elem.getName().asString());
            assertEquals(1, elem.getParameters().size());
            {
                IrDocMethodParameter param = elem.getParameters().get(0);
                assertEquals(IrDocElementKind.NAMED_TYPE, param.getType().getKind());
                assertEquals("Object", ((IrDocNamedType) param.getType()).getName().asString());
                assertTrue(param.isVariableArity());
                assertNotNull(param.getName());
                assertEquals("elems", param.getName().getIdentifier());
            }
            assertEquals(EOF, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("Math#max(int, int)");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, null);
            assertNotNull(elem);
            assertEquals("Math", elem.getDeclaringType().getName().asString());
            assertEquals("max", elem.getName().asString());
            assertEquals(2, elem.getParameters().size());
            {
                IrDocMethodParameter param = elem.getParameters().get(0);
                assertEquals(IrDocElementKind.BASIC_TYPE, param.getType().getKind());
                assertEquals(IrBasicTypeKind.INT, ((IrDocBasicType) param.getType()).getTypeKind());
                assertFalse(param.isVariableArity());
                assertNull(param.getName());
            }
            {
                IrDocMethodParameter param = elem.getParameters().get(1);
                assertEquals(IrDocElementKind.BASIC_TYPE, param.getType().getKind());
                assertEquals(IrBasicTypeKind.INT, ((IrDocBasicType) param.getType()).getTypeKind());
                assertFalse(param.isVariableArity());
                assertNull(param.getName());
            }
            assertEquals(EOF, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("Math#max(int a, int b)");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, null);
            assertNotNull(elem);
            assertEquals("Math", elem.getDeclaringType().getName().asString());
            assertEquals("max", elem.getName().asString());
            assertEquals(2, elem.getParameters().size());
            {
                IrDocMethodParameter param = elem.getParameters().get(0);
                assertEquals(IrDocElementKind.BASIC_TYPE, param.getType().getKind());
                assertEquals(IrBasicTypeKind.INT, ((IrDocBasicType) param.getType()).getTypeKind());
                assertFalse(param.isVariableArity());
                assertNotNull(param.getName());
                assertEquals("a", param.getName().getIdentifier());
            }
            {
                IrDocMethodParameter param = elem.getParameters().get(1);
                assertEquals(IrDocElementKind.BASIC_TYPE, param.getType().getKind());
                assertEquals(IrBasicTypeKind.INT, ((IrDocBasicType) param.getType()).getTypeKind());
                assertFalse(param.isVariableArity());
                assertNotNull(param.getName());
                assertEquals("b", param.getName().getIdentifier());
            }
            assertEquals(EOF, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("/Math#max(int a, int b)");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, null);
            assertNull(elem);
            assertEquals(0, scanner.getIndex());
        }
        {
            DefaultJavadocScanner scanner = string("Math+#max(int a, int b)");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, null);
            assertNull(elem);
            assertEquals(0, scanner.getIndex());
        }
        {
            DefaultJavadocScanner scanner = string("Math#+max(int a, int b)");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, null);
            assertNull(elem);
            assertEquals(0, scanner.getIndex());
        }
        {
            DefaultJavadocScanner scanner = string("Math#max+(int a, int b)");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, null);
            assertNull(elem);
            assertEquals(0, scanner.getIndex());
        }
        {
            DefaultJavadocScanner scanner = string("Math#max(+int a, int b)");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, null);
            assertNull(elem);
            assertEquals(0, scanner.getIndex());
        }
        {
            DefaultJavadocScanner scanner = string("Math#max(int *a, int b)");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, null);
            assertNull(elem);
            assertEquals(0, scanner.getIndex());
        }
        {
            DefaultJavadocScanner scanner = string("Math#max(int a+, int b)");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, null);
            assertNull(elem);
            assertEquals(0, scanner.getIndex());
        }
        {
            DefaultJavadocScanner scanner = string("Math#max(int a int b)");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, null);
            assertNull(elem);
            assertEquals(0, scanner.getIndex());
        }
        {
            DefaultJavadocScanner scanner = string("Math#max(int a, int b");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, null);
            assertNull(elem);
            assertEquals(0, scanner.getIndex());
        }
        {
            DefaultJavadocScanner scanner = string("String#length(){");
            IrDocMethod elem = JavadocBlockParserUtil.fetchMethod(scanner, EnumSet.of(WHITE_SPACES));
            assertNull(elem);
            assertEquals(0, scanner.getIndex());
        }
    }

    /**
     * Test method for {@link JavadocBlockParserUtil#fetchLinkTarget(JavadocScanner, java.util.Set)}.
     */
    @Test
    public void testFetchLinkTarget() {
        {
            DefaultJavadocScanner scanner = string("java.lang.String");
            IrDocFragment target = JavadocBlockParserUtil.fetchLinkTarget(scanner, null);
            assertNotNull(target);
            assertEquals(IrDocElementKind.NAMED_TYPE, target.getKind());
            IrDocNamedType elem = (IrDocNamedType) target;
            assertEquals("java.lang.String", elem.getName().asString());
            assertSameLocation(0, "java.lang.String".length(), elem.getLocation());
            assertEquals(EOF, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("Math#PI{");
            IrDocFragment target = JavadocBlockParserUtil.fetchLinkTarget(scanner, null);
            assertNotNull(target);
            assertEquals(IrDocElementKind.FIELD, target.getKind());
            IrDocField elem = (IrDocField) target;
            assertEquals("Math", elem.getDeclaringType().getName().asString());
            assertEquals("PI", elem.getName().getIdentifier());
            assertSameLocation(0, "Math#PI".length(), elem.getLocation());

            assertEquals(LEFT_BRACE, scanner.lookahead(0).getKind());
        }
        {
            DefaultJavadocScanner scanner = string("Math#max(int a, int b)");
            IrDocFragment target = JavadocBlockParserUtil.fetchLinkTarget(scanner, null);
            assertNotNull(target);
            assertEquals(IrDocElementKind.METHOD, target.getKind());
            IrDocMethod elem = (IrDocMethod) target;
            assertEquals("Math", elem.getDeclaringType().getName().asString());
            assertEquals("max", elem.getName().asString());
            assertEquals(2, elem.getParameters().size());
            {
                IrDocMethodParameter param = elem.getParameters().get(0);
                assertEquals(IrDocElementKind.BASIC_TYPE, param.getType().getKind());
                assertEquals(IrBasicTypeKind.INT, ((IrDocBasicType) param.getType()).getTypeKind());
                assertFalse(param.isVariableArity());
                assertNotNull(param.getName());
                assertEquals("a", param.getName().getIdentifier());
            }
            {
                IrDocMethodParameter param = elem.getParameters().get(1);
                assertEquals(IrDocElementKind.BASIC_TYPE, param.getType().getKind());
                assertEquals(IrBasicTypeKind.INT, ((IrDocBasicType) param.getType()).getTypeKind());
                assertFalse(param.isVariableArity());
                assertNotNull(param.getName());
                assertEquals("b", param.getName().getIdentifier());
            }
            assertEquals(EOF, scanner.lookahead(0).getKind());
        }
    }

    private void assertSameLocation(int start, int length, IrLocation location) {
        assertEquals(start, location.getStartPosition());
        assertEquals(length, location.getLength());
    }
}
