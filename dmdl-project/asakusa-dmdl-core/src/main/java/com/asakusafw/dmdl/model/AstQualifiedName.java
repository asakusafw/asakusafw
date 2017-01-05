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

import java.util.LinkedList;

import com.asakusafw.dmdl.Region;

/**
 * Represents simple names.
 * @since 0.2.0
 */
public class AstQualifiedName extends AbstractAstNode implements AstName {

    private final Region region;

    /**
     * The qualifier of this name.
     */
    public final AstName qualifier;

    /**
     * The simple name of this name in tail.
     */
    public final AstSimpleName simpleName;

    /**
     * Creates a new instance.
     * @param region the region of this node, or {@code null} if unknown
     * @param qualifier the qualifier name of this
     * @param simpleName the simple name of this
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AstQualifiedName(Region region, AstName qualifier, AstSimpleName simpleName) {
        if (qualifier == null) {
            throw new IllegalArgumentException("qualifier must not be null"); //$NON-NLS-1$
        }
        if (simpleName == null) {
            throw new IllegalArgumentException("simpleName must not be null"); //$NON-NLS-1$
        }
        this.region = region;
        this.qualifier = qualifier;
        this.simpleName = simpleName;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public AstName getQualifier() {
        return qualifier;
    }

    @Override
    public AstSimpleName getSimpleName() {
        return simpleName;
    }

    @Override
    public <C, R> R accept(C context, Visitor<C, R> visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor must not be null"); //$NON-NLS-1$
        }
        R result = visitor.visitQualifiedName(context, this);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + qualifier.hashCode();
        result = prime * result + simpleName.hashCode();
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
        AstQualifiedName other = (AstQualifiedName) obj;
        if (!qualifier.equals(other.qualifier)) {
            return false;
        }
        if (!simpleName.equals(other.simpleName)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        LinkedList<AstSimpleName> names = new LinkedList<>();
        AstName current = this;
        while (current.getQualifier() != null) {
            names.addFirst(current.getSimpleName());
            current = current.getQualifier();
        }
        assert current instanceof AstSimpleName;
        StringBuilder buf = new StringBuilder();
        buf.append(current.getSimpleName().identifier);
        for (AstSimpleName segment : names) {
            buf.append('.');
            buf.append(segment.identifier);
        }
        return buf.toString();
    }
}
