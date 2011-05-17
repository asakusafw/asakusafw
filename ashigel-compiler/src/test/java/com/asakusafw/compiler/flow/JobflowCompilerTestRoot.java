/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.apache.hadoop.io.Writable;
import org.junit.After;
import org.junit.Before;

import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.compiler.flow.plan.StagePlanner;
import com.asakusafw.compiler.flow.stage.CompiledShuffle;
import com.asakusafw.compiler.flow.stage.CompiledType;
import com.asakusafw.compiler.flow.stage.MapFragmentEmitter;
import com.asakusafw.compiler.flow.stage.ReduceFragmentEmitter;
import com.asakusafw.compiler.flow.stage.ShuffleAnalyzer;
import com.asakusafw.compiler.flow.stage.ShuffleGroupingComparatorEmitter;
import com.asakusafw.compiler.flow.stage.ShuffleKeyEmitter;
import com.asakusafw.compiler.flow.stage.ShuffleModel;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
import com.asakusafw.compiler.flow.stage.ShufflePartitionerEmitter;
import com.asakusafw.compiler.flow.stage.ShuffleSortComparatorEmitter;
import com.asakusafw.compiler.flow.stage.ShuffleValueEmitter;
import com.asakusafw.compiler.flow.stage.StageAnalyzer;
import com.asakusafw.compiler.flow.stage.StageCompiler;
import com.asakusafw.compiler.flow.stage.StageModel;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.stage.StageModel.MapUnit;
import com.asakusafw.compiler.flow.stage.StageModel.ReduceUnit;
import com.asakusafw.compiler.repository.SpiDataClassRepository;
import com.asakusafw.compiler.repository.SpiExternalIoDescriptionProcessorRepository;
import com.asakusafw.compiler.repository.SpiFlowElementProcessorRepository;
import com.asakusafw.compiler.repository.SpiFlowGraphRewriterRepository;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.flow.Rendezvous;
import com.asakusafw.runtime.flow.SegmentedWritable;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.ashigeru.lang.java.jsr199.testing.VolatileCompiler;
import com.ashigeru.lang.java.jsr199.testing.VolatileJavaFile;
import com.ashigeru.lang.java.model.syntax.Name;
import com.ashigeru.lang.java.model.util.Models;

/**
 * バッチコンパイラに関するテストの基底クラス。
 */
public class JobflowCompilerTestRoot {

    /**
     * ダンプ出力のためのフラグ
     */
    protected boolean dump = true;

    private final VolatileCompiler javaCompiler = new VolatileCompiler();

    private VolatilePackager packager = new VolatilePackager();

    /**
     * 利用可能な環境。
     */
    protected FlowCompilingEnvironment environment;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        packager = new VolatilePackager();
        FlowCompilerConfiguration config = new FlowCompilerConfiguration();
        config.setBatchId("batch");
        config.setFlowId("flow");
        config.setFactory(Models.getModelFactory());
        config.setProcessors(new SpiFlowElementProcessorRepository());
        config.setExternals(new SpiExternalIoDescriptionProcessorRepository());
        config.setDataClasses(new SpiDataClassRepository());
        config.setGraphRewriters(new SpiFlowGraphRewriterRepository());
        config.setPackager(packager);
        config.setRootPackageName("com.example");
        config.setRootLocation(Location.fromPath("com/example", '/'));
        config.setServiceClassLoader(getClass().getClassLoader());
        config.setOptions(new FlowCompilerOptions());
        environment = new FlowCompilingEnvironment(config);
        environment.bless();
    }

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        javaCompiler.close();
    }

    /**
     * ジョブフローを解析してステージグラフを返す。
     * @param aClass ジョブフロー記述クラス
     * @return ステージグラフ
     */
    protected List<StageModel> compile(Class<? extends FlowDescription> aClass) {
        assert aClass != null;
        StageGraph graph = jfToStageGraph(aClass);
        return compileStages(graph);
    }

    /**
     * ジョブフローを解析してステージグラフを返す。
     * @param aClass ジョブフロー記述クラス
     * @return ステージグラフ
     */
    protected StageGraph jfToStageGraph(Class<? extends FlowDescription> aClass) {
        assert aClass != null;
        JobFlowDriver analyzed = JobFlowDriver.analyze(aClass);
        assertThat(analyzed.getDiagnostics().toString(), analyzed.hasError(), is(false));
        JobFlowClass flow = analyzed.getJobFlowClass();
        FlowGraph flowGraph = flow.getGraph();
        return flowToStageGraph(flowGraph);
    }

    private StageGraph flowToStageGraph(FlowGraph flowGraph) {
        assert flowGraph != null;
        StagePlanner planner = new StagePlanner(
                environment.getGraphRewriters().getRewriters(),
                environment.getOptions());
        StageGraph planned = planner.plan(flowGraph);
        assertThat(planner.getDiagnostics().toString(),
                planner.getDiagnostics().isEmpty(),
                is(true));
        return planned;
    }

    /**
     * ステージグラフ全体をコンパイルして個々のステージの構造を返す。
     * @param graph ステージグラフ全体
     * @return コンパイル結果
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    protected List<StageModel> compileStages(StageGraph graph) {
        try {
            return new StageCompiler(environment).compile(graph);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * ステージブロックを解析して断片までの解析が終わったステージの構造を返す。
     * @param block ステージブロック
     * @return 断片までの解析が終わったステージの構造
     * @throws IOException 出力に失敗した場合
     */
    protected StageModel compileFragments(StageBlock block) throws IOException {
        ShuffleModel shuffle = compileShuffle(block);
        StageModel stage = new StageAnalyzer(environment).analyze(block, shuffle);
        for (MapUnit unit : stage.getMapUnits()) {
            for (Fragment fragment : unit.getFragments()) {
                compile(fragment, stage);
            }
        }
        for (ReduceUnit unit : stage.getReduceUnits()) {
            for (Fragment fragment : unit.getFragments()) {
                compile(fragment, stage);
            }
        }
        return stage;
    }

    private void compile(Fragment fragment, StageModel stage) throws IOException {
        if (fragment.isRendezvous()) {
            CompiledType compiled = new ReduceFragmentEmitter(environment).emit(
                    fragment,
                    stage.getShuffleModel(),
                    stage.getStageBlock());
            fragment.setCompiled(compiled);
        } else {
            CompiledType compiled = new MapFragmentEmitter(environment).emit(
                    fragment,
                    stage.getStageBlock());
            fragment.setCompiled(compiled);
        }
    }

    /**
     * ステージブロックを解析してシャッフルの構造を返す。
     * @param block ステージブロック
     * @return シャッフルの構造、シャッフルしない場合は{@code null}
     * @throws IOException 出力に失敗した場合
     */
    protected ShuffleModel compileShuffle(StageBlock block) throws IOException {
        ShuffleModel shuffle = new ShuffleAnalyzer(environment).analyze(block);
        assertThat(environment.hasError(), is(false));
        if (shuffle == null) {
            return null;
        }
        Name keyTypeName = new ShuffleKeyEmitter(environment).emit(shuffle);
        Name valueTypeName = new ShuffleValueEmitter(environment).emit(shuffle);
        Name groupComparatorTypeName = new ShuffleGroupingComparatorEmitter(environment).emit(shuffle, keyTypeName);
        Name sortComparatorTypeName = new ShuffleSortComparatorEmitter(environment).emit(shuffle, keyTypeName);
        Name partitionerTypeName = new ShufflePartitionerEmitter(environment).emit(shuffle, keyTypeName, valueTypeName);
        CompiledShuffle compiled = new CompiledShuffle(
                keyTypeName,
                valueTypeName,
                groupComparatorTypeName,
                sortComparatorTypeName,
                partitionerTypeName);
        shuffle.setCompiled(compiled);
        return shuffle;
    }

    /**
     * 指定のクラスローダーからクラスをロードし、そのクラスのインスタンスを生成して返す。
     * @param loader クラスローダー
     * @param name クラスの名前
     * @param arguments 引数の一覧
     * @return 生成したインスタンス
     */
    protected Object create(
            ClassLoader loader,
            Name name,
            Object...arguments) {
        try {
            Class<?> loaded = loader.loadClass(name.toNameString());
            for (Constructor<?> ctor : loaded.getConstructors()) {
                if (ctor.getParameterTypes().length == arguments.length) {
                    return ctor.newInstance(arguments);
                }
            }
            throw new AssertionError();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 指定のクラスローダーからクラスをロードし、そのクラスのインスタンスを生成して返す。
     * @param <T> 入力の型
     * @param loader クラスローダー
     * @param name クラスの名前
     * @param arguments 引数の一覧
     * @return 生成したインスタンス
     */
    @SuppressWarnings("unchecked")
    protected <T> Result<T> createResult(
            ClassLoader loader,
            Name name,
            Object...arguments) {
        return (Result<T>) create(loader, name, arguments);
    }

    /**
     * 指定のクラスローダーからクラスをロードし、そのクラスのインスタンスを生成して返す。
     * @param <K> キーの型
     * @param <V> 値の型
     * @param loader クラスローダー
     * @param name クラスの名前
     * @param arguments 引数の一覧
     * @return 生成したインスタンス
     */
    @SuppressWarnings("unchecked")
    protected <K extends Writable, V extends Writable> Rendezvous<V> createRendezvous(
            ClassLoader loader,
            Name name,
            Object...arguments) {
        return (Rendezvous<V>) create(loader, name, arguments);
    }

    /**
     * 指定の名前を持つメソッドを起動した結果を返す。
     * @param object 対象のオブジェクト
     * @param name メソッドの名前
     * @param arguments 引数の一覧
     * @return 起動結果
     */
    protected Object invoke(Object object, String name, Object... arguments) {
        try {
            for (Method method : object.getClass().getMethods()) {
                if (method.getName().equals(name)) {
                    return method.invoke(object, arguments);
                }
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        throw new AssertionError(name);
    }

    /**
     * 指定の名前を持つフィールドの内容を返す。
     * @param object 対象のオブジェクト
     * @param name フィールドの名前
     * @return 参照結果
     */
    protected Object access(Object object, String name) {
        try {
            for (Field field : object.getClass().getFields()) {
                if (field.getName().equals(name)) {
                    return field.get(object);
                }
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        throw new AssertionError(name);
    }

    /**
     * 指定のステージのシャッフルキーを返す。
     * @param loader 利用するローダー
     * @param stage 対象のステージ
     * @return 生成したシャッフルキー
     */
    protected SegmentedWritable createShuffleKey(
            ClassLoader loader,
            StageModel stage) {
        assertThat(stage.getShuffleModel(), not(nullValue()));
        Name name = stage.getShuffleModel().getCompiled().getKeyTypeName();
        return (SegmentedWritable) create(loader, name);
    }

    /**
     * 指定のステージのシャッフル値を返す。
     * @param loader 利用するローダー
     * @param stage 対象のステージ
     * @return 生成したシャッフル値
     */
    protected SegmentedWritable createShuffleValue(
            ClassLoader loader,
            StageModel stage) {
        assertThat(stage.getShuffleModel(), not(nullValue()));
        Name name = stage.getShuffleModel().getCompiled().getValueTypeName();
        return (SegmentedWritable) create(loader, name);
    }

    /**
     * 指定のシャッフルキーに値を設定する。
     * @param segment 対象のセグメント
     * @param key キー
     * @param toSet 設定する値
     */
    protected void setShuffleKey(
            Segment segment,
            SegmentedWritable key,
            Object toSet) {
        String name = Naming.getShuffleKeySetter(segment.getPortId());
        try {
            Method method = key.getClass().getMethod(name, toSet.getClass());
            method.invoke(key, toSet);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 指定のシャッフル値に値を設定する。
     * @param segment 対象のセグメント
     * @param value 対象のシャッフル値
     * @param toSet 設定する値
     */
    protected void setShuffleValue(
            Segment segment,
            SegmentedWritable value,
            Object toSet) {
        String name = Naming.getShuffleValueSetter(segment.getPortId());
        try {
            Method method = value.getClass().getMethod(name, toSet.getClass());
            method.invoke(value, toSet);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 指定のシャッフルキーとシャッフル値に値を設定する。
     * @param segment 対象のセグメント
     * @param key シャッフルキー
     * @param value シャッフル値
     * @param toSet 設定する値
     */
    protected void setShuffleKeyValue(
            Segment segment,
            SegmentedWritable key,
            SegmentedWritable value,
            Object toSet) {
        setShuffleKey(segment, key, toSet);
        setShuffleValue(segment, value, toSet);
    }

    /**
     * 指定のシャッフル値から特定セグメントの値を設定する。
     * @param segment 対象のセグメント
     * @param value 対象のシャッフル値
     * @return セグメントの値
     */
    protected Object getShuffleValue(
            Segment segment,
            SegmentedWritable value) {
        String name = Naming.getShuffleValueGetter(segment.getPortId());
        try {
            Method method = value.getClass().getMethod(name);
            return method.invoke(value);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * エミッターに追加された結果を元にコンパイルを実行する。
     * @return 結果をロードするためのローダー
     */
    protected ClassLoader start() {
        List<Diagnostic<? extends JavaFileObject>> diagnostics = doCompile();
        for (Diagnostic<?> d : diagnostics) {
            if (d.getKind() != Diagnostic.Kind.NOTE) {
                throw new AssertionError(diagnostics);
            }
        }
        return javaCompiler.getClassLoader();
    }

    private List<Diagnostic<? extends JavaFileObject>> doCompile() {
        List<VolatileJavaFile> sources = packager.getEmitter().getEmitted();
        if (dump) {
            for (JavaFileObject java : sources) {
                try {
                    System.out.println("====" + java.getName());
                    System.out.println(java.getCharContent(true));
                } catch (IOException e) {
                    // ignore.
                }
            }
        }

        for (JavaFileObject java : sources) {
            javaCompiler.addSource(java);
        }
        if (sources.isEmpty()) {
            javaCompiler.addSource(new VolatileJavaFile("A", "public class A {}"));
        }
        List<Diagnostic<? extends JavaFileObject>> diagnostics = javaCompiler.doCompile();
        if (dump) {
            for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
                System.out.println("====");
                System.out.println(d);
            }
        }
        return diagnostics;
    }
}
