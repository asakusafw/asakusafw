/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.testdriver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.io.Text;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.SourceDataModelSource;
import com.asakusafw.testdriver.core.SourceDataModelSourceFactory;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.utils.io.Provider;
import com.asakusafw.utils.io.Source;
import com.asakusafw.utils.io.Sources;

final class FlowDriverPortTestHelper {

    static final MockTextDefinition DEFINITION = new MockTextDefinition();

    static final TestContext.Empty CONTEXT = new TestContext.Empty();

    static List<Text> list(String... texts) {
        List<Text> results = new ArrayList<>();
        for (String text : texts) {
            results.add(new Text(text));
        }
        return results;
    }

    static DataModelSource source(String... texts) {
        return new SourceDataModelSource<>(DEFINITION, Sources.wrap(list(texts).iterator()));
    }

    static Provider<Source<Text>> provider(final String... texts) {
        return new Provider<Source<Text>>() {
            @Override
            public Source<Text> open() throws IOException, InterruptedException {
                return Sources.wrap(list(texts).iterator());
            }
            @Override
            public void close() throws IOException {
                return;
            }
        };
    }

    static DataModelSourceFactory factory(String... texts) {
        return new SourceDataModelSourceFactory(provider(texts));
    }

    static <T> void verify(DataModelSourceFactory target, DataModelDefinition<T> def, Collection<T> expected) {
        try (DataModelSource source = target.createSource(def, CONTEXT)) {
            Set<Object> set = new HashSet<Object>(expected);
            while (true) {
                DataModelReflection next = source.next();
                if (next == null) {
                    break;
                }
                T object = def.toObject(next);
                assertThat(object.toString(), set.contains(object), is(true));
                set.remove(object);
            }
            assertThat(set, hasSize(0));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private FlowDriverPortTestHelper() {
        return;
    }
}
