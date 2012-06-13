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
package com.asakusafw.utils.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * リストに関するユーティリティ群。
 */
public final class Lists {

    /**
     * 空のリストを生成して返す。
     * <p>
     * 返されるリストは、続けて編集することができる。
     * </p>
     * @param <E> 要素の型
     * @return 生成したリスト
     */
    public static <E> List<E> create() {
        return new ArrayList<E>();
    }

    /**
     * 指定の要素からなるリストを生成して返す。
     * <p>
     * 返されるリストは、続けて編集することができる。
     * </p>
     * @param <E> 要素の型
     * @param elem 要素
     * @return 生成したリスト
     */
    public static <E> List<E> create(E elem) {
        ArrayList<E> result = new ArrayList<E>();
        result.add(elem);
        return result;
    }

    /**
     * 指定の要素からなるリストを生成して返す。
     * <p>
     * 返されるリストは、続けて編集することができる。
     * </p>
     * @param <E> 要素の型
     * @param elem1 第1要素
     * @param elem2 第2要素
     * @return 生成したリスト
     */
    public static <E> List<E> create(E elem1, E elem2) {
        ArrayList<E> result = new ArrayList<E>();
        result.add(elem1);
        result.add(elem2);
        return result;
    }

    /**
     * 指定の要素からなるリストを生成して返す。
     * <p>
     * 返されるリストは、続けて編集することができる。
     * </p>
     * @param <E> 要素の型
     * @param elem1 第1要素
     * @param elem2 第2要素
     * @param elem3 第3要素
     * @return 生成したリスト
     */
    public static <E> List<E> create(E elem1, E elem2, E elem3) {
        ArrayList<E> result = new ArrayList<E>();
        result.add(elem1);
        result.add(elem2);
        result.add(elem3);
        return result;
    }

    /**
     * 指定の要素からなるリストを生成して返す。
     * <p>
     * 返されるリストは、続けて編集することができる。
     * </p>
     * @param <E> 要素の型
     * @param elem1 第1要素
     * @param elem2 第2要素
     * @param elem3 第3要素
     * @param elem4 第4要素
     * @param rest 第5要素以降
     * @return 生成したリスト
     * @throws IllegalArgumentException 引数{@code rest}に{@code null}が指定された場合
     */
    public static <E> List<E> create(E elem1, E elem2, E elem3, E elem4, E... rest) {
        if (rest == null) {
            throw new IllegalArgumentException("rest must not be null"); //$NON-NLS-1$
        }
        ArrayList<E> result = new ArrayList<E>();
        result.add(elem1);
        result.add(elem2);
        result.add(elem3);
        result.add(elem4);
        Collections.addAll(result, rest);
        return result;
    }

    /**
     * 指定の要素列をリストに複製して返す。
     * <p>
     * 返されるリストは、続けて編集することができる。
     * </p>
     * @param <E> 要素の型
     * @param iterable 複製対象の要素列
     * @return 複製した結果のリスト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static <E> List<E> copy(Iterable<? extends E> iterable) {
        if (iterable == null) {
            throw new IllegalArgumentException("iterable must not be null"); //$NON-NLS-1$
        }
        ArrayList<E> copy = new ArrayList<E>();
        for (E element : iterable) {
            copy.add(element);
        }
        return copy;
    }

    /**
     * 指定の要素列をリストに複製して返す。
     * <p>
     * 返されるリストは、続けて編集することができる。
     * </p>
     * @param <E> 要素の型
     * @param array 複製対象の要素列
     * @return 複製した結果のリスト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static <E> List<E> copy(E[] array) {
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        ArrayList<E> copy = new ArrayList<E>();
        Collections.addAll(copy, array);
        return copy;
    }

    /**
     * 指定の要素列をリストに複製し、変更不可能にして返す。
     * <p>
     * 返されるリストは変更できない。
     * </p>
     * @param <E> 要素の型
     * @param iterable 複製対象の要素列
     * @return 複製した結果のリスト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static <E> List<E> freeze(Iterable<? extends E> iterable) {
        if (iterable == null) {
            throw new IllegalArgumentException("iterable must not be null"); //$NON-NLS-1$
        }
        Iterator<? extends E> iter = iterable.iterator();
        if (iter.hasNext() == false) {
            return Collections.emptyList();
        }
        E first = iter.next();
        if (iter.hasNext() == false) {
            return Collections.singletonList(first);
        }
        ArrayList<E> copy = new ArrayList<E>();
        copy.add(first);
        for (E element : iterable) {
            copy.add(element);
        }
        copy.trimToSize();
        return Collections.unmodifiableList(copy);
    }

    /**
     * 指定の要素列をリストに複製し、変更不可能にして返す。
     * <p>
     * 返されるリストは変更できない。
     * </p>
     * @param <E> 要素の型
     * @param array 複製対象の要素列
     * @return 複製した結果のリスト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static <E> List<E> freeze(E[] array) {
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        E[] copy = array.clone();
        return Collections.unmodifiableList(Arrays.asList(copy));
    }

    private Lists() {
        throw new AssertionError();
    }
}
