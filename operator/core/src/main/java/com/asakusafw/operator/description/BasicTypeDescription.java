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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents primitive types and void type.
 */
public class BasicTypeDescription extends ReifiableTypeDescription {

    private final BasicTypeKind basicTypeKind;

    /**
     * Creates a new instance.
     * @param basicTypeKind the type kind
     */
    public BasicTypeDescription(BasicTypeKind basicTypeKind) {
        this.basicTypeKind = Objects.requireNonNull(basicTypeKind);
    }

    /**
     * Returns an instance.
     * @param aClass the reflective object
     * @return the related instance
     */
    public static BasicTypeDescription of(Class<?> aClass) {
        if (aClass.isPrimitive() == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "must be basic type: {0}", //$NON-NLS-1$
                    aClass.getName()));
        }
        BasicTypeDescription.BasicTypeKind kind = BasicTypeDescription.BasicTypeKind.of(aClass);
        return new BasicTypeDescription(kind);
    }

    @Override
    public TypeKind getTypeKind() {
        return TypeKind.BASIC;
    }

    @Override
    public BasicTypeDescription getErasure() {
        return this;
    }

    /**
     * Returns the basic type kind.
     * @return the basic type kind
     */
    public BasicTypeKind getBasicTypeKind() {
        return basicTypeKind;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + basicTypeKind.hashCode();
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
        BasicTypeDescription other = (BasicTypeDescription) obj;
        if (basicTypeKind != other.basicTypeKind) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return basicTypeKind.getKeyword();
    }

    /**
     * Represents a kind of {@link BasicTypeDescription}.
     */
    public enum BasicTypeKind {

        /**
         * {@code void} type.
         */
        VOID('V', void.class, Void.class),

        /**
         * {@code int} type.
         */
        INT('I', int.class, Integer.class),

        /**
         * {@code long} type.
         */
        LONG('J', long.class, Long.class),

        /**
         * {@code float} type.
         */
        FLOAT('F', float.class, Float.class),

        /**
         * {@code double} type.
         */
        DOUBLE('D', double.class, Double.class),

        /**
         * {@code short} type.
         */
        SHORT('S', short.class, Short.class),

        /**
         * {@code char} type.
         */
        CHAR('C', char.class, Character.class),

        /**
         * {@code byte} type.
         */
        BYTE('B', byte.class, Byte.class),

        /**
         * {@code boolean} type.
         */
        BOOLEAN('Z', boolean.class, Boolean.class),
        ;

        private Class<?> reflectiveObject;

        private Class<?> wrapperType;

        BasicTypeKind(char descriptor, Class<?> reflectiveObject, Class<?> wrapperType) {
            assert reflectiveObject != null;
            assert reflectiveObject.isPrimitive();
            this.reflectiveObject = reflectiveObject;
            this.wrapperType = wrapperType;
        }

        /**
         * Returns the reflective object.
         * @return the reflective object
         */
        public Class<?> getReflectiveObject() {
            return reflectiveObject;
        }

        /**
         * Returns the corresponded wrapper type.
         * @return the wrapper type
         */
        public Class<?> getWrapperType() {
            return wrapperType;
        }

        /**
         * Returns the constant from its reflective object.
         * @param aClass the reflective object
         * @return the constant, or {@code null} if it is not defined
         */
        public static BasicTypeKind of(Class<?> aClass) {
            return ClassToBasicTypeKind.get(aClass);
        }

        /**
         * Returns the keyword of this basic type.
         * @return the keyword
         */
        public String getKeyword() {
            return name().toLowerCase();
        }

        /**
         * Returns the constant from its keyword.
         * @param keyword the basic type keyword
         * @return the constant, or {@code null} if it is not defined
         */
        public static BasicTypeKind of(String keyword) {
            return KeywordToBasicTypeKind.get(keyword);
        }

        @Override
        public String toString() {
            return getKeyword();
        }

        private static class ClassToBasicTypeKind {

            private static final Map<Class<?>, BasicTypeKind> REVERSE_DICTIONARY;
            static {
                Map<Class<?>, BasicTypeKind> map = new HashMap<>();
                for (BasicTypeKind elem : BasicTypeKind.values()) {
                    map.put(elem.getReflectiveObject(), elem);
                }
                REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
            }

            static BasicTypeKind get(Class<?> key) {
                return REVERSE_DICTIONARY.get(key);
            }
        }

        private static class KeywordToBasicTypeKind {
            private static final Map<String, BasicTypeKind> REVERSE_DICTIONARY;
            static {
                Map<String, BasicTypeKind> map = new HashMap<>();
                for (BasicTypeKind elem : BasicTypeKind.values()) {
                    map.put(elem.getKeyword(), elem);
                }
                REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
            }

            static BasicTypeKind get(String key) {
                return REVERSE_DICTIONARY.get(key);
            }
        }
    }
}
