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
package com.asakusafw.testdriver.json;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.Test;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.model.SimpleDataModelDefinition;

/**
 * Test for {@link JsonDataModelSource}.
 * @since 0.2.0
 */
public class JsonDataModelSourceTest {

    static final DataModelDefinition<Simple> SIMPLE = new SimpleDataModelDefinition<Simple>(Simple.class);

    /**
     * simple JSON.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        JsonDataModelSource source = open("simple");
        try {
            Simple s1 = SIMPLE.toObject(source.next());
            assertThat(s1.number, is(100));
            assertThat(source.next(), is(nullValue()));
        } finally {
            source.close();
        }
    }

    /**
     * multiple JSON.
     * @throws Exception if failed
     */
    @Test
    public void multiple() throws Exception {
        JsonDataModelSource source = open("multiple");
        try {
            Simple s1 = SIMPLE.toObject(source.next());
            assertThat(s1.number, is(100));

            Simple s2 = SIMPLE.toObject(source.next());
            assertThat(s2.number, is(nullValue()));
            assertThat(s2.text, is("Hello, world!"));

            Simple s3 = SIMPLE.toObject(source.next());
            assertThat(s3.booleanValue, is(true));
            assertThat(s3.doubleValue, is(100.5));

            assertThat(source.next(), is(nullValue()));
        } finally {
            source.close();
        }
    }

    /**
     * malformed JSON.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void malform() throws Exception {
        JsonDataModelSource source = open("malform");
        source.next();
    }

    private JsonDataModelSource open(String name) {
        URL resource = getClass().getResource(name + ".json");
        assertThat(name, resource, not(nullValue()));
        try {
            return new JsonDataModelSource(
                    resource.toURI(),
                    SIMPLE,
                    new InputStreamReader(resource.openStream(), "UTF-8"));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
