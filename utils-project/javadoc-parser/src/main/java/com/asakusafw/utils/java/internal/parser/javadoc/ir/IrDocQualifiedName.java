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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 限定名。
 */
public class IrDocQualifiedName extends IrDocName {

    private static final long serialVersionUID = 1L;

    private IrDocName qualifier;
    private IrDocSimpleName name;

    /**
     * インスタンスを生成する。
     * @param qualifier 修飾する名前
     * @param name 修飾される名前
     * @throws IllegalArgumentException 引数に自分が含まれる場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれていた場合
     */
    public IrDocQualifiedName(IrDocName qualifier, IrDocSimpleName name) {
        super();
        if (qualifier == null) {
            throw new IllegalArgumentException("qualifier"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name"); //$NON-NLS-1$
        }
        this.qualifier = qualifier;
        this.name = name;
    }

    @Override
    public IrDocElementKind getKind() {
        return IrDocElementKind.QUALIFIED_NAME;
    }

    /**
     * この名前を修飾する別の名前を返す。
     * @return この名前を修飾する別の名前
     */
    public IrDocName getQualifier() {
        return this.qualifier;
    }

    /**
     * この名前を修飾する別の名前を設定する。
     * @param qualifier 設定する修飾名
     * @throws IllegalArgumentException 引数に自分が含まれる場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれていた場合
     */
    public void setQualifier(IrDocName qualifier) {
        if (qualifier == null) {
            throw new IllegalArgumentException("qualifier"); //$NON-NLS-1$
        }
        checkCyclic(qualifier);
        this.qualifier = qualifier;
    }

    private void checkCyclic(IrDocName target) {
        IrDocName current = target;
        while (current.getKind() == IrDocElementKind.QUALIFIED_NAME) {
            if (current == this) {
                throw new IllegalArgumentException(target.toString());
            }
            current = ((IrDocQualifiedName) target).getQualifier();
        }
    }

    /**
     * 単純名を返す。
     * @return 修飾された単純名
     */
    public IrDocSimpleName getName() {
        return this.name;
    }

    /**
     * 単純名を設定する。
     * @param name 設定する名前
     * @throws IllegalArgumentException 引数に{@code null}が含まれていた場合
     */
    public void setName(IrDocSimpleName name) {
        if (name == null) {
            throw new IllegalArgumentException("name"); //$NON-NLS-1$
        }
        this.name = name;
    }

    @Override
    public String asString() {
        LinkedList<IrDocSimpleName> names = new LinkedList<IrDocSimpleName>();
        IrDocName current = getQualifier();
        while (current.getKind() == IrDocElementKind.QUALIFIED_NAME) {
            IrDocQualifiedName qName = (IrDocQualifiedName) current;
            names.addFirst(qName.getName());
            current = qName.getQualifier();
        }
        names.addFirst((IrDocSimpleName) current);

        StringBuilder buf = new StringBuilder();

        for (IrDocSimpleName n: names) {
            buf.append(n.getIdentifier());
            buf.append('.');
        }
        buf.append(getName().getIdentifier());
        return buf.toString();
    }

    @Override
    public List<IrDocSimpleName> asSimpleNameList() {
        LinkedList<IrDocSimpleName> names = new LinkedList<IrDocSimpleName>();

        names.addFirst(getName());
        IrDocName current = this.getQualifier();
        while (current.getKind() == IrDocElementKind.QUALIFIED_NAME) {
            IrDocQualifiedName q = (IrDocQualifiedName) current;
            names.addFirst(q.getName());
            current = q.getQualifier();
        }
        names.addFirst((IrDocSimpleName) current);
        return new ArrayList<IrDocSimpleName>(names);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + qualifier.hashCode();
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
        final IrDocQualifiedName other = (IrDocQualifiedName) obj;
        if (!name.equals(other.name)) {
            return false;
        }
        if (!qualifier.equals(other.qualifier)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public <R, P> R accept(IrDocElementVisitor<R, P> visitor, P context) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor"); //$NON-NLS-1$
        }
        return visitor.visitQualifiedName(this, context);
    }
}
