/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.windgate.core.resource;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.ProcessScript;

/**
 * Test for {@link DriverRepository}.
 */
public class DriverRepositoryTest {

    /**
     * Test method for {@link DriverRepository#createSource(com.asakusafw.windgate.core.ProcessScript)}.
     * @throws Exception if failed
     */
    @Test
    public void createSource() throws Exception {
        DriverRepository repo = new DriverRepository(Arrays.asList(new MockResourceMirror("test1")));
        ProcessScript<String> script = new ProcessScript<String>(
                "testing", "simple", String.class, driver("test1"), driver("test2"));

        SourceDriver<String> source = repo.createSource(script);
        assertThat(source, is(instanceOf(MockSourceDriver.class)));

        MockSourceDriver<String> mock = (MockSourceDriver<String>) source;
        assertThat(mock.name, is("test1"));
    }

    /**
     * Attempts to create a source driver but the corresponded resource was not registered.
     * @throws Exception expected
     */
    @Test(expected = IOException.class)
    public void createSource_missing() throws Exception {
        DriverRepository repo = new DriverRepository(Arrays.asList(new MockResourceMirror("test")));
        ProcessScript<String> script = new ProcessScript<String>(
                "testing", "simple", String.class, driver("MISSING"), driver("test"));

        repo.createSource(script);
    }

    /**
     * Test method for {@link DriverRepository#createDrain(com.asakusafw.windgate.core.ProcessScript)}.
     * @throws Exception if failed
     */
    @Test
    public void testCreateDrain() throws Exception {
        DriverRepository repo = new DriverRepository(Arrays.asList(new MockResourceMirror("test2")));
        ProcessScript<String> script = new ProcessScript<String>(
                "testing", "simple", String.class, driver("test1"), driver("test2"));

        DrainDriver<String> source = repo.createDrain(script);
        assertThat(source, is(instanceOf(MockDrainDriver.class)));

        MockDrainDriver<String> mock = (MockDrainDriver<String>) source;
        assertThat(mock.name, is("test2"));
    }

    /**
     * Attempts to create a drain driver but the corresponded resource was not registered.
     * @throws Exception expected
     */
    @Test(expected = IOException.class)
    public void createDrain_missing() throws Exception {
        DriverRepository repo = new DriverRepository(Arrays.asList(new MockResourceMirror("test")));
        ProcessScript<String> script = new ProcessScript<String>(
                "testing", "simple", String.class, driver("test"), driver("MISSING"));

        repo.createDrain(script);
    }

    private DriverScript driver(String name) {
        assert name != null;
        return new DriverScript(name, Collections.<String, String>emptyMap());
    }
}
