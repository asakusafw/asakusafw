/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
 * Represents a folding specification for each property.
 * @since 0.2.0
 */
public class AstModelFolding extends AbstractAstNode {

    private final Region region;

    /**
     * The folding functions for each property.
     */
    public final List<AstPropertyFolding> properties;

    /**
     * Creates and returns a new instance.
     * @param region the region of this node on the enclosing script, or {@code null} if unknown
     * @param properties the folding functions for each property
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AstModelFolding(Region region, List<AstPropertyFolding> properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        this.region = region;
        this.properties = Lists.freeze(properties);
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
        R result = visitor.visitModelFolding(context, this);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + properties.hashCode();
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
        AstModelFolding other = (AstModelFolding) obj;
        if (!properties.equals(other.properties)) {
            return false;
        }
        return true;
    }
}
