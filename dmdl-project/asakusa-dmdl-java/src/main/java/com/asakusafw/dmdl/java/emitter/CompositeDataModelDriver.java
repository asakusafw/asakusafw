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
package com.asakusafw.dmdl.java.emitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.Type;

/**
 * Composition of {@link JavaDataModelDriver}.
 */
public class CompositeDataModelDriver extends JavaDataModelDriver {

    static final Logger LOG = LoggerFactory.getLogger(CompositeDataModelDriver.class);

    private final List<JavaDataModelDriver> drivers;

    /**
     * Creates composite data model driver with using registered {@link JavaDataModelDriver} as services.
     * @param serviceClassLoader the class loader to load services
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CompositeDataModelDriver(ClassLoader serviceClassLoader) {
        if (serviceClassLoader == null) {
            throw new IllegalArgumentException("serviceClassLoader must not be null"); //$NON-NLS-1$
        }
        this.drivers = Lists.freeze(loadSpi(serviceClassLoader));
    }

    private List<JavaDataModelDriver> loadSpi(ClassLoader serviceClassLoader) {
        assert serviceClassLoader != null;
        List<JavaDataModelDriver> results = new ArrayList<>();
        ServiceLoader<JavaDataModelDriver> loader =
            ServiceLoader.load(JavaDataModelDriver.class, serviceClassLoader);
        for (JavaDataModelDriver driver : loader) {
            LOG.debug("Activating Java data model driver: {}", driver.getClass().getName()); //$NON-NLS-1$
            results.add(driver);
        }
        Collections.sort(results, (o1, o2) -> {
            assert o2 != null;
            int diff = o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
            if (diff != 0) {
                return diff;
            }
            return o1.getClass().getName().compareTo(o2.getClass().getName());
        });
        return results;
    }

    /**
     * Creates and returns a new instance.
     * @param drivers the drivers to composite
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public CompositeDataModelDriver(List<? extends JavaDataModelDriver> drivers) {
        if (drivers == null) {
            throw new IllegalArgumentException("drivers must not be null"); //$NON-NLS-1$
        }
        this.drivers = Lists.freeze(drivers);
    }

    /**
     * Returns the element drivers.
     * @return the element drivers
     */
    public List<JavaDataModelDriver> getDrivers() {
        return drivers;
    }

    @Override
    public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
        for (JavaDataModelDriver driver : drivers) {
            driver.generateResources(context, model);
        }
    }

    @Override
    public List<Type> getInterfaces(EmitContext context, ModelDeclaration model) throws IOException {
        List<Type> results = new ArrayList<>();
        for (JavaDataModelDriver driver : drivers) {
            results.addAll(driver.getInterfaces(context, model));
        }
        return results;
    }

    @Override
    public List<FieldDeclaration> getFields(EmitContext context, ModelDeclaration model) throws IOException {
        List<FieldDeclaration> results = new ArrayList<>();
        for (JavaDataModelDriver driver : drivers) {
            results.addAll(driver.getFields(context, model));
        }
        return results;
    }

    @Override
    public List<MethodDeclaration> getMethods(EmitContext context, ModelDeclaration model) throws IOException {
        List<MethodDeclaration> results = new ArrayList<>();
        for (JavaDataModelDriver driver : drivers) {
            results.addAll(driver.getMethods(context, model));
        }
        return results;
    }

    @Override
    public List<Annotation> getTypeAnnotations(EmitContext context, ModelDeclaration model) throws IOException {
        List<Annotation> results = new ArrayList<>();
        for (JavaDataModelDriver driver : drivers) {
            results.addAll(driver.getTypeAnnotations(context, model));
        }
        return results;
    }

    @Override
    public List<Annotation> getMemberAnnotations(EmitContext context, PropertyDeclaration property) throws IOException {
        List<Annotation> results = new ArrayList<>();
        for (JavaDataModelDriver driver : drivers) {
            results.addAll(driver.getMemberAnnotations(context, property));
        }
        return results;
    }
}
