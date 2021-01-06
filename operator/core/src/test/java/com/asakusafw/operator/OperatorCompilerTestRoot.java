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
package com.asakusafw.operator;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.ServiceLoader;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.junit.After;

import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.flowpart.FlowPartAnnotationProcessor;
import com.asakusafw.operator.method.OperatorAnnotationProcessor;
import com.asakusafw.operator.util.Logger;
import com.asakusafw.utils.java.jsr199.testing.SafeProcessor;
import com.asakusafw.utils.java.jsr199.testing.VolatileCompiler;
import com.asakusafw.utils.java.jsr199.testing.VolatileJavaFile;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.Models;

/**
 * Test helper for operator compilers.
 */
public class OperatorCompilerTestRoot {

    static final Logger LOG = Logger.get(OperatorCompilerTestRoot.class);

    final ModelFactory f = Models.getModelFactory();

    private final VolatileCompiler compiler = new VolatileCompiler();

    private final List<JavaFileObject> sources = new ArrayList<>();

    /**
     * operator drivers.
     */
    public final List<OperatorDriver> operatorDrivers = new ArrayList<>();

    /**
     * data model mirrors.
     */
    public final List<DataModelMirrorRepository> dataModelMirrors = new ArrayList<>();

    /**
     * Dumps generated source files.
     */
    protected boolean dump = true;

    /**
     * Disposes compiler.
     * @throws Exception if failed.
     */
    @After
    public void tearDown() throws Exception {
        compiler.close();
    }

    /**
     * Loads a class with the specified class name.
     * @param loader target class loader
     * @param name class name
     * @return the loaded class
     */
    protected Class<?> load(ClassLoader loader, String name) {
        try {
            return loader.loadClass(name);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates an object with the specified class name.
     * @param loader target class loader
     * @param aClass the target class
     * @return the generated instance
     */
    protected Object create(ClassLoader loader, ClassDescription aClass) {
        try {
            Class<?> loaded = loader.loadClass(aClass.getBinaryName());
            return loaded.newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the specified method.
     * @param aClass target class
     * @param name method name
     * @param parameterTypes parameter types
     * @return the specified method, or {@code null} if it does not exist
     */
    protected Method method(Class<?> aClass, String name, Class<?>... parameterTypes) {
        try {
            return aClass.getMethod(name, parameterTypes);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the specified field.
     * @param aClass target class
     * @param name field name
     * @return the specified field, or {@code null} if it does not exist
     */
    protected Field field(Class<?> aClass, String name) {
        try {
            return aClass.getField(name);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the result of method invocation.
     * @param object target object
     * @param name method name
     * @param arguments method arguments
     * @return invocation result
     */
    protected Object invoke(Object object, String name, Object... arguments) {
        try {
            for (Method method : object.getClass().getMethods()) {
                if (method.getName().equals(name)
                        && method.getParameterTypes().length == arguments.length) {
                    return method.invoke(object, arguments);
                }
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        throw new AssertionError(name);
    }

    /**
     * Returns the content of target field.
     * @param object target object
     * @param name field name
     * @return content
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
     * Add a new file content.
     * @param name target file name
     * @param contents target file content
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void add(String name, String contents) {
        sources.add(new VolatileJavaFile(name, contents));
    }

    /**
     * Adds a new file in {@code <class-name>.files/<name>.java.txt}.
     * @param name target file name prefix
     */
    public void add(String name) {
        Class<?> aClass = getClass();
        ClassDescription desc = new ClassDescription(name);
        String file = MessageFormat.format(
                "{0}.files/{1}.java.txt",
                aClass.getSimpleName(),
                desc.getInternalName());
        StringBuilder buf = new StringBuilder();
        try (InputStream in = aClass.getResourceAsStream(file)) {
            assertThat(file, in, not(nullValue()));
            try (Scanner s = new Scanner(in, "UTF-8")) {
                while (s.hasNextLine()) {
                    String line = s.nextLine();
                    buf.append(line
                            .replaceAll("\\$s\\b", desc.getSimpleName())
                            .replaceAll("\\$p\\b", desc.getPackageName()));
                    buf.append("\n");
                }
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        sources.add(new VolatileJavaFile(desc.getInternalName(), buf.toString()));
    }

    /**
     * Adds {@link DataModelMirrorRepository} objects from SPI.
     * @param loader target class loader
     */
    public void addSpiDataModelMirrorRepositories(ClassLoader loader) {
        for (DataModelMirrorRepository repo : ServiceLoader.load(DataModelMirrorRepository.class, loader)) {
            dataModelMirrors.add(repo);
        }
    }

    /**
     * Adds an operator driver for next compilation.
     * @param driver added operator driver
     */
    public void add(OperatorDriver driver) {
        operatorDrivers.add(driver);
    }

    /**
     * Adds a data model mirror repository for next compilation.
     * @param repository a data model mirror repository
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void add(DataModelMirrorRepository repository) {
        dataModelMirrors.add(repository);
    }

    /**
     * Creates an operator annotation processor using current settings.
     * @return an operator annotation processor
     */
    protected Processor operatorProcessor() {
        return new OperatorAnnotationProcessor() {
            @Override
            protected CompileEnvironment createCompileEnvironment(ProcessingEnvironment processingEnv) {
                return new CompileEnvironment(processingEnv, operatorDrivers, dataModelMirrors);
            }
        };
    }

    /**
     * Creates an flow-part annotation processor using current settings.
     * @return an flow-part annotation processor
     */
    protected Processor flowPartProcessor() {
        return new FlowPartAnnotationProcessor() {
            @Override
            protected CompileEnvironment createCompileEnvironment(ProcessingEnvironment processingEnv) {
                return new CompileEnvironment(processingEnv, operatorDrivers, dataModelMirrors);
            }
        };
    }

    /**
     * Starts compilation using the specified annotation processor.
     * @param processor annotation processor
     * @return compilation result
     */
    protected ClassLoader start(Processor processor) {
        SafeProcessor safe = new SafeProcessor(processor);
        compiler.addProcessor(safe);
        ClassLoader loader = compile();
        safe.rethrow();
        return loader;
    }

    /**
     * Starts compilation using the specified callback object.
     * @param callback target callback
     * @return compilation result
     */
    protected ClassLoader start(Callback callback) {
        compiler.addProcessor(new DelegateProcessor(callback));
        ClassLoader loader = compile();
        callback.rethrow();
        return loader;
    }

    /**
     * Requires fail for compilation using the specified annotation processor.
     * @param processor target annotation processor
     */
    protected void error(Processor processor) {
        SafeProcessor safe = new SafeProcessor(processor);
        compiler.addProcessor(safe);
        List<Diagnostic<? extends JavaFileObject>> diagnostics = doCompile();
        safe.rethrow();
        assertThat(diagnostics.isEmpty(), is(false));
    }

    /**
     * Requires fail for compilation using the specified callback object.
     * @param callback target callback object
     */
    protected void error(Callback callback) {
        compiler.addProcessor(new DelegateProcessor(callback));
        List<Diagnostic<? extends JavaFileObject>> diagnostics = doCompile();
        callback.rethrow();
        assertThat("must have errors", diagnostics.isEmpty(), is(false));
    }

    private ClassLoader compile() {
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
