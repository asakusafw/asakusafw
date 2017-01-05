/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
 * Represents plain text in {@link IrDocComment}.
 */
public class IrDocText extends AbstractIrDocElement implements IrDocFragment {

    private static final long serialVersionUID = 1L;

    private String content;

    /**
     * Creates a new instance.
     * @param content the contents
     * @throws IllegalArgumentException if the string contains comment delimiters (<code>&#47;&#42;</code>)
     */
    public IrDocText(String content) {
        checkContent(content);
        this.content = content;
    }

    @Override
    public IrDocElementKind getKind() {
        return IrDocElementKind.TEXT;
    }

    /**
     * Returns the contents.
     * @return the contents
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Sets the contents.
     * @param content the contents
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @throws IllegalArgumentException if the string contains comment delimiters (<code>&#47;&#42;</code>)
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
        IrDocText other = (IrDocText) obj;
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
