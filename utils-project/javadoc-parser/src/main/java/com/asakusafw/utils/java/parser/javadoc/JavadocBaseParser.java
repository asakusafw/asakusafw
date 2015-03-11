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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBlock;

/**
 * Javadocのパーサの親クラス。
 */
public abstract class JavadocBaseParser {

    private List<? extends JavadocBlockParser> blockParsers;

    /**
     * インスタンスを生成する。
     * @param blockParsers ブロックパーサの一覧
     */
    public JavadocBaseParser(List<? extends JavadocBlockParser> blockParsers) {
        super();
        setBlockParsers(blockParsers);
    }

    /**
     * ブロックを解析するインスタンスの一覧を返す。
     * @return ブロックを解析するインスタンスの一覧
     */
    public final List<? extends JavadocBlockParser> getBlockParsers() {
        return this.blockParsers;
    }

    /**
     * ブロックを解析するインスタンスの一覧を設定する。
     * @param blockParsers 設定するパーサの一覧
     * @throws IllegalArgumentException 引数が{@code null}であった場合
     */
    public final void setBlockParsers(List<? extends JavadocBlockParser> blockParsers) {
        if (blockParsers == null) {
            throw new IllegalArgumentException("blockParsers"); //$NON-NLS-1$
        }
        this.blockParsers = Collections.unmodifiableList(
            new ArrayList<JavadocBlockParser>(blockParsers));
    }

    /**
     * このパーサに登録されたブロックパーサを利用し、対象のブロックを解析する。
     * @param block 対象のブロック
     * @return 解析結果
     * @throws JavadocParseException ブロックの解析に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public IrDocBlock parseBlock(JavadocBlockInfo block) throws JavadocParseException {
        if (block == null) {
            throw new IllegalArgumentException("block"); //$NON-NLS-1$
        }
        String tag = block.getTagName();
        for (JavadocBlockParser parser: getBlockParsers()) {
            if (parser.canAccept(tag)) {
                IrDocBlock result = parser.parse(tag, block.getBlockScanner());
                result.setLocation(block.getLocation());
                return result;
            }
        }
        throw new MissingJavadocBlockParserException(tag, block.getLocation(), null);
    }
}
