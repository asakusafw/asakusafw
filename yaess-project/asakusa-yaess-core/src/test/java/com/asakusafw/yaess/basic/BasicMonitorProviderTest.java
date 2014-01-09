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
package com.asakusafw.yaess.basic;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionMonitorProvider;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.ProfileContext;
import com.asakusafw.yaess.core.ServiceProfile;

/**
 * Test for {@link BasicMonitorProvider}.
 */
public class BasicMonitorProviderTest {

    private static final Map<String, String> EMPTY = Collections.<String, String>emptyMap();

    private static final ExecutionContext CONTEXT = new ExecutionContext("b", "f", "e", ExecutionPhase.MAIN, EMPTY);

    /**
     * Simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        ServiceProfile<ExecutionMonitorProvider> profile = new ServiceProfile<ExecutionMonitorProvider>(
                "testing", BasicMonitorProvider.class, conf, ProfileContext.system(getClass().getClassLoader()));

        ExecutionMonitorProvider instance = profile.newInstance();
        ExecutionMonitor monitor = instance.newInstance(CONTEXT);
        monitor.open(3);
        try {
            monitor.progressed(1);
            monitor.progressed(1);
            monitor.progressed(1);
        } finally {
            monitor.close();
        }
    }

    /**
     * with step logging.
     * @throws Exception if failed
     */
    @Test
    public void with_stepUnit() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(BasicMonitorProvider.KEY_STEP_UNIT, "0.1");
        ServiceProfile<ExecutionMonitorProvider> profile = new ServiceProfile<ExecutionMonitorProvider>(
                "testing", BasicMonitorProvider.class, conf, ProfileContext.system(getClass().getClassLoader()));

        ExecutionMonitorProvider instance = profile.newInstance();
        ExecutionMonitor monitor = instance.newInstance(CONTEXT);
        monitor.open(100);
        try {
            for (int i = 0; i < 100; i++) {
                monitor.progressed(1);
            }
        } finally {
            monitor.close();
        }
    }

    /**
     * with step logging.
     * @throws Exception if failed
     */
    @Test
    public void large_stepUnit() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(BasicMonitorProvider.KEY_STEP_UNIT, "0.5");
        ServiceProfile<ExecutionMonitorProvider> profile = new ServiceProfile<ExecutionMonitorProvider>(
                "testing", BasicMonitorProvider.class, conf, ProfileContext.system(getClass().getClassLoader()));

        ExecutionMonitorProvider instance = profile.newInstance();
        ExecutionMonitor monitor = instance.newInstance(CONTEXT);
        monitor.open(100);
        try {
            for (int i = 0; i < 100; i++) {
                monitor.progressed(1);
            }
        } finally {
            monitor.close();
        }
    }

    /**
     * with step logging.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void invalid_stepUnit() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        conf.put(BasicMonitorProvider.KEY_STEP_UNIT, "INVALID");
        ServiceProfile<ExecutionMonitorProvider> profile = new ServiceProfile<ExecutionMonitorProvider>(
                "testing", BasicMonitorProvider.class, conf, ProfileContext.system(getClass().getClassLoader()));

        profile.newInstance();
    }
}
