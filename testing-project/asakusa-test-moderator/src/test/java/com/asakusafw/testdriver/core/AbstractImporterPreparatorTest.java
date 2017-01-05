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
package com.asakusafw.testdriver.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * Test for {@link AbstractImporterPreparator}.
 * @since 0.2.0
 */
public class AbstractImporterPreparatorTest {

    /**
     * Resolve description from its signature.
     */
    @Test
    public void getDescriptionClass() {
        class Target extends AbstractImporterPreparator<DummyImporterDescription> {
            @Override
            public void truncate(DummyImporterDescription description) {
                return;
            }
            @Override
            public <V> ModelOutput<V> createOutput(DataModelDefinition<V> definition,
                    DummyImporterDescription description) {
                return null;
            }
        }
        Target obj = new Target();
        assertThat(obj.getDescriptionClass(), is((Object) DummyImporterDescription.class));
    }

    /**
     * Raw class definition fails to resolve description.
     */
    @Test(expected = RuntimeException.class)
    public void getDescriptionClass_raw() {
        @SuppressWarnings("rawtypes")
        class Target extends AbstractImporterPreparator {
            @Override
            public void truncate(ImporterDescription description) {
                return;
            }
            @Override
            public ModelOutput createOutput(DataModelDefinition definition,
                    ImporterDescription description) {
                return null;
            }
        }
        Target obj = new Target();
        obj.getDescriptionClass();
    }

    abstract static class DummyImporterDescription implements ImporterDescription {
        // no special members
    }
}
