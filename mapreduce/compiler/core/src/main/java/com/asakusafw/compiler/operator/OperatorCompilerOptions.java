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
package com.asakusafw.compiler.operator;

import java.text.MessageFormat;
import java.util.Map;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.repository.SpiDataModelMirrorRepository;
import com.asakusafw.utils.collections.Maps;

/**
 * Options for Operator DSL compiler.
 * @since 0.1.0
 * @version 0.7.0
 */
public final class OperatorCompilerOptions {

    private final ClassLoader serviceClassLoader;

    private final DataModelMirrorRepository dataModelRepository;

    private final Map<String, String> properties;

    private OperatorCompilerOptions(
            ClassLoader serviceClassLoader,
            DataModelMirrorRepository dataModelRepository,
            Map<String, String> properties) {
        this.serviceClassLoader = serviceClassLoader;
        this.dataModelRepository = dataModelRepository;
        this.properties = properties;
    }

    /**
     * Analyzes options for annotation processor and picks up options for the operator DSL compiler.
     * @param options the annotation processor options
     * @return the corresponded operator DSL compiler options
     * @throws IllegalArgumentException if the parameter is {@code null}
     * @throws OperatorCompilerException if failed to extract compiler options
     */
    public static OperatorCompilerOptions parse(Map<String, String> options) {
        Precondition.checkMustNotBeNull(options, "options"); //$NON-NLS-1$
        ClassLoader serviceClassLoader = OperatorCompilerOptions.class.getClassLoader();
        DataModelMirrorRepository dataModelRepository = new SpiDataModelMirrorRepository(serviceClassLoader);
        Map<String, String> properties = Maps.freeze(options);
        return new OperatorCompilerOptions(serviceClassLoader, dataModelRepository, properties);
    }

    /**
     * Returns the class loader to load compiler plug-ins.
     * @return a compiler plug-ins loader
     */
    public ClassLoader getServiceClassLoader() {
        return serviceClassLoader;
    }

    /**
     * Returns the data model repository.
     * @return the data model repository
     */
    public DataModelMirrorRepository getDataModelRepository() {
        return dataModelRepository;
    }

    /**
     * Returns the compiler properties.
     * @return the properties
     * @since 0.7.0
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Returns a compiler property.
     * @param key the property key
     * @param defaultValue the default value
     * @return the target property, or the default value if the target property is not defined
     * @since 0.7.0
     */
    public String getProperty(String key, String defaultValue) {
        Precondition.checkMustNotBeNull(key, "key"); //$NON-NLS-1$
        String value = getProperties().get(key);
        return value == null ? defaultValue : value;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}{1}", //$NON-NLS-1$
                getClass().getSimpleName(),
                getProperties());
    }
}
