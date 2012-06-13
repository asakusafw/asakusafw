/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.utils.java.internal.model.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.text.MessageFormat;

/**
 * {@link java.lang.reflect.Type}の種類ごとにメソッドを再束縛する。
 * @param <R> 再束縛される{@code visit*}メソッドの実行結果型
 * @param <C> 再束縛される{@code visit*}メソッドのコンテキストオブジェクト型
 * @param <E> 再束縛される{@code visit*}メソッドの例外型
 */
public abstract class ReflectionTypeVisitor<R, C, E extends Throwable> {

    /**
     * {@code type}の種類でメソッドの再束縛を行い、該当する{@code visit*}メソッドを起動する。
     * <p>
     * このメソッドが識別可能な型は次のとおりである。
     * <ul>
     *   <li>
     *     {@link Class}
     - *     {@link #visitClass(Class, Object)
     *     visitClass(type, context)}
     *   </li>
     *   <li>
     *     {@link GenericArrayType}
     - *     {@link #visitGenericArrayType(GenericArrayType, Object)
     *     visitGenericArrayType(type, context)}
     *   </li>
     *   <li>
     *     {@link ParameterizedType}
     - *     {@link #visitParameterizedType(ParameterizedType, Object)
     *     visitParameterizedType(type, context)}
     *   </li>
     *   <li>
     *     {@link TypeVariable}
     - *     {@link #visitTypeVariable(TypeVariable, Object)
     *     visitTypeVariable(type, context)}
     *   </li>
     *   <li>
     *     {@link WildcardType}
     - *     {@link #visitWildcardType(WildcardType, Object)
     *     visitWildcardType(type, context)}
     *   </li>
     * </ul>
     * 上記のいずれでもない場合、この呼び出しは失敗する。
     * また、上記のうち複数のサブタイプであるようなオブジェクトを{@code type}に指定した場合、
     * 実際に呼び出される{@code visit*}メソッドは保証されない。
     * </p>
     * @param type メソッドの再束縛をする型
     * @param context コンテキストオブジェクト(省略可)
     * @return 再束縛された{@code visit*}の実行結果
     * @throws E 再束縛された{@code visit*}の実行中に例外が発生した場合
     * @thwows IllArgumentException
     *     引数{@code type}が
     *     {@link Class},
     *     {@link GenericArrayType},
     *     {@link ParameterizedType},
     *     {@link TypeVariable},
     *     {@link WildcardType}
     *     のいずれでもない場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public final R dispatch(Type type, C context) throws E {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (type instanceof Class<?>) {
            return visitClass((Class<?>) type, context);
        } else if (type instanceof GenericArrayType) {
            return visitGenericArrayType((GenericArrayType) type, context);
        } else if (type instanceof ParameterizedType) {
            return visitParameterizedType((ParameterizedType) type, context);
        } else if (type instanceof TypeVariable<?>) {
            return visitTypeVariable((TypeVariable<?>) type, context);
        } else if (type instanceof WildcardType) {
            return visitWildcardType((WildcardType) type, context);
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                "Unknown Type Kind: {0} ({1})",
                type,
                type.getClass().getSimpleName()));
        }
    }

    /**
     * {@link #dispatch(Type, Object)}の第一引数に{@link Class}型の値が
     * 指定された際に呼び出される。
     * @param type
     *     処理対象の型オブジェクト({@link #dispatch(Type, Object)}の第一引数)
     * @param context
     *     コンテキストオブジェクト({@link #dispatch(Type, Object)}の第二引数、省略可)
     * @return 実行結果
     * @throws E 処理中に例外が発生した場合
     */
    protected R visitClass(Class<?> type, C context) throws E {
        return null;
    }

    /**
     * {@link #dispatch(Type, Object)}の第一引数に{@link GenericArrayType}型の値が
     * 指定された際に呼び出される。
     * @param type
     *     処理対象の型オブジェクト({@link #dispatch(Type, Object)}の第一引数)
     * @param context
     *     コンテキストオブジェクト({@link #dispatch(Type, Object)}の第二引数、省略可)
     * @return 実行結果
     * @throws E 処理中に例外が発生した場合
     */
    protected R visitGenericArrayType(GenericArrayType type, C context) throws E {
        return null;
    }

    /**
     * {@link #dispatch(Type, Object)}の第一引数に{@link ParameterizedType}型の値が
     * 指定された際に呼び出される。
     * @param type
     *     処理対象の型オブジェクト({@link #dispatch(Type, Object)}の第一引数)
     * @param context
     *     コンテキストオブジェクト({@link #dispatch(Type, Object)}の第二引数、省略可)
     * @return 実行結果
     * @throws E 処理中に例外が発生した場合
     */
    protected R visitParameterizedType(ParameterizedType type, C context) throws E {
        return null;
    }

    /**
     * {@link #dispatch(Type, Object)}の第一引数に{@link TypeVariable}型の値が
     * 指定された際に呼び出される。
     * @param type
     *     処理対象の型オブジェクト({@link #dispatch(Type, Object)}の第一引数)
     * @param context
     *     コンテキストオブジェクト({@link #dispatch(Type, Object)}の第二引数、省略可)
     * @return 実行結果
     * @throws E 処理中に例外が発生した場合
     */
    protected R visitTypeVariable(TypeVariable<?> type, C context) throws E {
        return null;
    }

    /**
     * {@link #dispatch(Type, Object)}の第一引数に{@link WildcardType}型の値が
     * 指定された際に呼び出される。
     * @param type
     *     処理対象の型オブジェクト({@link #dispatch(Type, Object)}の第一引数)
     * @param context
     *     コンテキストオブジェクト({@link #dispatch(Type, Object)}の第二引数、省略可)
     * @return 実行結果
     * @throws E 処理中に例外が発生した場合
     */
    protected R visitWildcardType(WildcardType type, C context) throws E {
        return null;
    }
}
