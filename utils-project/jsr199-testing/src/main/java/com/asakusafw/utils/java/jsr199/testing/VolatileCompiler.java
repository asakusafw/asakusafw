/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
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
 * Compiles Java source programs and generates source/resource/class files onto the heap.
 * @see VolatileJavaFile
 * @see VolatileResourceFile
 * @see VolatileClassFile
 */
public class VolatileCompiler implements Closeable {

    private static final String JAVA_VERSION = "1.8"; //$NON-NLS-1$

    private static final String ENCODING = StandardCharsets.UTF_8.name();

    private final JavaCompiler compiler;

    private final VolatileClassOutputManager files;

    private final List<String> arguments;

    private final List<JavaFileObject> targets;

    private final List<Processor> processors;

    /**
     * Creates a new instance.
     * @throws IllegalStateException if the Java compiler is not available
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
                StandardCharsets.UTF_8));
        this.arguments = new ArrayList<>();
        this.targets = new ArrayList<>();
        this.processors = new ArrayList<>();

        Collections.addAll(arguments, "-source", JAVA_VERSION); //$NON-NLS-1$
        Collections.addAll(arguments, "-target", JAVA_VERSION); //$NON-NLS-1$
        Collections.addAll(arguments, "-encoding", ENCODING); //$NON-NLS-1$
    }

    /**
     * Adds a Java source program.
     * @param java the target source file
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public VolatileCompiler addSource(JavaFileObject java) {
        if (java == null) {
            throw new IllegalArgumentException("java must not be null"); //$NON-NLS-1$
        }
        targets.add(java);
        return this;
    }

    /**
     * Adds an annotation processor.
     * @param processor the target annotation processor
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public VolatileCompiler addProcessor(Processor processor) {
        if (processor == null) {
            throw new IllegalArgumentException("processor must not be null"); //$NON-NLS-1$
        }
        processors.add(processor);
        return this;
    }

    /**
     * Clears the {@link #addArguments(String...) registered compiler arguments}.
     * Note that, this also removes preset following compiler arguments:
     * <ul>
     * <li> {@code -source} </li>
     * <li> {@code -target} </li>
     * <li> {@code -encoding} </li>
     * </ul>
     * @return this
     */
    public VolatileCompiler resetArguments() {
        arguments.clear();
        return this;
    }

    /**
     * Adds compiler arguments.
     * Initially the compiler has the following arguments:
     * <ul>
     * <li> {@code -source 1.8} </li>
     * <li> {@code -target 1.8} </li>
     * <li> {@code -encoding UTF-8} </li>
     * </ul>
     * @param compilerArguments the compiler arguments to add
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
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
     * Compiles the {@code #addSource(JavaFileObject) added source files} and returns its diagnostics objects.
     * @return the diagnostic objects
     */
    public List<Diagnostic<? extends JavaFileObject>> doCompile() {
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        CompilationTask task = compiler.getTask(
            new PrintWriter(new OutputStreamWriter(System.err, Charset.defaultCharset()), true),
            files,
            collector,
            arguments,
            Collections.emptyList(),
            targets);
        task.setProcessors(processors);
        task.call();
        return collector.getDiagnostics();
    }

    /**
     * Returns the class loader which provides compiled classes by the compiler.
     * @return the class loader for loading the compilation results
     */
    public ClassLoader getClassLoader() {
        DirectClassLoader loader = AccessController.doPrivileged((PrivilegedAction<DirectClassLoader>) () ->
                new DirectClassLoader(VolatileCompiler.this.getClass().getClassLoader()));
        loader.setDefaultAssertionStatus(true);
        for (VolatileClassFile klass : files.getCompiled()) {
            loader.add(klass.getBinaryName(), klass.getBinaryContent());
        }
        return loader;
    }

    /**
     * Returns the Java source files which are generated by this compiler.
     * @return the generated source files
     */
    public Collection<VolatileJavaFile> getSources() {
        return files.getSources();
    }

    /**
     * Returns the resource files which are generated by this compiler.
     * @return the generated resource files
     */
    public Collection<VolatileResourceFile> getResources() {
        return files.getResources();
    }

    /**
     * Returns the class files which are generated by this compiler.
     * @return the generated class files
     */
    public Collection<VolatileClassFile> getCompiled() {
        return files.getCompiled();
    }

    @Override
    public void close() throws IOException {
        files.close();
    }
}
