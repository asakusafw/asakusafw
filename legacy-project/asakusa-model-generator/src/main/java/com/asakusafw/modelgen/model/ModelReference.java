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
package com.asakusafw.modelgen.model;

import java.text.MessageFormat;

/**
 * モデルへの参照を表すオブジェクト。
 */
public class ModelReference {

    private String namespace;

    private String simpleName;

    /**
     * インスタンスを生成する。
     * @param namespace 参照先のモデルの名前空間、標準名前空間の場合は{@code null}
     * @param simpleName 参照先のモデルの単純名
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ModelReference(String namespace, String simpleName) {
        if (simpleName == null) {
            throw new IllegalArgumentException("simpleName must not be null"); //$NON-NLS-1$
        }
        this.namespace = namespace;
        this.simpleName = simpleName;
    }

    /**
     * このモデルが標準の名前空間に存在する場合のみ{@code true}を返す。
     * @return このモデルが標準の名前空間に存在する場合に{@code true}、
     *     それ以外の場合は{@code false}
     */
    public boolean isDefaultNameSpace() {
        return namespace == null;
    }

    /**
     * 対象のモデルが属する名前空間を返す。
     * <p>
     * 対象のモデルが標準の名前空間に存在する場合、この呼び出しは{@code null}を返す。
     * </p>
     * @return 対象のモデルが属する名前空間、標準の名前空間に属する場合は{@code null}
     */
    public String getNamespace() {
        return namespace;
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
        result = prime * result
                + ((namespace == null) ? 0 : namespace.hashCode());
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
        if (namespace == null) {
            if (other.namespace != null) {
                return false;
            }
        } else if (namespace.equals(other.namespace) == false) {
            return false;
        }
        if (simpleName.equals(other.simpleName) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "@{0}.{1}",
                namespace == null ? "" : namespace,
                simpleName);
    }
}
