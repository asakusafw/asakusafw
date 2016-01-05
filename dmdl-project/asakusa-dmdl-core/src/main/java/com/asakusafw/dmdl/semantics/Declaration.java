/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.dmdl.semantics;

import java.util.List;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstDescription;
import com.asakusafw.dmdl.model.AstSimpleName;

/**
 * Super interface of the all declarations.
 */
public interface Declaration extends Element {

    /**
     * Returns the name of this declared element.
     * @return the name
     */
    AstSimpleName getName();

    /**
     * Returns the description of this element.
     * @return the description of this element, or {@code null} not defind
     */
    AstDescription getDescription();

    /**
     * Returns the attached attributes of this element.
     * <p>
     * If not attributes are attached, this returns an empty list.
     * </p>
     * @return the attached attributes
     */
    List<AstAttribute> getAttributes();

    /**
     * Returns the specified trait of this declaration.
     * @param <T> type of trait
     * @param kind the kind of trait
     * @return the trait, or {@code null} if not put
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    <T extends Trait<T>> T getTrait(Class<T> kind);

    /**
     * Puts a trait into this declaration.
     * @param <T> type of trait
     * @param kind the kind of trait
     * @param trait the traits to put, or {@code null} to remove
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    <T extends Trait<T>> void putTrait(Class<T> kind, T trait);
}
