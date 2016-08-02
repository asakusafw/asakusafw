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
package com.asakusafw.operator.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.Collections;
import java.util.concurrent.Callable;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.junit.Test;

import com.asakusafw.operator.Callback;
import com.asakusafw.operator.OperatorCompilerTestRoot;
import com.asakusafw.operator.description.AnnotationDescription;
import com.asakusafw.operator.description.ArrayDescription;
import com.asakusafw.operator.description.BasicTypeDescription;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.description.TypeDescription;
import com.asakusafw.operator.description.ValueDescription;
import com.asakusafw.operator.description.BasicTypeDescription.BasicTypeKind;
import com.asakusafw.operator.mock.MockMarker;
import com.asakusafw.operator.mock.MockNested;
import com.asakusafw.operator.mock.MockSingleElement;
import com.asakusafw.operator.util.DescriptionHelper;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Test for {@link DescriptionHelper}.
 */
public class DescriptionHelperTest extends OperatorCompilerTestRoot {

    /**
     * object types to description.
     */
    @Test
    public void to_description_object() {
        start(new Callback(true) {
            @Override
            protected void test() {
                TypeMirror type = getType(String.class);
                TypeDescription desc = DescriptionHelper.toDescription(env, type);
                assertThat(desc, is((Object) Descriptions.typeOf(String.class)));
            }
        });
    }

    /**
     * array types to description.
     */
    @Test
    public void to_description_array() {
        start(new Callback() {
            @Override
            protected void test() {
                TypeMirror type = types.getArrayType(getType(String.class));
                TypeDescription desc = DescriptionHelper.toDescription(env, type);
                assertThat(desc, is((Object) Descriptions.typeOf(String[].class)));
            }
        });
    }

    /**
     * basic types to description.
     */
    @Test
    public void to_description_basic() {
        start(new Callback(true) {
            @Override
            protected void test() {
                for (BasicTypeKind kind : BasicTypeKind.values()) {
                    TypeMirror type;
                    if (kind == BasicTypeKind.VOID) {
                        type = types.getNoType(TypeKind.VOID);
                    } else {
                        type = types.getPrimitiveType(TypeKind.valueOf(kind.name()));
                    }
                    assertThat(
                            DescriptionHelper.toDescription(env, type),
                            is((TypeDescription) new BasicTypeDescription(kind)));
                }
            }
        });
    }

    /**
     * resolve basic type descriptions.
     */
    @Test
    public void resolve_basic() {
        assertThat(resolve(Descriptions.typeOf(int.class)), equalTo((Object) int.class));
    }

    /**
     * resolve object type descriptions.
     */
    @Test
    public void resolve_object() {
        assertThat(resolve(Descriptions.typeOf(String.class)), equalTo((Object) String.class));
    }

    /**
     * resolve array type descriptions.
     */
    @Test
    public void resolve_array() {
        assertThat(resolve(Descriptions.typeOf(String[][].class)), equalTo((Object) String[][].class));
    }

    /**
     * resolve immediate value.
     */
    @Test
    public void constant_immediate() {
        assertThat(constant(Descriptions.valueOf("Hello, world!")), equalTo((Object) "Hello, world!"));
    }

    /**
     * resolve enum constant value.
     */
    @Test
    public void constant_enum() {
        assertThat(constant(Descriptions.valueOf(ElementType.TYPE)), equalTo((Object) ElementType.TYPE));
    }

    /**
     * resolve class literal.
     */
    @Test
    public void constant_type() {
        assertThat(constant(Descriptions.valueOf(int.class)), equalTo((Object) int.class));
    }

    /**
     * resolve array.
     */
    @Test
    public void constant_array() {
        int[] array = { 1, 2, 3 };
        assertThat(constant(Descriptions.valueOf(array)), equalTo((Object) array));
    }

    /**
     * resolve null.
     */
    @Test
    public void constant_null() {
        assertThat(constant(Descriptions.valueOf(null)), is(nullValue()));
    }

    /**
     * resolve marker annotation.
     */
    @Test
    public void annotation_marker() {
        checkAnnotation(new AnnotationDescription(Descriptions.classOf(MockMarker.class)));
    }

    /**
     * resolve annotation with value.
     */
    @Test
    public void annotation_constant() {
        checkAnnotation(new AnnotationDescription(
                Descriptions.classOf(MockSingleElement.class),
                Descriptions.valueOf("Hello, world!")));
    }

    /**
     * resolve nesting annotation.
     */
    @Test
    public void annotation_nesting() {
        ClassDescription single = Descriptions.classOf(MockSingleElement.class);
        checkAnnotation(new AnnotationDescription(
                Descriptions.classOf(MockNested.class),
                ArrayDescription.elementsOf(single,
                        new AnnotationDescription(single, Descriptions.valueOf("A")),
                        new AnnotationDescription(single, Descriptions.valueOf("B")),
                        new AnnotationDescription(single, Descriptions.valueOf("C")))));
    }

    private Class<?> resolve(final TypeDescription description) {
        final ModelFactory f = Models.getModelFactory();
        ClassLoader loader = start(new Callback(true) {
            @Override
            protected void test() throws IOException {
                ImportBuilder imports = new ImportBuilder(
                        f,
                        f.newPackageDeclaration(Models.toName(f, "com.example.testing")),
                        ImportBuilder.Strategy.TOP_LEVEL);
                Type target = DescriptionHelper.resolve(imports, description);
                MethodDeclaration method = f.newMethodDeclaration(
                        null,
                        new AttributeBuilder(f).Public().toAttributes(),
                        imports.toType(Object.class),
                        f.newSimpleName("call"),
                        Collections.emptyList(),
                        Collections.singletonList(f.newReturnStatement(f.newClassLiteral(target))));
                TypeDeclaration type = f.newClassDeclaration(
                        null,
                        new AttributeBuilder(f).Public().toAttributes(),
                        f.newSimpleName("Work"),
                        null,
                        Collections.singletonList(new TypeBuilder(f, imports.toType(Callable.class))
                                .parameterize(imports.toType(Object.class))
                                .toType()),
                        Collections.singletonList(method));
                env.emit(f.newCompilationUnit(
                        imports.getPackageDeclaration(),
                        imports.toImportDeclarations(),
                        Collections.singletonList(type),
                        Collections.emptyList()));
            }
        });
        try {
            return (Class<?>) loader.loadClass("com.example.testing.Work")
                    .asSubclass(Callable.class)
                    .newInstance()
                    .call();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private Object constant(final ValueDescription description) {
        final ModelFactory f = Models.getModelFactory();
        ClassLoader loader = start(new Callback(true) {
            @Override
            protected void test() throws IOException {
                ImportBuilder imports = new ImportBuilder(
                        f,
                        f.newPackageDeclaration(Models.toName(f, "com.example.testing")),
                        ImportBuilder.Strategy.TOP_LEVEL);
                Expression target = DescriptionHelper.resolveConstant(imports, description);
                MethodDeclaration method = f.newMethodDeclaration(
                        null,
                        new AttributeBuilder(f).Public().toAttributes(),
                        imports.toType(Object.class),
                        f.newSimpleName("call"),
                        Collections.emptyList(),
                        Collections.singletonList(f.newReturnStatement(target)));
                TypeDeclaration type = f.newClassDeclaration(
                        null,
                        new AttributeBuilder(f).Public().toAttributes(),
                        f.newSimpleName("Work"),
                        null,
                        Collections.singletonList(new TypeBuilder(f, imports.toType(Callable.class))
                                .parameterize(imports.toType(Object.class))
                                .toType()),
                        Collections.singletonList(method));
                env.emit(f.newCompilationUnit(
                        imports.getPackageDeclaration(),
                        imports.toImportDeclarations(),
                        Collections.singletonList(type),
                        Collections.emptyList()));
            }
        });
        try {
            return loader.loadClass("com.example.testing.Work")
                    .asSubclass(Callable.class)
                    .newInstance()
                    .call();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private void checkAnnotation(AnnotationDescription description) {
        Annotation annotation = annotation(description);
        AnnotationDescription restored = AnnotationDescription.of(annotation);
        assertThat(restored, is(description));
    }

    private Annotation annotation(final AnnotationDescription description) {
        final ModelFactory f = Models.getModelFactory();
        ClassLoader loader = start(new Callback(true) {
            @Override
            protected void test() throws IOException {
                ImportBuilder imports = new ImportBuilder(
                        f,
                        f.newPackageDeclaration(Models.toName(f, "com.example.testing")),
                        ImportBuilder.Strategy.TOP_LEVEL);
                TypeDeclaration type = f.newClassDeclaration(
                        null,
                        new AttributeBuilder(f)
                            .annotation(DescriptionHelper.resolveAnnotation(imports, description))
                            .Public()
                            .toAttributes(),
                        f.newSimpleName("Work"),
                        null,
                        Collections.emptyList(),
                        Collections.emptyList());
                env.emit(f.newCompilationUnit(
                        imports.getPackageDeclaration(),
                        imports.toImportDeclarations(),
                        Collections.singletonList(type),
                        Collections.emptyList()));
            }
        });
        try {
            Class<? extends Annotation> annotationType = loader
                    .loadClass(description.getDeclaringClass().getBinaryName())
                    .asSubclass(Annotation.class);
            return loader.loadClass("com.example.testing.Work").getAnnotation(annotationType);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
