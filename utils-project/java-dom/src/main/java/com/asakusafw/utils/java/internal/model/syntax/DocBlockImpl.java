/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import java.util.List;

import com.asakusafw.utils.java.model.syntax.DocBlock;
import com.asakusafw.utils.java.model.syntax.DocElement;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * {@link DocBlock}の実装。
 */
public final class DocBlockImpl extends ModelRoot implements DocBlock {

    /**
     * タグ文字列。
     */
    private String tag;

    /**
     * インライン要素の一覧。
     */
    private List<? extends DocElement> elements;

    @Override
    public String getTag() {
        return this.tag;
    }

    /**
     * タグ文字列を設定する。
     * <p> タグが省略された場合、引数には空を指定する。 </p>
     * @param tag
     *     タグ文字列
     * @throws IllegalArgumentException
     *     {@code tag}に{@code null}が指定された場合
     */
    public void setTag(String tag) {
        Util.notNull(tag, "tag"); //$NON-NLS-1$
        this.tag = tag;
    }

    @Override
    public List<? extends DocElement> getElements() {
        return this.elements;
    }

    /**
     * インライン要素の一覧を設定する。
     * <p> インライン要素が一つも指定されない場合、引数には空を指定する。 </p>
     * @param elements
     *     インライン要素の一覧
     * @throws IllegalArgumentException
     *     {@code elements}に{@code null}が指定された場合
     */
    public void setElements(List<? extends DocElement> elements) {
        Util.notNull(elements, "elements"); //$NON-NLS-1$
        Util.notContainNull(elements, "elements"); //$NON-NLS-1$
        this.elements = Util.freeze(elements);
    }

    /**
     * この要素の種類を表す{@link ModelKind#DOC_BLOCK}を返す。
     * @return {@link ModelKind#DOC_BLOCK}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.DOC_BLOCK;
    }

    @Override
    public <R, C, E extends Throwable> R accept(
            Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitDocBlock(this, context);
    }
}
