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

/**
 * フィールドまたはメソッド。
 */
public abstract class IrDocMember extends AbstractIrDocElement implements IrDocFragment {

    /**
     * serialVersionUID を表す。
     */
    private static final long serialVersionUID = -7631714928819729918L;
    private IrDocNamedType declaringType;
    private IrDocSimpleName name;

    /**
     * インスタンスを生成する。
     */
    public IrDocMember() {
        super();
    }

    /**
     * このメンバを宣言した型を返す。
     * 省略されている場合、この呼び出しは{@code null}を返す。
     * @return メンバを宣言した型、省略されていた場合は{@code null}
     */
    public IrDocNamedType getDeclaringType() {
        return this.declaringType;
    }

    /**
     * 今メンバを宣言した型を設定する。
     * @param declaringType 設定する型、省略する場合は{@code null}
     */
    public void setDeclaringType(IrDocNamedType declaringType) {
        this.declaringType = declaringType;
    }

    /**
     * このメンバの名前を返す。
     * @return このメンバの名前
     */
    public IrDocSimpleName getName() {
        return this.name;
    }

    /**
     * このメンバの名前を設定する。
     * @param name このメンバの名前
     * @throws IllegalArgumentException 引数に{@code null}が含まれていた場合
     */
    public void setName(IrDocSimpleName name) {
        if (name == null) {
            throw new IllegalArgumentException("name"); //$NON-NLS-1$
        }
        this.name = name;
    }
}
