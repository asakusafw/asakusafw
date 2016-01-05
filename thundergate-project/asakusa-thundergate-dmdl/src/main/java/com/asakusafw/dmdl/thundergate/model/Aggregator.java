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
package com.asakusafw.dmdl.thundergate.model;

/**
 * 集約関数。
 */
public enum Aggregator {

    /**
     * 集約なし。
     */
    IDENT {
        @Override
        public PropertyType inferType(PropertyType original) {
            return original;
        }

    },

    /**
     * 合計。
     */
    SUM {
        @Override
        public PropertyType inferType(PropertyType original) {
            switch (original.getKind()) {
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
                return new BasicType(PropertyTypeKind.LONG);
            case BIG_DECIMAL:
                return original;
            default:
                return null; // error
            }
        }

    },

    /**
     * カウント。
     */
    COUNT {
        @Override
        public PropertyType inferType(PropertyType original) {
            return new BasicType(PropertyTypeKind.LONG);
        }
    },

    /**
     * 最大値。
     */
    MAX {
        @Override
        public PropertyType inferType(PropertyType original) {
            switch (original.getKind()) {
            case INT:
            case LONG:
            case BIG_DECIMAL:
            case DATE:
            case DATETIME:
                return original;
            default:
                return null; // error
            }
        }
    },

    /**
     * 最小値。
     */
    MIN {
        @Override
        public PropertyType inferType(PropertyType original) {
            return MAX.inferType(original);
        }
    },
    ;

    /**
     * この集約関数を適用した結果のプロパティの型を返す。
     * @param original 元のプロパティの型
     * @return この集約関数を適用した結果のプロパティの型
     */
    public abstract PropertyType inferType(PropertyType original);
}
