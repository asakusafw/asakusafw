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
package com.asakusafw.dmdl.java.emitter;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.dmdl.java.GeneratorTesterRoot;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.Type;

/**
 * Test for {@link CompositeDataModelDriver}.
 */
public class CompositeDataModelDriverTest extends GeneratorTesterRoot {

    /**
     * Temporary folder.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * empty drivers.
     */
    @Test
    public void empty() {
        emitDrivers.add(new CompositeDataModelDriver(Collections.<JavaDataModelDriver>emptyList()));
        generate("simple");
    }

    /**
     * single driver.
     */
    @Test
    public void single() {
        TrackingDriver driver = new TrackingDriver();
        emitDrivers.add(new CompositeDataModelDriver(Arrays.asList(driver)));
        generate("simple");

        assertThat(driver.interfaces, hasItem("simple"));
        assertThat(driver.methods, hasItem("simple"));
        assertThat(driver.typeAnnotations, hasItem("simple"));
        assertThat(driver.propertyAnnotations, hasItem("value"));
    }

    /**
     * multi drivers.
     */
    @Test
    public void multi() {
        TrackingDriver[] drivers = new TrackingDriver[] {
                new TrackingDriver(),
                new TrackingDriver(),
                new TrackingDriver(),
        };
        emitDrivers.add(new CompositeDataModelDriver(Arrays.asList(drivers)));
        generate("simple");

        for (TrackingDriver driver : drivers) {
            assertThat(driver.interfaces, hasItem("simple"));
            assertThat(driver.methods, hasItem("simple"));
            assertThat(driver.typeAnnotations, hasItem("simple"));
            assertThat(driver.propertyAnnotations, hasItem("value"));
        }
    }

    /**
     * Load spi.
     */
    @Test
    public void load_spi() {
        ClassLoader serviceClassLoader;
        try {
            File services = new File(folder.getRoot(), "META-INF/services");
            Assume.assumeTrue(services.mkdirs());
            File spi = new File(services, JavaDataModelDriver.class.getName());
            PrintWriter output = new PrintWriter(spi, "UTF-8");
            try {
                output.println(HelloDriver.class.getName());
            } finally {
                output.close();
            }
            serviceClassLoader = new URLClassLoader(new URL[] {
                    folder.getRoot().toURI().toURL()
            });
        } catch (IOException e) {
            e.printStackTrace();
            Assume.assumeNoException(e);
            throw new AssertionError();
        }
        emitDrivers.add(new CompositeDataModelDriver(serviceClassLoader));
        ModelLoader loader = generate("simple");
        ModelWrapper object = loader.newModel("Simple");
        assertThat(object.invoke("hello"), is((Object) "hello"));
    }

    static class TrackingDriver extends JavaDataModelDriver {

        final Set<String> interfaces = new HashSet<String>();
        final Set<String> methods = new HashSet<String>();
        final Set<String> typeAnnotations = new HashSet<String>();
        final Set<String> propertyAnnotations = new HashSet<String>();

        @Override
        public List<Type> getInterfaces(EmitContext context, ModelDeclaration model) {
            interfaces.add(model.getName().identifier);
            return Collections.emptyList();
        }

        @Override
        public List<MethodDeclaration> getMethods(EmitContext context, ModelDeclaration model) {
            methods.add(model.getName().identifier);
            return Collections.emptyList();
        }

        @Override
        public List<Annotation> getTypeAnnotations(EmitContext context, ModelDeclaration model) {
            typeAnnotations.add(model.getName().identifier);
            return Collections.emptyList();
        }

        @Override
        public List<Annotation> getMemberAnnotations(EmitContext context, PropertyDeclaration property) {
            propertyAnnotations.add(property.getName().identifier);
            return Collections.emptyList();
        }
    }
}
