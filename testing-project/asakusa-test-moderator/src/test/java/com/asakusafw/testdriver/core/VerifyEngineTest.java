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
package com.asakusafw.testdriver.core;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Test for {@link VerifyEngine}.
 * @since 0.2.0
 */
public class VerifyEngineTest {

    /**
     * single perfectmatch.
     * @throws Exception if failed
     */
    @Test
    public void single() throws Exception {
        VerifyEngine engine = new VerifyEngine(new Rule());
        engine.addExpected(source("hello:world"));

        List<Difference> d1 = engine.inspectInput(source("hello:world"));
        assertThat(d1.size(), is(0));

        List<Difference> d2 = engine.inspectRest();
        assertThat(d2.size(), is(0));
    }

    /**
     * single value mismatch.
     * @throws Exception if failed
     */
    @Test
    public void mismatch_value() throws Exception {
        VerifyEngine engine = new VerifyEngine(new Rule());
        engine.addExpected(source("hello:world!"));

        List<Difference> d1 = engine.inspectInput(source("hello:world"));
        assertThat(d1.size(), is(1));

        List<Difference> d2 = engine.inspectRest();
        assertThat(d2.size(), is(0));
    }

    /**
     * single key mismatch.
     * @throws Exception if failed
     */
    @Test
    public void mismatch_key() throws Exception {
        VerifyEngine engine = new VerifyEngine(new Rule());
        engine.addExpected(source("hello!:world"));

        List<Difference> d1 = engine.inspectInput(source("hello:world"));
        assertThat(d1.size(), is(1));

        List<Difference> d2 = engine.inspectRest();
        assertThat(d2.size(), is(1));
    }

    DataModelSource source(String... values) {
        return new IteratorDataModelSource(
                ValueDefinition.of(String.class),
                Arrays.asList(values).iterator());
    }

    static class Rule implements VerifyRule {

        private final DataModelDefinition<String> def = ValueDefinition.of(String.class);

        @Override
        public Object getKey(DataModelReflection target) {
            String string = def.toObject(target);
            String[] split = string.split(":", 2);
            return split[0];
        }

        @Override
        public Object verify(DataModelReflection expected, DataModelReflection actual) {
            if (expected == null || actual == null) {
                return "invalid";
            }
            String ex = def.toObject(expected).split(":", 2)[1];
            String ac = def.toObject(actual).split(":", 2)[1];
            return ex.equals(ac) ? null : "mismatch";
        }
    }
}
