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

import static com.asakusafw.vocabulary.flow.util.PseudElementDescription.*;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.asakusafw.vocabulary.flow.Operator;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowElementResolver;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;

/**
 * 標準的な演算子オブジェクトを生成するファクトリ。
 * TODO i18n
 * @since 0.1.0
 * @version 0.7.3
 */
public class CoreOperatorFactory {

    /**
     * 空演算子の共通インスタンス名。
     */
    public static final String EMPTY_NAME = "empty"; //$NON-NLS-1$

    /**
     * 停止演算子の共通インスタンス名。
     */
    public static final String STOP_NAME = "stop"; //$NON-NLS-1$

    /**
     * 合流演算子の共通インスタンス名。
     */
    public static final String CONFLUENT_NAME = "confluent"; //$NON-NLS-1$

    /**
     * チェックポイント演算子の共通インスタンス名。
     */
    public static final String CHECKPOINT_NAME = "checkpoint"; //$NON-NLS-1$

    /**
     * 射影演算子の共通インスタンス名。
     */
    public static final String PROJECT_NAME = "project"; //$NON-NLS-1$

    /**
     * 拡張演算子の共通インスタンス名。
     */
    public static final String EXTEND_NAME = "extend"; //$NON-NLS-1$

    /**
     * 再構築演算子の共通インスタンス名。
     */
    public static final String RESTRUCTURE_NAME = "restructure"; //$NON-NLS-1$

    /**
     * 出力先に何もデータを流さない疑似演算子。入力のダミーとして振る舞う。
     * @param <T> 取り扱うデータの種類
     * @param type 取り扱うデータの種類
     * @return 空(から)演算子
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public <T> Empty<T> empty(Class<T> type) {
        return empty((Type) type);
    }

    /**
     * 出力先に何もデータを流さない疑似演算子。入力のダミーとして振る舞う。
     * @param <T> 取り扱うデータの種類
     * @param type 取り扱うデータの種類
     * @return 空(から)演算子
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public <T> Empty<T> empty(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return new Empty<T>(type);
    }

    /**
     * 空演算子の断片を生成する。
     * <p>
     * この演算子の断片は、対象の演算子が取り扱うデータの型を指定することで、演算子として利用できるようになる。
     * </p>
     * @return 空(から)演算子の断片
     * @since 0.7.3
     * @see #empty(Class)
     */
    public EmptyFragment empty() {
        return new EmptyFragment();
    }

    /**
     * 入力に対して何も行わない疑似演算子。出力のダミーとして振る舞う。
     * @param in 入力
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public void stop(Source<?> in) {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        PseudElementDescription desc = new PseudElementDescription(
                STOP_NAME,
                getPortType(in),
                true,
                false,
                FlowBoundary.STAGE);
        FlowElementResolver resolver = new FlowElementResolver(desc);
        resolver.resolveInput(INPUT_PORT_NAME, in);
    }

    /**
     * 複数の入力をまとめ、それらの流れるすべてのデータを単一の出力に流す。
     * @param <T> 取り扱うデータの種類
     * @param a 入力1
     * @param b 入力2
     * @return 合流演算子
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public <T> Confluent<T> confluent(Source<T> a, Source<T> b) {
        if (a == null) {
            throw new IllegalArgumentException("a must not be null"); //$NON-NLS-1$
        }
        if (b == null) {
            throw new IllegalArgumentException("b must not be null"); //$NON-NLS-1$
        }
        Type type = getPortType(a);
        List<Source<T>> input = new ArrayList<Source<T>>();
        input.add(a);
        input.add(b);
        return new Confluent<T>(type, input);
    }

    /**
     * 複数の入力をまとめ、それらの流れるすべてのデータを単一の出力に流す。
     * @param <T> 取り扱うデータの種類
     * @param a 入力1
     * @param b 入力2
     * @param c 入力3
     * @return 合流演算子
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public <T> Confluent<T> confluent(Source<T> a, Source<T> b, Source<T> c) {
        if (a == null) {
            throw new IllegalArgumentException("a must not be null"); //$NON-NLS-1$
        }
        if (b == null) {
            throw new IllegalArgumentException("b must not be null"); //$NON-NLS-1$
        }
        if (c == null) {
            throw new IllegalArgumentException("b must not be null"); //$NON-NLS-1$
        }
        Type type = getPortType(a);
        List<Source<T>> input = new ArrayList<Source<T>>();
        input.add(a);
        input.add(b);
        input.add(c);
        return new Confluent<T>(type, input);
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
     */
    public <T> Confluent<T> confluent(
            Source<T> a,
            Source<T> b,
            Source<T> c,
            Source<T> d) {
        if (a == null) {
            throw new IllegalArgumentException("a must not be null"); //$NON-NLS-1$
        }
        if (b == null) {
            throw new IllegalArgumentException("b must not be null"); //$NON-NLS-1$
        }
        if (c == null) {
            throw new IllegalArgumentException("b must not be null"); //$NON-NLS-1$
        }
        if (d == null) {
            throw new IllegalArgumentException("d must not be null"); //$NON-NLS-1$
        }

        Type type = getPortType(a);
        List<Source<T>> input = new ArrayList<Source<T>>();
        input.add(a);
        input.add(b);
        input.add(c);
        input.add(d);
        return new Confluent<T>(type, input);
    }

    /**
     * 複数の入力をまとめ、それらの流れるすべてのデータを単一の出力に流す。
     * @param <T> 取り扱うデータの種類
     * @param inputs 入力の一覧
     * @return 合流演算子
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public <T> Confluent<T> confluent(Iterable<? extends Source<T>> inputs) {
        if (inputs == null) {
            throw new IllegalArgumentException("inputs must not be null"); //$NON-NLS-1$
        }
        List<Source<T>> input = new ArrayList<Source<T>>();
        for (Source<T> in : inputs) {
            if (in == null) {
                throw new IllegalArgumentException("inputs must not contain null"); //$NON-NLS-1$
            }
            input.add(in);
        }
        if (input.isEmpty()) {
            throw new IllegalArgumentException("inputs must not be empty"); //$NON-NLS-1$
        }
        Type type = getPortType(input.get(0));
        return new Confluent<T>(type, input);
    }

    /**
     * 入力されたデータをそのまま出力するが、その際にデータを永続化し、
     * 以降に失敗した場合に永続化したデータを利用して再試行できるようにする。
     * @param <T> 取り扱うデータの種類
     * @param in 永続化対象の入力
     * @return チェックポイント演算子
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public <T> Checkpoint<T> checkpoint(Source<T> in) {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        Type type = getPortType(in);
        return new Checkpoint<T>(type, in);
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
     * @since 0.2.0
     * @see #project(Source)
     */
    public <T> Project<T> project(Source<?> in, Class<T> targetType) {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null"); //$NON-NLS-1$
        }
        return new Project<T>(in, targetType);
    }

    /**
     * 射影演算子の断片を返す。
     * <p>
     * この演算子の断片は、射影演算子の変換後の型を指定することで、演算子として利用できるようになる。
     * </p>
     * @param in 射影対象の入力
     * @return 射影演算子の断片
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.7.3
     * @see #project(Source, Class)
     */
    public ProjectFragment project(Source<?> in) {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        return new ProjectFragment(in);
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
     * @since 0.2.0
     * @see #extend(Source)
     */
    public <T> Extend<T> extend(Source<?> in, Class<T> targetType) {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null"); //$NON-NLS-1$
        }
        return new Extend<T>(in, targetType);
    }

    /**
     * 拡張演算子の断片を返す。
     * <p>
     * この演算子の断片は、拡張演算子の変換後の型を指定することで、演算子として利用できるようになる。
     * </p>
     * @param in 拡張対象の入力
     * @return 拡張演算子の断片
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.7.3
     * @see #extend(Source, Class)
     */
    public ExtendFragment extend(Source<?> in) {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        return new ExtendFragment(in);
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
     * @since 0.2.1
     * @see #restructure(Source)
     */
    public <T> Restructure<T> restructure(Source<?> in, Class<T> targetType) {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        if (targetType == null) {
            throw new IllegalArgumentException("targetType must not be null"); //$NON-NLS-1$
        }
        return new Restructure<T>(in, targetType);
    }

    /**
     * 再構築演算子の断片を返す。
     * <p>
     * この演算子の断片は、再構築演算子の変換後の型を指定することで、演算子として利用できるようになる。
     * </p>
     * @param in 再構築対象の入力
     * @return 再構築演算子の断片
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.7.3
     * @see #restructure(Source, Class)
     */
    public RestructureFragment restructure(Source<?> in) {
        if (in == null) {
            throw new IllegalArgumentException("in must not be null"); //$NON-NLS-1$
        }
        return new RestructureFragment(in);
    }

    private <T> Type getPortType(Source<T> source) {
        assert source != null;
        FlowElementOutput port = source.toOutputPort();
        Type type = port.getDescription().getDataType();
        return type;
    }

    /**
     * 出力先に何もデータを流さない疑似演算子。入力のダミーとして振る舞う。
     * @param <T> 取り扱うデータの種類
     */
    public static final class Empty<T> implements Source<T> {

        /**
         * この演算子の唯一の出力。
         */
        public final Source<T> out;

        private final FlowElementResolver resolver;

        Empty(Type type) {
            assert type != null;
            this.out = this;
            PseudElementDescription desc = new PseudElementDescription(
                    EMPTY_NAME,
                    type,
                    false,
                    true,
                    FlowBoundary.STAGE);
            this.resolver = new FlowElementResolver(desc);
        }

        @Override
        public FlowElementOutput toOutputPort() {
            return resolver.getOutput(OUTPUT_PORT_NAME);
        }
    }

    /**
     * 複数の入力をまとめて、単一の出力とする演算子。
     * @param <T> 取り扱うデータの種類
     */
    public static final class Confluent<T> implements Source<T> {

        /**
         * この演算子の唯一の出力。
         */
        public final Source<T> out;

        private final FlowElementResolver resolver;

        Confluent(Type type, List<Source<T>> input) {
            assert type != null;
            assert input != null;
            this.out = this;
            PseudElementDescription desc = new PseudElementDescription(
                    CONFLUENT_NAME,
                    type,
                    true,
                    true);
            resolver = new FlowElementResolver(desc);
            for (Source<T> in : input) {
                resolver.resolveInput(INPUT_PORT_NAME, in);
            }
        }

        @Override
        public FlowElementOutput toOutputPort() {
            return resolver.getOutput(OUTPUT_PORT_NAME);
        }
    }

    /**
     * 入力されたデータをそのまま出力するが、その際にデータを永続化し、
     * 以降に失敗した場合に永続化したデータを利用して再試行できるようにする。
     * @param <T> 取り扱うデータの種類
     */
    public static final class Checkpoint<T> implements Source<T> {

        /**
         * この演算子の唯一の出力。
         */
        public final Source<T> out;

        private final FlowElementResolver resolver;

        Checkpoint(Type type, Source<T> in) {
            assert type != null;
            assert in != null;
            this.out = this;
            PseudElementDescription desc = new PseudElementDescription(
                    CHECKPOINT_NAME,
                    type,
                    true,
                    true,
                    FlowBoundary.STAGE);
            this.resolver = new FlowElementResolver(desc);
            resolver.resolveInput(INPUT_PORT_NAME, in);
        }

        @Override
        public FlowElementOutput toOutputPort() {
            return resolver.getOutput(OUTPUT_PORT_NAME);
        }
    }

    /**
     * 入力されたデータを指定のデータ型に射影する。
     * @param <T> 変換後の型
     * @since 0.2.0
     */
    public static final class Project<T> implements Operator, Source<T> {

        /**
         * この演算子の唯一の出力。
         */
        public final Source<T> out;

        private final FlowElementResolver resolver;

        Project(Source<?> in, Class<T> targetClass) {
            assert in != null;
            assert targetClass != null;
            OperatorDescription.Builder builder =
                new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.Project.class);
            builder.declare(CoreOperatorFactory.class, CoreOperatorFactory.class, "project"); //$NON-NLS-1$
            builder.declareParameter(Source.class);
            builder.declareParameter(Class.class);
            builder.addInput(INPUT_PORT_NAME, in);
            builder.addOutput(OUTPUT_PORT_NAME, targetClass);
            this.resolver = builder.toResolver();
            this.resolver.resolveInput(INPUT_PORT_NAME, in);
            this.resolver.setName(PROJECT_NAME);
            this.out = this.resolver.resolveOutput(OUTPUT_PORT_NAME);
        }

        @Override
        public FlowElementOutput toOutputPort() {
            return resolver.getOutput(OUTPUT_PORT_NAME);
        }
    }

    /**
     * 入力されたデータを指定のデータ型に拡張する。
     * @param <T> 変換後の型
     * @since 0.2.0
     */
    public static final class Extend<T> implements Operator, Source<T> {

        /**
         * この演算子の唯一の出力。
         */
        public final Source<T> out;

        private final FlowElementResolver resolver;

        Extend(Source<?> in, Class<T> targetClass) {
            assert in != null;
            assert targetClass != null;
            OperatorDescription.Builder builder =
                new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.Extend.class);
            builder.declare(CoreOperatorFactory.class, CoreOperatorFactory.class, "extend"); //$NON-NLS-1$
            builder.declareParameter(Source.class);
            builder.declareParameter(Class.class);
            builder.addInput(INPUT_PORT_NAME, in);
            builder.addOutput(OUTPUT_PORT_NAME, targetClass);
            this.resolver = builder.toResolver();
            this.resolver.resolveInput(INPUT_PORT_NAME, in);
            this.resolver.setName(EXTEND_NAME);
            this.out = this.resolver.resolveOutput(OUTPUT_PORT_NAME);
        }

        @Override
        public FlowElementOutput toOutputPort() {
            return resolver.getOutput(OUTPUT_PORT_NAME);
        }
    }

    /**
     * 入力されたデータを指定のデータ型に再構築する。
     * @param <T> 変換後の型
     * @since 0.2.1
     */
    public static final class Restructure<T> implements Operator, Source<T> {

        /**
         * この演算子の唯一の出力。
         */
        public final Source<T> out;

        private final FlowElementResolver resolver;

        Restructure(Source<?> in, Class<T> targetClass) {
            assert in != null;
            assert targetClass != null;
            OperatorDescription.Builder builder =
                new OperatorDescription.Builder(com.asakusafw.vocabulary.operator.Restructure.class);
            builder.declare(CoreOperatorFactory.class, CoreOperatorFactory.class, "restructure"); //$NON-NLS-1$
            builder.declareParameter(Source.class);
            builder.declareParameter(Class.class);
            builder.addInput(INPUT_PORT_NAME, in);
            builder.addOutput(OUTPUT_PORT_NAME, targetClass);
            this.resolver = builder.toResolver();
            this.resolver.resolveInput(INPUT_PORT_NAME, in);
            this.resolver.setName(RESTRUCTURE_NAME);
            this.out = this.resolver.resolveOutput(OUTPUT_PORT_NAME);
        }

        @Override
        public FlowElementOutput toOutputPort() {
            return resolver.getOutput(OUTPUT_PORT_NAME);
        }
    }

    /**
     * An <em>empty operator</em> fragment which requires the target type.
     * @since 0.7.3
     */
    public static final class EmptyFragment {

        EmptyFragment() {
            return;
        }

        /**
         * 対象の型を指定し、空演算子を生成する。
         * @param <T> 対象の型
         * @param target 対象の型
         * @return 生成した演算子
         */
        public <T> Empty<T> as(Class<T> target) {
            return new Empty<T>(target);
        }

        /**
         * 対象の型と同様の型を持つ入力を指定し、空演算子を生成する。
         * @param <T> 対象の型
         * @param target 対象の型と同様の型を持つ入力
         * @return 生成した演算子
         */
        public <T> Empty<T> as(Source<T> target) {
            return as(classOf(target));
        }

        /**
         * 対象の型と同様の型を持つ出力を指定し、空演算子を生成する。
         * @param <T> 対象の型
         * @param target 対象の型と同様の型を持つ出力
         * @return 生成した演算子
         */
        public <T> Empty<T> as(Out<T> target) {
            return as(classOf(target));
        }
    }

    /**
     * An <em>project operator</em> fragment which requires the target type.
     * @since 0.7.3
     */
    public static final class ProjectFragment {

        private final Source<?> in;

        ProjectFragment(Source<?> in) {
            this.in = in;
        }

        /**
         * 変換対象の型を指定し、射影演算子を生成する。
         * @param <T> 変換対象の型
         * @param target 変換対象の型
         * @return 生成した演算子
         */
        public <T> Project<T> as(Class<T> target) {
            return new Project<T>(in, target);
        }

        /**
         * 変換対象の型と同様の型を持つ入力を指定し、射影演算子を生成する。
         * @param <T> 変換対象の型
         * @param target 対象の型と同様の型を持つ入力
         * @return 生成した演算子
         */
        public <T> Project<T> as(Source<T> target) {
            return as(classOf(target));
        }

        /**
         * 変換対象の型と同様の型を持つ出力を指定し、射影演算子を生成する。
         * @param <T> 変換対象の型
         * @param target 対象の型と同様の型を持つ出力
         * @return 生成した演算子
         */
        public <T> Project<T> as(Out<T> target) {
            return as(classOf(target));
        }
    }

    /**
     * An <em>extend operator</em> fragment which requires the target type.
     * @since 0.7.3
     */
    public static final class ExtendFragment {

        private final Source<?> in;

        ExtendFragment(Source<?> in) {
            this.in = in;
        }

        /**
         * 変換対象の型を指定し、拡張演算子を生成する。
         * @param <T> 変換対象の型
         * @param target 変換対象の型
         * @return 生成した演算子
         */
        public <T> Extend<T> as(Class<T> target) {
            return new Extend<T>(in, target);
        }

        /**
         * 変換対象の型と同様の型を持つ入力を指定し、拡張演算子を生成する。
         * @param <T> 変換対象の型
         * @param target 対象の型と同様の型を持つ入力
         * @return 生成した演算子
         */
        public <T> Extend<T> as(Source<T> target) {
            return as(classOf(target));
        }

        /**
         * 変換対象の型と同様の型を持つ出力を指定し、拡張演算子を生成する。
         * @param <T> 変換対象の型
         * @param target 対象の型と同様の型を持つ出力
         * @return 生成した演算子
         */
        public <T> Extend<T> as(Out<T> target) {
            return as(classOf(target));
        }
    }

    /**
     * An <em>restructure operator</em> fragment which requires the target type.
     * @since 0.7.3
     */
    public static final class RestructureFragment {

        private final Source<?> in;

        RestructureFragment(Source<?> in) {
            this.in = in;
        }

        /**
         * 変換対象の型を指定し、再構築演算子を生成する。
         * @param <T> 変換対象の型
         * @param target 変換対象の型
         * @return 生成した演算子
         */
        public <T> Restructure<T> as(Class<T> target) {
            return new Restructure<T>(in, target);
        }

        /**
         * 変換対象の型と同様の型を持つ入力を指定し、再構築演算子を生成する。
         * @param <T> 変換対象の型
         * @param target 対象の型と同様の型を持つ入力
         * @return 生成した演算子
         */
        public <T> Restructure<T> as(Source<T> target) {
            return as(classOf(target));
        }

        /**
         * 変換対象の型と同様の型を持つ出力を指定し、再構築演算子を生成する。
         * @param <T> 変換対象の型
         * @param target 対象の型と同様の型を持つ出力
         * @return 生成した演算子
         */
        public <T> Restructure<T> as(Out<T> target) {
            return as(classOf(target));
        }
    }

    static <T> Class<T> classOf(Source<T> source) {
        Type type = source.toOutputPort().getDescription().getDataType();
        if ((type instanceof Class<?>) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("CoreOperatorFactory.errorRequireClass"), //$NON-NLS-1$
                    type));
        }
        @SuppressWarnings("unchecked")
        Class<T> aClass = (Class<T>) type;
        return aClass;
    }

    static <T> Class<T> classOf(Out<T> out) {
        Type type = out.toInputPort().getDescription().getDataType();
        if ((type instanceof Class<?>) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("CoreOperatorFactory.errorRequireClass"), //$NON-NLS-1$
                    type));
        }
        @SuppressWarnings("unchecked")
        Class<T> aClass = (Class<T>) type;
        return aClass;
    }
}
