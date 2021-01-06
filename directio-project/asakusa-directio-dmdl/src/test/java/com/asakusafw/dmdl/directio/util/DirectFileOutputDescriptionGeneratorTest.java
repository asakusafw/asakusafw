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
package com.asakusafw.dmdl.directio.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.asakusafw.dmdl.directio.common.driver.GeneratorTesterRoot;
import com.asakusafw.dmdl.directio.util.DirectFileOutputDescriptionGenerator.Description;
import com.asakusafw.dmdl.directio.util.mock.MockData;
import com.asakusafw.dmdl.directio.util.mock.MockDataFormat;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.directio.DirectFileOutputDescription;

/**
 * Test for {@link DirectFileOutputDescriptionGenerator}.
 */
public class DirectFileOutputDescriptionGeneratorTest extends GeneratorTesterRoot {

    private final ModelFactory f = Models.getModelFactory();

    /**
     * simple case.
     */
    @Test
    public void simple() {
        Class<? extends DirectFileOutputDescription> aClass = generate(new Description("Testing", name(MockData.class)));
        assertThat(DirectFileOutputDescription.class.isAssignableFrom(aClass), is(true));
        assertThat(Modifier.isAbstract(aClass.getModifiers()), is(true));

        assertThat(aClass, hasGetter("getModelType"));
        assertThat(aClass, not(hasGetter("getBasePath")));
        assertThat(aClass, not(hasGetter("getResourcePattern")));
        assertThat(aClass, not(hasGetter("getFormat")));
    }

    /**
     * generate concrete class.
     * @throws Exception if failed
     */
    @Test
    public void concrete() throws Exception {
        Description description = new Description("Testing", name(MockData.class));
        description.setBasePath("base-path");
        description.setResourcePattern("*");
        description.setFormatClassName(name(MockDataFormat.class));

        Class<? extends DirectFileOutputDescription> aClass = generate(description);
        assertThat(DirectFileOutputDescription.class.isAssignableFrom(aClass), is(true));
        assertThat(Modifier.isAbstract(aClass.getModifiers()), is(false));

        DirectFileOutputDescription object = aClass.newInstance();
        assertThat(object.getModelType(), is((Object) MockData.class));
        assertThat(object.getBasePath(), is("base-path"));
        assertThat(object.getResourcePattern(), is("*"));
        assertThat(object.getOrder(), is(Collections.emptyList()));
        assertThat(object.getDeletePatterns(), is(Collections.emptyList()));
        assertThat(object.getFormat(), is((Object) MockDataFormat.class));
    }

    /**
     * generate /w all properties.
     * @throws Exception if failed
     */
    @Test
    public void w_all() throws Exception {
        Description description = new Description("Testing", name(MockData.class));
        description.setBasePath("base-path");
        description.setResourcePattern("*");
        description.getOrder().add("something1");
        description.getOrder().add("something2");
        description.getDeletePatterns().add("delete1-*");
        description.getDeletePatterns().add("delete2-*");
        description.setFormatClassName(name(MockDataFormat.class));

        Class<? extends DirectFileOutputDescription> aClass = generate(description);
        assertThat(DirectFileOutputDescription.class.isAssignableFrom(aClass), is(true));
        assertThat(Modifier.isAbstract(aClass.getModifiers()), is(false));

        DirectFileOutputDescription object = aClass.newInstance();
        assertThat(object.getModelType(), is((Object) MockData.class));
        assertThat(object.getBasePath(), is("base-path"));
        assertThat(object.getResourcePattern(), is("*"));
        assertThat(object.getOrder(), is(Arrays.asList("something1", "something2")));
        assertThat(object.getDeletePatterns(), is(Arrays.asList("delete1-*", "delete2-*")));
        assertThat(object.getFormat(), is((Object) MockDataFormat.class));
    }

    private Matcher<Class<?>> hasGetter(String name) {
        return new BaseMatcher<Class<?>>() {
            @Override
            public boolean matches(Object item) {
                try {
                    Method method = ((Class<?>) item).getMethod(name);
                    if (Modifier.isPublic(method.getModifiers()) == false) {
                        return false;
                    }
                    if (Modifier.isAbstract(method.getModifiers())) {
                        return false;
                    }
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            @Override
            public void describeTo(org.hamcrest.Description description) {
                description.appendText(String.format("has getter \"%s()\"", name));
            }
        };
    }

    private Name name(Class<?> aClass) {
        return ((NamedType) Models.toType(f, aClass)).getName();
    }

    @SuppressWarnings("unchecked")
    private Class<? extends DirectFileOutputDescription> generate(Description description) {
        emitDrivers.add(new JavaDataModelDriver() {
            @Override
            public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
                EmitContext next = new EmitContext(
                        context.getSemantics(),
                        context.getConfiguration(),
                        model,
                        "testing",
                        "MockFileOutputDescription");
                DirectFileOutputDescriptionGenerator.generate(next, description);
            }
        });
        ModelLoader loader = generateJavaFromLines("model = { prop : INT; };");
        return (Class<? extends DirectFileOutputDescription>) loader.load("testing", "MockFileOutputDescription");
    }
}
