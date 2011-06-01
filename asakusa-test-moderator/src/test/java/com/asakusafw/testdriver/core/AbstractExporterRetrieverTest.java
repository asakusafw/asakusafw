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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.FileExporterDescription;

/**
 * Test for {@link AbstractExporterRetriever}.
 * @since 0.2.0
 */
public class AbstractExporterRetrieverTest {

    /**
     * Resolve description from its signature.
     */
    @Test
    public void getDescriptionClass() {
        class Target extends AbstractExporterRetriever<FileExporterDescription> {
            @Override
            public void truncate(FileExporterDescription description) throws IOException {
                return;
            }
            @Override
            public <V> ModelOutput<V> createOutput(DataModelDefinition<V> definition,
                    FileExporterDescription description) throws IOException {
                return null;
            }
            @Override
            public <V> DataModelSource createSource(DataModelDefinition<V> definition,
                    FileExporterDescription description) throws IOException {
                return null;
            }
        }
        Target obj = new Target();
        assertThat(obj.getDescriptionClass(), is((Object) FileExporterDescription.class));
    }

    /**
     * Raw class definition fails to resolve description.
     */
    @Test(expected = RuntimeException.class)
    public void getDescriptionClass_raw() {
        @SuppressWarnings("rawtypes")
        class Target extends AbstractExporterRetriever {
            @Override
            public void truncate(ExporterDescription description) {
                return;
            }
            @Override
            public ModelOutput createOutput(DataModelDefinition definition,
                    ExporterDescription description) throws IOException {
                return null;
            }
            @Override
            public DataModelSource createSource(DataModelDefinition definition,
                    ExporterDescription description) throws IOException {
                return null;
            }
        }
        Target obj = new Target();
        obj.getDescriptionClass();
    }
}
