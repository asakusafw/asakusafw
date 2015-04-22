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
package com.asakusafw.compiler.operator;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;
import com.asakusafw.utils.java.jsr199.testing.SafeProcessor;
import com.asakusafw.utils.java.jsr199.testing.VolatileCompiler;
import com.asakusafw.utils.java.jsr199.testing.VolatileJavaFile;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.flow.Source;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowIn;
import com.asakusafw.vocabulary.flow.graph.PortConnection;
import com.asakusafw.vocabulary.flow.testing.MockIn;

/**
 * 演算子コンパイラに関するテストの基底。
 */
public class OperatorCompilerTestRoot {

    static final Logger LOG = LoggerFactory.getLogger(OperatorCompilerTestRoot.class);

    ModelFactory f = Models.getModelFactory();

    private final VolatileCompiler compiler = new VolatileCompiler();

    private final List<JavaFileObject> sources = Lists.create();

    /**
     * テストの情報を破棄する。
     * @throws Exception 例外が発生した場合
     */
    @After
    public void tearDown() throws Exception {
        compiler.close();
    }

    /**
     * 指定のクラスローダーからクラスをロードし、そのクラスのインスタンスを生成して返す。
     * @param loader クラスローダー
     * @param name クラスの名前
     * @return 生成したインスタンス
     */
    protected Object create(ClassLoader loader, String name) {
        try {
            Class<?> loaded = loader.loadClass(name);
            return loaded.newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
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
     * 指定の名前を持つフィールドの内容を演算子の出力として返す。
     * @param dataType データの種類
     * @param object 対象のオブジェクト
     * @param name フィールドの名前
     * @param <T> データの種類
     * @return 参照結果
     */
    @SuppressWarnings("unchecked")
    protected <T> Source<T> output(Class<T> dataType, Object object, String name) {
        try {
            for (Field field : object.getClass().getFields()) {
                if (field.getName().equals(name)) {
                    return (Source<T>) field.get(object);
                }
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        throw new AssertionError(name);
    }

    /**
     * 文字列の集合との比較を行う述語を返す。
     * @param names 対象の文字列の一覧
     * @return 述語
     */
    protected Matcher<? super Set<String>> isJust(String... names) {
        return Matchers.<Set<String>>is(Sets.from(names));
    }

    /**
     * 指定の入力から辿れるすべての要素のグラフを返す。
     * @param inputs 入力
     * @return グラフ
     */
    protected Graph<String> toGraph(MockIn<?>...inputs) {
        FlowElement[] elements = new FlowElement[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            elements[i] = inputs[i].toElement();
        }
        return toGraph(elements);
    }

    /**
     * 指定の入力から辿れるすべての要素のグラフを返す。
     * @param inputs 入力
     * @return グラフ
     */
    protected Graph<String> toGraph(List<FlowIn<?>> inputs) {
        FlowElement[] elements = new FlowElement[inputs.size()];
        for (int i = 0, n = inputs.size(); i < n; i++) {
            elements[i] = inputs.get(i).getFlowElement();
        }
        return toGraph(elements);
    }

    /**
     * 指定の入力から辿れるすべての要素のグラフを返す。
     * @param startingElements 開始要素の一覧
     * @return グラフ
     */
    protected Graph<String> toGraph(FlowElement... startingElements) {
        Set<String> saw = Sets.create();
        LinkedList<FlowElement> work = new LinkedList<FlowElement>();
        for (FlowElement elem : startingElements) {
            work.add(elem);
        }
        Graph<String> graph = Graphs.newInstance();

        while (work.isEmpty() == false) {
            FlowElement elem = work.removeFirst();

            String self = elem.getDescription().getName();
            if (saw.contains(self)) {
                continue;
            }
            saw.add(self);

            // 逆辺をキューに追加
            for (FlowElementInput input : elem.getInputPorts()) {
                for (PortConnection conn : input.getConnected()) {
                    work.add(conn.getUpstream().getOwner());
                }
            }

            // 順辺をグラフに追加
            for (FlowElementOutput output : elem.getOutputPorts()) {
                for (PortConnection conn : output.getConnected()) {
                    FlowElement opposite = conn.getDownstream().getOwner();
                    work.add(opposite);
                    String dest = opposite.getDescription().getName();
                    graph.addEdge(self, dest);
                }
            }
        }

        return graph;
    }

    /**
     * 指定のクラス名のファイルを追加する。
     * @param name クラス名
     */
    protected void add(String name) {
        Class<?> aClass = getClass();
        String file = MessageFormat.format(
                "{0}.files/{1}.java.txt",
                aClass.getSimpleName(),
                name.replace('.', '/'));
        StringBuilder buf = new StringBuilder();
        InputStream in = aClass.getResourceAsStream(file);
        assertThat(file, in, not(nullValue()));
        try {
            Reader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            while (true) {
                int c = reader.read();
                if (c == -1) {
                    break;
                }
                buf.append((char) c);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        sources.add(new VolatileJavaFile(
                name.replace('.', '/'),
                buf.toString()));
    }

    /**
     * コンパイルを指定の注釈プロセッサを利用して実行し、コンパイル結果のローダーを返す。
     * @param processor 利用する注釈プロセッサ
     * @return コンパイル結果
     */
    protected ClassLoader start(Processor processor) {
        SafeProcessor safe = new SafeProcessor(processor);
        compiler.addProcessor(safe);
        ClassLoader loader = start();
        safe.rethrow();
        return loader;
    }

    /**
     * コンパイルを指定のコールバックを利用して実行し、コンパイル結果のローダーを返す。
     * @param callback 利用するコールバック
     * @return コンパイル結果
     */
    protected ClassLoader start(Callback callback) {
        compiler.addProcessor(new DelegateProcessor(callback));
        ClassLoader loader = start();
        callback.rethrow();
        return loader;
    }

    /**
     * コンパイルを指定の演算子プロセッサを利用して実行し、コンパイル結果のローダーを返す。
     * @param procs 利用する演算子プロセッサの一覧
     * @return コンパイル結果
     */
    protected ClassLoader start(final OperatorProcessor... procs) {
        return start(new OperatorCompiler() {
            @Override
            protected Iterable<OperatorProcessor> findOperatorProcessors(OperatorCompilingEnvironment env) {
                return Arrays.asList(procs);
            }
        });
    }

    /**
     * コンパイルを実行し、エラーが発生することを確認する。
     * @param procs 利用する演算子プロセッサの一覧
     */
    protected void error(final OperatorProcessor... procs) {
        SafeProcessor proc = new SafeProcessor(new OperatorCompiler() {
            @Override
            protected Iterable<OperatorProcessor> findOperatorProcessors(OperatorCompilingEnvironment env) {
                return Arrays.asList(procs);
            }
        });
        compiler.addProcessor(proc);
        List<Diagnostic<? extends JavaFileObject>> diagnostics = doCompile();
        proc.rethrow();
        assertThat(diagnostics, not(hasSize(0)));
    }

    /**
     * コンパイルを実行し、エラーが発生することを確認する。
     * @param callback 呼び戻される
     */
    protected void error(Callback callback) {
        compiler.addProcessor(new DelegateProcessor(callback));
        List<Diagnostic<? extends JavaFileObject>> diagnostics = doCompile();
        callback.rethrow();
        assertThat(diagnostics, not(hasSize(0)));
    }

    private ClassLoader start() {
        List<Diagnostic<? extends JavaFileObject>> diagnostics = doCompile();
        boolean wrong = false;
        for (Diagnostic<?> d : diagnostics) {
            if (d.getKind() != Diagnostic.Kind.NOTE) {
                wrong = true;
                break;
            }
        }
        if (wrong) {
            for (JavaFileObject java : compiler.getSources()) {
                try {
                    System.out.println("====" + java.getName());
                    System.out.println(java.getCharContent(true));
                } catch (IOException e) {
                    // ignore.
                }
            }
            for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
                System.out.println("====");
                System.out.println(d);
            }
            throw new AssertionError(diagnostics);
        }
        return compiler.getClassLoader();
    }

    private List<Diagnostic<? extends JavaFileObject>> doCompile() {
        if (LOG.isDebugEnabled()) {
            for (JavaFileObject java : sources) {
                try {
                    LOG.debug("==== {}", java.getName());
                    LOG.debug("{}", java.getCharContent(true));
                } catch (IOException e) {
                    // ignore.
                }
            }
        }

        compiler.addArguments("-Xlint:unchecked");
        for (JavaFileObject java : sources) {
            compiler.addSource(java);
        }
        if (sources.isEmpty()) {
            compiler.addSource(new VolatileJavaFile("A", "public class A {}"));
        }
        List<Diagnostic<? extends JavaFileObject>> diagnostics = compiler.doCompile();
        if (LOG.isDebugEnabled()) {
            for (JavaFileObject java : compiler.getSources()) {
                try {
                    LOG.debug("==== {}", java.getName());
                    LOG.debug("{}", java.getCharContent(true));
                } catch (IOException e) {
                    // ignore.
                }
            }
            for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
                LOG.debug("====");
                LOG.debug("{}", d);
            }
        }
        return diagnostics;
    }
}
