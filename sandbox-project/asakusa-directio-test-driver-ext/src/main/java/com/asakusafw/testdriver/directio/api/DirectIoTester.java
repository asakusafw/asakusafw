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
package com.asakusafw.testdriver.directio.api;

import java.io.IOException;

import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.testdriver.DriverElementBase;
import com.asakusafw.testdriver.DriverToolBase;
import com.asakusafw.testdriver.OperatorTestEnvironment;
import com.asakusafw.testdriver.TesterBase;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.utils.io.Provider;
import com.asakusafw.utils.io.Source;
import com.asakusafw.vocabulary.directio.DirectFileInputDescription;

/**
 * Testing API for Direct I/O.
 * Example for operator testing:
<pre><code>
&#64;Rule
public final OperatorTestEnvironment environment =
        new OperatorTestEnvironment("conf-path-with-directio-settings.xml");

&#64;Test
public void someTest() {
    ...
    DirectIoTester.with(environment)
        .resource(SomeData.class, "base/path", "resource/pattern-*.csv")
        .prepare("test-data.xls#sheet");
    ...
}
</code></pre>
 * Example for flow testing:
<pre><code>
FlowPartTester tester = ...;
...
DirectIoTester.with(tester)
    .resource(SomeData.class, "base/path", "resource/pattern-*.csv")
    .prepare("test-data.xls#sheet");
...
tester.runTest(...);
</code></pre>
 * @since 0.7.3
 */
public abstract class DirectIoTester {

    /**
     * Creates a new instance.
     * @param tester the target tester
     * @return the helper for Direct I/O
     */
    public static DirectIoTester with(TesterBase tester) {
        return new DirectIoFlowTester(tester);
    }

    /**
     * Creates a new instance.
     * @param tester the target tester
     * @return the helper for Direct I/O
     */
    public static DirectIoTester with(OperatorTestEnvironment tester) {
        return new DirectIoOperatorTester(tester);
    }

    /**
     * Declares about Direct I/O resource.
     * @param formatClass the Direct I/O data format class
     * @param basePath the base path (must not contain variables)
     * @param resourcePattern the resource pattern (must not contain variables)
     * @return the external resource manipulator
     */
    public DirectIoResource resource(
            Class<? extends DataFormat<?>> formatClass,
            String basePath,
            String resourcePattern) {
        TesterFileInputDescription description = createDescription(formatClass, basePath, resourcePattern);
        return resource(description);
    }

    /**
     * Returns resource for the {@link DirectFileInputDescription}.
     * @param description the importer description to be prepared
     * @return the external resource manipulator
     */
    protected abstract DirectIoResource resource(DirectFileInputDescription description);


    private TesterFileInputDescription createDescription(
            Class<? extends DataFormat<?>> formatClass,
            String basePath,
            String resourcePattern) {
        DataFormat<?> format;
        try {
            format = formatClass.newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        TesterFileInputDescription description = new TesterFileInputDescription(
                format.getSupportedType(),
                basePath,
                resourcePattern,
                formatClass);
        return description;
    }

    /**
     * External resource manipulator for Direct I/O.
     * @since 0.7.3
     */
    public abstract static class DirectIoResource extends DriverToolBase {

        /**
         * Returns the target description.
         */
        final DirectFileInputDescription description;

        DirectIoResource(DriverElementBase target, DirectFileInputDescription description) {
            super(target);
            this.description = description;
        }

        /**
         * Sets a data source for this external resource.
         * @param source input data
         */
        public abstract void prepare(DataModelSourceFactory source);

        /**
         * Sets a data source path for this external resource.
         * @param sourcePath the source path
         * @throws IllegalArgumentException if the source is not found
         */
        public final void prepare(String sourcePath) {
            prepare(toDataModelSourceFactory(sourcePath));
        }

        /**
         * Sets a data objects for this external resource.
         * @param objects a list of data model objects
         */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public final void prepare(Iterable<?> objects) {
            try {
                DataModelDefinition def = getTestTools().toDataModelDefinition(description.getModelType());
                prepare(toDataModelSourceFactory(def, objects));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        /**
         * Sets a data source provider for this external resource.
         * @param provider provider that provides data model objects
         */
        public final void prepare(Provider<? extends Source<?>> provider) {
            prepare(toDataModelSourceFactory(provider));
        }
    }
}
