/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBlock;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocComment;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrLocation;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocToken;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocTokenKind;

/**
 * A parser for Java documentation comments.
 */
public final class JavadocParser extends JavadocBaseParser {

    /**
     * Creates a new instance.
     * @param blockParsers the block parsers
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public JavadocParser(List<? extends JavadocBlockParser> blockParsers) {
        super(blockParsers);
    }

    /**
     * Parses the documentation comments.
     * @param scanner the source scanner
     * @return the analyzed element
     * @throws JavadocParseException if the documentation comment is malformed
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public IrDocComment parse(JavadocScanner scanner) throws JavadocParseException {
        if (scanner == null) {
            throw new IllegalArgumentException("scanner"); //$NON-NLS-1$
        }
        int index = scanner.getIndex();
        try {
            JavadocInfo info = fetchJavadocInfo(scanner);
            List<IrDocBlock> blocks = new ArrayList<>(info.getBlocks().size());
            for (JavadocBlockInfo b: info.getBlocks()) {
                IrDocBlock block = parseBlock(b);
                blocks.add(block);
            }
            IrDocComment elem = new IrDocComment();
            elem.setBlocks(blocks);
            elem.setLocation(info.getLocation());
            return elem;
        } catch (JavadocParseException e) {
            scanner.seek(index);
            throw e;
        }
    }

    /**
     * Parses a block.
     * @param scanner the source scanner
     * @return the analyzed block
     * @throws JavadocParseException if the block is malformed
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public IrDocBlock parseBlock(JavadocScanner scanner) throws JavadocParseException {
        if (scanner == null) {
            throw new IllegalArgumentException("scanner"); //$NON-NLS-1$
        }

        // search for end of comment
        int eoc = JavadocScannerUtil.countUntilCommentEnd(scanner, true, 0);
        if (eoc >= 0) {
            throw new IllegalEndOfCommentException(scanner.lookahead(0).getLocation(), null);
        }

        JavadocTokenKind kind = scanner.lookahead(0).getKind();
        int index = scanner.getIndex();
        try {
            JavadocBlockInfo info;
            if (kind == JavadocTokenKind.AT) {
                info = fetchStandAloneBlock(scanner, scanner.getTokens());
            } else {
                info = fetchSynopsisBlock(scanner);
            }

            if (info == null) {
                throw new IllegalArgumentException("Empty block");
            }

            return parseBlock(info);
        } catch (JavadocParseException e) {
            scanner.seek(index);
            throw e;
        }

    }

    private static JavadocInfo fetchJavadocInfo(JavadocScanner scanner) throws IllegalDocCommentFormatException {
        int offset = 0;

        IrLocation firstLocation = scanner.lookahead(0).getLocation();

        // check: "/" "*" "*"
        if (!hasJavadocHead(scanner, offset)) {
            throw new IllegalDocCommentFormatException(true, scanner.lookahead(0).getLocation(), null);
        }
        offset += 3;

        // avoid: "/" "*" "*" "/"
        if (scanner.lookahead(offset).getKind() == JavadocTokenKind.SLASH) {
            throw new IllegalDocCommentFormatException(true, scanner.lookahead(0).getLocation(), null);
        }

        // check content range
        int bodyStart = offset;
        offset += JavadocScannerUtil.countUntilCommentEnd(scanner, false, offset);
        int bodyEnd = offset;

        // check: "*" "/"
        if (!hasJavadocTail(scanner, offset)) {
            throw new IllegalDocCommentFormatException(false, scanner.lookahead(0).getLocation(), null);
        }

        IrLocation lastLocation = scanner.lookahead(offset + 1).getLocation();

        // create content scanner
        int base = scanner.getIndex();
        JavadocScanner bodyScanner = new DefaultJavadocScanner(
            scanner.getTokens().subList(base + bodyStart, base + bodyEnd),
            scanner.lookahead(offset).getStartPosition());

        List<JavadocBlockInfo> blockScanners = splitBlocks(bodyScanner);

        int locStart = firstLocation.getStartPosition();
        int locEnd = lastLocation.getStartPosition() + lastLocation.getLength();
        IrLocation location = new IrLocation(locStart, locEnd - locStart);
        scanner.consume(offset);
        return new JavadocInfo(location, blockScanners);
    }

    private static List<JavadocBlockInfo> splitBlocks(JavadocScanner scanner) {
        List<JavadocBlockInfo> blocks = new ArrayList<>();

        JavadocBlockInfo synopsis = fetchSynopsisBlock(scanner);
        if (synopsis != null) {
            blocks.add(synopsis);
        }

        List<JavadocToken> tokens = scanner.getTokens();
        while (true) {
            JavadocBlockInfo info = fetchStandAloneBlock(scanner, tokens);
            if (info == null) {
                break;
            }
            blocks.add(info);
        }

        return blocks;
    }

    private static JavadocBlockInfo fetchStandAloneBlock(JavadocScanner scanner, List<JavadocToken> tokens) {
        JavadocToken first = scanner.lookahead(0);
        JavadocTokenKind kind = first.getKind();
        if (kind == JavadocTokenKind.EOF) {
            return null;
        }
        if (kind != JavadocTokenKind.AT) {
            throw new AssertionError(MessageFormat.format(
                "AT <-> {0}({1})@{2}", //$NON-NLS-1$
                first,
                first.getKind(),
                first.getLocation()));
        }

        scanner.consume(1);

        // fetch: tag name
        int tagCount = JavadocBlockParserUtil.countWhileTagName(scanner, 0);
        List<JavadocToken> tagNames = new ArrayList<>(tagCount);
        for (int i = 0; i < tagCount; i++) {
            tagNames.add(scanner.nextToken());
        }
        String tagName = JavadocBlockParserUtil.buildString(tagNames);

        // fetch: body
        int bodyStart = scanner.getIndex();
        int count = JavadocScannerUtil.countUntilBlockEnd(scanner, 0);

        int success = scanner.lookahead(count).getStartPosition();
        DefaultJavadocScanner bs = new DefaultJavadocScanner(
            new ArrayList<>(tokens.subList(bodyStart, bodyStart + count)),
            success);
        int init = first.getStartPosition();
        IrLocation location = new IrLocation(init, success - init);
        JavadocBlockInfo info = new JavadocBlockInfo(tagName, bs, location);
        scanner.consume(count);

        return info;
    }

    private static JavadocBlockInfo fetchSynopsisBlock(JavadocScanner scanner) {
        int start = scanner.getIndex();

        List<JavadocToken> tokens = scanner.getTokens();

        int offset = 0;

        // remove top "*"
        offset += JavadocScannerUtil.countWhile(EnumSet.of(JavadocTokenKind.ASTERISK), scanner, offset);

        // remove white-spaces
        offset += JavadocScannerUtil.countUntilNextPrintable(scanner, offset);

        JavadocTokenKind kind = scanner.lookahead(offset).getKind();
        if (kind == JavadocTokenKind.AT || kind == JavadocTokenKind.EOF) {
            // no synopsis block
            scanner.consume(offset);
            return null;
        } else {
            int count = JavadocScannerUtil.countUntilBlockEnd(scanner, offset);
            int success = scanner.lookahead(offset + count).getStartPosition();
            DefaultJavadocScanner bs = new DefaultJavadocScanner(
                new ArrayList<>(tokens.subList(start + offset, start + offset + count)),
                success);
            JavadocToken first = scanner.lookahead(offset);
            int init = first.getStartPosition();
            IrLocation location = new IrLocation(init, success - init);
            JavadocBlockInfo info = new JavadocBlockInfo(null, bs, location);
            scanner.consume(offset + count);
            return info;
        }
    }

    private static boolean hasJavadocHead(JavadocScanner scanner, int start) {
        if (scanner.lookahead(start + 0).getKind() != JavadocTokenKind.SLASH) {
            return false;
        }
        if (scanner.lookahead(start + 1).getKind() != JavadocTokenKind.ASTERISK) {
            return false;
        }
        if (scanner.lookahead(start + 2).getKind() != JavadocTokenKind.ASTERISK) {
            return false;
        }
        return true;
    }

    private static boolean hasJavadocTail(JavadocScanner scanner, int start) {
        if (scanner.lookahead(start + 0).getKind() != JavadocTokenKind.ASTERISK) {
            return false;
        }
        if (scanner.lookahead(start + 1).getKind() != JavadocTokenKind.SLASH) {
            return false;
        }
        return true;
    }

    private static class JavadocInfo {

        private final IrLocation location;
        private final List<JavadocBlockInfo> blockScanners;

        JavadocInfo(IrLocation location, List<JavadocBlockInfo> blockScanners) {
            this.location = location;
            this.blockScanners = blockScanners;
        }

        public IrLocation getLocation() {
            return this.location;
        }

        public List<JavadocBlockInfo> getBlocks() {
            return this.blockScanners;
        }
    }
}
