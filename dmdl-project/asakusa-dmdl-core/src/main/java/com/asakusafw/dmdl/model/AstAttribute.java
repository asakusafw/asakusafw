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
package com.asakusafw.dmdl.model;

import java.util.Arrays;
import java.util.List;

import com.asakusafw.dmdl.Region;
import com.asakusafw.utils.collections.Lists;

/**
 * Represents attributes of declarations.
 * @since 0.2.0
 */
public class AstAttribute extends AbstractAstNode {

    private final Region region;

    /**
     * The name of this attribute.
     */
    public final AstName name;

    /**
     * Sub elements of this attribute.
     */
    public final List<AstAttributeElement> elements;

    /**
     * Creates and returns a new instance.
     * @param region the region of this node on the enclosing script, or {@code null} if unknown
     * @param name the name of this attribute
     * @param elements sub-elements of this attribute
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AstAttribute(
            Region region,
            AstName name,
            List<AstAttributeElement> elements) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (elements == null) {
            throw new IllegalArgumentException("elements must not be null"); //$NON-NLS-1$
        }
        this.region = region;
        this.name = name;
        this.elements = Lists.freeze(elements);
    }

    /**
     * Creates and returns a new instance.
     * @param region the region of this node on the enclosing script, or {@code null} if unknown
     * @param name the name of this attribute
     * @param elements sub-elements of this attribute
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AstAttribute(
            Region region,
            AstName name,
            AstAttributeElement... elements) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (elements == null) {
            throw new IllegalArgumentException("elements must not be null"); //$NON-NLS-1$
        }
        this.region = region;
        this.name = name;
        this.elements = Lists.freeze(Arrays.asList(elements));
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
        R result = visitor.visitAttribute(context, this);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + elements.hashCode();
        result = prime * result + name.hashCode();
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
        AstAttribute other = (AstAttribute) obj;
        if (!elements.equals(other.elements)) {
            return false;
        }
        if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
