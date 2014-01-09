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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

import java.text.MessageFormat;

/**
 * メソッド引数。
 */
public class IrDocMethodParameter extends AbstractIrDocElement {

    private static final long serialVersionUID = 1L;

    private IrDocType type;
    private boolean variableArity;
    private IrDocSimpleName name;

    @Override
    public IrDocElementKind getKind() {
        return IrDocElementKind.METHOD_PARAMETER;
    }

    /**
     * この引数の型を返す。
     * @return この引数の型
     */
    public IrDocType getType() {
        return this.type;
    }

    /**
     * この引数の型を設定する。
     * @param type 設定する型
     * @throws IllegalArgumentException 引数に{@code null}が含まれていた場合
     */
    public void setType(IrDocType type) {
        if (type == null) {
            throw new IllegalArgumentException("type"); //$NON-NLS-1$
        }
        this.type = type;
    }

    /**
     * この引数が可変長引数として宣言されている場合のみ{@code true}を返す。
     * @return 可変長引数として宣言されている場合のみ{@code true}
     */
    public boolean isVariableArity() {
        return this.variableArity;
    }

    /**
     * この引数の可変長性を設定する。
     * @param variableArity {@code true}ならばこの引数は可変長
     */
    public void setVariableArity(boolean variableArity) {
        this.variableArity = variableArity;
    }

    /**
     * この引数の名前を返す。
     * @return この引数の名前
     */
    public IrDocSimpleName getName() {
        return this.name;
    }

    /**
     * この引数の名前を設定する。
     * @param name 設定する名前(省略可能)
     */
    public void setName(IrDocSimpleName name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + (variableArity ? 1231 : 1237);
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
        final IrDocMethodParameter other = (IrDocMethodParameter) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (variableArity != other.variableArity) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (getName() == null) {
            if (isVariableArity()) {
                return getType() + "..."; //$NON-NLS-1$
            } else {
                return getType().toString();
            }
        } else {
            return MessageFormat.format(
                "{0}{1}{2}", //$NON-NLS-1$
                getType(),
                isVariableArity() ? "..." : " ", //$NON-NLS-1$ //$NON-NLS-2$
                getName());
        }
    }

    @Override
    public <R, P> R accept(IrDocElementVisitor<R, P> visitor, P context) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor"); //$NON-NLS-1$
        }
        return visitor.visitMethodParameter(this, context);
    }
}
