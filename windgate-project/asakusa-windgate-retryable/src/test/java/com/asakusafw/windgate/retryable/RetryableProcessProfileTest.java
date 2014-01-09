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
package com.asakusafw.windgate.retryable;

import static com.asakusafw.windgate.retryable.RetryableProcessProfile.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.ProfileContext;
import com.asakusafw.windgate.core.process.ProcessProfile;
import com.asakusafw.windgate.core.process.ProcessProvider;
import com.asakusafw.windgate.core.resource.DriverFactory;

/**
 * Test for {@link RetryableProcessProfile}.
 */
public class RetryableProcessProfileTest {

    /**
     * Converts simple profile.
     * @throws Exception if failed
     */
    @Test
    public void convert_simple() throws Exception {
        ProcessProfile profile = profile(
                KEY_RETRY_COUNT, "1",
                KEY_COMPONENT, DummyProcess.class.getName());
        RetryableProcessProfile result = RetryableProcessProfile.convert(profile);
        assertThat(result.getRetryCount(), is(1));
        assertThat(result.getRetryInterval(), is(DEFAULT_RETRY_INTERVAL));
        assertThat(result.getComponent(), instanceOf(DummyProcess.class));
        ProcessProfile inner = ((DummyProcess) result.getComponent()).inner;
        assertThat(inner.getName(), is(not(profile.getName())));
        assertThat(inner.getConfiguration(), is(map()));
    }

    /**
     * Converts a profile with options.
     * @throws Exception if failed
     */
    @Test
    public void convert_options() throws Exception {
        ProcessProfile profile = profile(
                KEY_RETRY_COUNT, "10",
                KEY_RETRY_INTERVAL, "1000",
                KEY_COMPONENT, DummyProcess.class.getName(),
                PREFIX_COMPONENT + "hello1", "world1",
                PREFIX_COMPONENT + "hello2", "world2",
                PREFIX_COMPONENT + "hello3", "world3");
        RetryableProcessProfile result = RetryableProcessProfile.convert(profile);
        assertThat(result.getRetryCount(), is(10));
        assertThat(result.getRetryInterval(), is(1000L));
        assertThat(result.getComponent(), instanceOf(DummyProcess.class));
        ProcessProfile inner = ((DummyProcess) result.getComponent()).inner;
        assertThat(inner.getName(), is(not(profile.getName())));
        assertThat(inner.getConfiguration(), is(map(
                "hello1", "world1",
                "hello2", "world2",
                "hello3", "world3")));
    }

    /**
     * Attempts to convert profile without retry count.
     * @throws Exception if failed
     */
    @Test
    public void convert_count_unknown() throws Exception {
        ProcessProfile profile = profile(
                KEY_COMPONENT, DummyProcess.class.getName());
        try {
            RetryableProcessProfile.convert(profile);
            fail();
        } catch (IllegalArgumentException e) {
            // ok.
        }
    }

    /**
     * Attempts to convert profile with invalid retry count.
     * @throws Exception if failed
     */
    @Test
    public void convert_count_invalid() throws Exception {
        ProcessProfile profile = profile(
                KEY_RETRY_COUNT, "__UNKNOWN__",
                KEY_COMPONENT, DummyProcess.class.getName());
        try {
            RetryableProcessProfile.convert(profile);
            fail();
        } catch (IllegalArgumentException e) {
            // ok.
        }
    }

    /**
     * Attempts to convert profile with invalid retry count.
     * @throws Exception if failed
     */
    @Test
    public void convert_count_illegal() throws Exception {
        ProcessProfile profile = profile(
                KEY_RETRY_COUNT, "0",
                KEY_COMPONENT, DummyProcess.class.getName());
        try {
            RetryableProcessProfile.convert(profile);
            fail();
        } catch (IllegalArgumentException e) {
            // ok.
        }
    }

    /**
     * Attempts to convert profile with invalid retry interval.
     * @throws Exception if failed
     */
    @Test
    public void convert_interval_invalid() throws Exception {
        ProcessProfile profile = profile(
                KEY_RETRY_INTERVAL, "__UNKNOWN__",
                KEY_COMPONENT, DummyProcess.class.getName());
        try {
            RetryableProcessProfile.convert(profile);
            fail();
        } catch (IllegalArgumentException e) {
            // ok.
        }
    }

    /**
     * Attempts to convert profile with invalid retry interval.
     * @throws Exception if failed
     */
    @Test
    public void convert_interval_illeval() throws Exception {
        ProcessProfile profile = profile(
                KEY_RETRY_INTERVAL, "-1",
                KEY_COMPONENT, DummyProcess.class.getName());
        try {
            RetryableProcessProfile.convert(profile);
            fail();
        } catch (IllegalArgumentException e) {
            // ok.
        }
    }

    /**
     * Attempts to convert profile without component provider class.
     * @throws Exception if failed
     */
    @Test
    public void convert_component_missing() throws Exception {
        ProcessProfile profile = profile(
                KEY_RETRY_COUNT, "1");
        try {
            RetryableProcessProfile.convert(profile);
            fail();
        } catch (IllegalArgumentException e) {
            // ok.
        }
    }

    /**
     * Attempts to convert profile with invalid component provider class.
     * @throws Exception if failed
     */
    @Test
    public void convert_component_unknown() throws Exception {
        ProcessProfile profile = profile(
                KEY_RETRY_COUNT, "1",
                KEY_COMPONENT, "__UNKNOWN__");
        try {
            RetryableProcessProfile.convert(profile);
            fail();
        } catch (IllegalArgumentException e) {
            // ok.
        }
    }

    /**
     * Attempts to convert profile with invalid component provider class.
     * @throws Exception if failed
     */
    @Test
    public void convert_component_invalid() throws Exception {
        ProcessProfile profile = profile(
                KEY_RETRY_COUNT, "1",
                KEY_COMPONENT, String.class.getName());
        try {
            RetryableProcessProfile.convert(profile);
            fail();
        } catch (IllegalArgumentException e) {
            // ok.
        }
    }

    /**
     * Attempts to convert profile with invalid component provider class.
     * @throws Exception if failed
     */
    @Test
    public void convert_component_failed() throws Exception {
        ProcessProfile profile = profile(
                KEY_RETRY_COUNT, "1",
                KEY_COMPONENT, InvalidProcess.class.getName());
        try {
            RetryableProcessProfile.convert(profile);
            fail();
        } catch (IOException e) {
            // ok.
        }
    }

    private ProcessProfile profile(String... conf) {
        ProcessProfile profile = new ProcessProfile(
                "dummy",
                RetryableProcessProvider.class,
                ProfileContext.system(getClass().getClassLoader()),
                map(conf));
        return profile;
    }

    private Map<String, String> map(String... keyValuePairs) {
        assertThat(keyValuePairs.length % 2, is(0));
        Map<String, String> results = new HashMap<String, String>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            results.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return results;
    }

    /**
     * dummy.
     */
    public static class DummyProcess extends ProcessProvider {

        ProcessProfile inner;

        @Override
        protected void configure(ProcessProfile profile) throws IOException {
            this.inner = profile;
        }

        @Override
        public <T> void execute(DriverFactory drivers, ProcessScript<T> script) throws IOException {
            return;
        }
    }

    /**
     * invalid.
     */
    public static class InvalidProcess extends ProcessProvider {

        @Override
        protected void configure(ProcessProfile profile) throws IOException {
            throw new IOException();
        }

        @Override
        public <T> void execute(DriverFactory drivers, ProcessScript<T> script) throws IOException {
            return;
        }
    }
}
