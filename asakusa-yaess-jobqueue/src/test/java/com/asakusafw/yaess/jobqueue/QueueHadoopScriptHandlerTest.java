/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.yaess.jobqueue;

import static com.asakusafw.yaess.jobqueue.client.JobStatus.Kind.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.HadoopScript;
import com.asakusafw.yaess.core.ProfileContext;
import com.asakusafw.yaess.core.ServiceProfile;
import com.asakusafw.yaess.core.VariableResolver;
import com.asakusafw.yaess.jobqueue.client.JobClient;
import com.asakusafw.yaess.jobqueue.client.JobId;
import com.asakusafw.yaess.jobqueue.client.JobScript;
import com.asakusafw.yaess.jobqueue.client.JobStatus;

/**
 * Test for {@link QueueHadoopScriptHandler}.
 */
public class QueueHadoopScriptHandlerTest {

    /**
     * simple execution.
     * @throws Exception if failed
     */
    @Test
    public void execute() throws Exception {
        MockJobClient c1 = new MockJobClient("testing", COMPLETED);
        JobClientProfile profile = new JobClientProfile("testing", list(c1), 1000, 10);
        QueueHadoopScriptHandler handler = create();
        handler.doConfigure(profile);
        ExecutionContext context = context();
        HadoopScript script = script();
        handler.execute(ExecutionMonitor.NULL, context, script);

        JobScript js = c1.registered.get("testing");
        assertThat(js, is(notNullValue()));
        assertThat(js.getBatchId(), is(context.getBatchId()));
        assertThat(js.getFlowId(), is(context.getFlowId()));
        assertThat(js.getExecutionId(), is(context.getExecutionId()));
        assertThat(js.getPhase(), is(context.getPhase()));
        assertThat(js.getStageId(), is(script.getId()));
        assertThat(js.getMainClassName(), is(script.getClassName()));
        assertThat(js.getArguments(), is(context.getArguments()));

        Map<String, String> properties = new HashMap<String, String>();
        properties.putAll(map("s", "service"));
        properties.putAll(script.getHadoopProperties());

        assertThat(js.getProperties(), is(properties));
        assertThat(js.getEnvironmentVariables(), is(script.getEnvironmentVariables()));
    }

    /**
     * execution with multi step polling.
     * @throws Exception if failed
     */
    @Test
    public void execute_step() throws Exception {
        MockJobClient c1 = new MockJobClient("testing", WAITING, RUNNING, COMPLETED);
        JobClientProfile profile = new JobClientProfile("testing", list(c1), 100, 10);
        QueueHadoopScriptHandler handler = create();
        handler.doConfigure(profile);
        ExecutionContext context = context();
        HadoopScript script = script();
        handler.execute(ExecutionMonitor.NULL, context, script);
    }

    /**
     * execution failed.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void execute_fail() throws Exception {
        MockJobClient c1 = new MockJobClient("testing");
        JobStatus fail = new JobStatus();
        fail.setKind(COMPLETED);
        fail.setExitCode(1);
        c1.add(fail);

        JobClientProfile profile = new JobClientProfile("testing", list(c1), 1000, 10);
        QueueHadoopScriptHandler handler = create();
        handler.doConfigure(profile);
        ExecutionContext context = context();
        HadoopScript script = script();
        handler.execute(ExecutionMonitor.NULL, context, script);
    }

    /**
     * execution aborted.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void execute_error() throws Exception {
        MockJobClient c1 = new MockJobClient("testing", ERROR);
        JobClientProfile profile = new JobClientProfile("testing", list(c1), 1000, 10);
        QueueHadoopScriptHandler handler = create();
        handler.doConfigure(profile);
        ExecutionContext context = context();
        HadoopScript script = script();
        handler.execute(ExecutionMonitor.NULL, context, script);
    }

    /**
     * execution aborted.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void execute_aborted() throws Exception {
        MockJobClient c1 = new MockJobClient("testing");
        JobClientProfile profile = new JobClientProfile("testing", list(c1), 1000, 10);
        QueueHadoopScriptHandler handler = create();
        handler.doConfigure(profile);
        ExecutionContext context = context();
        HadoopScript script = script();
        handler.execute(ExecutionMonitor.NULL, context, script);
    }

    /**
     * execute but failed to register.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void execute_register_failed() throws Exception {
        MockJobClient c1 = new MockJobClient(null, COMPLETED);
        JobClientProfile profile = new JobClientProfile("testing", list(c1), 1000, 10);
        QueueHadoopScriptHandler handler = create();
        handler.doConfigure(profile);
        ExecutionContext context = context();
        HadoopScript script = script();
        handler.execute(ExecutionMonitor.NULL, context, script);
    }

    /**
     * failover to registration.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void execute_register_failover() throws Exception {
        MockJobClient c1 = new MockJobClient(null, ERROR);
        MockJobClient c2 = new MockJobClient("testing", COMPLETED);
        JobClientProfile profile = new JobClientProfile("testing", list(c1), 1000, 10);
        QueueHadoopScriptHandler handler = create();
        handler.doConfigure(profile);
        ExecutionContext context = context();
        HadoopScript script = script();
        handler.execute(ExecutionMonitor.NULL, context, script);

        assertThat(c2.count, is(greaterThan(0)));
    }

    /**
     * failover to registration.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void execute_register_timeout() throws Exception {
        MockJobClient c1 = new MockJobClient("testing", ERROR) {
            @Override
            public JobId register(JobScript script) throws IOException, InterruptedException {
                Thread.sleep(10000);
                return super.register(script);
            }
        };
        MockJobClient c2 = new MockJobClient("testing", COMPLETED);
        JobClientProfile profile = new JobClientProfile("testing", list(c1), 100, 10);
        QueueHadoopScriptHandler handler = create();
        handler.doConfigure(profile);
        ExecutionContext context = context();
        HadoopScript script = script();
        handler.execute(ExecutionMonitor.NULL, context, script);

        assertThat(c2.count, is(greaterThan(0)));
    }

    /**
     * failover to registration.
     * @throws Exception if failed
     */
    @Test(expected = IOException.class)
    public void execute_round() throws Exception {
        MockJobClient c1 = new MockJobClient("testing", COMPLETED);
        MockJobClient c2 = new MockJobClient("testing", COMPLETED);
        JobClientProfile profile = new JobClientProfile("testing", list(c1), 1000, 10);
        QueueHadoopScriptHandler handler = create();
        handler.doConfigure(profile);
        ExecutionContext context = context();
        HadoopScript script = script();

        handler.execute(ExecutionMonitor.NULL, context, script);
        handler.execute(ExecutionMonitor.NULL, context, script);

        assertThat(c1.count, is(greaterThan(0)));
        assertThat(c2.count, is(greaterThan(0)));
    }

    /**
     * simple cleanup.
     * @throws Exception if failed
     */
    @Test
    public void cleanup() throws Exception {
        MockJobClient c1 = new MockJobClient("testing", COMPLETED);
        JobClientProfile profile = new JobClientProfile("testing", list(c1), 1000, 10);
        QueueHadoopScriptHandler handler = create();
        handler.doConfigure(profile);
        ExecutionContext context = context();
        handler.cleanUp(ExecutionMonitor.NULL, context);

        JobScript js = c1.registered.get("testing");
        assertThat(js, is(notNullValue()));
        assertThat(js.getBatchId(), is(context.getBatchId()));
        assertThat(js.getFlowId(), is(context.getFlowId()));
        assertThat(js.getExecutionId(), is(context.getExecutionId()));
        assertThat(js.getPhase(), is(context.getPhase()));
        assertThat(js.getMainClassName(), is(QueueHadoopScriptHandler.CLEANUP_STAGE_CLASS));
        assertThat(js.getArguments(), is(context.getArguments()));

        Map<String, String> properties = new HashMap<String, String>();
        properties.putAll(map("s", "service"));

        assertThat(js.getProperties(), is(properties));
    }

    QueueHadoopScriptHandler create() {
        ServiceProfile<QueueHadoopScriptHandler> profile = new ServiceProfile<QueueHadoopScriptHandler>(
                "testing",
                QueueHadoopScriptHandler.class,
                map("prop.s", "service", "1.url", "http://example.com"),
                new ProfileContext(getClass().getClassLoader(), new VariableResolver(map())));
        try {
            return profile.newInstance();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private HadoopScript script() {
        Map<String, String> prop = new HashMap<String, String>();
        prop.put("p", "prop");

        Map<String, String> env = new HashMap<String, String>();
        env.put("e", "env");

        return new HadoopScript("s", Collections.<String>emptySet(), "Cls", prop, env);
    }

    private ExecutionContext context() {
        Map<String, String> args = new HashMap<String, String>();
        args.put("a", "arg");
        return new ExecutionContext("b", "f", "e", ExecutionPhase.MAIN, args);
    }

    private <T> List<T> list(T... values) {
        return Arrays.asList(values);
    }

    private Map<String, String> map(String... keyValuePairs) {
        assert keyValuePairs.length % 2 == 0;
        Map<String, String> results = new HashMap<String, String>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            results.put(keyValuePairs[i + 0], keyValuePairs[i + 1]);
        }
        return results;
    }

    private static class MockJobClient implements JobClient {

        volatile int count;

        final Map<String, JobScript> registered = new ConcurrentHashMap<String, JobScript>();

        private final JobId jobId;

        private final LinkedList<JobStatus> sequence;

        private volatile boolean submitted;

        public MockJobClient(String id, JobStatus.Kind... sequence) {
            this.jobId = id == null ? null : new JobId(id);
            this.sequence = new LinkedList<JobStatus>();
            for (JobStatus.Kind kind : sequence) {
                JobStatus result = new JobStatus();
                result.setKind(kind);
                result.setJobId(id == null ? "DUMMY" : id);
                result.setExitCode(0);
                this.sequence.addLast(result);
            }
        }

        void add(JobStatus status) {
            sequence.add(status);
        }

        @Override
        public JobId register(JobScript script) throws IOException, InterruptedException {
            count++;
            if (jobId == null) {
                throw new IOException();
            }
            registered.put(jobId.getToken(), script);
            return jobId;
        }

        @Override
        public void submit(JobId id) throws IOException, InterruptedException {
            assertThat(id, is(jobId));
            submitted = true;
        }

        @Override
        public JobStatus getStatus(JobId id) throws IOException, InterruptedException {
            assertThat(id, is(jobId));
            if (submitted == false) {
                throw new IOException();
            }
            if (sequence.isEmpty()) {
                throw new IOException();
            }
            JobStatus status = sequence.removeFirst();
            return status;
        }
    }
}
