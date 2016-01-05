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
package com.asakusafw.dmdl.thundergate.model;

import java.text.MessageFormat;

/**
 * モデルへの参照を表すオブジェクト。
 */
public class ModelReference {

    private String simpleName;

    /**
     * インスタンスを生成する。
     * @param simpleName 参照先のモデルの単純名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ModelReference(String simpleName) {
        if (simpleName == null) {
            throw new IllegalArgumentException("simpleName must not be null"); //$NON-NLS-1$
        }
        this.simpleName = simpleName;
    }

    /**
     * 対象のモデルの単純名を返す。
     * @return 対象のモデルの単純名
     */
    public String getSimpleName() {
        return simpleName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + simpleName.hashCode();
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
        ModelReference other = (ModelReference) obj;
        if (simpleName.equals(other.simpleName) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "@{0}",
                simpleName);
    }
}
