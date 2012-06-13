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

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBlock;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocFragment;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocNamedType;

/**
 * タグの直後に名前付き型を指定するブロックを解析する。
 */
public class FollowsNamedTypeBlockParser extends AcceptableJavadocBlockParser {

    /**
     * インスタンスを生成する。
     * @param tagNames タグ名の一覧
     */
    public FollowsNamedTypeBlockParser(String...tagNames) {
        super(tagNames);
    }

    @Override
    public IrDocBlock parse(String tag, JavadocScanner scanner) throws JavadocParseException {
        ArrayList<IrDocFragment> fragments = new ArrayList<IrDocFragment>();
        IrDocNamedType namedType = fetchNamedType(scanner);
        if (namedType != null) {
            fragments.add(namedType);
        }
        fragments.addAll(fetchRestFragments(scanner));
        fragments.trimToSize();
        return newBlock(tag, fragments);
    }
}
