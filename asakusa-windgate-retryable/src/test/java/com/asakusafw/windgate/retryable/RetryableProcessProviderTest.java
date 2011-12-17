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
package com.asakusafw.windgate.retryable;

import static junit.framework.Assert.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.ProfileContext;
import com.asakusafw.windgate.core.process.ProcessProfile;
import com.asakusafw.windgate.core.process.ProcessProvider;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.DriverFactory;
import com.asakusafw.windgate.core.resource.SourceDriver;

/**
 * Test for {@link RetryableProcessProvider}.
 */
public class RetryableProcessProviderTest {

    /**
     * normal run.
     * @throws Exception if failed
     */
    @Test
    public void execute_simple() throws Exception {
        ProcessProfile profile = profile(2, Action.SUCCESS);
        ProcessProvider provider = profile.createProvider();
        provider.execute(factory(), script());
    }

    /**
     * retry once.
     * @throws Exception if failed
     */
    @Test
    public void execute_retry1() throws Exception {
        ProcessProfile profile = profile(2, Action.EXCEPTION, Action.SUCCESS);
        ProcessProvider provider = profile.createProvider();
        provider.execute(factory(), script());
    }

    /**
     * retry twice.
     * @throws Exception if failed
     */
    @Test
    public void execute_retry2() throws Exception {
        ProcessProfile profile = profile(2, Action.EXCEPTION, Action.EXCEPTION, Action.SUCCESS);
        ProcessProvider provider = profile.createProvider();
        provider.execute(factory(), script());
    }

    /**
     * retry over.
     * @throws Exception if failed
     */
    @Test
    public void execute_retry_over() throws Exception {
        ProcessProfile profile = profile(2, Action.EXCEPTION, Action.EXCEPTION, Action.EXCEPTION, Action.SUCCESS);
        ProcessProvider provider = profile.createProvider();
        try {
            provider.execute(factory(), script());
            fail();
        } catch (IOException e) {
            // ok.
        }
    }

    /**
     * interrupted.
     * @throws Exception if failed
     */
    @Test
    public void execute_interrupted() throws Exception {
        ProcessProfile profile = profile(2, Action.INTERRUPT, Action.SUCCESS);
        ProcessProvider provider = profile.createProvider();
        try {
            provider.execute(factory(), script());
            fail();
        } catch (IOException e) {
            // ok.
        }
    }

    /**
     * container invalid.
     * @throws Exception if failed
     */
    @Test
    public void configure_invalid_container() throws Exception {
        ProcessProfile profile = profile(0, Action.EXCEPTION, Action.SUCCESS);
        try {
            profile.createProvider();
            fail();
        } catch (IOException e) {
            // ok.
        }
    }

    /**
     * component invalid.
     * @throws Exception if failed
     */
    @Test
    public void configure_invalid_component() throws Exception {
        ProcessProfile profile = profile(3);
        try {
            profile.createProvider();
            fail();
        } catch (IOException e) {
            // ok.
        }
    }

    private ProcessProfile profile(int retryCount, Action... actions) {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(RetryableProcessProfile.KEY_RETRY_COUNT, String.valueOf(retryCount));
        conf.put(RetryableProcessProfile.KEY_COMPONENT, Mock.class.getName());
        if (actions.length > 0) {
            StringBuilder buf = new StringBuilder();
            for (Action action : actions) {
                buf.append(action.name());
                buf.append(',');
            }
            buf.append(Action.FAIL.name());
            conf.put(RetryableProcessProfile.PREFIX_COMPONENT + Mock.KEY_ATTEMPTS, buf.toString());
        }
        return new ProcessProfile(
                "testing",
                RetryableProcessProvider.class,
                ProfileContext.system(getClass().getClassLoader()),
                conf);
    }

    private DriverFactory factory() {
        return DummyDriverFactory.INSTANCE;
    }

    private ProcessScript<String> script() {
        return new ProcessScript<String>(
                "dummy",
                "testing",
                String.class,
                new DriverScript("dummy", Collections.<String, String>emptyMap()),
                new DriverScript("dummy", Collections.<String, String>emptyMap()));
    }

    /**
     * Mock process provider.
     */
    public static class Mock extends ProcessProvider {

        static final String KEY_ATTEMPTS = "attempts";

        private volatile Iterator<Action> attempts;

        @Override
        protected void configure(ProcessProfile profile) throws IOException {
            String attemptsString = profile.getConfiguration().get(KEY_ATTEMPTS);
            if (attemptsString == null) {
                throw new IOException();
            }
            List<Action> actions = new ArrayList<Action>();
            for (String string : attemptsString.split(",")) {
                actions.add(Action.valueOf(string));
            }
            attempts = actions.iterator();
        }

        @Override
        public <T> void execute(DriverFactory drivers, ProcessScript<T> script) throws IOException {
            attempts.next().perform();
        }
    }

    private enum Action {

        SUCCESS {
            @Override
            public void perform() throws IOException {
                return;
            }
        },

        EXCEPTION {
            @Override
            public void perform() throws IOException {
                throw new IOException();
            }
        },

        INTERRUPT {
            @Override
            public void perform() throws IOException {
                throw new InterruptedIOException();
            }
        },

        FAIL {
            @Override
            public void perform() throws IOException {
                throw new AssertionError();
            }
        },
        ;

        public abstract void perform() throws IOException;
    }

    private static class DummyDriverFactory implements DriverFactory {

        static final DriverFactory INSTANCE = new DummyDriverFactory();

        @Override
        public <T> SourceDriver<T> createSource(ProcessScript<T> script) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> DrainDriver<T> createDrain(ProcessScript<T> script) throws IOException {
            throw new UnsupportedOperationException();
        }
    }
}
