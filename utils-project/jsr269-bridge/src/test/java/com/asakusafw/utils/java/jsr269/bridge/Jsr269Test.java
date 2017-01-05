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
package com.asakusafw.utils.java.jsr269.bridge;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.utils.java.jsr199.testing.VolatileCompiler;
import com.asakusafw.utils.java.jsr199.testing.VolatileJavaFile;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.WildcardBoundKind;
import com.asakusafw.utils.java.model.util.Models;

/**
 * Test for {@link Jsr269}.
 */
public class Jsr269Test {

    ModelFactory f;

    Jsr269 target;

    private VolatileCompiler compiler;

    /**
     * Initializes this test object.
     * @throws Exception if occur
     */
    @Before
    public void setUp() throws Exception {
        f = Models.getModelFactory();
        compiler = new VolatileCompiler();
        target = new Jsr269(f);
    }

    /**
     * Finalizes this test object.
     * @throws Exception if exception was occurred
     */
    @After
    public void tearDown() throws Exception {
        if (compiler != null) {
            compiler.close();
        }
    }

    /**
     * convert names.
     */
    @Test
    public void name() {
        start(new Callback() {
            @Override protected void test() {
                assertThat(
                    target.convert(elements.getName("something")),
                    is(Models.toName(f, "something")));
                assertThat(
                    target.convert(elements.getName("com.example.jsr269")),
                    is(Models.toName(f, "com.example.jsr269")));
            }
        });
    }

    /**
     * convert primitive types.
     */
    @Test
    public void primitiveTypes() {
        start(new Callback() {
            @Override protected void test() {
                assertThat(
                    target.convert((TypeMirror) types.getPrimitiveType(TypeKind.INT)),
                    is(Models.toType(f, int.class)));
                assertThat(
                    target.convert((TypeMirror) types.getPrimitiveType(TypeKind.LONG)),
                    is(Models.toType(f, long.class)));
                assertThat(
                    target.convert((TypeMirror) types.getPrimitiveType(TypeKind.FLOAT)),
                    is(Models.toType(f, float.class)));
                assertThat(
                    target.convert((TypeMirror) types.getPrimitiveType(TypeKind.DOUBLE)),
                    is(Models.toType(f, double.class)));
                assertThat(
                    target.convert((TypeMirror) types.getPrimitiveType(TypeKind.BYTE)),
                    is(Models.toType(f, byte.class)));
                assertThat(
                    target.convert((TypeMirror) types.getPrimitiveType(TypeKind.SHORT)),
                    is(Models.toType(f, short.class)));
                assertThat(
                    target.convert((TypeMirror) types.getPrimitiveType(TypeKind.CHAR)),
                    is(Models.toType(f, char.class)));
                assertThat(
                    target.convert((TypeMirror) types.getPrimitiveType(TypeKind.BOOLEAN)),
                    is(Models.toType(f, boolean.class)));
            }
        });
    }

    /**
     * convert declared types.
     */
    @Test
    public void declaredType() {
        start(new Callback() {
            @Override protected void test() {
                assertThat(
                    target.convert(getType(Object.class)),
                    is(Models.toType(f, Object.class)));
                assertThat(
                    target.convert(getType(String.class)),
                    is(Models.toType(f, String.class)));
                assertThat(
                    target.convert(getType(List.class)),
                    is(Models.toType(f, List.class)));
            }
        });
    }

    /**
     * convert parameterized types.
     */
    @Test
    public void parameterizedType() {
        start(new Callback() {
            @Override protected void test() {
                assertThat(
                    target.convert(getType(List.class, getType(String.class))),
                    is((Type) f.newParameterizedType(
                        Models.toType(f, List.class),
                        Arrays.asList(new Type[] {
                            Models.toType(f, String.class)
                        }))));
                assertThat(
                    target.convert(getType(
                        Map.class,
                        getType(Integer.class), getType(Object.class))),
                    is((Type) f.newParameterizedType(
                        Models.toType(f, Map.class),
                        Arrays.asList(new Type[] {
                            Models.toType(f, Integer.class),
                            Models.toType(f, Object.class),
                        }))));
            }
        });
    }

    /**
     * convert non types.
     */
    @Test
    public void noType() {
        start(new Callback() {
            @Override protected void test() {
                assertThat(
                    target.convert((TypeMirror) types.getNoType(TypeKind.VOID)),
                    is(Models.toType(f, void.class)));
                assertThat(
                    target.convert((TypeMirror) types.getNoType(TypeKind.NONE)),
                    is(nullValue()));
            }
        });
    }

    /**
     * convert type variables.
     */
    @Test
    public void typeVariable() {
        start(new Callback() {
            @Override protected void test() {
                TypeElement map = elements.getTypeElement(Map.class.getName());
                TypeParameterElement k = map.getTypeParameters().get(0);
                assertThat(
                    target.convert(k.asType()),
                    is((Type) f.newNamedType(f.newSimpleName("K"))));

                TypeElement list = elements.getTypeElement(List.class.getName());
                TypeParameterElement e = list.getTypeParameters().get(0);
                assertThat(
                    target.convert(e.asType()),
                    is((Type) f.newNamedType(f.newSimpleName("E"))));
            }
        });
    }

    /**
     * convert array types.
     */
    @Test
    public void arrayTypes() {
        start(new Callback() {
            @Override protected void test() {
                assertThat(
                    target.convert((TypeMirror) types.getArrayType(
                        types.getPrimitiveType(TypeKind.INT))),
                    is(Models.toType(f, int[].class)));
                assertThat(
                    target.convert((TypeMirror) types.getArrayType(
                        types.getArrayType(
                            getType(String.class)))),
                    is(Models.toType(f, String[][].class)));
            }
        });
    }

    /**
     * convert wildcards.
     */
    @Test
    public void wildcard() {
        start(new Callback() {
            @Override protected void test() {
                assertThat(
                    target.convert((TypeMirror) types.getWildcardType(null, null)),
                    is((Type) f.newWildcard(WildcardBoundKind.UNBOUNDED, null)));
                assertThat(
                    target.convert((TypeMirror) types.getWildcardType(
                        getType(CharSequence.class), null)),
                    is((Type) f.newWildcard(WildcardBoundKind.UPPER_BOUNDED,
                        Models.toType(f, CharSequence.class))));
                assertThat(
                    target.convert((TypeMirror) types.getWildcardType(
                        null, getType(CharSequence.class))),
                    is((Type) f.newWildcard(WildcardBoundKind.LOWER_BOUNDED,
                        Models.toType(f, CharSequence.class))));
            }
        });
    }

    private void start(Callback callback, JavaFileObject... sources) {
        for (JavaFileObject java : sources) {
            compiler.addSource(java);
        }
        if (sources.length == 0) {
            compiler.addSource(new VolatileJavaFile("A", "public class A {}"));
        }
        compiler.addProcessor(new DelegateProcessor(callback));
        List<Diagnostic<? extends JavaFileObject>> diagnostics = compiler.doCompile();
        for (Diagnostic<?> d : diagnostics) {
            if (d.getKind() == Diagnostic.Kind.ERROR) {
                throw new AssertionError(diagnostics);
            }
        }
        callback.rethrow();
    }
}
