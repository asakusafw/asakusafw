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

import java.util.List;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBlock;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocFragment;

/**
 * An implementation of {@link JavadocBaseParser} which parses generic blocks.
 */
public class DefaultJavadocBlockParser extends JavadocBlockParser {

    /**
     * Creates a new instance without any inline block parsers.
     */
    public DefaultJavadocBlockParser() {
        return;
    }

    /**
     * Creates a new instance with inline block parsers.
     * @param blockParsers the supported inline block parsers
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
