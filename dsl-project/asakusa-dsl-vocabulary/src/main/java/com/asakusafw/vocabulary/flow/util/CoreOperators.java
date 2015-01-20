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
package com.asakusafw.vocabulary.flow.util;

import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Checkpoint;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Confluent;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Empty;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Extend;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Project;
import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory.Restructure;

/**
 * 標準的な演算子オブジェクトを生成するファクトリ。
 * {@link CoreOperatorFactory}のメソッドの一部を{@code static}メソッドとして提供する。
 * @since 0.2.6
 */
public final class CoreOperators {

    private static final CoreOperatorFactory FACTORY = new CoreOperatorFactory();

    private CoreOperators() {
        return;
    }

    /**
     * 出力先に何もデータを流さない疑似演算子。入力のダミーとして振る舞う。
     * @param <T> 取り扱うデータの種類
     * @param type 取り扱うデータの種類
     * @return 空(から)演算子
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     * @see CoreOperatorFactory#empty(Class)
     */
    public static <T> Empty<T> empty(Class<T> type) {
        return FACTORY.empty(type);
    }

    /**
     * 入力に対して何も行わない疑似演算子。出力のダミーとして振る舞う。
     * @param in 入力
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     * @see CoreOperatorFactory#stop(Source)
     */
    public static void stop(Source<?> in) {
        FACTORY.stop(in);
    }

    /**
     * 複数の入力をまとめ、それらの流れるすべてのデータを単一の出力に流す。
     * @param <T> 取り扱うデータの種類
     * @param a 入力1
     * @param b 入力2
     * @return 合流演算子
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     * @see CoreOperatorFactory#confluent(Source, Source)
     */
    public static <T> Confluent<T> confluent(Source<T> a, Source<T> b) {
        return FACTORY.confluent(a, b);
    }

    /**
     * 複数の入力をまとめ、それらの流れるすべてのデータを単一の出力に流す。
     * @param <T> 取り扱うデータの種類
     * @param a 入力1
     * @param b 入力2
     * @param c 入力3
     * @return 合流演算子
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     * @see CoreOperatorFactory#confluent(Source, Source, Source)
     */
    public static <T> Confluent<T> confluent(Source<T> a, Source<T> b, Source<T> c) {
        return FACTORY.confluent(a, b, c);
    }

    /**
     * 複数の入力をまとめ、それらの流れるすべてのデータを単一の出力に流す。
     * @param <T> 取り扱うデータの種類
     * @param a 入力1
     * @param b 入力2
     * @param c 入力3
     * @param d 入力4
     * @return 合流演算子
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     * @see CoreOperatorFactory#confluent(Source, Source, Source, Source)
     */
    public static <T> Confluent<T> confluent(
            Source<T> a,
            Source<T> b,
            Source<T> c,
            Source<T> d) {
        return FACTORY.confluent(a, b, c, d);
    }

    /**
     * 複数の入力をまとめ、それらの流れるすべてのデータを単一の出力に流す。
     * @param <T> 取り扱うデータの種類
     * @param inputs 入力の一覧
     * @return 合流演算子
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     * @see CoreOperatorFactory#confluent(Iterable)
     */
    public static <T> Confluent<T> confluent(Iterable<? extends Source<T>> inputs) {
        return FACTORY.confluent(inputs);
    }

    /**
     * 入力されたデータをそのまま出力するが、その際にデータを永続化し、
     * 以降に失敗した場合に永続化したデータを利用して再試行できるようにする。
     * @param <T> 取り扱うデータの種類
     * @param in 永続化対象の入力
     * @return チェックポイント演算子
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     * @see CoreOperatorFactory#checkpoint(Source)
     */
    public static <T> Checkpoint<T> checkpoint(Source<T> in) {
        return FACTORY.checkpoint(in);
    }

    /**
     * 入力されたデータを指定のデータ型に射影する。
     * <p>
     * 入力するデータ型は変換後のデータ型の全てのプロパティを有していなければならない。
     * この演算子の処理結果は、入力されたデータのうち変換後のデータ型に含まれる
     * 全てのプロパティをコピーしたデータになる。
     * </p>
     * <p>
     * 入力されたデータの型と出力先のデータの型に、同じ名前で異なる型のプロパティが存在する場合、
     * この演算子を含むフローのコンパイルは失敗する。
     * </p>
     * @param <T> 変換後のデータの種類
     * @param in 射影対象の入力
     * @param targetType 射影する型
     * @return 射影演算子
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see CoreOperatorFactory#project(Source, Class)
     */
    public static <T> Project<T> project(Source<?> in, Class<T> targetType) {
        return FACTORY.project(in, targetType);
    }

    /**
     * 入力されたデータを指定のデータ型に拡張する。
     * <p>
     * 変換後のデータ型は入力するデータ型の全てのプロパティを有していなければならない。
     * この演算子の処理結果は、入力されたデータ型に含まれる全てのプロパティをコピーしたデータになる。
     * また、入力されたデータに含まれないプロパティは、それぞれの初期値となる。
     * </p>
     * <p>
     * 入力されたデータの型と出力先のデータの型に、同じ名前で異なる型のプロパティが存在する場合、
     * この演算子を含むフローのコンパイルは失敗する。
     * </p>
     * @param <T> 変換後のデータの種類
     * @param in 拡張対象の入力
     * @param targetType 射影する型
     * @return 拡張演算子
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see CoreOperatorFactory#extend(Source, Class)
     */
    public static <T> Extend<T> extend(Source<?> in, Class<T> targetType) {
        return FACTORY.extend(in, targetType);
    }

    /**
     * 入力されたデータを指定のデータ型に再構築する。
     * <p>
     * この演算子の処理結果は、入力されたデータ型に含まれるプロパティのうち、
     * 対象のデータ型にも含まれるプロパティのみをすべてコピーしたデータになる。
     * また、入力されたデータに含まれないプロパティは、それぞれの初期値となる。
     * </p>
     * <p>
     * 入力されたデータの型と出力先のデータの型に、同じ名前で異なる型のプロパティが存在する場合、
     * この演算子を含むフローのコンパイルは失敗する。
     * </p>
     * @param <T> 変換後のデータの種類
     * @param in 再構築対象の入力
     * @param targetType 射影する型
     * @return 再構築演算子
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see CoreOperatorFactory#restructure(Source, Class)
     */
    public static <T> Restructure<T> restructure(Source<?> in, Class<T> targetType) {
        return FACTORY.restructure(in, targetType);
    }
}
