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

import java.text.MessageFormat;

/**
 * 10進数の型。
 */
public class DecimalType implements PropertyType {

    private int precision;

    private int scale;

    /**
     * インスタンスを生成する。
     * @param precision 10進数の合計桁数
     * @param scale 小数点以下の桁数
     */
    public DecimalType(int precision, int scale) {
        this.precision = precision;
        this.scale = scale;
    }

    /**
     * 合計の桁数を返す。
     * @return 合計の桁数
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * 小数点以下の桁数を返す。
     * @return 小数点以下の桁数
     */
    public int getScale() {
        return scale;
    }

    @Override
    public PropertyTypeKind getKind() {
        return PropertyTypeKind.BIG_DECIMAL;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + precision;
        result = prime * result + scale;
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
        DecimalType other = (DecimalType) obj;
        if (precision != other.precision) {
            return false;
        }
        if (scale != other.scale) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format("DECIMAL({0}, {1})",
                String.valueOf(precision),
                String.valueOf(scale));
    }
}
