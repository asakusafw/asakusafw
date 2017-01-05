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
package com.asakusafw.utils.java.model.syntax;

/**
 * A root interface of all Java DOM objects.
 */
public interface Model {

    /**
     * Returns the model kind.
     * @return the model kind
     */
    ModelKind getModelKind();

    /**
     * Accepts the {@link Visitor} and processes this object in the visitor.
     * @param <C> type of visitor context
     * @param <R> type of visitor result
     * @param <E> type of visitor exception
     * @param visitor the target visitor
     * @param context the current context (nullable)
     * @return the processing result
     * @throws E if error occurred while processing this object in the visitor
     * @throws IllegalArgumentException if {@code visitor} is {@code null}
     */
    <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context)
        throws E;

    /**
     * Returns the hash code of this model object.
     * Note that, any {@link #findModelTrait(Class) traits} does not have any effects to the hash code.
     * @return the hash code
     */
    @Override
    int hashCode();

    /**
     * Returns whether this object is equivalent to the specified object or not.
     * Note that, any {@link #findModelTrait(Class) traits} does not have any effects to the equivalence.
     * @param other the target object
     * @return {@code true} if this object is equivalent to the specified object, otherwise {@code false}
     */
    @Override
    boolean equals(Object other);

    /**
     * Returns a model trait for the specified type.
     * @param <T> the trait type
     * @param traitClass the trait type
     * @return the corresponded trait, or {@code null} if there is no such a trait
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    <T> T findModelTrait(Class<T> traitClass);

    /**
     * Puts a model trait.
     * @param <T> the trait type
     * @param traitClass the trait type
     * @param traitObject the trait object, or {@code null} to remove the target trait
     * @throws IllegalArgumentException if {@code traitClass} is {@code null}
     */
    <T> void putModelTrait(Class<T> traitClass, T traitObject);
}
