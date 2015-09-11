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
package com.asakusafw.utils.java.model.syntax;

/**
 * Represents a kind of {@code import} declarations.
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:7.5] Import Declarations} </li>
 *   </ul> </li>
 * </ul>
 */
public enum ImportKind {

    /**
     * Single type import.
     * <ul>
     *   <li> Specified In: <ul>
     *     <li> {@code [JLS3:7.5.1] Single-Type-Import Declaration} </li>
     *   </ul> </li>
     * </ul>
     */
    SINGLE_TYPE(Target.TYPE, Range.SINGLE),

    /**
     * On-demand type import.
     * <ul>
     *   <li> Specified In: <ul>
     *     <li> {@code [JLS3:7.5.2] Type-Import-on-Demand Declaration} </li>
     *   </ul> </li>
     * </ul>
     */
    TYPE_ON_DEMAND(Target.TYPE, Range.ON_DEMAND),

    /**
     * Single static import.
     * <ul>
     *   <li> Specified In: <ul>
     *     <li> {@code [JLS3:7.5.3] Single Static Import Declaration} </li>
     *   </ul> </li>
     * </ul>
     */
    SINGLE_STATIC(Target.MEMBER, Range.SINGLE),

    /**
     * On-demand static import.
     * <ul>
     *   <li> Specified In: <ul>
     *     <li> {@code [JLS3:7.5.4] Static-Import-on-Demand Declaration} </li>
     *   </ul> </li>
     * </ul>
     */
    STATIC_ON_DEMAND(Target.MEMBER, Range.ON_DEMAND),
    ;

    private Target target;

    private Range range;

    private ImportKind(Target target, Range range) {
        assert target != null;
        assert range != null;
        this.target = target;
        this.range = range;
    }

    /**
     * Returns the import target kind.
     * @return the import target kind
     */
    public Target getTarget() {
        return target;
    }

    /**
     * Returns the import scope kind.
     * @return the import scope kind
     */
    public Range getRange() {
        return range;
    }

    /**
     * Returns the import kind from its target and scope kinds.
     * @param target the target kind
     * @param range the scope kind
     * @return the corresponding import kind
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static ImportKind valueOf(Target target, Range range) {
        if (target == null) {
            throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
        }
        if (range == null) {
            throw new IllegalArgumentException("range must not be null"); //$NON-NLS-1$
        }
        if (target == Target.TYPE) {
            if (range == Range.SINGLE) {
                return SINGLE_TYPE;
            } else {
                return TYPE_ON_DEMAND;
            }
        } else {
            if (range == Range.SINGLE) {
                return SINGLE_STATIC;
            } else {
                return STATIC_ON_DEMAND;
            }
        }
    }

    /**
     * Represents a kind of import target.
     */
    public enum Target {

        /**
         * Import types.
         */
        TYPE,

        /**
         * Import members.
         */
        MEMBER,
    }

    /**
     * Represents a kind of import scope.
     */
    public enum Range {

        /**
         * Single import.
         */
        SINGLE,

        /**
         * On-demand import ({@code import ...*}).
         */
        ON_DEMAND,
    }
}
