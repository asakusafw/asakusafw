/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.dmdl.thundergate.view.model;

import java.text.MessageFormat;

/**
 * 結合条件(等価結合のみ)。
 */
public class On {

    /**
     * 第一項の名前。
     */
    public final Name left;

    /**
     * 第二項の名前。
     */
    public final Name right;

    /**
     * インスタンスを生成する。
     * @param left 第一項の名前
     * @param right 第二項の名前
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public On(Name left, Name right) {
        if (left == null) {
            throw new IllegalArgumentException("left must not be null"); //$NON-NLS-1$
        }
        if (right == null) {
            throw new IllegalArgumentException("right must not be null"); //$NON-NLS-1$
        }
        this.left = left;
        this.right = right;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + left.hashCode();
        result = prime * result + right.hashCode();
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
        On other = (On) obj;
        if (!left.equals(other.left)) {
            return false;
        }
        if (!right.equals(other.right)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0} = {1}",
                left,
                right);
    }
}
