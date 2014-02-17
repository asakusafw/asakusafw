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
package com.asakusafw.utils.java.internal.model.syntax;

import com.asakusafw.utils.java.model.syntax.DocText;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link DocText}の実装。
 */
public final class DocTextImpl extends ModelRoot implements DocText {

    /**
     * テキストを構成する文字列。
     */
    private String string;

    @Override
    public String getString() {
        return this.string;
    }

    /**
     * テキストを構成する文字列を設定する。
     * @param string
     *     テキストを構成する文字列
     * @throws IllegalArgumentException
     *     {@code string}に{@code null}が指定された場合
     */
    public void setString(String string) {
        Util.notNull(string, "string"); //$NON-NLS-1$
        this.string = string;
    }

    /**
     * この要素の種類を表す{@link ModelKind#DOC_TEXT}を返す。
     * @return {@link ModelKind#DOC_TEXT}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.DOC_TEXT;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitDocText(this, context);
    }
}
