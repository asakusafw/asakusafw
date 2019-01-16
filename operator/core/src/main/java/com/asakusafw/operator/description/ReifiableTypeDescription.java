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
package com.asakusafw.operator.description;

/**
 * Represents a reifiable type (can be represented in {@link Class}).
 */
public abstract class ReifiableTypeDescription implements TypeDescription, ValueDescription {

    @Override
    public final ValueKind getValueKind() {
        return ValueKind.TYPE;
    }

    @Override
    public final TypeDescription getValueType() {
        return of(Class.class);
    }

    /**
     * Returns an instance.
     * @param aClass the reflective object
     * @return the related instance
     */
    public static ReifiableTypeDescription of(Class<?> aClass) {
        if (aClass.isPrimitive()) {
            return BasicTypeDescription.of(aClass);
        } else if (aClass.isArray()) {
            return ArrayTypeDescription.of(aClass);
        } else {
            return ClassDescription.of(aClass);
        }
    }
}
