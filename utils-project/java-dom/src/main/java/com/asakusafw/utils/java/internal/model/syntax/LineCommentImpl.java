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
package com.asakusafw.utils.java.internal.model.syntax;

import com.asakusafw.utils.java.model.syntax.LineComment;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link LineComment}.
 */
public final class LineCommentImpl extends ModelRoot implements LineComment {

    private String string;

    @Override
    public String getString() {
        return this.string;
    }

    /**
     * Sets the comment text.
     * This must start with the starting symbol (<code>&#47;&#47;</code>).
     * @param string the comment text
     * @throws IllegalArgumentException if {@code string} was {@code null}
     * @throws IllegalArgumentException if {@code string} was empty
     */
    public void setString(String string) {
        Util.notNull(string, "string"); //$NON-NLS-1$
        this.string = string;
    }

    /**
     * Returns {@link ModelKind#LINE_COMMENT} which represents this element kind.
     * @return {@link ModelKind#LINE_COMMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.LINE_COMMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitLineComment(this, context);
    }
}
