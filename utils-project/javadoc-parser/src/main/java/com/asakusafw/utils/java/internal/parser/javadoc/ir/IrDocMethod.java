/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * フィールドへの参照。
 */
public class IrDocMethod extends IrDocMember {

    private static final long serialVersionUID = 1L;

    private List<? extends IrDocMethodParameter> parameters;

    /**
     * インスタンスを生成する。
     */
    public IrDocMethod() {
        super();
        this.parameters = Collections.emptyList();
    }

    @Override
    public IrDocElementKind getKind() {
        return IrDocElementKind.METHOD;
    }

    /**
     * 引数リストを返す。
     * @return 引数リスト
     */
    public List<? extends IrDocMethodParameter> getParameters() {
        return this.parameters;
    }

    /**
     * 引数リストを設定する。
     * @param parameters 設定する引数の一覧
     * @throws IllegalArgumentException 引数に{@code null}が含まれていた場合
     */
    public void setParameters(List<? extends IrDocMethodParameter> parameters) {
        if (parameters == null) {
            throw new IllegalArgumentException("parameters"); //$NON-NLS-1$
        }
        this.parameters = Collections.unmodifiableList(
            new ArrayList<IrDocMethodParameter>(parameters));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        IrDocNamedType type = getDeclaringType();
        result = prime * result + (type == null ? 0 : type.hashCode());
        IrDocSimpleName name = getName();
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (parameters == null ? 0 : parameters.hashCode());
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
        final IrDocMethod other = (IrDocMethod) obj;
        IrDocNamedType type = getDeclaringType();
        IrDocNamedType oType = other.getDeclaringType();
        if (type == null) {
            if (oType != null) {
                return false;
            }
        } else if (type.equals(oType) == false) {
            return false;
        }
        IrDocSimpleName name = getName();
        IrDocSimpleName oName = other.getName();
        if (name == null) {
            if (oName != null) {
                return false;
            }
        } else if (name.equals(oName) == false) {
            return false;
        }
        if (parameters == null) {
            if (other.parameters != null) {
                return false;
            }
        } else if (parameters.equals(other.parameters) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (getDeclaringType() == null) {
            return MessageFormat.format(
                "#{1}({2})", //$NON-NLS-1$
                getDeclaringType(),
                getName(),
                parameters);
        } else {
            return MessageFormat.format(
                "{0}#{1}({2})", //$NON-NLS-1$
                getDeclaringType(),
                getName(),
                parameters);
        }
    }

    @Override
    public <R, P> R accept(IrDocElementVisitor<R, P> visitor, P context) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor"); //$NON-NLS-1$
        }
        return visitor.visitMethod(this, context);
    }
}
