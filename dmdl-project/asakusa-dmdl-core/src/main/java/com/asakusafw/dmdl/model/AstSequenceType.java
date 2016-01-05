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
package com.asakusafw.dmdl.model;

import com.asakusafw.dmdl.Region;

/**
 * Represents a sequence type of a property.
 */
public class AstSequenceType extends AbstractAstNode implements AstType {

    private final Region region;

    /**
     * The type of elements in this sequence.
     */
    public final AstType elementType;

    /**
     * Creates and returns a new instance.
     * @param region the region of this node on the enclosing script, or {@code null} if unknown
     * @param elementType the type of elements in this sequence
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AstSequenceType(Region region, AstType elementType) {
        if (elementType == null) {
            throw new IllegalArgumentException("elementType must not be null"); //$NON-NLS-1$
        }
        this.region = region;
        this.elementType = elementType;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public <C, R> R accept(C context, Visitor<C, R> visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor must not be null"); //$NON-NLS-1$
        }
        R result = visitor.visitSequenceType(context, this);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + elementType.hashCode();
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
        AstSequenceType other = (AstSequenceType) obj;
        if (!elementType.equals(other.elementType)) {
            return false;
        }
        return true;
    }
}
