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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

/**
 * テキスト。
 */
public class IrDocText extends AbstractIrDocElement implements IrDocFragment {

    private static final long serialVersionUID = 1L;

    private String content;

    /**
     * Creates a new instance.
     * @param content 内容のテキスト
     */
    public IrDocText(String content) {
        super();
        this.content = content;
    }

    @Override
    public IrDocElementKind getKind() {
        return IrDocElementKind.TEXT;
    }

    /**
     * 内容のテキストを返す。
     * @return 内容のテキスト
     */
    public String getContent() {
        return this.content;
    }

    /**
     * 内容のテキストを設定する。
     * @param content 設定するテキスト
     * @throws IllegalArgumentException 引数に{@code null}が含まれていた場合
     * @throws IllegalArgumentException 引数に{@code "*" "/"}の連続が含まれる場合
     */
    public void setContent(String content) {
        if (content == null) {
            throw new IllegalArgumentException("content"); //$NON-NLS-1$
        }
        checkContent(content);
        this.content = content;
    }

    private void checkContent(String text) {
        if (text.indexOf("*/") >= 0) { //$NON-NLS-1$
            throw new IllegalArgumentException(text);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IrDocText other = (IrDocText) obj;
        if (content == null) {
            if (other.content != null) {
                return false;
            }
        } else if (!content.equals(other.content)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getContent();
    }

    @Override
    public <R, P> R accept(IrDocElementVisitor<R, P> visitor, P context) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor"); //$NON-NLS-1$
        }
        return visitor.visitText(this, context);
    }
}
