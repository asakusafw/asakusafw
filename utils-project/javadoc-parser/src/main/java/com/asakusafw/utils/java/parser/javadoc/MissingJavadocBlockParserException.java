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

import java.text.MessageFormat;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrLocation;

/**
 * An exception which is occurred when the suitable {@link JavadocBlockParser} is not found while parsing documents.
 */
public class MissingJavadocBlockParserException extends JavadocParseException {

    private static final long serialVersionUID = 1L;

    private final String tagName;

    /**
     * Creates a new instance.
     * @param tagName the target tag name
     * @param location the parsing location
     * @param cause the original exception (nullable)
     */
    public MissingJavadocBlockParserException(String tagName, IrLocation location, Throwable cause) {
        super(buildMessage(tagName), location, cause);
        this.tagName = tagName;
    }

    private static String buildMessage(String tag) {
        String blockName = (tag == null
                ? Messages.getString("MissingJavadocBlockParserException.nameSynopsisBlock") //$NON-NLS-1$
                : tag);
        return MessageFormat.format(
                Messages.getString("MissingJavadocBlockParserException.errorMissingBlockParser"),  //$NON-NLS-1$
                blockName);
    }

    /**
     * Returns the target tag name.
     * @return the target tag name, or {@code null} if the target is a synopsis block
     */
    public String getTagName() {
        return this.tagName;
    }
}
