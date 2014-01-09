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

import java.text.MessageFormat;
import java.util.List;

import com.asakusafw.dmdl.Region;

/**
 * Represents a model definition.
 * @param <T> the type of target model kind
 * @since 0.2.0
 */
public class AstModelDefinition<T extends AstTerm<T>> extends AbstractAstNode {

    private final Region region;

    /**
     * The kind of the defining model.
     */
    public final ModelDefinitionKind kind;

    /**
     * The description of the defining model, or {@code null}.
     */
    public final AstDescription description;

    /**
     * The attributes of the defining model.
     */
    public final List<AstAttribute> attributes;

    /**
     * The name of the defining model.
     */
    public final AstSimpleName name;

    /**
     * The model structure of this definition.
     */
    public final AstExpression<T> expression;

    /**
     * Creates and returns a new instance.
     * @param region the region of this node on the enclosing script, or {@code null} if unknown
     * @param kind the kind of this definition
     * @param description the description of this model, or {@code null} if is omitted
     * @param attributes the attributes of this model
     * @param name the name
     * @param expression the model structure of this definition
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AstModelDefinition(
            Region region,
            ModelDefinitionKind kind,
            AstDescription description,
            List<AstAttribute> attributes,
            AstSimpleName name,
            AstExpression<T> expression) {
        if (kind == null) {
            throw new IllegalArgumentException("kind must not be null"); //$NON-NLS-1$
        }
        if (attributes == null) {
            throw new IllegalArgumentException("attributes must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (expression == null) {
            throw new IllegalArgumentException("expression must not be null"); //$NON-NLS-1$
        }
        this.region = region;
        this.kind = kind;
        this.description = description;
        this.attributes = attributes;
        this.name = name;
        this.expression = expression;
    }

    /**
     * Safely casts this definition as a record model definition.
     * @return this
     * @throws IllegalStateException if this is not a record model definition
     */
    public AstModelDefinition<AstRecord> asRecord() {
        return cast(ModelDefinitionKind.RECORD);
    }

    /**
     * Safely casts this definition as a projective model definition.
     * @return this
     * @throws IllegalStateException if this is not a projective model definition
     */
    public AstModelDefinition<AstRecord> asProjective() {
        return cast(ModelDefinitionKind.PROJECTIVE);
    }

    /**
     * Safely casts this definition as a joined model definition.
     * @return this
     * @throws IllegalStateException if this is not a joined model definition
     */
    public AstModelDefinition<AstJoin> asJoined() {
        return cast(ModelDefinitionKind.JOINED);
    }

    /**
     * Safely casts this definition as a summarized model definition.
     * @return this
     * @throws IllegalStateException if this is not a summarized model definition
     */
    public AstModelDefinition<AstSummarize> asSummarized() {
        return cast(ModelDefinitionKind.SUMMARIZED);
    }

    private <U extends AstTerm<U>> AstModelDefinition<U> cast(ModelDefinitionKind target) {
        assert target != null;
        if (kind == target) {
            @SuppressWarnings("unchecked")
            AstModelDefinition<U> cast = (AstModelDefinition<U>) this;
            return cast;
        }
        throw new IllegalStateException(MessageFormat.format(
                "Failed to cast {0} into {1} ({2})", //$NON-NLS-1$
                name,
                target,
                kind));
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
        R result = visitor.visitModelDefinition(context, this);
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + kind.hashCode();
        result = prime * result + name.hashCode();
        result = prime * result + attributes.hashCode();
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + expression.hashCode();
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
        AstModelDefinition<?> other = (AstModelDefinition<?>) obj;
        if (kind != other.kind) {
            return false;
        }
        if (!name.equals(other.name)) {
            return false;
        }
        if (!attributes.equals(other.attributes)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (!expression.equals(other.expression)) {
            return false;
        }
        return true;
    }
}
