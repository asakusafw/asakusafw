/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
 * Represents named type in {@link IrDocComment}.
 */
public class IrDocNamedType extends AbstractIrDocElement implements IrDocType {

    private static final long serialVersionUID = 1L;

    private IrDocName name;

    /**
     * Creates a new instance.
     * @param name the type name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public IrDocNamedType(IrDocName name) {
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
     * Returns the type name.
     * @return the type name
     */
    public IrDocName getName() {
        return this.name;
    }

    /**
     * Sets the type name.
     * @param name the type name
     * @throws IllegalArgumentException if the parameter is {@code null}
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
        IrDocNamedType other = (IrDocNamedType) obj;
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
