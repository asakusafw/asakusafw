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

import java.text.MessageFormat;

/**
 * Represents method or constructor parameters in {@link IrDocMethod}.
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
     * Returns the parameter type.
     * @return the parameter type
     */
    public IrDocType getType() {
        return this.type;
    }

    /**
     * Sets the parameter type.
     * @param type the parameter type
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void setType(IrDocType type) {
        if (type == null) {
            throw new IllegalArgumentException("type"); //$NON-NLS-1$
        }
        this.type = type;
    }

    /**
     * Returns whether this parameter is variable arity or not.
     * @return {@code true} if this parameter is variable arity, otherwise {@code false}
     */
    public boolean isVariableArity() {
        return this.variableArity;
    }

    /**
     * Sets the whether this parameter is variable arity.
     * @param variableArity {@code true} if this parameter is variable arity, otherwise {@code false}
     */
    public void setVariableArity(boolean variableArity) {
        this.variableArity = variableArity;
    }

    /**
     * Returns the parameter name.
     * @return the parameter name, or {@code null} if it is not specified
     */
    public IrDocSimpleName getName() {
        return this.name;
    }

    /**
     * Sets the parameter name.
     * @param name the parameter name, or {@code null} to unset
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
