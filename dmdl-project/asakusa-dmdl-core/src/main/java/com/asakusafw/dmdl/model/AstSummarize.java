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

import com.asakusafw.dmdl.Region;

/**
 * Represents a term of join model.
 * @since 0.2.0
 */
public class AstSummarize extends AbstractAstNode implements AstTerm<AstSummarize> {

    private final Region region;

    /**
     * The reference to the summarize target model.
     */
    public final AstModelReference reference;

    /**
     * The folding function how to convert summarize target to the result model.
     */
    public final AstModelFolding folding;

    /**
     * The grouping properties for aggregation.
     * <p>
     * This can be {@code null} if use total grouping.
     * </p>
     */
    public final AstGrouping grouping;

    /**
     * Creates and returns a new instance.
     * @param region the region of this node on the enclosing script, or {@code null} if unknown
     * @param reference the reference to the summarize target model
     * @param folding the folding function
     * @param grouping the grouping properties, or {@code null} on total grouping
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AstSummarize(
            Region region,
            AstModelReference reference,
            AstModelFolding folding,
            AstGrouping grouping) {
        if (reference == null) {
            throw new IllegalArgumentException("reference must not be null"); //$NON-NLS-1$
        }
        if (folding == null) {
            throw new IllegalArgumentException("folding must not be null"); //$NON-NLS-1$
        }
        this.region = region;
        this.reference = reference;
        this.folding = folding;
        this.grouping = grouping;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public AstSummarize getUnit() {
        return this;
    }

    @Override
    public <C, R> R accept(C context, AstNode.Visitor<C, R> visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor must not be null"); //$NON-NLS-1$
        }
        R result = visitor.visitSummarize(context, this);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + reference.hashCode();
        result = prime * result + folding.hashCode();
        result = prime * result + ((grouping == null) ? 0 : grouping.hashCode());
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
        AstSummarize other = (AstSummarize) obj;
        if (!reference.equals(other.reference)) {
            return false;
        }
        if (!folding.equals(other.folding)) {
            return false;
        }
        if (grouping == null) {
            if (other.grouping != null) {
                return false;
            }
        } else if (!grouping.equals(other.grouping)) {
            return false;
        }
        return true;
    }
}
