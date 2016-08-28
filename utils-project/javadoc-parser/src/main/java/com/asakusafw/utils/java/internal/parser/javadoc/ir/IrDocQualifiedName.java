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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents qualified names in {@link IrDocComment}.
 */
public class IrDocQualifiedName extends IrDocName {

    private static final long serialVersionUID = 1L;

    private IrDocName qualifier;
    private IrDocSimpleName name;

    /**
     * Creates a new instance.
     * @param qualifier the name qualifier
     * @param name the simple name
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public IrDocQualifiedName(IrDocName qualifier, IrDocSimpleName name) {
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
     * Returns the name qualifier.
     * @return the name qualifier
     */
    public IrDocName getQualifier() {
        return this.qualifier;
    }

    /**
     * Sets the name qualifier.
     * @param qualifier the name qualifier
     * @throws IllegalArgumentException if the qualifier contains this name
     * @throws IllegalArgumentException if the parameter is {@code null}
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
     * Returns the simple name.
     * @return the simple name
     */
    public IrDocSimpleName getName() {
        return this.name;
    }

    /**
     * Sets the simple name.
     * @param name the simple name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void setName(IrDocSimpleName name) {
        if (name == null) {
            throw new IllegalArgumentException("name"); //$NON-NLS-1$
        }
        this.name = name;
    }

    @Override
    public String asString() {
        LinkedList<IrDocSimpleName> names = new LinkedList<>();
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
        LinkedList<IrDocSimpleName> names = new LinkedList<>();

        names.addFirst(getName());
        IrDocName current = this.getQualifier();
        while (current.getKind() == IrDocElementKind.QUALIFIED_NAME) {
            IrDocQualifiedName q = (IrDocQualifiedName) current;
            names.addFirst(q.getName());
            current = q.getQualifier();
        }
        names.addFirst((IrDocSimpleName) current);
        return new ArrayList<>(names);
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
        IrDocQualifiedName other = (IrDocQualifiedName) obj;
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
