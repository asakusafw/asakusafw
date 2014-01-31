/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.utils.io;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * Test for {@link Sources}.
 */
public class SourcesTest {

    /**
     * simple test for {@link Sources#wrap(java.util.Iterator)}.
     * @throws Exception if failed
     */
    @Test
    public void wrap() throws Exception {
        List<String> objects = dump(Sources.wrap(iter("1", "2", "3")));
        assertThat(objects, contains("1", "2", "3"));
    }

    /**
     * empty elements for {@link Sources#wrap(java.util.Iterator)}.
     * @throws Exception if failed
     */
    @Test
    public void wrap_empty() throws Exception {
        List<String> objects = dump(Sources.wrap(this.<String>iter()));
        assertThat(objects, hasSize(0));
    }

    /**
     * Test method for {@link Sources#concat(java.util.List)}.
     * @throws Exception if failed
     */
    @Test
    public void concat() throws Exception {
        List<Source<String>> sources = new ArrayList<Source<String>>();
        sources.add(Sources.wrap(iter("1", "2")));
        sources.add(Sources.wrap(iter("3", "4")));
        sources.add(Sources.wrap(iter("5", "6")));
        List<String> objects = dump(Sources.concat(sources));

        assertThat(objects, contains("1", "2", "3", "4", "5", "6"));
    }

    /**
     * empty elements for {@link Sources#concat(java.util.List)}.
     * @throws Exception if failed
     */
    @Test
    public void concat_empty() throws Exception {
        List<Source<String>> sources = new ArrayList<Source<String>>();
        List<String> objects = dump(Sources.concat(sources));

        assertThat(objects, hasSize(0));
    }

    private <T> Iterator<T> iter(T... values) {
        return Arrays.asList(values).iterator();
    }

    private <T> List<T> dump(Source<T> source) throws IOException, InterruptedException {
        try {
            List<T> results = new ArrayList<T>();
            while (source.next()) {
                results.add(source.get());
            }
            return results;
        } finally {
            source.close();
        }
    }
}
