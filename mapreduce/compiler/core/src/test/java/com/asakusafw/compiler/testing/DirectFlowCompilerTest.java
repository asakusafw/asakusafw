/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipInputStream;

import org.junit.Assume;
import org.junit.ClassRule;
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
import com.asakusafw.compiler.util.tester.CompilerTester.TestInput;
import com.asakusafw.compiler.util.tester.CompilerTester.TestOutput;
import com.asakusafw.compiler.util.tester.HadoopDriver;
import com.asakusafw.runtime.value.ValueOption;
import com.asakusafw.runtime.windows.WindowsSupport;

/**
 * Test for {@link DirectFlowCompiler}.
 */
public class DirectFlowCompilerTest {

    /**
     * Windows platform support.
     */
    @ClassRule
    public static final WindowsSupport WINDOWS_SUPPORT = new WindowsSupport();

    /**
     * A test helper.
     */
    @Rule
    public CompilerTester tester = new CompilerTester();

    /**
     * compute library path from Class object.
     * @throws Exception if exception was occurred
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
     * compute library path from Class object - the path includes white-spaces.
     * @throws Exception if exception was occurred
     */
    @Test
    public void folderLibraryPathWithSpace() throws Exception {
        File file = extract("example.jar", "example w space");
        Class<?> aClass = load(file, "com.example.Hello");
        File library = DirectFlowCompiler.toLibraryPath(aClass);
        assertThat(library, not(nullValue()));
        assertThat(file.getCanonicalFile(), is(library.getCanonicalFile()));
    }

    /**
     * compute library path from Class object - the class is not top-level.
     * @throws Exception if exception was occurred
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
     * compute library path from Class object.
     * @throws Exception if exception was occurred
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
     * compute library path from Class object - the class is not top-level.
     * @throws Exception if exception was occurred
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
     * compute library path from Class object - the file is ZIP.
     * @throws Exception if exception was occurred
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
     * compiles simple flow.
     * @throws Exception if exception was occurred
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
     * compiles sequential map fragments.
     * @throws Exception if exception was occurred
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

        List<Ex1> results = out.toList((o1, o2) -> o1.getSidOption().compareTo(o2.getSidOption()));
        assertThat(results.size(), is(2));
        assertThat(results.get(0).getValue(), is(1));
        assertThat(results.get(1).getValue(), is(100));
    }

    /**
     * compiles flow - an operator appears twice or more.
     * @throws Exception if exception was occurred
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
     * compiles reduce-map fragments.
     * @throws Exception if exception was occurred
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
        try (JarFile jar = new JarFile(file)) {
            assertThat(path, jar.getEntry(path), is(notNullValue()));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private File copy(String name) {
        try (InputStream input = open(name)){
            File target = new File(tester.framework().getWork("temp"), name);
            tester.framework().dump(input, target);
            return target;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private File extract(String name) {
        return extract(name, name);
    }

    private File extract(String source, String into) {
        try (InputStream input = open(source);
                ZipInputStream zip = new ZipInputStream(input)) {
            File target = new File(tester.framework().getWork("temp"), into);
            tester.framework().extract(zip, target);
            return target;
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
