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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocToken;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocTokenKind;

/**
 * {@link JavadocScanner}のユーティリティ群。
 */
public final class JavadocScannerUtil {

    private static final Set<JavadocTokenKind> S_LINE_BREAK =
        Collections.singleton(JavadocTokenKind.LINE_BREAK);

    private static final Set<JavadocTokenKind> S_WHITE_SPACES =
        Collections.singleton(JavadocTokenKind.WHITE_SPACES);

    private static final Set<JavadocTokenKind> S_ASTERISK =
        Collections.singleton(JavadocTokenKind.ASTERISK);

    private static final Set<JavadocTokenKind> S_RIGHT_BRACE =
        Collections.singleton(JavadocTokenKind.RIGHT_BRACE);

    private JavadocScannerUtil() {
        return;
    }

    /**
     * 指定のレンジのトークン一覧を返す。
     * 指定の開始位置から指定の個数だけトークンを返すが、
     * その中に終端トークン({@link JavadocTokenKind#EOF})が含まれていた場合は
     * そのトークンおよびそれ以降のトークンを結果に含めない。
     * @param scanner 対象のスキャナ
     * @param start 開始オフセット
     * @param count トークンの個数 (EOFが出現した場合はこの数よりも少ないトークン数のリストが返される)
     * @return トークン一覧
     */
    public static List<JavadocToken> lookaheadTokens(JavadocScanner scanner, int start, int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        List<JavadocToken> tokens = new ArrayList<JavadocToken>(count);
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
     * {@code kinds}で指定された種類を持つトークンが、開始オフセットから連続する個数を返す。
     * @param kinds 対象の種類の一覧、{@link JavadocTokenKind#EOF}は無視される
     * @param scanner 対象のスキャナ
     * @param start 開始オフセット
     * @return 開始オフセットから指定の種類のトークンが連続する個数
     */
    public static int countWhile(
            Collection<JavadocTokenKind> kinds,
            JavadocScanner scanner,
            int start) {
        return countWhileUntil(kinds, scanner, start, false);
    }

    /**
     * {@code kinds}で指定された種類を持つトークンが、開始オフセットから出現するまでの個数を返す。
     * @param kinds 対象の種類の一覧、{@link JavadocTokenKind#EOF}は暗黙に追加される
     * @param scanner 対象のスキャナ
     * @param start 開始オフセット
     * @return 開始オフセットから指定の種類のトークンが出現するまでの個数
     */
    public static int countUntil(
            Collection<JavadocTokenKind> kinds,
            JavadocScanner scanner,
            int start) {
        return countWhileUntil(kinds, scanner, start, true);
    }

    /**
     * 指定の位置を起点として、このブロックの末尾となるトークンまでの個数を返す。
     * この個数は、末端となるトークンを含まない。
     * @param scanner 対象のスキャナ
     * @param start 開始オフセット
     * @return ブロックの終端となるトークンまでの個数
     */
    public static int countUntilBlockEnd(
            JavadocScanner scanner,
            int start) {

        // カーソルを改行文字へ
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
     * 指定の位置を基点として、このコメントの終端となるトークンまでの個数を返す。
     * この個数は、末端となるトークンを含まない。
     * @param scanner 対象のスキャナ
     * @param returnMinusIfMissing
     *      {@code true}ならば発見できなかった場合に{@code -1}を返し、
     *      {@code false}ならば発見できなかった場合に末尾までのトークン数を返す
     * @param start 開始オフセット
     * @return 終端となるトークンまでの個数
     */
    public static int countUntilCommentEnd(
            JavadocScanner scanner,
            boolean returnMinusIfMissing,
            int start) {
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
            if (
                    kind == JavadocTokenKind.EOF
                    || kinds.contains(kind) == breakOnFound) {
                return offset;
            } else {
                offset++;
            }
        }
    }

    /**
     * 指定の位置を基点として、次の行の先頭となるトークンまでの個数を返す。
     * その際、行頭の空白文字およびアスタリスクはすべてスキップされる。
     * 次の行が存在しない場合、この呼び出しは終端までのトークン数を返す。
     * @param scanner 対象のスキャナ
     * @param start 開始オフセット
     * @return 開始オフセットから次の行頭までのトークンが連続する個数
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
     * 指定の位置を基点として、次の行の先頭となるトークンまでの個数を返す。
     * その際、行頭の空白文字およびアスタリスクはすべてスキップされる。
     * 次の行が存在しない場合、この呼び出しは終端までのトークン数を返す。
     * @param scanner 対象のスキャナ
     * @param start 開始オフセット
     * @return 開始オフセットから次の行頭までのトークンが連続する個数
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
     * 行末文字と、それに後続する空白文字の列、アスタリスクの列、空白文字の列を消費する。
     * {@code multiline}に{@code true}が指定された場合、連続する同様の構造を全て消費する。
     * @param scanner 対象のスキャナ
     * @param multiline 複数行に対して行末を消費する
     * @return 消費した行数
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
