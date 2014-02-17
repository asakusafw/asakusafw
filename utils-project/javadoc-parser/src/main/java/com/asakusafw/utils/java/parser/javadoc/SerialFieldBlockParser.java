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

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBlock;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocFragment;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocSimpleName;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocType;

/**
 * {@code serialField}を解析する。
 */
public class SerialFieldBlockParser extends AcceptableJavadocBlockParser {

    /**
     * インスタンスを生成する。
     * インラインブロックを解析するパーサは存在しない状態となる。
     */
    public SerialFieldBlockParser() {
        super("serialField"); //$NON-NLS-1$
    }

    /**
     * インスタンスを生成する。
     * @param tagName 処理可能なタグ名
     * @param tagNames 処理可能なタグ名の一覧
     */
    public SerialFieldBlockParser(String tagName, String... tagNames) {
        super(tagName, tagNames);
    }

    @Override
    public IrDocBlock parse(String tag, JavadocScanner scanner) throws JavadocParseException {
        ArrayList<IrDocFragment> fragments = new ArrayList<IrDocFragment>();

        IrDocSimpleName name = fetchSimpleName(scanner);
        if (name != null) {
            fragments.add(name);

            IrDocType type = fetchType(scanner);
            if (type != null) {
                fragments.add(type);
            }
        }

        fragments.addAll(fetchRestFragments(scanner));
        fragments.trimToSize();
        return newBlock(tag, fragments);
    }
}
