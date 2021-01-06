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
package com.asakusafw.info.operator;

import java.util.Objects;

import com.asakusafw.info.value.ClassInfo;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Details of a core operator.
 * @since 0.9.2
 */
public final class CoreOperatorSpec implements OperatorSpec {

    static final String KIND = "core";

    @JsonProperty(Constants.ID_CATEGORY)
    private final CoreOperatorKind category;

    private CoreOperatorSpec(CoreOperatorKind annotation) {
        this.category = annotation;
    }

    /**
     * Returns an instance.
     * @param category the category
     * @return the instance
     */
    @JsonCreator
    public static CoreOperatorSpec of(
            @JsonProperty(Constants.ID_CATEGORY) CoreOperatorKind category) {
        return new CoreOperatorSpec(category);
    }

    @Override
    public OperatorKind getOperatorKind() {
        return OperatorKind.CORE;
    }

    /**
     * Returns the core operator kind.
     * @return the kind
     */
    public CoreOperatorKind getCategory() {
        return category;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = Objects.hashCode(getOperatorKind());
        result = prime * result + Objects.hashCode(category);
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
        CoreOperatorSpec other = (CoreOperatorSpec) obj;
        return Objects.equals(category, other.category);
    }

    @Override
    public String toString() {
        return String.format("Core(%s)", category);
    }

    /**
     * Represents a kind of core operator.
     */
    public enum CoreOperatorKind {

        /**
         * Checkpoint operators.
         */
        CHECKPOINT("Checkpoint"), //$NON-NLS-1$

        /**
         * Project operators.
         */
        PROJECT("Project"), //$NON-NLS-1$

        /**
         * Extend operators.
         */
        EXTEND("Extend"), //$NON-NLS-1$

        /**
         * Restructure operators.
         */
        RESTRUCTURE("Restructure"), //$NON-NLS-1$
        ;

        private static final String PREFIX_ANNOTATION_TYPE = "com.asakusafw.vocabulary.operator."; //$NON-NLS-1$

        private final ClassInfo annotationType;

        CoreOperatorKind(String simpleName) {
            this(ClassInfo.of(PREFIX_ANNOTATION_TYPE + simpleName));
        }

        CoreOperatorKind(ClassInfo annotationType) {
            this.annotationType = annotationType;
        }

        /**
         * Returns the (pseudo) annotation type for this.
         * @return the annotation type
         */
        public ClassInfo getAnnotationType() {
            return annotationType;
        }

        @Override
        public String toString() {
            return String.format("@%s", annotationType.getSimpleName());
        }
    }
}
