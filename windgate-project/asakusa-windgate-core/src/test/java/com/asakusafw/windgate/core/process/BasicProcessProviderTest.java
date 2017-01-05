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
package com.asakusafw.windgate.core.process;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.runtime.core.context.RuntimeContext.ExecutionMode;
import com.asakusafw.runtime.core.context.RuntimeContextKeeper;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.ProfileContext;
import com.asakusafw.windgate.core.resource.MockDrainDriver;
import com.asakusafw.windgate.core.resource.MockSourceDriver;

/**
 * Test for {@link BasicProcessProvider}.
 */
public class BasicProcessProviderTest {

    /**
     * Keeps runtime context.
     */
    @Rule
    public final RuntimeContextKeeper rc = new RuntimeContextKeeper();

    BasicProcessProvider provider = new BasicProcessProvider();
    {
        provider.configure(new ProcessProfile(
                "plain",
                BasicProcessProvider.class,
                ProfileContext.system(BasicProcessProvider.class.getClassLoader()),
                Collections.emptyMap()));
    }

    /**
     * Test method for {@link BasicProcessProvider#execute(com.asakusafw.windgate.core.resource.DriverFactory, ProcessScript)}.
     * @throws IOException if failed
     */
    @Test
    public void execute() throws IOException {
        MockDriverFactory factory = new MockDriverFactory();
        MockSourceDriver<String> source = factory.add("testing", new MockSourceDriver<String>("source"));
        MockDrainDriver<String> drain = factory.add("testing", new MockDrainDriver<String>("drain"));
        ProcessScript<String> script = new ProcessScript<>(
                "testing", "plain", String.class, driver("source"), driver("drain"));

        List<String> data = Arrays.asList("Hello", "world", "!");
        source.setIterable(data);
        provider.execute(factory, script);

        assertThat(drain.getResults(), is(data));
    }

    /**
     * Test method for {@link BasicProcessProvider#execute(com.asakusafw.windgate.core.resource.DriverFactory, ProcessScript)}.
     * @throws IOException expected
     */
    @Test(expected = IOException.class)
    public void execute_invalid_source() throws IOException {
        MockDriverFactory factory = new MockDriverFactory();
        ProcessScript<String> script = new ProcessScript<>(
                "testing", "plain", String.class, driver("source"), driver("drain"));

        provider.execute(factory, script);
    }

    /**
     * Test method for {@link BasicProcessProvider#execute(com.asakusafw.windgate.core.resource.DriverFactory, ProcessScript)}.
     * @throws IOException expected
     */
    @Test(expected = IOException.class)
    public void execute_invalid_drain() throws IOException {
        MockDriverFactory factory = new MockDriverFactory();
        MockSourceDriver<String> source = factory.add("testing", new MockSourceDriver<String>("source"));
        ProcessScript<String> script = new ProcessScript<>(
                "testing", "plain", String.class, driver("source"), driver("drain"));

        List<String> data = Arrays.asList("Hello", "world", "!");
        source.setIterable(data);
        provider.execute(factory, script);
    }

    /**
     * Test method for {@link BasicProcessProvider#execute(com.asakusafw.windgate.core.resource.DriverFactory, ProcessScript)}.
     * @throws IOException expected
     */
    @Test(expected = IOException.class)
    public void execute_transfer_failed() throws IOException {
        MockDriverFactory factory = new MockDriverFactory();
        factory.add("testing", new MockSourceDriver<String>("source"));
        factory.add("testing", new MockDrainDriver<String>("drain"));
        ProcessScript<String> script = new ProcessScript<>(
                "testing", "plain", String.class, driver("source"), driver("drain"));

        provider.execute(factory, script);
    }

    /**
     * Test method for {@link BasicProcessProvider#execute(com.asakusafw.windgate.core.resource.DriverFactory, ProcessScript)}.
     * @throws IOException expected
     */
    @Test
    public void execute_simulated() throws IOException {
        RuntimeContext.set(RuntimeContext.DEFAULT.mode(ExecutionMode.SIMULATION));

        MockDriverFactory factory = new MockDriverFactory();
        factory.add("testing", new MockSourceDriver<String>("source") {

            @Override
            public void prepare() throws IOException {
                throw new AssertionError();
            }

            @Override
            public boolean next() throws IOException {
                throw new AssertionError();
            }

            @Override
            public String get() throws IOException {
                throw new AssertionError();
            }

            @Override
            public void close() throws IOException {
                return;
            }
        });
        factory.add("testing", new MockDrainDriver<String>("drain") {

            @Override
            public void prepare() throws IOException {
                throw new AssertionError();
            }

            @Override
            public void put(String object) throws IOException {
                throw new AssertionError();
            }

            @Override
            public void close() throws IOException {
                return;
            }
        });
        ProcessScript<String> script = new ProcessScript<>(
                "testing", "plain", String.class, driver("source"), driver("drain"));

        provider.execute(factory, script); // no exceptions
    }

    private DriverScript driver(String name) {
        return new DriverScript(name, Collections.emptyMap());
    }
}
