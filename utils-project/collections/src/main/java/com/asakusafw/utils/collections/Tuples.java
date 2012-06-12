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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 要素の組に関するユーティリティ群。
 * @see Tuple2
 */
public class Tuples {

    /**
     * 二要素からなる組を構築して返す。
     * @param <T1> 第一要素の型
     * @param <T2> 第二要素の型
     * @param _1 第一要素
     * @param _2 第二要素
     * @return 構築した組
     */
    public static <T1, T2> Tuple2<T1, T2> of(T1 _1, T2 _2) {
        return new Tuple2<T1, T2>(_1, _2);
    }

    /**
     * 2つのリストの先頭から順に値を取り出し、それらの組のリストを構築して返す。
     * <p>
     * リストの長さが異なる場合、短いほうのリストの長さを元に組のリストを構築する。
     * </p>
     * @param <T1> 第一要素の型
     * @param <T2> 第二要素の型
     * @param _1 それぞれの第一要素の値
     * @param _2 それぞれの第二要素の値
     * @return 構築した組のリスト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static <T1, T2> List<Tuple2<T1, T2>> zip(
            List<? extends T1> _1,
            List<? extends T2> _2) {
        if (_1 == null) {
            throw new IllegalArgumentException("_1 must not be null"); //$NON-NLS-1$
        }
        if (_2 == null) {
            throw new IllegalArgumentException("_2 must not be null"); //$NON-NLS-1$
        }
        ArrayList<Tuple2<T1, T2>> zipped = new ArrayList<Tuple2<T1,T2>>();
        Iterator<? extends T1> iter1 = _1.iterator();
        Iterator<? extends T2> iter2 = _2.iterator();
        while (iter1.hasNext() && iter2.hasNext()) {
            T1 elem1 = iter1.next();
            T2 elem2 = iter2.next();
            zipped.add(of(elem1, elem2));
        }
        return zipped;
    }

    /**
     * 組の列を展開し、2つのリストに変換して返す。
     * @param <T1> 第一要素の型
     * @param <T2> 第二要素の型
     * @param zipped 組の列
     * @return 組のそれぞれの要素からなるリストの組
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static <T1, T2> Tuple2<List<T1>, List<T2>> unzip(
            Iterable<? extends Tuple2<? extends T1, ? extends T2>> zipped) {
        if (zipped == null) {
            throw new IllegalArgumentException("zipped must not be null"); //$NON-NLS-1$
        }
        List<T1> unzipped1 = new ArrayList<T1>();
        List<T2> unzipped2 = new ArrayList<T2>();
        for (Tuple2<? extends T1, ? extends T2> tuple : zipped) {
            if (tuple == null) {
                throw new IllegalArgumentException("zipped must not contain null"); //$NON-NLS-1$
            }
            unzipped1.add(tuple._1);
            unzipped2.add(tuple._2);
        }
        return of(unzipped1, unzipped2);
    }

    /**
     * 二要素からなる組を構築して返す。
     * @param <T1> 第一要素の型
     * @param <T2> 第二要素の型
     * @param map キーと値のペア集合
     * @return 構築した組
     */
    public static <T1, T2> List<Tuple2<T1, T2>> valueOf(
            Map<? extends T1, ? extends T2> map) {
        if (map == null) {
            throw new IllegalArgumentException("map must not be null"); //$NON-NLS-1$
        }
        return valueOf(map.entrySet());
    }

    /**
     * 二要素からなる組を構築して返す。
     * @param <T1> 第一要素の型
     * @param <T2> 第二要素の型
     * @param entries キーと値のペア集合
     * @return 構築した組
     */
    public static <T1, T2> List<Tuple2<T1, T2>> valueOf(
            Iterable<? extends Map.Entry<? extends T1, ? extends T2>> entries) {
        if (entries == null) {
            throw new IllegalArgumentException("iterables must not be null"); //$NON-NLS-1$
        }
        List<Tuple2<T1, T2>> result = new ArrayList<Tuple2<T1,T2>>();
        for (Map.Entry<? extends T1, ? extends T2> entry : entries) {
            result.add(valueOf(entry));
        }
        return result;
    }

    /**
     * キーと値の二要素からなる組を構築して返す。
     * @param <T1> 第一要素の型
     * @param <T2> 第二要素の型
     * @param entry キーと値を持つ組
     * @return 構築した組
     */
    public static <T1, T2> Tuple2<T1, T2> valueOf(
            Map.Entry<? extends T1, ? extends T2> entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry must not be null"); //$NON-NLS-1$
        }
        return new Tuple2<T1, T2>(entry.getKey(), entry.getValue());
    }

    /**
     * 二要素からなる組に対する比較器を生成して返す。
     * <p>
     * 返される比較器は、第一要素、第二要素の順に辞書式の比較を行う。
     * また、それぞれの要素の比較には、引数に指定した比較器を利用する。
     * </p>
     * <p>
     * ただし、それぞれの要素に{@code null}を含む場合、
     * {@code null}は他のどの値よりも小さな値として処理される。
     * また、{@code null}は他の{@code null}に等しい。
     * </p>
     * @param <T1> 第一要素の型
     * @param <T2> 第二要素の型
     * @param firstComparator 第一要素の比較器
     * @param secondComparator 第二要素の比較器
     * @return 生成した比較器
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static <T1, T2> Comparator<Tuple2<T1, T2>> comparator(
            Comparator<? super T1> firstComparator,
            Comparator<? super T2> secondComparator) {
        if (firstComparator == null) {
            throw new IllegalArgumentException("firstComparator must not be null"); //$NON-NLS-1$
        }
        if (secondComparator == null) {
            throw new IllegalArgumentException("secondComparator must not be null"); //$NON-NLS-1$
        }
        return new Tuple2Comparator<T1, T2>(firstComparator, secondComparator);
    }

    /**
     * インスタンス生成の禁止。
     */
    private Tuples() {
        throw new AssertionError();
    }

    private static final class Tuple2Comparator<T1, T2> implements Comparator<Tuple2<T1, T2>> {

        private final Comparator<? super T1> _1;

        private final Comparator<? super T2> _2;

        Tuple2Comparator(Comparator<? super T1> _1, Comparator<? super T2> _2) {
            assert _1 != null;
            assert _2 != null;
            this._1 = _1;
            this._2 = _2;
        }

        @Override
        public int compare(Tuple2<T1, T2> o1, Tuple2<T1, T2> o2) {
            int diff1 = Tuples.compare(_1, o1._1, o2._1);
            if (diff1 != 0) {
                return diff1;
            }
            return Tuples.compare(_2, o1._2, o2._2);
        }
    }

    static <T> int compare(Comparator<? super T> comparator, T a, T b) {
        assert comparator != null;
        if (a == null) {
            if (b == null) {
                return 0;
            }
            return -1;
        }
        else if (b == null) {
            return +1;
        }
        return comparator.compare(a, b);
    }
}
