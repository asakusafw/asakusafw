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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import com.asakusafw.vocabulary.external.ExporterDescription;

/**
 * Mock {@link ExporterRetriever}.
 * @since 0.2.0
 */
public class MockExporterRetriever extends AbstractExporterRetriever<MockExporterRetriever.Desc> {

    /**
     * Returns {@link ExporterDescription} which has lines of content.
     * @param lines content
     * @return the created description
     */
    public static Desc create(String... lines) {
        return new Desc(Arrays.asList(lines));
    }

    /**
     * Wrap this object.
     * @return wrapped
     */
    public SpiExporterRetriever wrap() {
        return new SpiExporterRetriever(Collections.singletonList(this));
    }

    @Override
    public <V> DataModelSource open(DataModelDefinition<V> definition, Desc description) throws IOException {
        return new IteratorDataModelSource(ValueDefinition.of(String.class), description.lines.iterator());
    }

    /**
     * Mock {@link ExporterDescription} for {@link MockExporterRetriever}.
     * @since 0.2.0
     */
    public static class Desc implements ExporterDescription {

        final Iterable<String> lines;

        Desc(Iterable<String> lines) {
            this.lines = lines;
        }

        @Override
        public Class<?> getModelType() {
            return String.class;
        }
    }
}
