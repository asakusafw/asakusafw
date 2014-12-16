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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrBasicTypeKind;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocArrayType;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBasicType;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocElement;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocField;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocFragment;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocMethod;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocMethodParameter;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocName;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocNamedType;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocQualifiedName;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocSimpleName;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocText;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocType;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrLocation;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocToken;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.JavadocTokenKind;

/**
 * Javadocタグブロックの構文解析に利用するツール集。
 */
public final class JavadocBlockParserUtil {

    /**
     * 空白文字の一覧。
     */
    public static final Set<JavadocTokenKind> S_WHITE;
    static {
        EnumSet<JavadocTokenKind> set = EnumSet.noneOf(JavadocTokenKind.class);
        set.add(WHITE_SPACES);
        set.add(LINE_BREAK);
        set.add(EOF);
        S_WHITE = Collections.unmodifiableSet(set);
    }

    private static final Set<JavadocTokenKind> S_TEXT_DELIM;
    static {
        EnumSet<JavadocTokenKind> set = EnumSet.noneOf(JavadocTokenKind.class);
        set.add(LINE_BREAK);
        set.add(LEFT_BRACE);
        S_TEXT_DELIM = Collections.unmodifiableSet(set);
    }

    private static final Set<JavadocTokenKind> S_TAG_NAME_DELIM;
    static {
        EnumSet<JavadocTokenKind> set = EnumSet.noneOf(JavadocTokenKind.class);
        set.add(WHITE_SPACES);
        set.add(LINE_BREAK);
        set.add(AT);
        set.add(RIGHT_BRACE);
        S_TAG_NAME_DELIM = Collections.unmodifiableSet(set);
    }

    private static final Set<JavadocTokenKind> S_INLINE_BLOCK_DELIM;
    static {
        EnumSet<JavadocTokenKind> set = EnumSet.noneOf(JavadocTokenKind.class);
        set.add(LEFT_BRACE);
        set.add(RIGHT_BRACE);
        S_INLINE_BLOCK_DELIM = Collections.unmodifiableSet(set);
    }

    private static final Map<String, IrBasicTypeKind> BASIC_TYPE_NAMES;
    static {
        Map<String, IrBasicTypeKind> map = new HashMap<String, IrBasicTypeKind>();
        for (IrBasicTypeKind k : IrBasicTypeKind.values()) {
            map.put(k.getSymbol().intern(), k);
        }
        BASIC_TYPE_NAMES = Collections.unmodifiableMap(map);
    }

    private JavadocBlockParserUtil() {
        return;
    }

    /**
     * 指定の要素に位置情報を付与する。
     * 元となる位置情報が不明確である場合、この呼び出しは何も行わない。
     * @param <T> 要素の型
     * @param elem 位置情報を与える対象の要素
     * @param start 指定の要素を構成する先頭のトークン
     * @param stop 指定の要素を構成する末尾のトークン
     * @return 位置情報を付与した要素
     */
    public static <T extends IrDocElement> T setLocation(
            T elem,
            JavadocToken start,
            JavadocToken stop) {
        int s = start.getStartPosition();
        int e = stop.getStartPosition() + stop.getText().length();
        IrLocation location = new IrLocation(s, e - s);
        elem.setLocation(location);
        return elem;
    }

    /**
     * 指定の要素に位置情報を付与する。
     * 元となる位置情報が不明確である場合、この呼び出しは何も行わない。
     * @param <T> 要素の型
     * @param elem 位置情報を与える対象の要素
     * @param start 指定の要素を構成する先頭の要素
     * @param stop 指定の要素を構成する末尾のトークン
     * @return 位置情報を付与した要素
     */
    public static <T extends IrDocElement> T setLocation(
            T elem,
            IrLocation start,
            IrLocation stop) {
        if (start == null || stop == null) {
            return elem;
        }
        int s = start.getStartPosition();
        int e = stop.getStartPosition() + stop.getLength();
        IrLocation location = new IrLocation(s, e - s);
        elem.setLocation(location);
        return elem;
    }

    /**
     * 対象のスキャナから、次の任意テキストを読み出して返す。
     * 結果としてのテキストが空となる場合、解析は失敗したとみなされ{@code null}が返される。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * 任意テキストの終端となるシーケンスは次のとおりである。
     * <ul>
     * <li> 単一の改行 ({@link JavadocTokenKind#LINE_BREAK}) </li>
     * <li> インラインブロックの開始 (<code>&quot;{&quot; &quot;&#64;&quot;</code>) </li>
     * </ul>
     * @param scanner 対象のスキャナ
     * @param trimHead テキスト先頭の空白を除去する
     * @param trimTail テキスト末尾の空白を除去する
     * @return {@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれていた場合
     */
    public static IrDocText fetchText(
            JavadocScanner scanner,
            boolean trimHead,
            boolean trimTail) {
        if (scanner == null) {
            throw new IllegalArgumentException("scanner"); //$NON-NLS-1$
        }
        int offset = 0;
        while (true) {
            offset += JavadocScannerUtil.countUntil(S_TEXT_DELIM, scanner,
                offset);
            JavadocTokenKind kind = scanner.lookahead(offset).getKind();
            if (kind == LEFT_BRACE) {
                JavadocToken la = scanner.lookahead(offset + 1);
                if (la.getKind() == JavadocTokenKind.AT) {
                    break;
                }
                offset++;
            } else {
                break;
            }
        }
        return consumeAsText(scanner, offset, trimHead, trimTail);
    }

    /**
     * 対象のスキャナから、次のインラインブロックを読み出して{@link JavadocBlockInfo}として返す。
     * ブロックが後続しない場合、この呼び出しは{@code null}を返しスキャナの状態を変更しない。
     * ブロックの読み出しに成功した場合、インラインブロックを構成するトークンが消費される。
     * @param scanner 対象のスキャナ
     * @return 後続するインラインブロックの情報、存在しない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれていた場合
     */
    public static JavadocBlockInfo fetchBlockInfo(JavadocScanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("scanner"); //$NON-NLS-1$
        }

        int offset = 0;
        offset += JavadocScannerUtil.countUntilNextPrintable(scanner, offset);

        // 先頭の "{"
        JavadocToken head = scanner.lookahead(offset);
        if (head.getKind() != LEFT_BRACE) {
            return null;
        }

        // タグ開始文字 "@"
        offset++;
        JavadocToken at = scanner.lookahead(offset);
        if (at.getKind() != AT) {
            return null;
        }

        // タグ名
        offset++;
        int nameCount = countWhileTagName(scanner, offset);
        String tagName = buildString(JavadocScannerUtil.lookaheadTokens(
            scanner, offset, nameCount));

        offset += nameCount;

        // 末端の検出
        int blockEnd = JavadocScannerUtil.countUntil(S_INLINE_BLOCK_DELIM,
            scanner,
            offset);

        JavadocToken token = scanner.lookahead(offset + blockEnd);
        JavadocTokenKind kind = token.getKind();

        // 次のブロックの開始を検出すると、このブロックは長さ0になる
        if (kind == LEFT_BRACE) {
            blockEnd = 0;
        }

        boolean legalBlock = (kind == JavadocTokenKind.RIGHT_BRACE);
        JavadocToken tail = scanner.lookahead(offset + blockEnd);

        int startIndex = scanner.getIndex() + offset;
        int stopIndex = startIndex + blockEnd;

        int startPos = head.getStartPosition();
        int endPos = tail.getStartPosition();
        if (legalBlock) {
            endPos += tail.getText().length();
        }

        IrLocation blockLocation = new IrLocation(startPos, endPos - startPos);
        DefaultJavadocScanner blockScanner = new DefaultJavadocScanner(
            new ArrayList<JavadocToken>(scanner.getTokens().subList(startIndex,
            stopIndex)),
            endPos);

        scanner.consume(offset + blockEnd);
        if (legalBlock) {
            scanner.consume(1);
        }

        return new JavadocBlockInfo(tagName, blockScanner, blockLocation);
    }

    /**
     * 指定の位置から後続するタグ名と認識可能なトークン数を返す。
     * @param scanner 対象のスキャナ
     * @param start 開始オフセット
     * @return タグ名と認識可能なトークン数
     */
    public static int countWhileTagName(JavadocScanner scanner, int start) {
        return JavadocScannerUtil.countUntil(S_TAG_NAME_DELIM, scanner, start);
    }

    /**
     * 対象のスキャナから、次の単純名を読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @param follow この要素に後続可能なトークン、後続しない場合は解析が失敗する (省略可)
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 省略可能でない引数に{@code null}が含まれていた場合
     */
    public static IrDocSimpleName fetchSimpleName(JavadocScanner scanner,
            Set<JavadocTokenKind> follow) {
        DefaultJavadocTokenStream stream = new DefaultJavadocTokenStream(
            scanner);
        stream.mark();
        IrDocSimpleName elem = fetchSimpleName(stream);
        if (elem == null) {
            return null;
        }
        if (!follows(stream, follow)) {
            stream.rewind();
            return null;
        } else {
            stream.discard();
            return elem;
        }
    }

    /**
     * 対象のスキャナから、次の単純名または限定名を読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @param follow この要素に後続可能なトークン、後続しない場合は解析が失敗する (省略可)
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 省略可能でない引数に{@code null}が含まれていた場合
     */
    public static IrDocName fetchName(JavadocScanner scanner,
            Set<JavadocTokenKind> follow) {
        DefaultJavadocTokenStream stream = new DefaultJavadocTokenStream(
            scanner);
        stream.mark();
        IrDocName elem = fetchName(stream);
        if (elem == null) {
            return null;
        }
        if (!follows(stream, follow)) {
            stream.rewind();
            return null;
        } else {
            stream.discard();
            return elem;
        }
    }

    /**
     * 対象のスキャナから、次の基本型を読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @param follow この要素に後続可能なトークン、後続しない場合は解析が失敗する (省略可)
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 省略可能でない引数に{@code null}が含まれていた場合
     */
    public static IrDocBasicType fetchBasicType(JavadocScanner scanner,
            Set<JavadocTokenKind> follow) {
        JavadocTokenStream stream = new DefaultJavadocTokenStream(scanner);
        stream.mark();
        IrDocBasicType elem = fetchBasicType(stream);
        if (!follows(stream, follow)) {
            stream.rewind();
            return null;
        } else {
            stream.discard();
            return elem;
        }
    }

    /**
     * 対象のスキャナから、次のプリミティブ型を読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @param follow この要素に後続可能なトークン、後続しない場合は解析が失敗する (省略可)
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 省略可能でない引数に{@code null}が含まれていた場合
     */
    public static IrDocBasicType fetchPrimitiveType(JavadocScanner scanner,
            Set<JavadocTokenKind> follow) {
        JavadocTokenStream stream = new DefaultJavadocTokenStream(scanner);
        stream.mark();
        IrDocBasicType elem = fetchBasicType(stream);
        if (elem.getTypeKind() == IrBasicTypeKind.VOID) {
            stream.rewind();
            return null;
        }
        if (!follows(stream, follow)) {
            stream.rewind();
            return null;
        } else {
            stream.discard();
            return elem;
        }
    }

    /**
     * 対象のスキャナから、次の名前つき型を読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @param follow この要素に後続可能なトークン、後続しない場合は解析が失敗する (省略可)
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 省略可能でない引数に{@code null}が含まれていた場合
     */
    public static IrDocNamedType fetchNamedType(JavadocScanner scanner,
            Set<JavadocTokenKind> follow) {
        JavadocTokenStream stream = new DefaultJavadocTokenStream(scanner);
        stream.mark();
        IrDocNamedType elem = fetchNamedType(stream);
        if (!follows(stream, follow)) {
            stream.rewind();
            return null;
        } else {
            stream.discard();
            return elem;
        }
    }

    /**
     * 対象のスキャナから、次の型を読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @param follow この要素に後続可能なトークン、後続しない場合は解析が失敗する (省略可)
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 省略可能でない引数に{@code null}が含まれていた場合
     */
    public static IrDocType fetchType(JavadocScanner scanner,
            Set<JavadocTokenKind> follow) {
        JavadocTokenStream stream = new DefaultJavadocTokenStream(scanner);
        stream.mark();
        IrDocType elem = fetchType(stream);
        if (!follows(stream, follow)) {
            stream.rewind();
            return null;
        } else {
            stream.discard();
            return elem;
        }
    }

    /**
     * 対象のスキャナから、次のフィールドを読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @param follow この要素に後続可能なトークン、後続しない場合は解析が失敗する (省略可)
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 省略可能でない引数に{@code null}が含まれていた場合
     */
    public static IrDocField fetchField(JavadocScanner scanner,
            Set<JavadocTokenKind> follow) {
        JavadocTokenStream stream = new DefaultJavadocTokenStream(scanner);
        stream.mark();
        IrDocField elem = fetchField(stream);
        if (!follows(stream, follow)) {
            stream.rewind();
            return null;
        } else {
            stream.discard();
            return elem;
        }
    }

    /**
     * 対象のスキャナから、次のメソッドを読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @param follow この要素に後続可能なトークン、後続しない場合は解析が失敗する (省略可)
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 省略可能でない引数に{@code null}が含まれていた場合
     */
    public static IrDocMethod fetchMethod(JavadocScanner scanner,
            Set<JavadocTokenKind> follow) {
        JavadocTokenStream stream = new DefaultJavadocTokenStream(scanner);
        stream.mark();
        IrDocMethod elem = fetchMethod(stream);
        if (!follows(stream, follow)) {
            stream.rewind();
            return null;
        } else {
            stream.discard();
            return elem;
        }
    }

    /**
     * 対象のスキャナから、次のメソッド、フィールド、型のいずれかのうち最もトークンの消費数が多いものを読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @param follow この要素に後続可能なトークン、後続しない場合は解析が失敗する (省略可)
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 省略可能でない引数に{@code null}が含まれていた場合
     */
    public static IrDocFragment fetchLinkTarget(JavadocScanner scanner,
            Set<JavadocTokenKind> follow) {
        IrDocMethod method = fetchMethod(scanner, follow);
        if (method != null) {
            return method;
        }
        IrDocField field = fetchField(scanner, follow);
        if (field != null) {
            return field;
        }
        IrDocNamedType type = fetchNamedType(scanner, follow);
        if (type != null) {
            return type;
        }

        return null;
    }

    private static IrDocMethod fetchMethod(JavadocTokenStream stream) {
        stream.mark();
        IrDocField field = fetchField(stream);
        if (field == null) {
            stream.rewind();
            return null;
        }

        if (consumeIfMatch(stream, LEFT_PAREN) == null) {
            stream.rewind();
            return null;
        }

        JavadocToken delim;
        List<IrDocMethodParameter> parameters;
        IrDocMethodParameter first = fetchMethodParameter(stream);
        if (first == null) {
            delim = consumeIfMatch(stream, RIGHT_PAREN);
            if (delim == null) {
                stream.rewind();
                return null;
            }
            parameters = Collections.emptyList();
        } else {
            parameters = new ArrayList<IrDocMethodParameter>();
            parameters.add(first);
            while (true) {
                delim = stream.nextToken();
                if (delim.getKind() == RIGHT_PAREN) {
                    break;
                } else if (delim.getKind() == COMMA) {
                    IrDocMethodParameter p = fetchMethodParameter(stream);
                    if (p == null) {
                        stream.rewind();
                        return null;
                    }
                    parameters.add(p);
                } else {
                    stream.rewind();
                    return null;
                }
            }
        }
        stream.discard();

        IrDocMethod elem = new IrDocMethod();
        elem.setDeclaringType(field.getDeclaringType());
        elem.setName(field.getName());
        elem.setParameters(parameters);
        setLocation(elem, field.getLocation(), delim.getLocation());
        return elem;
    }

    private static IrDocMethodParameter fetchMethodParameter(
            JavadocTokenStream stream) {
        stream.mark();
        IrDocType type = fetchType(stream);
        if (type == null) {
            stream.rewind();
            return null;
        } else {
            stream.discard();
        }
        IrLocation delim = type.getLocation();
        boolean varargs;
        if (consumeIfMatch(stream, DOT) != null) {
            if ((stream.lookahead(0).getKind() != DOT)
                    || (stream.lookahead(1).getKind() != DOT)) {
                stream.rewind();
                return null;
            }
            stream.nextToken();
            JavadocToken lastDot = stream.nextToken();
            delim = lastDot.getLocation();
            varargs = true;
        } else {
            varargs = false;
        }

        IrDocSimpleName name = fetchSimpleName(stream);
        if (name != null) {
            delim = name.getLocation();
        }

        IrDocMethodParameter elem = new IrDocMethodParameter();
        elem.setType(type);
        elem.setVariableArity(varargs);
        elem.setName(name);
        setLocation(elem, type.getLocation(), delim);
        return elem;
    }

    private static IrDocField fetchField(JavadocTokenStream stream) {
        stream.mark();

        IrDocNamedType decl = fetchNamedType(stream);

        JavadocToken sharp = consumeIfMatch(stream, SHARP);
        if (sharp == null) {
            stream.rewind();
            return null;
        }

        IrDocSimpleName name = fetchSimpleName(stream);
        if (name == null) {
            stream.rewind();
            return null;
        }

        IrDocField elem = new IrDocField();
        elem.setDeclaringType(decl);
        elem.setName(name);
        setLocation(elem, decl == null ? sharp.getLocation() : decl
            .getLocation(), name.getLocation());

        return elem;
    }

    private static IrDocNamedType fetchNamedType(JavadocTokenStream stream) {
        IrDocName name = fetchName(stream);
        if (name == null) {
            return null;
        }
        IrDocNamedType elem = new IrDocNamedType(name);
        setLocation(elem, name.getLocation(), name.getLocation());
        return elem;
    }

    private static IrDocType fetchType(JavadocTokenStream stream) {
        stream.mark();
        IrDocType elem = fetchBasicType(stream);
        if (elem == null) {
            IrDocName name = fetchName(stream);
            if (name == null) {
                stream.rewind();
                return null;
            }
            elem = new IrDocNamedType(name);
            elem.setLocation(name.getLocation());
        }
        while (true) {
            stream.mark();
            if (consumeIfMatch(stream, JavadocTokenKind.LEFT_BRACKET) == null) {
                stream.rewind();
                break;
            }
            JavadocToken stop = consumeIfMatch(stream,
                JavadocTokenKind.RIGHT_BRACKET);
            if (stop == null) {
                stream.rewind();
                break;
            } else {
                stream.discard();
                IrDocArrayType t = new IrDocArrayType(elem);
                setLocation(t, elem.getLocation(), stop.getLocation());
                elem = t;
            }
        }
        stream.discard();
        return elem;
    }

    private static IrDocName fetchName(JavadocTokenStream stream) {
        IrDocName name = fetchSimpleName(stream);
        if (name == null) {
            return null;
        }
        while (true) {
            stream.mark();
            if (consumeIfMatch(stream, JavadocTokenKind.DOT) == null) {
                stream.rewind();
                break;
            }
            IrDocSimpleName simple = fetchSimpleName(stream);
            if (simple == null) {
                stream.rewind();
                break;
            } else {
                IrDocQualifiedName qualified = new IrDocQualifiedName(name,
                    simple);
                setLocation(qualified, name.getLocation(), simple.getLocation());
                name = qualified;
                stream.discard();
            }
        }
        return name;
    }

    private static IrDocBasicType fetchBasicType(JavadocTokenStream stream) {
        JavadocToken token = stream.peek();
        if (token.getKind() == JavadocTokenKind.IDENTIFIER) {
            if (BASIC_TYPE_NAMES.containsKey(token.getText())) {
                stream.nextToken();
                IrBasicTypeKind k = BASIC_TYPE_NAMES.get(token.getText());
                IrDocBasicType elem = new IrDocBasicType(k);
                setLocation(elem, token, token);
                return elem;
            }
        }
        return null;
    }

    private static IrDocSimpleName fetchSimpleName(JavadocTokenStream stream) {
        JavadocToken token = consumeIfMatch(stream, JavadocTokenKind.IDENTIFIER);
        if (token != null) {
            IrDocSimpleName name = new IrDocSimpleName(token.getText());
            setLocation(name, token, token);
            return name;
        } else {
            return null;
        }
    }

    private static boolean follows(JavadocTokenStream stream,
            Collection<JavadocTokenKind> set) {
        if (set == null) {
            return true;
        }
        JavadocTokenKind kind = stream.lookahead(0).getKind();
        return set.contains(kind);
    }

    private static JavadocToken consumeIfMatch(JavadocTokenStream stream,
            JavadocTokenKind kind) {
        JavadocToken token = stream.peek();
        if (token.getKind() == kind) {
            return stream.nextToken();
        }
        return null;
    }

    private static IrDocText consumeAsText(
            JavadocScanner scanner,
            int count,
            boolean trimHead,
            boolean trimTail) {
        assert scanner != null;
        assert count >= 0;
        int mark = scanner.getIndex();
        List<JavadocToken> tokens = consumeTokens(scanner, count, trimHead,
            trimTail);
        IrDocText elem = buildText(tokens);
        if (elem == null) {
            scanner.seek(mark);
            return null;
        } else {
            return elem;
        }
    }

    private static IrDocText buildText(List<? extends JavadocToken> tokens) {
        assert tokens != null;
        if (tokens.isEmpty()) {
            return null;
        }
        String text = buildString(tokens);
        IrDocText elem = new IrDocText(text);
        JavadocToken start = tokens.get(0);
        JavadocToken stop = tokens.get(tokens.size() - 1);
        setLocation(elem, start, stop);
        return elem;
    }

    /**
     * 指定のトークン列を一連のテキストとしてまとめあげる。
     * @param tokens トークン列
     * @return テキスト列
     */
    public static String buildString(List<? extends JavadocToken> tokens) {
        if (tokens == null) {
            throw new IllegalArgumentException("tokens"); //$NON-NLS-1$
        }
        if (tokens.isEmpty()) {
            return ""; //$NON-NLS-1$
        }
        StringBuilder buf = new StringBuilder();
        for (JavadocToken t : tokens) {
            buf.append(t.getText());
        }
        return buf.toString();
    }

    private static List<JavadocToken> consumeTokens(JavadocScanner scanner,
            int count, boolean trimHead, boolean trimTail) {
        if (count == 0) {
            return Collections.emptyList();
        }
        int rest = count;
        if (trimHead) {
            int offset = JavadocScannerUtil.countWhile(S_WHITE, scanner, 0);
            scanner.consume(offset);
            rest -= offset;
            if (rest == 0) {
                return Collections.emptyList();
            }
        }
        List<JavadocToken> tokens = new ArrayList<JavadocToken>(rest);
        for (int i = 0; i < rest; i++) {
            JavadocToken t = scanner.nextToken();
            if (t.getKind() == JavadocTokenKind.EOF) {
                break;
            }
            tokens.add(t);
        }
        if (trimTail) {
            int lastWs = tokens.size();
            while (lastWs >= 0) {
                JavadocToken t = tokens.get(lastWs - 1);
                if (t.getKind() != WHITE_SPACES) {
                    break;
                }
                lastWs--;
            }
            if (lastWs != tokens.size()) {
                tokens = tokens.subList(0, lastWs);
            }
        }
        return tokens;
    }
}
