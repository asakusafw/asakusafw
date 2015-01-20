/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.dmdl.directio.hive.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.asakusafw.dmdl.directio.hive.common.GeneratorTesterRoot;
import com.asakusafw.dmdl.directio.hive.util.DirectFileInputDescriptionGenerator.Description;
import com.asakusafw.dmdl.directio.hive.util.mock.MockData;
import com.asakusafw.dmdl.directio.hive.util.mock.MockDataFormat;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.directio.DirectFileInputDescription;
import com.asakusafw.vocabulary.external.ImporterDescription.DataSize;

/**
 * Test for {@link DirectFileInputDescriptionGenerator}.
 */
public class DirectFileInputDescriptionGeneratorTest extends GeneratorTesterRoot {

    private final ModelFactory f = Models.getModelFactory();

    /**
     * simple case.
     */
    @Test
    public void simple() {
        Class<? extends DirectFileInputDescription> aClass = generate(new Description("Testing", name(MockData.class)));
        assertThat(DirectFileInputDescription.class.isAssignableFrom(aClass), is(true));
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

        Class<? extends DirectFileInputDescription> aClass = generate(description);
        assertThat(DirectFileInputDescription.class.isAssignableFrom(aClass), is(true));
        assertThat(Modifier.isAbstract(aClass.getModifiers()), is(false));

        DirectFileInputDescription object = aClass.newInstance();
        assertThat(object.getModelType(), is((Object) MockData.class));
        assertThat(object.getBasePath(), is("base-path"));
        assertThat(object.getResourcePattern(), is("*"));
        assertThat(object.getFormat(), is((Object) MockDataFormat.class));
        assertThat(object.isOptional(), is(false));
        assertThat(object.getDataSize(), is(DataSize.UNKNOWN));
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
        description.setOptional(true);
        description.setDataSize(DataSize.TINY);
        description.setFormatClassName(name(MockDataFormat.class));

        Class<? extends DirectFileInputDescription> aClass = generate(description);
        assertThat(DirectFileInputDescription.class.isAssignableFrom(aClass), is(true));
        assertThat(Modifier.isAbstract(aClass.getModifiers()), is(false));

        DirectFileInputDescription object = aClass.newInstance();
        assertThat(object.getModelType(), is((Object) MockData.class));
        assertThat(object.getBasePath(), is("base-path"));
        assertThat(object.getResourcePattern(), is("*"));
        assertThat(object.getFormat(), is((Object) MockDataFormat.class));
        assertThat(object.isOptional(), is(true));
        assertThat(object.getDataSize(), is(DataSize.TINY));
    }

    private Matcher<Class<?>> hasGetter(final String name) {
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
    private Class<? extends DirectFileInputDescription> generate(final Description description) {
        emitDrivers.add(new JavaDataModelDriver() {
            @Override
            public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
                EmitContext next = new EmitContext(
                        context.getSemantics(),
                        context.getConfiguration(),
                        model,
                        "testing",
                        "MockFileInputDescription");
                DirectFileInputDescriptionGenerator.generate(next, description);
            }
        });
        ModelLoader loader = generateJava("model = { prop : INT; };");
        return (Class<? extends DirectFileInputDescription>) loader.load("testing", "MockFileInputDescription");
    }
}
