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
 * 名前付きの型。
 */
public class IrDocNamedType extends AbstractIrDocElement implements IrDocType {

    private static final long serialVersionUID = 1L;

    private IrDocName name;

    /**
     * インスタンスを生成する。
     * @param name 型の名前
     * @throws IllegalArgumentException 引数に{@code null}が含まれていた場合
     */
    public IrDocNamedType(IrDocName name) {
        super();
        if (name == null) {
            throw new IllegalArgumentException("name"); //$NON-NLS-1$
        }
        this.name = name;
    }

    @Override
    public IrDocElementKind getKind() {
        return IrDocElementKind.NAMED_TYPE;
    }

    /**
     * この型の名前を返す。
     * @return 型の名前
     */
    public IrDocName getName() {
        return this.name;
    }

    /**
     * 型の名前を設定する。
     * @param name 設定する名前
     * @throws IllegalArgumentException 引数に{@code null}が含まれていた場合
     */
    public void setName(IrDocName name) {
        if (name == null) {
            throw new IllegalArgumentException("name"); //$NON-NLS-1$
        }
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        final IrDocNamedType other = (IrDocNamedType) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getName().toString();
    }

    @Override
    public <R, P> R accept(IrDocElementVisitor<R, P> visitor, P context) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor"); //$NON-NLS-1$
        }
        return visitor.visitNamedType(this, context);
    }
}
