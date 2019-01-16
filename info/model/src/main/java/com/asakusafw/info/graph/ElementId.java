/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.info.graph;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonAutoDetect(
        creatorVisibility = Visibility.NONE,
        fieldVisibility = Visibility.NONE,
        getterVisibility = Visibility.NONE,
        isGetterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE
)
abstract class ElementId {

    private final int value;

    ElementId(int value) {
        this.value = value;
    }

    @JsonValue
    int getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ElementId)) {
            return false;
        }
        ElementId other = (ElementId) obj;
        return value == other.value;
    }

    @Override
    public String toString() {
        Optional<Class<?>> parent = Optional.ofNullable(getClass().getEnclosingClass());
        return String.format(
                "%s(id=%,d)",
                parent.map(Class::getSimpleName).orElse("?"),
                value);
    }
}