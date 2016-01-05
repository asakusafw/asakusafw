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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

/**
 * An abstract super interface of elements in ({@link IrDocComment Java documentation comments}).
 */
public interface IrDocElement {

    /**
     * Returns the element kind.
     * @return the element kind
     */
    IrDocElementKind getKind();

    /**
     * Returns the location where this element appears.
     * @return the location where this element appears, or {@code null} if it is not specified
     */
    IrLocation getLocation();

    /**
     * Sets the location where this element appears.
     * @param location the location, or {@code null} to unset it
     */
    void setLocation(IrLocation location);

    /**
     * Accepts and calls back the visitor.
     * @param <P> type of visitor context
     * @param <R> type of visitor result
     * @param context the visitor context
     * @param visitor the visitor to call back
     * @return call back result
     * @throws IllegalArgumentException if {@code visitor} was {@code null}
     */
    <R, P> R accept(IrDocElementVisitor<R, P> visitor, P context);
}
