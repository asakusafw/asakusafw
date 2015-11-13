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
 * Represents array types.
 */
public class IrDocArrayType extends AbstractIrDocElement implements IrDocType {

    private static final long serialVersionUID = 1L;

    private IrDocType componentType;

    /**
     * Creates a new instance.
     * @param componentType the element type
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public IrDocArrayType(IrDocType componentType) {
        if (componentType == null) {
            throw new IllegalArgumentException("componentType"); //$NON-NLS-1$
        }
        this.componentType = componentType;
    }

    @Override
    public IrDocElementKind getKind() {
        return IrDocElementKind.ARRAY_TYPE;
    }

    /**
     * Returns the element type.
     * @return the element type
     */
    public IrDocType getComponentType() {
        return this.componentType;
    }

    /**
     * Sets the the element type.
     * @param componentType the element type
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @throws IllegalArgumentException if the component type contains this type
     */
    public void setComponentType(IrDocType componentType) {
        if (componentType == null) {
            throw new IllegalArgumentException("componentType"); //$NON-NLS-1$
        }
        checkCyclic(componentType);
        this.componentType = componentType;
    }

    private void checkCyclic(IrDocType t) {
        IrDocType current = t;
        while (t.getKind() == IrDocElementKind.ARRAY_TYPE) {
            if (current == this) {
                throw new IllegalArgumentException(t.toString());
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((componentType == null) ? 0 : componentType.hashCode());
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
        final IrDocArrayType other = (IrDocArrayType) obj;
        if (componentType == null) {
            if (other.componentType != null) {
                return false;
            }
        } else if (!componentType.equals(other.componentType)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getComponentType() + "[]"; //$NON-NLS-1$
    }

    @Override
    public <R, P> R accept(IrDocElementVisitor<R, P> visitor, P context) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor"); //$NON-NLS-1$
        }
        return visitor.visitArrayType(this, context);
    }
}
