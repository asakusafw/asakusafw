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
package com.asakusafw.compiler.testing;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipInputStream;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.processor.flow.UpdateFlowSimple;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.compiler.testing.flow.DuplicateFragments;
import com.asakusafw.compiler.testing.flow.StraightFragments;
import com.asakusafw.compiler.testing.flow.StraightRendezvousFragments;
import com.asakusafw.compiler.util.tester.CompilerTester;
import com.asakusafw.compiler.util.tester.HadoopDriver;
import com.asakusafw.compiler.util.tester.CompilerTester.TestInput;
import com.asakusafw.compiler.util.tester.CompilerTester.TestOutput;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Test for {@link DirectFlowCompiler}.
 */
public class DirectFlowCompilerTest {

    /**
     * コンパイラのテストに利用する。
     */
    @Rule
    public CompilerTester tester = new CompilerTester();

    /**
     * フォルダの中を検索するクラスパス。
     * @throws Exception テスト中に例外が発生した場合
     */
    @Test
    public void folderLibraryPath() throws Exception {
        File file = extract("example.jar");
        Class<?> aClass = load(file, "com.example.Hello");
        File library = DirectFlowCompiler.toLibraryPath(aClass);
        assertThat(library, not(nullValue()));
        assertThat(file.getCanonicalFile(), is(library.getCanonicalFile()));
    }

    /**
     * フォルダの中を検索するクラスパス。
     * @throws Exception テスト中に例外が発生した場合
     */
    @Test
    public void folderLibraryPathWithInner() throws Exception {
        File file = extract("example.jar");
        Class<?> aClass = load(file, "com.example.Hello$World");
        File library = DirectFlowCompiler.toLibraryPath(aClass);
        assertThat(library, not(nullValue()));
        assertThat(file.getCanonicalFile(), is(library.getCanonicalFile()));
    }

    /**
     * JARの中を検索するクラスパス。
     * @throws Exception テスト中に例外が発生した場合
     */
    @Test
    public void jarLibraryPath() throws Exception {
        File file = copy("example.jar");
        Class<?> aClass = load(file, "com.example.Hello");
        File library = DirectFlowCompiler.toLibraryPath(aClass);
        assertThat(library, not(nullValue()));
        assertThat(file.getCanonicalFile(), is(library.getCanonicalFile()));
    }

    /**
     * JARの中を検索するクラスパス。
     * @throws Exception テスト中に例外が発生した場合
     */
    @Test
    public void jarLibraryPathWithInner() throws Exception {
        File file = copy("example.jar");
        Class<?> aClass = load(file, "com.example.Hello$World");
        File library = DirectFlowCompiler.toLibraryPath(aClass);
        assertThat(library, not(nullValue()));
        assertThat(file.getCanonicalFile(), is(library.getCanonicalFile()));
    }

    /**
     * ZIPの中を検索するクラスパス。
     * @throws Exception テスト中に例外が発生した場合
     */
    @Test
    public void zipLibraryPath() throws Exception {
        File file = copy("example.zip");
        Class<?> aClass = load(file, "com.example.Hello");
        File library = DirectFlowCompiler.toLibraryPath(aClass);
        assertThat(library, not(nullValue()));
        assertThat(file.getCanonicalFile(), is(library.getCanonicalFile()));
    }

    /**
     * 単純な例のコンパイル。
     * @throws Exception コンパイル中に例外が発生した場合
     */
    @Test
    public void simpleCompile() throws Exception {
        List<File> classpath = Arrays.asList(new File[] {
                DirectFlowCompiler.toLibraryPath(ValueOption.class),
                DirectFlowCompiler.toLibraryPath(Ex1.class),
        });
        TestInput<Ex1> in = tester.input(Ex1.class, "ex1");
        TestOutput<Ex1> out = tester.output(Ex1.class, "ex1");

        Ex1 ex1 = new Ex1();
        ex1.setSid(0);
        ex1.setValue(100);
        in.add(ex1);

        JobflowInfo info = DirectFlowCompiler.compile(
                tester.analyzeFlow(new UpdateFlowSimple(in.flow(), out.flow())),
                "simple",
                "simple",
                "com.example",
                Location.fromPath(HadoopDriver.RUNTIME_WORK_ROOT, '/'),
                tester.framework().getWork("build"),
                classpath,
                getClass().getClassLoader(),
                FlowCompilerOptions.load(System.getProperties()));
        assertThat(tester.run(info), is(true));

        List<Ex1> results = out.toList();
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getValue(), is(101));
    }

    /**
     * Mapper Fragmentが直列に並ぶ例のコンパイル。
     * @throws Exception コンパイル中に例外が発生した場合
     */
    @Test
    public void straightFragmentsCompile() throws Exception {
        List<File> classpath = Arrays.asList(new File[] {
                DirectFlowCompiler.toLibraryPath(ValueOption.class),
                DirectFlowCompiler.toLibraryPath(Ex1.class),
        });
        TestInput<Ex1> in = tester.input(Ex1.class, "ex1");
        TestOutput<Ex1> out = tester.output(Ex1.class, "ex1");

        Ex1 ex1 = new Ex1();
        ex1.setSid(0);
        ex1.setValue(0);
        in.add(ex1);
        ex1.setSid(1);
        ex1.setValue(100);
        in.add(ex1);

        JobflowInfo info = DirectFlowCompiler.compile(
                tester.analyzeFlow(new StraightFragments(in.flow(), out.flow())),
                "simple",
                "simple",
                "com.example",
                Location.fromPath(HadoopDriver.RUNTIME_WORK_ROOT, '/'),
                tester.framework().getWork("build"),
                classpath,
                getClass().getClassLoader(),
                FlowCompilerOptions.load(System.getProperties()));
        assertThat(tester.run(info), is(true));

        List<Ex1> results = out.toList(new Comparator<Ex1>() {
            @Override
            public int compare(Ex1 o1, Ex1 o2) {
                return o1.getSidOption().compareTo(o2.getSidOption());
            }
        });
        assertThat(results.size(), is(2));
        assertThat(results.get(0).getValue(), is(1));
        assertThat(results.get(1).getValue(), is(100));
    }

    /**
     * 分岐のある例のコンパイル。
     * @throws Exception コンパイル中に例外が発生した場合
     */
    @Test
    public void duplicateCompile() throws Exception {
        List<File> classpath = Arrays.asList(new File[] {
                DirectFlowCompiler.toLibraryPath(ValueOption.class),
                DirectFlowCompiler.toLibraryPath(Ex1.class),
        });
        TestInput<Ex1> in = tester.input(Ex1.class, "ex1");
        TestOutput<Ex1> out = tester.output(Ex1.class, "ex1");

        Ex1 ex1 = new Ex1();
        ex1.setStringAsString("Hello");
        ex1.setSid(0);
        ex1.setValue(100);
        in.add(ex1);

        JobflowInfo info = DirectFlowCompiler.compile(
                tester.analyzeFlow(new DuplicateFragments(in.flow(), out.flow())),
                "simple",
                "simple",
                "com.example",
                Location.fromPath(HadoopDriver.RUNTIME_WORK_ROOT, '/'),
                tester.framework().getWork("build"),
                classpath,
                getClass().getClassLoader(),
                FlowCompilerOptions.load(System.getProperties()));
        assertThat(tester.run(info), is(true));

        List<Ex1> results = out.toList();
        assertThat(results.size(), is(2));
        assertThat(results.get(0).getValue(), is(102));
        assertThat(results.get(1).getValue(), is(102));
    }

    /**
     * Reducer/Mapper Fragmentが直列に並ぶ例のコンパイル。
     * @throws Exception コンパイル中に例外が発生した場合
     */
    @Test
    public void straightRendezvousFragmentsCompile() throws Exception {
        List<File> classpath = Arrays.asList(new File[] {
                DirectFlowCompiler.toLibraryPath(ValueOption.class),
                DirectFlowCompiler.toLibraryPath(Ex1.class),
        });
        TestInput<Ex1> in = tester.input(Ex1.class, "ex1");
        TestOutput<Ex1> out = tester.output(Ex1.class, "ex1");

        Ex1 ex1 = new Ex1();
        ex1.setSid(0);
        ex1.setValue(10);
        in.add(ex1);
        ex1.setSid(1);
        ex1.setValue(20);
        in.add(ex1);

        JobflowInfo info = DirectFlowCompiler.compile(
                tester.analyzeFlow(new StraightRendezvousFragments(in.flow(), out.flow())),
                "simple",
                "simple",
                "com.example",
                Location.fromPath(HadoopDriver.RUNTIME_WORK_ROOT, '/'),
                tester.framework().getWork("build"),
                classpath,
                getClass().getClassLoader(),
                FlowCompilerOptions.load(System.getProperties()));
        assertThat(tester.run(info), is(true));

        List<Ex1> results = out.toList();
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getValue(), is(31));
    }

    /**
     * compile with a submodule.
     * @throws Exception if failed
     */
    @Test
    public void compileWithSubmoduleJar() throws Exception {
        List<File> classpath = Arrays.asList(new File[] {
                DirectFlowCompiler.toLibraryPath(ValueOption.class),
                DirectFlowCompiler.toLibraryPath(Ex1.class),
        });
        File jar = copy("submodule.jar");
        ClassLoader cl = new URLClassLoader(new URL[] { jar.toURI().toURL() }, getClass().getClassLoader());

        TestInput<Ex1> in = tester.input(Ex1.class, "ex1");
        TestOutput<Ex1> out = tester.output(Ex1.class, "ex1");

        JobflowInfo info = DirectFlowCompiler.compile(
                tester.analyzeFlow(new UpdateFlowSimple(in.flow(), out.flow())),
                "simple",
                "simple",
                "com.example",
                Location.fromPath(HadoopDriver.RUNTIME_WORK_ROOT, '/'),
                tester.framework().getWork("build"),
                classpath,
                cl,
                FlowCompilerOptions.load(System.getProperties()));

        File compiled = info.getPackageFile();
        load(compiled, "com.example.Submodule");
        find(compiled, "META-INF/other/info");
    }

    /**
     * compile with a submodule.
     * @throws Exception if failed
     */
    @Test
    public void compileWithSubmoduleFolder() throws Exception {
        List<File> classpath = Arrays.asList(new File[] {
                DirectFlowCompiler.toLibraryPath(ValueOption.class),
                DirectFlowCompiler.toLibraryPath(Ex1.class),
        });
        File jar = extract("submodule.jar");
        ClassLoader cl = new URLClassLoader(new URL[] { jar.toURI().toURL() }, getClass().getClassLoader());

        TestInput<Ex1> in = tester.input(Ex1.class, "ex1");
        TestOutput<Ex1> out = tester.output(Ex1.class, "ex1");

        JobflowInfo info = DirectFlowCompiler.compile(
                tester.analyzeFlow(new UpdateFlowSimple(in.flow(), out.flow())),
                "simple",
                "simple",
                "com.example",
                Location.fromPath(HadoopDriver.RUNTIME_WORK_ROOT, '/'),
                tester.framework().getWork("build"),
                classpath,
                cl,
                FlowCompilerOptions.load(System.getProperties()));

        File compiled = info.getPackageFile();
        load(compiled, "com.example.Submodule");
        find(compiled, "META-INF/other/info");
    }

    /**
     * compile with duplicated class libraries.
     * @throws Exception if failed
     */
    @Test
    public void compileWithSubmoduleDuplicated() throws Exception {
        File jar = copy("submodule.jar");
        List<File> classpath = Arrays.asList(new File[] {
                DirectFlowCompiler.toLibraryPath(ValueOption.class),
                DirectFlowCompiler.toLibraryPath(Ex1.class),
                DirectFlowCompiler.toLibraryPath(ValueOption.class),
                DirectFlowCompiler.toLibraryPath(Ex1.class),
                jar,
        });
        ClassLoader cl = new URLClassLoader(new URL[] { jar.toURI().toURL() }, getClass().getClassLoader());

        TestInput<Ex1> in = tester.input(Ex1.class, "ex1");
        TestOutput<Ex1> out = tester.output(Ex1.class, "ex1");

        JobflowInfo info = DirectFlowCompiler.compile(
                tester.analyzeFlow(new UpdateFlowSimple(in.flow(), out.flow())),
                "simple",
                "simple",
                "com.example",
                Location.fromPath(HadoopDriver.RUNTIME_WORK_ROOT, '/'),
                tester.framework().getWork("build"),
                classpath,
                cl,
                FlowCompilerOptions.load(System.getProperties()));

        File compiled = info.getPackageFile();
        load(compiled, "com.example.Submodule");
        find(compiled, "META-INF/other/info");
    }

    private Class<?> load(File file, String className) {
        try {
            URLClassLoader loader = new URLClassLoader(new URL[] { file.toURI().toURL() });
            return Class.forName(className, false, loader);
        } catch (MalformedURLException e) {
            Assume.assumeNoException(e);
            // may not occur
            throw new AssertionError(e);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private void find(File file, String path) {
        try {
            JarFile jar = new JarFile(file);
            try {
                assertThat(path, jar.getEntry(path), is(notNullValue()));
            } finally {
                jar.close();
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private File copy(String name) {
        InputStream input = open(name);
        try {
            try {
                File target = new File(tester.framework().getWork("temp"), name);
                tester.framework().dump(input, target);
                return target;
            } finally {
                input.close();
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private File extract(String name) {
        InputStream input = open(name);
        try {
            try {
                ZipInputStream zip = new ZipInputStream(input);
                File target = new File(tester.framework().getWork("temp"), name);
                tester.framework().extract(zip, target);
                zip.close();
                return target;
            } finally {
                input.close();
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private InputStream open(String name) {
        String path = getClass().getSimpleName() + ".files/" + name;
        InputStream input = getClass().getResourceAsStream(path);
        assertThat(path, input, not(nullValue()));
        return input;
    }
}
