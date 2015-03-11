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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 基本型の種類。
 * <p>
 * 基本型はプリミティブ型と{@code void}型からなる。
 * </p>
 */
public enum BasicTypeKind {

    /**
     * {@code void}型。
     */
    VOID('V', void.class),

    /**
     * {@code int}型。
     */
    INT('I', int.class),

    /**
     * {@code long}型。
     */
    LONG('J', long.class),

    /**
     * {@code float}型。
     */
    FLOAT('F', float.class),

    /**
     * {@code double}型。
     */
    DOUBLE('D', double.class),

    /**
     * {@code short}型。
     */
    SHORT('S', short.class),

    /**
     * {@code char}型。
     */
    CHAR('C', char.class),

    /**
     * {@code byte}型。
     */
    BYTE('B', byte.class),

    /**
     * {@code boolean}型。
     */
    BOOLEAN('Z', boolean.class),
    ;

    private final char descriptor;

    private Class<?> javaRepresentation;

    /**
     * インスタンスを生成する。
     * @param descriptor デスクリプタ
     * @param klass Javaでの表現
     */
    private BasicTypeKind(char descriptor, Class<?> klass) {
        assert klass != null;
        assert klass.isPrimitive();
        this.descriptor = descriptor;
        this.javaRepresentation = klass;
    }

    /**
     * 指定のデスクリプタで表現されるこの型の要素を返す。
     * @param descriptor 対象のデスクリプタ文字
     * @return 対応する要素、存在しない場合は{@code null}
     */
    public static BasicTypeKind descriptorOf(char descriptor) {
        return DescriptorToBasicTypeKind.get(descriptor);
    }

    /**
     * この基本型の種類を表現するデスクリプタを返す。
     * @return この基本型の種類を表現するデスクリプタ
     */
    public char getDescriptor() {
        return descriptor;
    }

    /**
     * この基本型のJavaでの表現({@link java.lang.Class})を返す。
     * @return この基本型のJavaでの表現
     */
    public Class<?> getJavaRepresentation() {
        return javaRepresentation;
    }

    /**
     * Javaでの表現に対応するこの列挙型の定数を返す。
     * <p>
     * 対応する定数が存在しない場合、この呼び出しは{@code null}を返す。
     * </p>
     * @param klass Javaでの表現
     * @return 対応する定数、存在しない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static BasicTypeKind valueOf(Class<?> klass) {
        if (klass == null) {
            throw new IllegalArgumentException("klass must not be null"); //$NON-NLS-1$
        }
        return ClassToBasicTypeKind.get(klass);
    }

    /**
     * この型を表現するキーワードを返す。
     * @return この型を表現するキーワード
     */
    public String getKeyword() {
        return name().toLowerCase();
    }

    /**
     * Javaでのキーワードに対応するこの列挙型の定数を返す。
     * <p>
     * 対応する定数が存在しない場合、この呼び出しは{@code null}を返す。
     * </p>
     * @param keyword Javaでのキーワード
     * @return 対応する定数、存在しない場合は{@code null}
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
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
            Map<Class<?>, BasicTypeKind> map = new HashMap<Class<?>, BasicTypeKind>();
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
            Map<Character, BasicTypeKind> map = new HashMap<Character, BasicTypeKind>();
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
            Map<String, BasicTypeKind> map = new HashMap<String, BasicTypeKind>();
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
