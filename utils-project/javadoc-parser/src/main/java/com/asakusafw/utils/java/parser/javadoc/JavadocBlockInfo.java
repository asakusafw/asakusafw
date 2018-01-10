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

import java.text.MessageFormat;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrLocation;

/**
 * An internal Java documentation comment block information.
 * @see JavadocBlockParserUtil#fetchBlockInfo(JavadocScanner)
 */
public class JavadocBlockInfo {

    private final IrLocation location;

    private final String tagName;

    private final JavadocScanner blockScanner;

    JavadocBlockInfo(String tagName, JavadocScanner blockScanner, IrLocation location) {
        this.tagName = tagName;
        this.blockScanner = blockScanner;
        this.location = location;
    }

    /**
     * Returns the location where this block was appeared.
     * @return the location
     */
    public IrLocation getLocation() {
        return this.location;
    }

    /**
     * Returns the tag name.
     * @return the tag name (nullable)
     */
    public String getTagName() {
        return this.tagName;
    }

    /**
     * Returns the scanner which provides the block contents.
     * @return the scanner
     */
    public JavadocScanner getBlockScanner() {
        return this.blockScanner;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
            "'{'{0} {1}'}' ({2})", //$NON-NLS-1$
            getTagName(),
            getBlockScanner(),
            getLocation());
    }
}