/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.modelgen.emitter;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.apache.hadoop.io.Writable;
import org.junit.After;
import org.junit.Before;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
import com.asakusafw.runtime.io.RecordParser;
import com.asakusafw.utils.java.jsr199.testing.VolatileCompiler;
import com.asakusafw.utils.java.jsr199.testing.VolatileJavaFile;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.util.Emitter;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.JoinedModel;
import com.asakusafw.vocabulary.model.SummarizedModel;

/**
 * Test root for this package.
 */
public abstract class EmitterTestRoot {

    /**
     * ファクトリオブジェクト。
     */
    protected ModelFactory f;

    /**
     * 生成したファイルの一覧。
     */
    List<VolatileJavaFile> files;

    /**
     * 利用可能なコンパイラ
     */
    VolatileCompiler compiler;

    /**
     * テストを初期化する。
     * @throws Exception if occur
     */
    @Before
    public void setUp() throws Exception {
        f = Models.getModelFactory();
        files = new ArrayList<VolatileJavaFile>();
        compiler = new VolatileCompiler();
    }

    /**
     * テストの情報を破棄する。
     * @throws Exception 例外が発生した場合
     */
    @After
    public void tearDown() throws Exception {
        if (compiler != null) {
            compiler.close();
        }
    }

    /**
     * 仮想的な出力を開く。
     * @param source 対象ソースプログラム
     * @return 出力
     */
    protected PrintWriter createOutputFor(CompilationUnit source) {
        StringBuilder buf = new StringBuilder();
        TypeDeclaration type = Emitter.findPrimaryType(source);
        if (source.getPackageDeclaration() != null) {
            buf.append(source.getPackageDeclaration().toString().replace('.', '/'));
            buf.append('/');
        }
        buf.append(type.getName().getToken());
        VolatileJavaFile file = new VolatileJavaFile(buf.toString());
        files.add(file);
        return new PrintWriter(file.openWriter());
    }

    /**
     * コンパイルを実行する。
     * @return コンパイル結果のクラスローダー。
     */
    protected ClassLoader compile() {
        if (files.isEmpty()) {
            throw new AssertionError();
        }
        for (JavaFileObject java : files) {
            compiler.addSource(java);
        }
        compiler.addArguments("-Xlint");
        List<Diagnostic<? extends JavaFileObject>> diagnostics = compiler.doCompile();
        boolean hasWrong = false;
        for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
            if (d.getKind() == Diagnostic.Kind.ERROR || d.getKind() == Diagnostic.Kind.WARNING) {
                JavaFileObject java = d.getSource();
                if (java != null) {
                    try {
                        System.out.println("=== " + java.getName());
                        System.out.println(java.getCharContent(true));
                        System.out.println();
                        System.out.println();
                    } catch (IOException e) {
                        // ignored
                    }
                }
                System.out.println("--");
                System.out.println(d.getMessage(Locale.getDefault()));
                hasWrong = true;
            }
        }
        if (hasWrong) {
            throw new AssertionError(diagnostics);
        }
        return compiler.getClassLoader();
    }

    /**
     * 指定の名前のクラスインスタンスを生成する。
     * @param loader 対象のローダー
     * @param name クラス名
     * @return 対象クラスのインスタンス
     */
    protected Object create(ClassLoader loader, String name) {
        try {
            Class<?> klass = loader.loadClass("com.example." + name);
            return klass.newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 指定の名前の{@link ModelInput}を生成する。
     * @param loader 対象のローダー
     * @param parser パーサー
     * @param name クラス名
     * @return 対象クラスのインスタンス
     */
    @SuppressWarnings("unchecked")
    protected ModelInput<Object> createInput(
            ClassLoader loader,
            RecordParser parser,
            String name) {
        try {
            Class<?> klass = loader.loadClass("com.example." + name);
            Constructor<?> ctor = klass.getConstructor(RecordParser.class);
            return (ModelInput<Object>) ctor.newInstance(parser);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 指定の名前の{@link ModelOutput}を生成する。
     * @param loader 対象のローダー
     * @param emitter エミッター
     * @param name クラス名
     * @return 対象クラスのインスタンス
     */
    @SuppressWarnings("unchecked")
    protected ModelOutput<Object> createOutput(
            ClassLoader loader,
            RecordEmitter emitter,
            String name) {
        try {
            Class<?> klass = loader.loadClass("com.example." + name);
            Constructor<?> ctor = klass.getConstructor(RecordEmitter.class);
            return (ModelOutput<Object>) ctor.newInstance(emitter);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 指定のオブジェクトのプロパティの内容を返す。
     * @param object 対象のオブジェクト
     * @param name プロパティに対応するgetterの名称
     * @return 対応するプロパティ
     * @throws Throwable 例外が発生した場合
     */
    public static Object get(Object object, String name) throws Throwable {
        try {
            return find(object, name).invoke(object);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 指定のオブジェクトのプロパティの内容を変更する。
     * @param object 対象のオブジェクト
     * @param name プロパティに対応するsetterの名称
     * @param value 設定する値
     * @throws Throwable 例外が発生した場合
     */
    public static void set(Object object, String name, Object value) throws Throwable {
        try {
            Method method = find(object, name);
            method.invoke(object, value);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * {@code argument}の内容を、{@code object}にコピーする。
     * @param object コピー先のオブジェクト
     * @param argument コピー元のオブジェクト
     * @throws Throwable 例外が発生した場合
     */
    public static void copyFrom(Object object, Object argument) throws Throwable {
        try {
            Method method = find(object, DataModel.Interface.METHOD_NAME_COPY_FROM);
            method.invoke(object, argument);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 対象の二つのオブジェクトの内容を結合して、単一のオブジェクトに書き出す。
     * @param object コピー元のオブジェクト
     * @param left 結合されるオブジェクト
     * @param right 結合するオブジェクト
     * @throws Throwable 例外が発生した場合
     */
    public static void joinFrom(Object object, Object left, Object right) throws Throwable {
        try {
            Method method = find(object, JoinedModel.Interface.METHOD_NAME_JOIN_FROM);
            method.invoke(object, left, right);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 結合された単一のオブジェクトの内容を分割し、対象の二つのオブジェクトに書き出す。
     * @param object コピー先のオブジェクト
     * @param left 結合されたオブジェクト
     * @param right 結合したオブジェクト
     * @throws Throwable 例外が発生した場合
     */
    public static void split(Object object, Object left, Object right) throws Throwable {
        try {
            Method method = find(object, JoinedModel.Interface.METHOD_NAME_SPLIT_INTO);
            method.invoke(object, left, right);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 指定のオブジェクトの内容を、集計用のオブジェクトに書き出す。
     * @param object 対象の集計用オブジェクト
     * @param argument 書き出すオブジェクト
     * @throws Throwable 例外が発生した場合
     */
    public static void startSummarize(Object object, Object argument) throws Throwable {
        try {
            Method method = find(object, SummarizedModel.Interface.METHOD_NAME_START_SUMMARIZATION);
            method.invoke(object, argument);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 指定のオブジェクトの内容を、集計用のオブジェクトに追記する。
     * @param object 対象の集計用オブジェクト
     * @param argument 追記するオブジェクト
     * @throws Throwable 例外が発生した場合
     */
    public static void combineSummarize(Object object, Object argument) throws Throwable {
        try {
            Method method = find(object, SummarizedModel.Interface.METHOD_NAME_COMBINE_SUMMARIZATION);
            method.invoke(object, argument);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static Method find(Object object, String name) {
        List<Method> found = new ArrayList<Method>();
        for (Method method : object.getClass().getMethods()) {
            if (method.getName().equals(name)) {
                found.add(method);
            }
        }
        if (found.size() != 1) {
            throw new AssertionError(name + found);
        }
        return found.get(0);
    }

    /**
     * Writableとして書き出した後に復元する。
     * @param <T> データの種類
     * @param value 対象のデータ
     * @return 復元したデータ
     */
    @SuppressWarnings("unchecked")
    protected <T> T restore(T value) {
        assertThat(value, instanceOf(Writable.class));
        Writable writable = (Writable) value;
        try {
            ByteArrayOutputStream write = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(write);
            writable.write(out);
            out.close();

            ByteArrayInputStream read = new ByteArrayInputStream(write.toByteArray());
            ObjectInputStream in = new ObjectInputStream(read);
            Writable copy = writable.getClass().newInstance();
            copy.readFields(in);
            assertThat(in.read(), is(-1));
            assertThat(copy, is((Writable) value));
            assertThat(copy.hashCode(), is(value.hashCode()));
            return (T) copy;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * {@link TableModelEntityEmitter}のモック。
     */
    protected class Table extends TableModelEntityEmitter {

        Table() {
            super(
                    Models.getModelFactory(),
                    new File("."),
                    "com.example",
                    Collections.singletonList("Table Model Entity Emitter"));
        }

        @Override
        protected PrintWriter openOutputFor(CompilationUnit source) throws IOException {
            return createOutputFor(source);
        }
    }

    /**
     * {@link JoinedModelEntityEmitter}のモック。
     */
    protected class Joined extends JoinedModelEntityEmitter {

        Joined() {
            super(
                    Models.getModelFactory(),
                    new File("."),
                    "com.example",
                    Collections.singletonList("Joined Model Entity Emitter"));
        }

        @Override
        protected PrintWriter openOutputFor(CompilationUnit source) throws IOException {
            return createOutputFor(source);
        }
    }

    /**
     * {@link SummarizedModelEntityEmitter}のモック。
     */
    protected class Summarized extends SummarizedModelEntityEmitter {

        Summarized() {
            super(
                    Models.getModelFactory(),
                    new File("."),
                    "com.example",
                    Collections.singletonList("Summarized Model Entity Emitter"));
        }

        @Override
        protected PrintWriter openOutputFor(CompilationUnit source) throws IOException {
            return createOutputFor(source);
        }
    }

    /**
     * {@link ModelInputEmitter}のモック。
     */
    protected class TsvIn extends ModelInputEmitter {

        TsvIn() {
            super(
                    Models.getModelFactory(),
                    new File("."),
                    "com.example",
                    Collections.singletonList("TSV Input Emitter"));
        }

        @Override
        protected PrintWriter openOutputFor(CompilationUnit source) throws IOException {
            return createOutputFor(source);
        }
    }

    /**
     * {@link ModelInputEmitter}のモック。
     */
    protected class TsvOut extends ModelOutputEmitter {

        TsvOut() {
            super(
                    Models.getModelFactory(),
                    new File("."),
                    "com.example",
                    Collections.singletonList("TSV Output Emitter"));
        }

        @Override
        protected PrintWriter openOutputFor(CompilationUnit source) throws IOException {
            return createOutputFor(source);
        }
    }
}
