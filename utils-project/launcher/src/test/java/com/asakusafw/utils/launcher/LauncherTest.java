/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.utils.launcher;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.utils.launcher.ClassLoaderContext;
import com.asakusafw.utils.launcher.Launcher;
import com.asakusafw.utils.launcher.LauncherException;

/**
 * Creates a new instance.
 */
public class LauncherTest {

    private static final String DIR_SOURCE = "src";

    private static final String DIR_OBJECT = "bin";

    private static final Charset ENCODING = StandardCharsets.UTF_8;

    /**
     * Temporary folder for testing.
     */
    @Rule
    public final TemporaryFolder temporary = new TemporaryFolder();

    /**
     * simple case.
     * @throws Throwable if failed
     */
    @Test
    public void simple() throws Throwable {
        String main = java("com.example.Hello", new String[] {
                "public class Hello {",
                "  public static void main(String[] args) {",
                "    System.out.println(\"Hello, launcher!\");",
                "    assertThat(Arrays.asList(args), empty());",
                "  }",
                "}",
        });
        File script = script(main);
        Launcher.main(script.getAbsolutePath());
    }

    /**
     * simple case.
     * @throws Throwable if failed
     */
    @Test
    public void arguments() throws Throwable {
        String main = java("com.example.Hello", new String[] {
                "public class Hello {",
                "  public static void main(String[] args) {",
                "    assertThat(Arrays.asList(args), contains(\"A\", \"B\", \"C\"));",
                "  }",
                "}",
        });
        File script = script(main, "A", "B", "C");
        Launcher.main(script.getAbsolutePath());
    }

    /**
     * raise exception in the target application.
     * @throws Throwable if failed
     */
    @Test(expected = UnsupportedOperationException.class)
    public void raise_exception() throws Throwable {
        String main = java("com.example.Hello", new String[] {
                "public class Hello {",
                "  public static void main(String[] args) {",
                "    throw new UnsupportedOperationException();",
                "  }",
                "}",
        });
        File script = script(main);
        Launcher.main(script.getAbsolutePath());
    }

    /**
     * check class loader.
     * @throws Throwable if failed
     */
    @Test
    public void check_class_loader() throws Throwable {
        String main = java("com.example.Check", new String[] {
                "public class Check {",
                "  public static void main(String[] args) {",
                "    assertThat(ClassLoader.getSystemClassLoader(),",
                "      is(not(Thread.currentThread().getContextClassLoader())));",
                "  }",
                "}",
        });
        File script = script(main);
        try (ClassLoaderContext context = new ClassLoaderContext(ClassLoader.getSystemClassLoader())) {
            Launcher.main(script.getAbsolutePath());
            assertThat(Thread.currentThread().getContextClassLoader(), is(context.active));
        }
    }

    /**
     * missing script.
     * @throws Throwable if failed
     */
    @Test(expected = LauncherException.class)
    public void invalid_missing_script() throws Throwable {
        Launcher.main(new File(temporary.getRoot(), "MISSING.properties").getPath());
    }

    /**
     * w/ empty script.
     * @throws Throwable if failed
     */
    @Test(expected = LauncherException.class)
    public void invalid_empty_script() throws Throwable {
        Launcher.main(temporary.newFile().getPath());
    }

    /**
     * missing class.
     * @throws Throwable if failed
     */
    @Test(expected = LauncherException.class)
    public void invalid_missing_class() throws Throwable {
        java("com.example.Hello", new String[] {
                "public class Hello {",
                "  public static void main(String[] args) {}",
                "}",
        });
        File script = script("com.example.MISSING");
        Launcher.main(script.getAbsolutePath());
    }

    /**
     * missing main method.
     * @throws Throwable if failed
     */
    @Test(expected = LauncherException.class)
    public void invalid_missing_main() throws Throwable {
        String main = java("com.example.Hello", new String[] {
                "public class Hello {",
                "}",
        });
        File script = script(main);
        Launcher.main(script.getAbsolutePath());
    }

    String java(String className, String[] sourceLines) {
        File src = new File(temporary.getRoot(), DIR_SOURCE);
        File file = new File(src, className.replace('.', '/') + ".java");
        File parent = file.getParentFile();
        assertThat(parent.mkdirs() || parent.isDirectory(), is(true));

        int lastDot = className.lastIndexOf('.');
        String packageName = className.substring(0, lastDot);

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), ENCODING))) {
            writer.printf("package %s;%n", packageName);
            writer.println("import static org.junit.Assert.*;");
            writer.println("import static org.hamcrest.Matchers.*;");
            writer.println("import java.util.*;");
            for (String s : sourceLines) {
                writer.println(s);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return className;
    }

    private File script(String main, String... args) {
        try {
            File source = new File(temporary.getRoot(), DIR_SOURCE);
            File classes = new File(temporary.getRoot(), DIR_OBJECT);
            assertThat(classes.mkdirs() || classes.isDirectory(), is(true));
            compile(source, classes);
            Properties script = new Properties();
            script.setProperty(Launcher.KEY_MAIN_CLASS, main);
            script.setProperty(Launcher.KEY_CLASSPATH_PREFIX + 0, classes.getAbsolutePath());
            for (int i = 0; i < args.length; i++) {
                script.setProperty(Launcher.KEY_ARGUMENT_PREFIX + i, args[i]);
            }

            File result = temporary.newFile();
            try (OutputStream out = new FileOutputStream(result)) {
                script.store(out, null);
            }
            return result;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void compile(File source, File classes) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Assume.assumeNotNull(compiler);
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager files = compiler.getStandardFileManager(
                diagnostics, Locale.getDefault(), ENCODING)) {
            files.setLocation(StandardLocation.SOURCE_PATH, Arrays.asList(source));
            files.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(classes));

            List<String> arguments = new ArrayList<>();
            Collections.addAll(arguments, "-encoding", ENCODING.name());

            List<File> sources = new ArrayList<>();
            collectSourceFiles(sources, source);

            StringWriter errors = new StringWriter();
            boolean success;
            try (PrintWriter pw = new PrintWriter(errors)) {
                CompilationTask task = compiler.getTask(
                        pw,
                        files,
                        diagnostics,
                        arguments,
                        Collections.<String>emptyList(),
                        files.getJavaFileObjectsFromFiles(sources));
                success = task.call();
            }
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                switch (diagnostic.getKind()) {
                case ERROR:
                case MANDATORY_WARNING:
                    throw new AssertionError(diagnostic);
                default:
                    System.out.println(diagnostic);
                    break;
                }
            }
            assertThat(success, is(true));
        }
    }

    private void collectSourceFiles(List<File> sink, File file) {
        String name = file.getName();
        if (name.startsWith(".")) {
            return;
        }
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                collectSourceFiles(sink, child);
            }
        } else if (file.isFile()) {
            if (name.endsWith(".java")) {
                sink.add(file);
            }
        }
    }
}
