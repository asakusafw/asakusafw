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

import java.util.List;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBlock;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocFragment;

/**
 * 特に構造化されていないブロックを解析する。
 */
public class DefaultJavadocBlockParser extends JavadocBlockParser {

    /**
     * インスタンスを生成する。
     * インラインブロックを解析するパーサは存在しない状態となる。
     */
    public DefaultJavadocBlockParser() {
        super();
    }

    /**
     * インスタンスを生成する。
     * @param blockParsers インラインブロックを解析するパーサ
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public DefaultJavadocBlockParser(List<? extends JavadocBlockParser> blockParsers) {
        super(blockParsers);
    }

    @Override
    public boolean canAccept(String tag) {
        return true;
    }

    @Override
    public IrDocBlock parse(String tag, JavadocScanner scanner) throws JavadocParseException {
        List<IrDocFragment> fragments = fetchRestFragments(scanner);
        return newBlock(tag, fragments);
    }
}
