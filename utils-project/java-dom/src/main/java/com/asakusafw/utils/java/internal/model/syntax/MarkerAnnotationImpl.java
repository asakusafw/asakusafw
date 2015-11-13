/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.utils.java.internal.model.syntax;

import com.asakusafw.utils.java.model.syntax.MarkerAnnotation;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link MarkerAnnotation}.
 */
public final class MarkerAnnotationImpl extends ModelRoot implements MarkerAnnotation {

    private NamedType type;

    @Override
    public NamedType getType() {
        return this.type;
    }

    /**
     * Sets the annotation type.
     * @param type the annotation type
     * @throws IllegalArgumentException if {@code type} was {@code null}
     */
    public void setType(NamedType type) {
        Util.notNull(type, "type"); //$NON-NLS-1$
        this.type = type;
    }

    /**
     * Returns {@link ModelKind#MARKER_ANNOTATION} which represents this element kind.
     * @return {@link ModelKind#MARKER_ANNOTATION}
     */
    @Override
    public ModelKind getModelKind() {
        return ModelKind.MARKER_ANNOTATION;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitMarkerAnnotation(this, context);
    }
}
