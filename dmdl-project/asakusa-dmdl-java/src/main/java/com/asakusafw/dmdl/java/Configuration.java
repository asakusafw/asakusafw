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

import java.util.Locale;

import com.asakusafw.dmdl.source.DmdlSourceRepository;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.Name;
import com.ashigeru.lang.java.model.util.Emitter;

/**
 * Configurations for DMDL Java emitter.
 */
public class Configuration {

    private final ModelFactory factory;

    private final DmdlSourceRepository source;

    private final Name basePackage;

    private final Emitter output;

    private final ClassLoader serviceClassLoader;

    private final Locale locale;

    /**
     * Creates and returns a new instance.
     * @param factory Java DOM element factory
     * @param source DMDL source repository
     * @param basePackage the Java base package
     * @param output the Java DOM emitter
     * @param serviceClassLoader the class loader to load external services
     * @param locale the locale information to generate programs
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Configuration(
            ModelFactory factory,
            DmdlSourceRepository source,
            Name basePackage,
            Emitter output,
            ClassLoader serviceClassLoader,
            Locale locale) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (source == null) {
            throw new IllegalArgumentException("source must not be null"); //$NON-NLS-1$
        }
        if (basePackage == null) {
            throw new IllegalArgumentException("basePackage must not be null"); //$NON-NLS-1$
        }
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        if (serviceClassLoader == null) {
            throw new IllegalArgumentException("serviceClassLoader must not be null"); //$NON-NLS-1$
        }
        if (locale == null) {
            throw new IllegalArgumentException("locale must not be null"); //$NON-NLS-1$
        }
        this.factory = factory;
        this.source = source;
        this.basePackage = basePackage;
        this.output = output;
        this.serviceClassLoader = serviceClassLoader;
        this.locale = locale;
    }

    /**
     * Returns Java DOM element factory.
     * @return the element factory
     */
    public ModelFactory getFactory() {
        return factory;
    }

    /**
     * Returns DMDL source repository.
     * @return the source repository
     */
    public DmdlSourceRepository getSource() {
        return source;
    }

    /**
     * Returns the Java base package.
     * @return the base package
     */
    public Name getBasePackage() {
        return basePackage;
    }

    /**
     * Returns the Java DOM emitter.
     * @return the Java DOM emitter
     */
    public Emitter getOutput() {
        return output;
    }

    /**
     * Returns the service class loader.
     * @return the class loader to load the external service classes
     */
    public ClassLoader getServiceClassLoader() {
        return serviceClassLoader;
    }

    /**
     * Returns the locale information to generate programs.
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }
}
