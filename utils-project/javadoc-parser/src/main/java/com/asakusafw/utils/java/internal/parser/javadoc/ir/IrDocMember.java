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

/**
 * An abstract super class of member references in {@link IrDocComment}.
 */
public abstract class IrDocMember extends AbstractIrDocElement implements IrDocFragment {

    private static final long serialVersionUID = -7631714928819729918L;

    private IrDocNamedType declaringType;

    private IrDocSimpleName name;

    /**
     * Returns the declaring type.
     * @return the declaring type, or {@code null} if it is not specified
     */
    public IrDocNamedType getDeclaringType() {
        return this.declaringType;
    }

    /**
     * Sets the declaring type.
     * @param declaringType the declaring type, or {@code null} to unset
     */
    public void setDeclaringType(IrDocNamedType declaringType) {
        this.declaringType = declaringType;
    }

    /**
     * Returns the member name.
     * @return the member name
     */
    public IrDocSimpleName getName() {
        return this.name;
    }

    /**
     * Sets the member name.
     * @param name the member name
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public void setName(IrDocSimpleName name) {
        if (name == null) {
            throw new IllegalArgumentException("name"); //$NON-NLS-1$
        }
        this.name = name;
    }
}
