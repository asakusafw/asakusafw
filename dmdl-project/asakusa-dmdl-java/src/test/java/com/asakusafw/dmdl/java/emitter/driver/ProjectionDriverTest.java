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
package com.asakusafw.dmdl.java.emitter.driver;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import com.asakusafw.dmdl.java.GeneratorTesterRoot;

/**
 * Test for {@link ProjectionDriver}.
 */
public class ProjectionDriverTest extends GeneratorTesterRoot {

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        emitDrivers.add(new ProjectionDriver());
    }

    /**
     * projections.
     */
    @Test
    public void projection() {
        ModelLoader loader = generate();

        Class<?> a = loader.modelType("A");
        Class<?> b = loader.modelType("B");
        Class<?> c = loader.modelType("C");
        Class<?> d = loader.modelType("D");

        assertThat(a.getInterfaces(), not(includes(b)));
        assertThat(a.getInterfaces(), not(includes(c)));
        assertThat(a.getInterfaces(), not(includes(d)));

        assertThat(b.getInterfaces(), includes(a));
        assertThat(b.getInterfaces(), not(includes(c)));
        assertThat(b.getInterfaces(), not(includes(d)));

        assertThat(c.getInterfaces(), not(includes(a)));
        assertThat(c.getInterfaces(), not(includes(b)));
        assertThat(c.getInterfaces(), not(includes(d)));

        assertThat(d.getInterfaces(), not(includes(a)));
        assertThat(d.getInterfaces(), includes(b));
        assertThat(d.getInterfaces(), includes(c));
    }

    private Matcher<Object[]> includes(Object object) {
        return hasItemInArray(object);
    }
}
