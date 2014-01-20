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
package com.asakusafw.dmdl.model;

import com.asakusafw.dmdl.Region;

/**
 * Represents elements of an attribute.
 * @since 0.2.0
 */
public class AstAttributeElement extends AbstractAstNode {

    private final Region region;

    /**
     * The name of this element.
     */
    public final AstSimpleName name;

    /**
     * The holding value of this element.
     */
    public final AstAttributeValue value;

    /**
     * Creates and returns a new instance.
     * @param region the region of this node on the enclosing script, or {@code null} if unknown
     * @param name the name
     * @param value the holding value
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AstAttributeElement(
            Region region,
            AstSimpleName name,
            AstAttributeValue value) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (value == null) {
            throw new IllegalArgumentException("value must not be null"); //$NON-NLS-1$
        }
        this.region = region;
        this.name = name;
        this.value = value;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public <C, R> R accept(C context, AstNode.Visitor<C, R> visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor must not be null"); //$NON-NLS-1$
        }
        R result = visitor.visitAttributeElement(context, this);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + value.hashCode();
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
        AstAttributeElement other = (AstAttributeElement) obj;
        if (!name.equals(other.name)) {
            return false;
        }
        if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }
}
