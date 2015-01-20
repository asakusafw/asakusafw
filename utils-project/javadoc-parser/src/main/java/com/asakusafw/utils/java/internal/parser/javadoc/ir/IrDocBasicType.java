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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

/**
 * 基本型。
 */
public class IrDocBasicType extends AbstractIrDocElement implements IrDocType {

    private static final long serialVersionUID = 1L;

    private IrBasicTypeKind typeKind;

    /**
     * インスタンスを生成する。
     * @param typeKind 型の種類
     * @throws IllegalArgumentException 省略可能でない引数に{@code null}が含まれていた場合
     */
    public IrDocBasicType(IrBasicTypeKind typeKind) {
        super();
        if (typeKind == null) {
            throw new IllegalArgumentException("typeKind"); //$NON-NLS-1$
        }
        this.typeKind = typeKind;
    }

    @Override
    public IrDocElementKind getKind() {
        return IrDocElementKind.BASIC_TYPE;
    }

    /**
     * この型の種類を返す。
     * @return この型の種類
     */
    public IrBasicTypeKind getTypeKind() {
        return this.typeKind;
    }

    /**
     * この型の種類を設定する。
     * @param typeKind 設定する種類
     * @throws IllegalArgumentException 引数に{@code null}が含まれていた場合
     */
    public void setTypeKind(IrBasicTypeKind typeKind) {
        if (typeKind == null) {
            throw new IllegalArgumentException("typeKind"); //$NON-NLS-1$
        }
        this.typeKind = typeKind;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + typeKind.hashCode();
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
        final IrDocBasicType other = (IrDocBasicType) obj;
        if (!typeKind.equals(other.typeKind)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getTypeKind().getSymbol();
    }

    @Override
    public <R, P> R accept(IrDocElementVisitor<R, P> visitor, P context) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor"); //$NON-NLS-1$
        }
        return visitor.visitBasicType(this, context);
    }
}
