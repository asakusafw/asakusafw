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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBasicType;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBlock;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocField;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocFragment;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocMethod;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocName;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocNamedType;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocSimpleName;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocText;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocType;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocTokenKind;

/**
 * An abstract super class of Java documentation comment block parsers.
 */
public abstract class JavadocBlockParser extends JavadocBaseParser {

    private static final Set<JavadocTokenKind> S_FOLLOW;
    static {
        Set<JavadocTokenKind> set = EnumSet.noneOf(JavadocTokenKind.class);
        set.add(JavadocTokenKind.WHITE_SPACES);
        set.add(JavadocTokenKind.LINE_BREAK);
        set.add(JavadocTokenKind.EOF);
        S_FOLLOW = Collections.unmodifiableSet(set);
    }

    /**
     * Creates a new instance without any inline block parsers.
     */
    protected JavadocBlockParser() {
        this(Collections.emptyList());
    }

    /**
     * Creates a new instance with inline block parsers.
     * @param blockParsers the inline block parsers
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    protected JavadocBlockParser(List<? extends JavadocBlockParser> blockParsers) {
        super(blockParsers);
    }

    /**
     * Returns whether this parser can accept the block with target tag name.
     * If the target is an synopsis block, the tag name will be {@code null}.
     * @param tag the target tag name (without <code>&quot;&#64;&quot;</code>), or {@code null} for synopsis blocks
     * @return {@code true} if this parser can accept the target block, otherwise {@code false}
     */
    public abstract boolean canAccept(String tag);

    /**
     * Parses a block.
     * The scanner will provides contents of the target block (without its block tag).
     * @param tag the target tag name (without <code>&quot;&#64;&quot;</code>), or {@code null} for synopsis blocks
     * @param scanner the scanner for providing block contents
     * @return the parsed block
     * @throws JavadocParseException if error occurred while parsing the target block
     * @throws IllegalArgumentException if {@code scanner} is {@code null}
     */
    public abstract IrDocBlock parse(String tag, JavadocScanner scanner) throws JavadocParseException;

    /**
     * Creates a new block from its tag and fragments.
     * @param tag the tag name, or {@code null} for synopsis blocks
     * @param fragments the fragments
     * @return the created block
     * @throws IllegalArgumentException if {@code fragments} is {@code null}
     */
    public IrDocBlock newBlock(String tag, List<? extends IrDocFragment> fragments) {
        if (fragments == null) {
            throw new IllegalArgumentException("fragments"); //$NON-NLS-1$
        }
        IrDocBlock block = new IrDocBlock();
        block.setTag(tag);
        block.setFragments(fragments);
        return block;
    }

    /**
     * Consumes all tokens from the scanner, and converts them to a list of {@link IrDocFragment}s.
     * @param scanner the target scanner
     * @return the converted fragments
     * @throws JavadocParseException if error was occurred while converting tokens
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public List<IrDocFragment> fetchRestFragments(JavadocScanner scanner) throws JavadocParseException {
        int index = scanner.getIndex();
        try {
            ArrayList<IrDocFragment> fragments = new ArrayList<>();
            while (true) {
                JavadocTokenKind la = scanner.lookahead(0).getKind();
                if (la == JavadocTokenKind.LINE_BREAK) {
                    int count = JavadocScannerUtil.countUntilNextPrintable(scanner, 0);
                    scanner.consume(count);
                } else if (la == JavadocTokenKind.LEFT_BRACE) {
                    JavadocBlockInfo info = JavadocBlockParserUtil.fetchBlockInfo(scanner);
                    IrDocBlock inline = parseBlock(info);
                    fragments.add(inline);
                } else if (la == JavadocTokenKind.EOF) {
                    break;
                } else {
                    IrDocText text = JavadocBlockParserUtil.fetchText(scanner, false, false);
                    fragments.add(text);
                }
            }
            fragments.trimToSize();
            return fragments;
        } catch (JavadocParseException e) {
            scanner.seek(index);
            throw e;
        }
    }

    /**
     * Consumes tokens from the scanner and returns the corresponded simple name.
     * This will ignore successive while space tokens, and tokens are removed from the scanner only if this operation
     * was successfully completed.
     * @param scanner the target scanner
     * @return the analyzed element if this operation was successfully completed, otherwise {@code null}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public IrDocSimpleName fetchSimpleName(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchSimpleName(scanner, S_FOLLOW);
    }

    /**
     * Consumes tokens from the scanner and returns the corresponded (simple or qualified) name.
     * This will ignore successive while space tokens, and tokens are removed from the scanner only if this operation
     * was successfully completed.
     * @param scanner the target scanner
     * @return the analyzed element if this operation was successfully completed, otherwise {@code null}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public IrDocName fetchName(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchName(scanner, S_FOLLOW);
    }

    /**
     * Consumes tokens from the scanner and returns the corresponded basic type.
     * This will ignore successive while space tokens, and tokens are removed from the scanner only if this operation
     * was successfully completed.
     * @param scanner the target scanner
     * @return the analyzed element if this operation was successfully completed, otherwise {@code null}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public IrDocBasicType fetchBasicType(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchBasicType(scanner, S_FOLLOW);
    }

    /**
     * Consumes tokens from the scanner and returns the corresponded primitive type.
     * This will ignore successive while space tokens, and tokens are removed from the scanner only if this operation
     * was successfully completed.
     * @param scanner the target scanner
     * @return the analyzed element if this operation was successfully completed, otherwise {@code null}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public IrDocBasicType fetchPrimitiveType(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchPrimitiveType(scanner, S_FOLLOW);
    }

    /**
     * Consumes tokens from the scanner and returns the corresponded named type.
     * This will ignore successive while space tokens, and tokens are removed from the scanner only if this operation
     * was successfully completed.
     * @param scanner the target scanner
     * @return the analyzed element if this operation was successfully completed, otherwise {@code null}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public IrDocNamedType fetchNamedType(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchNamedType(scanner, S_FOLLOW);
    }

    /**
     * Consumes tokens from the scanner and returns the corresponded type.
     * This will ignore successive while space tokens, and tokens are removed from the scanner only if this operation
     * was successfully completed.
     * @param scanner the target scanner
     * @return the analyzed element if this operation was successfully completed, otherwise {@code null}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public IrDocType fetchType(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchType(scanner, S_FOLLOW);
    }

    /**
     * Consumes tokens from the scanner and returns the corresponded field.
     * This will ignore successive while space tokens, and tokens are removed from the scanner only if this operation
     * was successfully completed.
     * @param scanner the target scanner
     * @return the analyzed element if this operation was successfully completed, otherwise {@code null}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public IrDocField fetchField(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchField(scanner, S_FOLLOW);
    }

    /**
     * Consumes tokens from the scanner and returns the corresponded method or constructor.
     * This will ignore successive while space tokens, and tokens are removed from the scanner only if this operation
     * was successfully completed.
     * @param scanner the target scanner
     * @return the analyzed element if this operation was successfully completed, otherwise {@code null}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public IrDocMethod fetchMethod(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchMethod(scanner, S_FOLLOW);
    }

    /**
     * Consumes tokens from the scanner and returns the corresponded link target.
     * The link target means one of type, field, method, or constructor.
     * This will ignore successive while space tokens, and tokens are removed from the scanner only if this operation
     * was successfully completed.
     * @param scanner the target scanner
     * @return the analyzed element if this operation was successfully completed, otherwise {@code null}
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public IrDocFragment fetchLinkTarget(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchLinkTarget(scanner, S_FOLLOW);
    }
}
