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
package com.asakusafw.modelgen.model;

import java.text.MessageFormat;

/**
 * 文字列の型。
 */
public class StringType implements PropertyType {

    private int capacity;

    /**
     * インスタンスを生成する。
     * @param capacity 許容する最大文字数
     */
    public StringType(int capacity) {
        this.capacity = capacity;
    }

    /**
     * 許容する最大文字数を返す。
     * @return 許容する最大文字数
     */
    public int getCapacity() {
        return capacity;
    }

    @Override
    public PropertyTypeKind getKind() {
        return PropertyTypeKind.STRING;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + capacity;
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
        StringType other = (StringType) obj;
        if (capacity != other.capacity) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format("STRING({0})", String.valueOf(capacity));
    }
}
