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
package com.asakusafw.testdriver.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * Mock {@link ImporterPreparator}.
 * @since 0.2.0
 */
public class MockImporterPreparator extends AbstractImporterPreparator<MockImporterPreparator.Desc> {

    /**
     * Returns {@link ImporterDescription} to keep lines of content.
     * @return the created description
     */
    public static Desc create() {
        return new Desc();
    }

    /**
     * Wrap this object.
     * @return wrapped
     */
    public SpiImporterPreparator wrap() {
        return new SpiImporterPreparator(Collections.singletonList(this));
    }

    @Override
    public void truncate(Desc description) throws IOException {
        description.lines.clear();
    }

    @Override
    public <V> ModelOutput<V> createOutput(
            DataModelDefinition<V> definition,
            Desc description) throws IOException {
        List<String> lines = description.lines;
        return new ModelOutput<V>() {
            @Override
            public void write(V model) throws IOException {
                lines.add(String.valueOf(model));
            }
            @Override
            public void close() throws IOException {
                return;
            }
        };
    }

    /**
     * Mock {@link ImporterDescription} for {@link MockImporterPreparator}.
     * @since 0.2.0
     */
    public static class Desc implements ImporterDescription {

        /**
         * Contents.
         */
        public final List<String> lines = new ArrayList<>();

        @Override
        public Class<?> getModelType() {
            return String.class;
        }
    }
}
