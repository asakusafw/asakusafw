/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.dmdl.java;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TestName;

import com.asakusafw.dmdl.java.emitter.CompositeDataModelDriver;
import com.asakusafw.dmdl.java.emitter.NameConstants;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.java.util.JavaName;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.source.DmdlSourceRepository;
import com.asakusafw.dmdl.source.DmdlSourceResource;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.value.ValueOption;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.jsr199.testing.VolatileCompiler;
import com.asakusafw.utils.java.jsr199.testing.VolatileJavaFile;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.Models;

/**
 * Testing utilities for this project.
 */
public class GeneratorTesterRoot {

    /**
     * a test name handler.
     */
    @Rule
    public TestName currentTestName = new TestName();

    /**
     * Current compiler.
     */
    protected final VolatileCompiler compiler = new VolatileCompiler();

    /**
     * {@link JavaDataModelDriver}s.
     */
    protected final List<JavaDataModelDriver> emitDrivers = Lists.create();

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        compiler.close();
    }

    /**
     * Generate Java model classes from context DMDL and returns compile and load results.
     * @return generated classes
     */
    protected ModelLoader generate() {
        return generate(currentTestName.getMethodName());
    }

    /**
     * Generate Java model classes from specified DMDL and returns compile and load results.
     * @param name DMDL name
     * @return generated classes
     */
    protected ModelLoader generate(String name) {
        List<VolatileJavaFile> files = emit(name);
        ClassLoader loaded = compile(files);
        return new ModelLoader(loaded);
    }

    private ClassLoader compile(List<VolatileJavaFile> files) {
        if (files.isEmpty()) {
            throw new AssertionError();
        }
        for (JavaFileObject java : files) {
            try {
                System.out.println("=== " + java.getName());
                System.out.println(java.getCharContent(true));
                System.out.println();
                System.out.println();
            } catch (IOException e) {
                // ignored
            }
            compiler.addSource(java);
        }
        compiler.addArguments("-Xlint");
        List<Diagnostic<? extends JavaFileObject>> diagnostics = compiler.doCompile();
        boolean hasWrong = false;
        for (Diagnostic<?> d : diagnostics) {
            if (d.getKind() == Diagnostic.Kind.ERROR || d.getKind() == Diagnostic.Kind.WARNING) {
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

    private List<VolatileJavaFile> emit(String name) {
        ModelFactory factory = Models.getModelFactory();
        DmdlSourceRepository source = collectInput(name);
        VolatileEmitter emitter = new VolatileEmitter();
        Configuration conf = new Configuration(
                factory,
                source,
                Models.toName(factory, "com.example"),
                emitter,
                getClass().getClassLoader(),
                Locale.getDefault());

        GenerateTask task = new GenerateTask(conf);
        try {
            task.process(new CompositeDataModelDriver(emitDrivers));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return emitter.getEmitted();
    }

    private DmdlSourceRepository collectInput(String name) {
        URL url = getClass().getResource(name + ".txt");
        assertThat(currentTestName.getMethodName(), url, not(nullValue()));
        return new DmdlSourceResource(Collections.singletonList(url), Charset.forName("UTF-8"));
    }


    /**
     * Generated data model loader for testing.
     */
    protected static class ModelLoader {

        private final ClassLoader classLoader;

        private String namespace;

        ModelLoader(ClassLoader loaded) {
            assert loaded != null;
            this.classLoader = loaded;
            this.namespace = NameConstants.DEFAULT_NAMESPACE;
        }

        /**
         * Sets the class namespace and category.
         * @param namespace the namespace
         * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
         */
        public final void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        /**
         * Load a model type.
         * @param name the simple name of target class
         * @return the loaded type
         * @see #setNamespace(String)
         */
        public Class<?> modelType(String name) {
            return type(NameConstants.CATEGORY_DATA_MODEL, name);
        }

        /**
         * Creates a new model instance and wrap it.
         * @param name the simple name of target class
         * @return the wrapped instance
         * @see #setNamespace(String)
         */
        public ModelWrapper newModel(String name) {
            try {
                Class<?> loaded = modelType(name);
                Object instance = loaded.newInstance();
                return new ModelWrapper(instance);
            }
            catch (Exception e) {
                throw new AssertionError(e);
            }
        }

        /**
         * Creates a new object.
         * @param category the category name
         * @param name the simple name of target class
         * @return the created instance
         * @see #setNamespace(String)
         */
        public Object newObject(String category, String name) {
            try {
                Class<?> loaded = type(category, name);
                Object instance = loaded.newInstance();
                return instance;
            }
            catch (Exception e) {
                throw new AssertionError(e);
            }
        }

        private Class<?> type(String category, String name) {
            try {
                return classLoader.loadClass(MessageFormat.format(
                        "{0}.{1}.{2}.{3}",
                        "com.example",
                        namespace,
                        category,
                        name));
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        }
    }

    /**
     * DataModel class instance.
     */
    @SuppressWarnings("rawtypes")
    protected static class ModelWrapper {

        private final DataModel instance;

        private Class<?> interfaceType;

        ModelWrapper(Object instance) {
            this.instance = (DataModel) instance;
            this.interfaceType = instance.getClass();
        }

        /**
         * Returns the wrapped object.
         * @return the wrapped object
         */
        public Object unwrap() {
            return instance;
        }

        /**
         * Sets interface type of wrapped object.
         * @param interfaceType the interface type to set
         */
        public void setInterfaceType(Class<?> interfaceType) {
            this.interfaceType = interfaceType;
        }

        /**
         * Invokes is~.
         * @param name the property name
         * @return the result
         */
        public boolean is(String name) {
            JavaName jn = JavaName.of(new AstSimpleName(null, name));
            jn.addFirst("is");
            Object result = invoke(jn.toMemberName());
            return (Boolean) result;
        }

        /**
         * Invokes get~.
         * @param name the property name
         * @return the result
         */
        public Object get(String name) {
            JavaName jn = JavaName.of(new AstSimpleName(null, name));
            jn.addFirst("get");
            return invoke(jn.toMemberName());
        }

        /**
         * Invokes set~.
         * @param name the property name
         * @param value the value to set
         */
        public void set(String name, Object value) {
            JavaName jn = JavaName.of(new AstSimpleName(null, name));
            jn.addFirst("set");
            invoke(jn.toMemberName(), value);
        }

        /**
         * Invokes get~Option.
         * @param name the property name
         * @return the result
         */
        public ValueOption<?> getOption(String name) {
            JavaName jn = JavaName.of(new AstSimpleName(null, name));
            jn.addFirst("get");
            jn.addLast("option");
            return (ValueOption<?>) invoke(jn.toMemberName());
        }

        /**
         * Invokes set~Option.
         * @param name the property name
         * @param option the value to set
         */
        public void setOption(String name, ValueOption<?> option) {
            JavaName jn = JavaName.of(new AstSimpleName(null, name));
            jn.addFirst("set");
            jn.addLast("option");
            invoke(jn.toMemberName(), option);
        }

        /**
         * Invokes reset().
         */
        public void reset() {
            instance.reset();
        }

        /**
         * Invokes copyFrom.
         * @param wrapper source wrapper
         */
        @SuppressWarnings("unchecked")
        public void copyFrom(ModelWrapper wrapper) {
            instance.copyFrom(wrapper.instance);
        }

        /**
         * Invokes any method declared in the data model class.
         * @param name the method name
         * @param arguments the arguments
         * @return the result
         */
        public Object invoke(String name, Object... arguments) {
            for (Method method : interfaceType.getMethods()) {
                if (method.getName().equals(name)) {
                    try {
                        return method.invoke(instance, arguments);
                    } catch (Exception e) {
                        throw new AssertionError(e);
                    }
                }
            }
            throw new AssertionError(name);
        }
    }
}
