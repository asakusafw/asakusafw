/**
 * Copyright 2011-2013 Asakusa Framework Team.
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

import java.util.List;

import com.asakusafw.dmdl.Region;
import com.asakusafw.utils.collections.Lists;

/**
 * Represents a mapping function for each property.
 * @since 0.2.0
 */
public class AstPropertyMapping extends AbstractAstNode {

    private final Region region;

    /**
     * The description of the defining property, or {@code null} if is omitted.
     */
    public final AstDescription description;

    /**
     * The attributes of the defining property.
     */
    public final List<AstAttribute> attributes;

    /**
     * The property name from the source model.
     */
    public final AstSimpleName source;

    /**
     * The target property name.
     */
    public final AstSimpleName target;

    /**
     * Creates and returns a new instance.
     * @param region the region of this node on the enclosing script, or {@code null} if unknown
     * @param description the description of this property, or {@code null} if is omitted
     * @param attributes the attributes of this property
     * @param source the property name from the source model
     * @param target the target property name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AstPropertyMapping(
            Region region,
            AstDescription description,
            List<AstAttribute> attributes,
            AstSimpleName source,
            AstSimpleName target) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null"); //$NON-NLS-1$
        }
        if (target == null) {
            throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
        }
        this.region = region;
        this.description = description;
        this.attributes = Lists.freeze(attributes);
        this.source = source;
        this.target = target;
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
        R result = visitor.visitPropertyMapping(context, this);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + source.hashCode();
        result = prime * result + target.hashCode();
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
        AstPropertyMapping other = (AstPropertyMapping) obj;
        if (!source.equals(other.source)) {
            return false;
        }
        if (!target.equals(other.target)) {
            return false;
        }
        return true;
    }
}
