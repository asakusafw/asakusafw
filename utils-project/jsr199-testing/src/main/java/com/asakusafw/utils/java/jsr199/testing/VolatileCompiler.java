/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.utils.java.jsr199.testing;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

/**
 * ソースコードをメモリ上に展開してコンパイルを実施する。
 */
public class VolatileCompiler implements Closeable {

    private final JavaCompiler compiler;

    private final VolatileClassOutputManager files;

    private final List<String> arguments;

    private final List<JavaFileObject> targets;

    private final List<Processor> processors;

    /**
     * インスタンスを生成する。
     * @throws IllegalStateException コンパイラを利用できない場合
     */
    public VolatileCompiler() {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("No System Java Compiler");
        }
        this.files = new VolatileClassOutputManager(
            compiler.getStandardFileManager(
                null,
                Locale.ENGLISH,
                Charset.forName("UTF-8"))); //$NON-NLS-1$
        this.arguments = new ArrayList<String>();
        this.targets = new ArrayList<JavaFileObject>();
        this.processors = new ArrayList<Processor>();

        Collections.addAll(arguments, "-source", "1.6"); //$NON-NLS-1$ //$NON-NLS-2$
        Collections.addAll(arguments, "-target", "1.6"); //$NON-NLS-1$ //$NON-NLS-2$
        Collections.addAll(arguments, "-encoding", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * このコンパイラにソースプログラムを追加する。
     * @param java 追加するソースプログラム
     * @return このオブジェクト (メソッドチェイン用)
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public VolatileCompiler addSource(JavaFileObject java) {
        if (java == null) {
            throw new IllegalArgumentException("java must not be null"); //$NON-NLS-1$
        }
        targets.add(java);
        return this;
    }

    /**
     * このコンパイラに注釈プロセッサを追加する。
     * @param processor 追加するプロセッサ
     * @return このオブジェクト (メソッドチェイン用)
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public VolatileCompiler addProcessor(Processor processor) {
        if (processor == null) {
            throw new IllegalArgumentException("processor must not be null"); //$NON-NLS-1$
        }
        processors.add(processor);
        return this;
    }

    /**
     * このコンパイラに登録されたオプション引数の一覧をすべて削除する。
     * <p>
     * コンパイラにはあらかじめ下記の引数が指定されているが、それらについても削除する。
     * </p>
     * <ul>
     *   <li> {@code -source 1.6} </li>
     *   <li> {@code -target 1.6} </li>
     *   <li> {@code -encoding UTF-8} </li>
     * </ul>
     * @return このオブジェクト (メソッドチェイン用)
     */
    public VolatileCompiler resetArguments() {
        arguments.clear();
        return this;
    }

    /**
     * このコンパイラにオプション引数を追加する。
     * <p>
     * なお、コンパイラにはあらかじめ下記の引数が指定されている。
     * </p>
     * <ul>
     *   <li> {@code -source 1.6} </li>
     *   <li> {@code -target 1.6} </li>
     *   <li> {@code -encoding UTF-8} </li>
     * </ul>
     * @param compilerArguments 追加する引数の一覧
     * @return このオブジェクト (メソッドチェイン用)
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     * @see #resetArguments()
     */
    public VolatileCompiler addArguments(String...compilerArguments) {
        if (compilerArguments == null) {
            throw new IllegalArgumentException("compilerArguments must not be null"); //$NON-NLS-1$
        }
        Collections.addAll(arguments, compilerArguments);
        return this;
    }

    /**
     * コンパイルを実行し、結果の診断オブジェクトを返す。
     * @return 結果の診断オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public List<Diagnostic<? extends JavaFileObject>> doCompile() {
        DiagnosticCollector<JavaFileObject> collector =
            new DiagnosticCollector<JavaFileObject>();

        CompilationTask task = compiler.getTask(
            new PrintWriter(new OutputStreamWriter(System.err, Charset.defaultCharset()), true),
            files,
            collector,
            arguments,
            Arrays.<String>asList(),
            targets);

        task.setProcessors(processors);

        task.call();
        return collector.getDiagnostics();
    }

    /**
     * 現在までのコンパイル結果を元にしたクラスローダーを生成して返す。
     * @return クラスローダー
     */
    public ClassLoader getClassLoader() {
        DirectClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<DirectClassLoader>() {
            @Override
            public DirectClassLoader run() {
                return new DirectClassLoader(VolatileCompiler.this.getClass().getClassLoader());
            }
        });
        loader.setDefaultAssertionStatus(true);
        for (VolatileClassFile klass : files.getCompiled()) {
            loader.add(klass.getBinaryName(), klass.getBinaryContent());
        }
        return loader;
    }

    /**
     * コンパイラが生成したソースファイルの一覧を返す。
     * @return コンパイラが生成したソースファイルの一覧
     */
    public Collection<VolatileJavaFile> getSources() {
        return files.getSources();
    }

    /**
     * コンパイラが生成したリソースファイルの一覧を返す。
     * @return コンパイラが生成したリソースファイルの一覧
     */
    public Collection<VolatileResourceFile> getResources() {
        return files.getResources();
    }

    /**
     * コンパイラが生成したクラスファイルの一覧を返す。
     * @return コンパイラが生成したクラスファイルの一覧
     */
    public Collection<VolatileClassFile> getCompiled() {
        return files.getCompiled();
    }

    @Override
    public void close() throws IOException {
        files.close();
    }
}
