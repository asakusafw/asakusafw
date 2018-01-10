/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a kind of basic type.
 * This consists of primitive types and {@code void}.
 */
public enum BasicTypeKind {

    /**
     * The void type.
     */
    VOID('V', void.class),

    /**
     * The int type.
     */
    INT('I', int.class),

    /**
     * The long type.
     */
    LONG('J', long.class),

    /**
     * The float type.
     */
    FLOAT('F', float.class),

    /**
     * The double type.
     */
    DOUBLE('D', double.class),

    /**
     * The short type.
     */
    SHORT('S', short.class),

    /**
     * The char type.
     */
    CHAR('C', char.class),

    /**
     * The byte type.
     */
    BYTE('B', byte.class),

    /**
     * The boolean type.
     */
    BOOLEAN('Z', boolean.class),
    ;

    private final char descriptor;

    private Class<?> javaRepresentation;

    /**
     * Creates a new instance.
     * @param descriptor the type descriptor
     * @param klass the related Java reflection type
     */
    BasicTypeKind(char descriptor, Class<?> klass) {
        assert klass != null;
        assert klass.isPrimitive();
        this.descriptor = descriptor;
        this.javaRepresentation = klass;
    }

    /**
     * Returns the type which is represented in the specified descriptor.
     * @param descriptor the descriptor
     * @return the corresponded type kind, or {@code null} if there is no such the type kind
     */
    public static BasicTypeKind descriptorOf(char descriptor) {
        return DescriptorToBasicTypeKind.get(descriptor);
    }

    /**
     * Returns the type descriptor.
     * @return the type descriptor
     */
    public char getDescriptor() {
        return descriptor;
    }

    /**
     * Returns the reflection type ({@link java.lang.Class}) of this kind.
     * @return the related reflection type
     */
    public Class<?> getJavaRepresentation() {
        return javaRepresentation;
    }

    /**
     * Returns the type kind about the target Java type.
     * @param klass the Java type
     * @return the related type kind, or {@code null} if there is no such the type kind
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static BasicTypeKind valueOf(Class<?> klass) {
        if (klass == null) {
            throw new IllegalArgumentException("klass must not be null"); //$NON-NLS-1$
        }
        return ClassToBasicTypeKind.get(klass);
    }

    /**
     * Returns the Java keyword of this type.
     * @return the Java keyword
     */
    public String getKeyword() {
        return name().toLowerCase();
    }

    /**
     * Returns the type kind about the Java keyword.
     * @param keyword the Java keyword
     * @return the related type kind, or {@code null} if there is no such the type kind
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static BasicTypeKind keywordOf(String keyword) {
        if (keyword == null) {
            throw new IllegalArgumentException("keyword must not be null"); //$NON-NLS-1$
        }
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
                map.put(elem.getJavaRepresentation(), elem);
            }
            REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
        }

        static BasicTypeKind get(Class<?> key) {
            return REVERSE_DICTIONARY.get(key);
        }
    }

    private static class DescriptorToBasicTypeKind {
        private static final Map<Character, BasicTypeKind> REVERSE_DICTIONARY;
        static {
            Map<Character, BasicTypeKind> map = new HashMap<>();
            for (BasicTypeKind elem : BasicTypeKind.values()) {
                map.put(elem.getDescriptor(), elem);
            }
            REVERSE_DICTIONARY = Collections.unmodifiableMap(map);
        }

        static BasicTypeKind get(char key) {
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
