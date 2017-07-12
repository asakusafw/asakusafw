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
package com.asakusafw.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.asakusafw.utils.gradle.EnvironmentConfigurator;
import com.asakusafw.utils.gradle.PropertyConfigurator;

/**
 * A JUnit resource which provides {@link AsakusaProject}.
 * @since 0.9.2
 */
public class AsakusaProjectProvider implements TestRule {

    /**
     * Additional system properties path.
     */
    public static final String PATH_PROPERTIES = "META-INF/asakusa-integration/system.properties";

    final TemporaryFolder temporary = new TemporaryFolder();

    final List<Consumer<? super AsakusaProjectProvider>> providerConfigurators = new ArrayList<>();

    final List<Consumer<? super AsakusaProject>> projectConfigurators = new ArrayList<>();

    final AtomicBoolean active = new AtomicBoolean(false);

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                ClassLoader classLoader = description.getTestClass().getClassLoader();
                // built-in project configurators must be applied first
                projectConfigurators.addAll(0, Arrays.asList(
                        PropertyConfigurator.of(classLoader.getResources(PATH_PROPERTIES)),
                        EnvironmentConfigurator.system(),
                        PropertyConfigurator.system()));
                temporary.create();
                try {
                    // activate provider before process `withProvider()` actions
                    active.set(true);
                    providerConfigurators.forEach(it -> it.accept(AsakusaProjectProvider.this));
                    base.evaluate();
                } finally {
                    active.set(false);
                    temporary.delete();
                }
            }
        };
    }

    /**
     * Adds a configurator that configures this object.
     * @param configurator the configurator
     * @return this
     */
    public AsakusaProjectProvider withProvider(Consumer<? super AsakusaProjectProvider> configurator) {
        providerConfigurators.add(configurator);
        if (active.get()) {
            configurator.accept(this);
        }
        return this;
    }

    /**
     * Adds a configurator that configures each {@link AsakusaProject}.
     * @param configurator the configurator
     * @return this
     */
    public AsakusaProjectProvider withProject(Consumer<? super AsakusaProject> configurator) {
        projectConfigurators.add(configurator);
        return this;
    }

    /**
     * Returns a new project.
     * @param name the project name
     * @return the created project
     */
    public AsakusaProject newInstance(String name) {
        if (active.get() == false) {
            throw new IllegalStateException("provider has not been yet initialized");
        }
        Path base;
        try {
            base = temporary.newFolder().toPath();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        AtomicInteger counter = new AtomicInteger();
        Path project = folder(base, name);
        Path home = base.resolve(String.format("%s-fw", name)); // don't prepare empty dir for ASAKUSA_HOME
        Supplier<Path> temporaries = () -> folder(base, String.format("%s-tmp%d", name, counter.incrementAndGet()));

        AsakusaProject result = new AsakusaProject(project, home, temporaries);
        projectConfigurators.forEach(result::with);

        // setting ASAKUSA_HOME must be at the last for prevent from overwriting ASAKUSA_HOME
        result.with(EnvironmentConfigurator.of("ASAKUSA_HOME", home));

        return result;
    }

    private static Path folder(Path parent, String name) {
        Path path = parent.resolve(name);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "failed to create directory: {0}",
                    path), e);
        }
        return path;
    }
}
