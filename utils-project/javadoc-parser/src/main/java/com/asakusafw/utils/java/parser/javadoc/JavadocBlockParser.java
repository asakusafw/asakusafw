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
 * Javadocのタグブロックを解析するクラスの基底となるクラス。
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
     * インスタンスを生成する。
     * インラインブロックを解析するパーサは存在しない状態となる。
     */
    protected JavadocBlockParser() {
        this(Collections.<JavadocBlockParser>emptyList());
    }

    /**
     * インスタンスを生成する。
     * @param blockParsers インラインブロックを解析するパーサ
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    protected JavadocBlockParser(List<? extends JavadocBlockParser> blockParsers) {
        super(blockParsers);
    }

    /**
     * 指定の文字列からなるタグを持つブロック解析を可能な場合のみ{@code true}を返す。
     * 概要ブロックについてこのメソッドが呼び出された場合、{@code tag}には{@code null}が指定される。
     * @param tag 対象のタグ文字列(<code>&quot;&#64;&quot;を先頭に含まない</code>)、または{@code null}
     * @return ブロックを解析可能な場合のみ{@code true}
     */
    public abstract boolean canAccept(String tag);

    /**
     * 指定されたスキャナを利用してブロックを解析する。
     * {@code scanner}は先頭のタグを除いたブロックの先頭から
     * ブロックの末尾までのトークンを保持しており、ブロックは基本的に
     * スキャナ末尾までのトークンを元に構成する必要がある。
     * @param tag 対象のタグ文字列(<code>&quot;&#64;&quot;を先頭に含む</code>)、または{@code null}
     * @param scanner 対象のスキャナ
     * @return ブロックを表現する要素
     * @throws JavadocParseException 解析に失敗した場合
     * @throws IllegalArgumentException 引数{@code scanner}に{@code null}が指定された場合
     */
    public abstract IrDocBlock parse(String tag, JavadocScanner scanner) throws JavadocParseException;

    /**
     * 指定のタグと断片要素からなるブロックを生成して返す。
     * @param tag タグの名前、概要ブロックについては{@code null}
     * @param fragments 断片要素の一覧
     * @return 生成したブロック
     * @throws IllegalArgumentException 引数{@code fragments}に{@code null}が指定された場合
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
     * 対象のスキャナに含まれる残りすべての要素を、{@link IrDocFragment}の列として解析する。
     * このメソッドの呼び出し後、
     * @param scanner 対象のスキャナ
     * @return 残りすべての要素
     * @throws JavadocParseException ブロックの解析に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public List<IrDocFragment> fetchRestFragments(JavadocScanner scanner) throws JavadocParseException {
        int index = scanner.getIndex();
        try {
            ArrayList<IrDocFragment> fragments = new ArrayList<IrDocFragment>();
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
     * 対象のスキャナから、次の単純名を読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public IrDocSimpleName fetchSimpleName(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchSimpleName(scanner, S_FOLLOW);
    }

    /**
     * 対象のスキャナから、次の単純名または限定名を読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public IrDocName fetchName(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchName(scanner, S_FOLLOW);
    }

    /**
     * 対象のスキャナから、次の基本型を読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public IrDocBasicType fetchBasicType(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchBasicType(scanner, S_FOLLOW);
    }

    /**
     * 対象のスキャナから、次のプリミティブ型を読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public IrDocBasicType fetchPrimitiveType(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchPrimitiveType(scanner, S_FOLLOW);
    }

    /**
     * 対象のスキャナから、次の名前つき型を読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public IrDocNamedType fetchNamedType(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchNamedType(scanner, S_FOLLOW);
    }

    /**
     * 対象のスキャナから、次の型を読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public IrDocType fetchType(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchType(scanner, S_FOLLOW);
    }

    /**
     * 対象のスキャナから、次のフィールドを読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public IrDocField fetchField(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchField(scanner, S_FOLLOW);
    }

    /**
     * 対象のスキャナから、次のメソッドを読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public IrDocMethod fetchMethod(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchMethod(scanner, S_FOLLOW);
    }

    /**
     * 対象のスキャナから、次のメソッド、フィールド、型のいずれかのうち最もトークンの消費数が多いものを読み出して返す。
     * 構造に含まれるすべての空白文字からなるトークンは読み飛ばされる。
     * 解析に成功した場合、該当要素を構成するトークンはスキャナから消費される。
     * @param scanner 対象のスキャナ
     * @return 成功した場合は次の要素、失敗した場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public IrDocFragment fetchLinkTarget(JavadocScanner scanner) {
        return JavadocBlockParserUtil.fetchLinkTarget(scanner, S_FOLLOW);
    }
}
