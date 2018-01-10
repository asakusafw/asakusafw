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
package com.asakusafw.dmdl;

import java.io.Serializable;
import java.net.URI;
import java.text.MessageFormat;

/**
 * Region of the AST Node in the script file.
 * @since 0.2.0
 */
public final class Region implements Serializable {

    private static final long serialVersionUID = -6790117990125744571L;

    /**
     * The source file identifier.
     */
    public final URI sourceFile;

    /**
     * The beginning line number.
     */
    public final int beginLine;

    /**
     * The beginning column number.
     */
    public final int beginColumn;

    /**
     * The ending line number.
     */
    public final int endLine;

    /**
     * The ending column number.
     */
    public final int endColumn;

    /**
     * Creates a new instance.
     * @param sourceFile the source file location or {@code null}
     * @param beginLine the line number of the first character in the file
     * @param beginColumn the column number of the first character in the file
     * @param endLine the line number of the last character in the file
     * @param endColumn the column number of the last character in the file
     */
    public Region(
            URI sourceFile,
            int beginLine, int beginColumn,
            int endLine, int endColumn) {
        this.sourceFile = sourceFile;
        this.beginLine = beginLine;
        this.beginColumn = beginColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}/{1}:{2}-{3}:{4}", //$NON-NLS-1$
                sourceFile == null ? Messages.getString("Region.unknownSource") : sourceFile, //$NON-NLS-1$
                beginLine, beginColumn,
                endLine, endColumn);
    }
}