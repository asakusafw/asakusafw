/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.asakusafw.dmdl.Region;

/**
 * Represents simple names.
 * @since 0.2.0
 */
public class AstSimpleName extends AbstractAstNode implements AstName {

    private final Region region;

    /**
     * The identifier of this name.
     */
    public final String identifier;

    /**
     * Creates a new instance.
     * @param region the region of this node, or {@code null} if unknown
     * @param identifier the identifier of this name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AstSimpleName(Region region, String identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("identifier must not be null"); //$NON-NLS-1$
        }
        this.region = region;
        this.identifier = identifier;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public AstName getQualifier() {
        return null;
    }

    @Override
    public AstSimpleName getSimpleName() {
        return this;
    }

    /**
     * Returns the words in this name.
     * @return the words
     */
    public List<String> getWordList() {
        int start = identifier.indexOf('_');
        if (start < 0) {
            return Collections.singletonList(identifier);
        }
        List<String> results = new ArrayList<>();
        if (start != 0) {
            results.add(identifier.substring(0, start));
        }
        start++;
        while (true) {
            int next = identifier.indexOf('_', start);
            if (next < 0) {
                break;
            } else if (next > start) {
                results.add(identifier.substring(start, next));
            }
            start = next + 1;
        }
        results.add(identifier.substring(start));
        return results;
    }

    @Override
    public <C, R> R accept(C context, Visitor<C, R> visitor) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor must not be null"); //$NON-NLS-1$
        }
        R result = visitor.visitSimpleName(context, this);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + identifier.hashCode();
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
        AstSimpleName other = (AstSimpleName) obj;
        if (!identifier.equals(other.identifier)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return identifier;
    }
}
