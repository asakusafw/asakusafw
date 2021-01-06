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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocToken;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocTokenKind;

/**
 * Utilities for {@link JavadocScanner}.
 */
public final class JavadocScannerUtil {

    private static final Set<JavadocTokenKind> S_LINE_BREAK = Collections.singleton(JavadocTokenKind.LINE_BREAK);

    private static final Set<JavadocTokenKind> S_WHITE_SPACES = Collections.singleton(JavadocTokenKind.WHITE_SPACES);

    private static final Set<JavadocTokenKind> S_ASTERISK = Collections.singleton(JavadocTokenKind.ASTERISK);

    private static final Set<JavadocTokenKind> S_RIGHT_BRACE = Collections.singleton(JavadocTokenKind.RIGHT_BRACE);

    private JavadocScannerUtil() {
        return;
    }

    /**
     * Returns the a sequence of tokens.
     * This will returns number of tokens specified by {@code count},
     * except EOF token ({@link JavadocTokenKind#EOF}) and its trailing tokens.
     * @param scanner the target scanner
     * @param start the starting index
     * @param count the max token count
     * @return the tokens in the range
     */
    public static List<JavadocToken> lookaheadTokens(JavadocScanner scanner, int start, int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        List<JavadocToken> tokens = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            JavadocToken token = scanner.lookahead(start + i);
            if (token.getKind() == JavadocTokenKind.EOF) {
                break;
            }
            tokens.add(token);
        }
        return tokens;
    }

    /**
     * Returns the number of tokens from the starting index while each token has the specified kind.
     * @param kinds the target kinds ({@link JavadocTokenKind#EOF} will be ignored)
     * @param scanner the target scanner
     * @param start the starting index
     * @return the number of tokens
     */
    public static int countWhile(Collection<JavadocTokenKind> kinds, JavadocScanner scanner, int start) {
        return countWhileUntil(kinds, scanner, start, false);
    }

    /**
     * Returns the number of tokens from the starting index until each token has the specified kind.
     * @param kinds the target kinds ({@link JavadocTokenKind#EOF} will be always enabled)
     * @param scanner the target scanner
     * @param start the starting index
     * @return the number of tokens
     */
    public static int countUntil(Collection<JavadocTokenKind> kinds, JavadocScanner scanner, int start) {
        return countWhileUntil(kinds, scanner, start, true);
    }

    /**
     * Returns the number of tokens from the starting index until the current block was closed.
     * The terminal token will be not in the count.
     * @param scanner the target scanner
     * @param start the starting index
     * @return the number of tokens
     */
    public static int countUntilBlockEnd(JavadocScanner scanner, int start) {
        int offset = 0;
        while (true) {
            JavadocTokenKind kind = scanner.lookahead(start + offset).getKind();
            if (kind == JavadocTokenKind.LEFT_BRACE) {
                offset++;
                JavadocTokenKind la = scanner.lookahead(start + offset).getKind();
                if (la == JavadocTokenKind.AT) {
                    offset++;
                    offset += countUntil(S_RIGHT_BRACE, scanner, start + offset);
                }
            } else if (kind == JavadocTokenKind.LINE_BREAK) {
                offset += countUntilNextPrintable(scanner, start + offset);
                JavadocTokenKind la = scanner.lookahead(start + offset).getKind();
                if (la == JavadocTokenKind.AT) {
                    return offset;
                }
            } else if (kind == JavadocTokenKind.EOF) {
                return offset;
            } else {
                offset++;
            }
        }
    }

    /**
     * Returns the number of tokens from the starting index until the end of this comment.
     * @param scanner the target scanner
     * @param returnMinusIfMissing
     *      {@code true} to return {@code -1} if missing the end of comment,
     *      or {@code false} to return the number of tokens if missing it
     * @param start the starting index
     * @return the number of tokens
     */
    public static int countUntilCommentEnd(JavadocScanner scanner, boolean returnMinusIfMissing, int start) {
        JavadocToken token = scanner.lookahead(start);
        if (token.getKind() == JavadocTokenKind.EOF) {
            return (returnMinusIfMissing ? -1 : 0);
        }
        int offset = 1;
        boolean sawAster = (token.getKind() == JavadocTokenKind.ASTERISK);
        while (true) {
            JavadocToken la = scanner.lookahead(start + offset);
            JavadocTokenKind kind = la.getKind();
            if (kind == JavadocTokenKind.EOF) {
                return (returnMinusIfMissing ? -1 : offset);
            } else if (sawAster && kind == JavadocTokenKind.SLASH) {
                offset--;
                return offset;
            } else if (kind == JavadocTokenKind.ASTERISK) {
                sawAster = true;
                offset++;
            } else {
                sawAster = false;
                offset++;
            }
        }
    }

    private static int countWhileUntil(
            Collection<JavadocTokenKind> kinds,
            JavadocScanner scanner,
            int start,
            boolean breakOnFound) {
        int offset = 0;
        while (true) {
            JavadocToken token = scanner.lookahead(start + offset);
            JavadocTokenKind kind = token.getKind();
            if (kind == JavadocTokenKind.EOF || kinds.contains(kind) == breakOnFound) {
                return offset;
            } else {
                offset++;
            }
        }
    }

    /**
     * Returns the number of tokens from the starting index until the next printable token.
     * Note that white spaces and asterisks on beginning of lines are NOT printable.
     * @param scanner the target scanner
     * @param start the starting index
     * @return the number of tokens
     */
    public static int countUntilNextPrintable(JavadocScanner scanner, int start) {
        int offset = 0;
        while (true) {
            offset += countWhile(S_WHITE_SPACES, scanner, start + offset);
            JavadocToken token = scanner.lookahead(start + offset);
            JavadocTokenKind kind = token.getKind();
            if (kind == JavadocTokenKind.EOF) {
                return offset;
            } else if (kind != JavadocTokenKind.LINE_BREAK) {
                return offset;
            }
            offset += countUntilNextLineStart(scanner, start + offset);
        }
    }

    /**
     * Returns the number of tokens from the starting index until the next printable token on the subsequent lines.
     * If there is no subsequent lines or no such a printable token, this will returns the number of tokens until EOF.
     * Note that white spaces and asterisks on beginning of lines are NOT printable.
     * @param scanner the target scanner
     * @param start the starting index
     * @return the number of tokens
     */
    public static int countUntilNextLineStart(JavadocScanner scanner, int start) {
        int offset = 0;
        offset += countUntil(S_LINE_BREAK, scanner, start + offset);
        JavadocToken token = scanner.lookahead(start + offset);
        if (token.getKind() == JavadocTokenKind.EOF) {
            return offset;
        } else {
            offset++;
        }
        offset += countWhile(S_WHITE_SPACES, scanner, start + offset);
        offset += countWhile(S_ASTERISK, scanner, start + offset);
        return offset;
    }

    /**
     * Consumes the next line-break, and its consequent white-space, and asterisk tokens.
     * With {@code multiline} option, this also consumes them on the subsequent lines.
     * @param scanner the target scanner
     * @param multiline {@code true} to consume line end on multiple lines, otherwise {@code false}
     * @return the consumed number of lines
     */
    public static int consumeLineEnd(JavadocScanner scanner, boolean multiline) {
        int consumed = 0;
        do {
            int offset = countUntilNextLineStart(scanner, 0);
            if (offset == 0) {
                break;
            } else {
                scanner.consume(offset);
                consumed++;
            }

            int ws = countUntil(S_WHITE_SPACES, scanner, 0);
            if (ws != 0) {
                scanner.consume(ws);
            }

        } while (multiline);

        return consumed;
    }
}
