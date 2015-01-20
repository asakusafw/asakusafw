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
package com.asakusafw.yaess.bootstrap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionScriptHandlerBase;
import com.asakusafw.yaess.core.HadoopScript;
import com.asakusafw.yaess.core.HadoopScriptHandler;
import com.asakusafw.yaess.core.ServiceProfile;

/**
 * {@link HadoopScriptHandler} for execution tracking.
 * @since 0.2.3
 */
public class TrackingHadoopScriptHandler extends ExecutionScriptHandlerBase implements HadoopScriptHandler {

    private volatile ExecutionTracker tracker;

    private volatile ExecutionTracker.Id id;

    @Override
    protected void doConfigure(
            ServiceProfile<?> profile,
            Map<String, String> desiredProperties,
            Map<String, String> desiredEnvironmentVariables) throws InterruptedException, IOException {
        Map<String, String> conf = profile.getConfiguration();
        String trackerClassName = conf.get(ExecutionTracker.KEY_CLASS);
        String trackingId = conf.get(ExecutionTracker.KEY_ID);

        assertThat(trackerClassName, is(notNullValue()));
        assertThat(trackingId, is(notNullValue()));

        try {
            Class<?> trackerClass = profile.getContext().getClassLoader().loadClass(trackerClassName);
            this.tracker = trackerClass.asSubclass(ExecutionTracker.class).newInstance();
            this.id = ExecutionTracker.Id.get(trackingId);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void execute(
            ExecutionMonitor monitor,
            ExecutionContext context,
            HadoopScript script) throws InterruptedException, IOException {
        monitor.open(1);
        try {
            ExecutionTracker.Record record = new ExecutionTracker.Record(context, script, this);
            tracker.add(id, record);
        } finally {
            monitor.close();
        }
    }

    @Override
    public void setUp(
            ExecutionMonitor monitor,
            ExecutionContext context) throws InterruptedException, IOException {
        monitor.open(1);
        try {
            ExecutionTracker.Record record = new ExecutionTracker.Record(context, null, this);
            tracker.add(id, record);
        } finally {
            monitor.close();
        }
    }

    @Override
    public void cleanUp(
            ExecutionMonitor monitor,
            ExecutionContext context) throws InterruptedException, IOException {
        monitor.open(1);
        try {
            ExecutionTracker.Record record = new ExecutionTracker.Record(context, null, this);
            tracker.add(id, record);
        } finally {
            monitor.close();
        }
    }
}
