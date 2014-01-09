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
package com.asakusafw.utils.java.model.syntax;

/**
 * {@code import}宣言の種類。
 * <ul>
 *   <li> Specified In: <ul>
 *     <li> {@code [JLS3:7.5] Import Declarations} </li>
 *   </ul> </li>
 * </ul>
 */
public enum ImportKind {

    /**
     * 単一の型インポート宣言。
     * <ul>
     *   <li> Specified In: <ul>
     *     <li> {@code [JLS3:7.5.1] Single-Type-Import Declaration} </li>
     *   </ul> </li>
     * </ul>
     */
    SINGLE_TYPE(Target.TYPE, Range.SINGLE),

    /**
     * オンデマンドの型インポート宣言。
     * <ul>
     *   <li> Specified In: <ul>
     *     <li> {@code [JLS3:7.5.2] Type-Import-on-Demand Declaration} </li>
     *   </ul> </li>
     * </ul>
     */
    TYPE_ON_DEMAND(Target.TYPE, Range.ON_DEMAND),

    /**
     * 単一の{@code static}インポート宣言。
     * <ul>
     *   <li> Specified In: <ul>
     *     <li> {@code [JLS3:7.5.3] Single Static Import Declaration} </li>
     *   </ul> </li>
     * </ul>
     */
    SINGLE_STATIC(Target.MEMBER, Range.SINGLE),

    /**
     * オンデマンドの{@code static}インポート宣言。
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
     * インポート対象の種類を返す。
     * @return インポート対象の種類
     */
    public Target getTarget() {
        return target;
    }

    /**
     * インポートする範囲を返す。
     * @return インポートする範囲
     */
    public Range getRange() {
        return range;
    }

    /**
     * 指定の対象と範囲を表現するインポート宣言の種類を返す。
     * @param target 対象
     * @param range 範囲
     * @return 対応する宣言
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
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
     * インポート対象の種類。
     */
    public enum Target {

        /**
         * 型のインポート。
         */
        TYPE,

        /**
         * メンバのインポート。
         */
        MEMBER,
    }

    /**
     * インポートする範囲。
     */
    public enum Range {

        /**
         * 単一。
         */
        SINGLE,

        /**
         * オンデマンド。
         */
        ON_DEMAND,
    }
}
