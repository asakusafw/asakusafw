/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
 * An abstract super class of Java documentation comment parsers.
 */
public abstract class JavadocBaseParser {

    private List<? extends JavadocBlockParser> blockParsers;

    /**
     * Creates a new instance.
     * @param blockParsers the block parsers
     */
    public JavadocBaseParser(List<? extends JavadocBlockParser> blockParsers) {
        setBlockParsers(blockParsers);
    }

    /**
     * Returns the block parsers.
     * @return the block parsers
     */
    public final List<? extends JavadocBlockParser> getBlockParsers() {
        return this.blockParsers;
    }

    /**
     * Sets the block parsers.
     * @param blockParsers the block parsers
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public final void setBlockParsers(List<? extends JavadocBlockParser> blockParsers) {
        if (blockParsers == null) {
            throw new IllegalArgumentException("blockParsers"); //$NON-NLS-1$
        }
        this.blockParsers = Collections.unmodifiableList(new ArrayList<>(blockParsers));
    }

    /**
     * Parses the target block and returns the block information.
     * @param block the target block
     * @return the parsed information
     * @throws JavadocParseException if error was occurred while analyzing the target block
     * @throws IllegalArgumentException if the parameter is {@code null}
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
