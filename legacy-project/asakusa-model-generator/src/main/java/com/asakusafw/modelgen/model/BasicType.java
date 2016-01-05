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

/**
 * 属性を持たない型。
 */
public class BasicType implements PropertyType {

    private PropertyTypeKind kind;

    /**
     * インスタンスを生成する。
     * @param kind 型の種類
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public BasicType(PropertyTypeKind kind) {
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null"); //$NON-NLS-1$
        }
        if (kind.variant) {
            throw new IllegalArgumentException("kind must not have variant"); //$NON-NLS-1$
        }
        this.kind = kind;
    }

    @Override
    public PropertyTypeKind getKind() {
        return kind;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + kind.hashCode();
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
        BasicType other = (BasicType) obj;
        if (kind != other.kind) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return kind.toString();
    }
}
