/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
 * An implementation of {@link JavadocBaseParser} which parses blocks that tag follows a named type.
 */
public class FollowsNamedTypeBlockParser extends AcceptableJavadocBlockParser {

    /**
     * Creates a new instance.
     * @param tagNames the acceptable tag names
     */
    public FollowsNamedTypeBlockParser(String...tagNames) {
        super(tagNames);
    }

    @Override
    public IrDocBlock parse(String tag, JavadocScanner scanner) throws JavadocParseException {
        ArrayList<IrDocFragment> fragments = new ArrayList<>();
        IrDocNamedType namedType = fetchNamedType(scanner);
        if (namedType != null) {
            fragments.add(namedType);
        }
        fragments.addAll(fetchRestFragments(scanner));
        fragments.trimToSize();
        return newBlock(tag, fragments);
    }
}
