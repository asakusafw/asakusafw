/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
 * Represents a record structure definition.
 * @since 0.2.0
 */
public class AstRecordDefinition extends AbstractAstNode implements AstRecord {

    private final Region region;

    /**
     * The property definitions.
     */
    public final List<AstPropertyDefinition> properties;

    /**
     * Creates and returns a new instance.
     * @param region the region of this node, or {@code null} if unknown
     * @param properties the property definitions
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AstRecordDefinition(Region region, List<AstPropertyDefinition> properties) {
        this.region = region;
        this.properties = Lists.freeze(properties);
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public AstRecord getUnit() {
        return this;
    }

    @Override
    public <C, R> R accept(C context, AstNode.Visitor<C, R> visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor must not be null"); //$NON-NLS-1$
        }
        R result = visitor.visitRecordDefinition(context, this);
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
        AstRecordDefinition other = (AstRecordDefinition) obj;
        if (!properties.equals(other.properties)) {
            return false;
        }
        return true;
    }
}
