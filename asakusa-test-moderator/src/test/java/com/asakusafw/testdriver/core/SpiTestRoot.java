/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.testdriver.core;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * SPI testing base class.
 * @since 0.2.0
 */
public abstract class SpiTestRoot {

    /**
     * Temporary Folder.
     */
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Creates META-INF/services/API.
     * @param api API class
     * @param services service classes
     * @return service class loader
     */
    public ClassLoader register(Class<?> api, Class<?>... services) {
        File classpath = temporaryFolder.newFolder("classpath");
        try {
            File serviceFolder = new File(classpath, "META-INF/services");
            serviceFolder.mkdirs();
            PrintWriter output = new PrintWriter(new File(serviceFolder, api.getName()));
            try {
                for (Class<?> serviceClass : services) {
                    output.println(serviceClass.getName());
                }
            } finally {
                output.close();
            }
            return new URLClassLoader(new URL[] { classpath.toURI().toURL() });
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates {@link DataModelSource} from values.
     * @param <E> type of data model objects
     * @param definition the data model definition
     * @param values source values
     * @return the created object
     */
    public static <E> DataModelSource source(DataModelDefinition<E> definition, E... values) {
        return new IteratorDataModelSource(definition, Arrays.asList(values).iterator());
    }
}
