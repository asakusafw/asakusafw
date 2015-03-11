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
 * {@link LineComment}の実装。
 */
public final class LineCommentImpl extends ModelRoot implements LineComment {

    /**
     * コメント文字列。
     */
    private String string;

    @Override
    public String getString() {
        return this.string;
    }

    /**
     * コメント文字列を設定する。
     * @param string
     *     コメント文字列
     * @throws IllegalArgumentException
     *     {@code string}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code string}に空が指定された場合
     */
    public void setString(String string) {
        Util.notNull(string, "string"); //$NON-NLS-1$
        this.string = string;
    }

    /**
     * この要素の種類を表す{@link ModelKind#LINE_COMMENT}を返す。
     * @return {@link ModelKind#LINE_COMMENT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.LINE_COMMENT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitLineComment(this, context);
    }
}
