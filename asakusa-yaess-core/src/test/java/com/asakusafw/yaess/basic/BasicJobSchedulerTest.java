/**
 * Copyright 2011-2012 Asakusa Framework Team.
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.Job;
import com.asakusafw.yaess.core.JobScheduler;
import com.asakusafw.yaess.core.PhaseMonitor;
import com.asakusafw.yaess.core.ProfileContext;
import com.asakusafw.yaess.core.ServiceProfile;

/**
 * Test for {@link BasicJobScheduler}.
 */
public class BasicJobSchedulerTest {

    private static final ExecutionContext CONTEXT = new ExecutionContext(
            "b", "f", "e", ExecutionPhase.MAIN, Collections.<String, String>emptyMap());

    /**
     * Simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        ServiceProfile<JobScheduler> profile = new ServiceProfile<JobScheduler>(
                "testing", BasicJobScheduler.class, conf, ProfileContext.system(getClass().getClassLoader()));

        JobScheduler instance = profile.newInstance();

        List<Mock> jobs = new ArrayList<Mock>();
        jobs.add(new Mock("a"));
        instance.execute(PhaseMonitor.NULL, CONTEXT, jobs, JobScheduler.STRICT);
        Set<String> rest = collectRest(jobs);
        assertThat(rest.size(), is(0));
    }

    /**
     * Multiple execution.
     * @throws Exception if failed
     */
    @Test
    public void multiple() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        ServiceProfile<JobScheduler> profile = new ServiceProfile<JobScheduler>(
                "testing", BasicJobScheduler.class, conf, ProfileContext.system(getClass().getClassLoader()));

        JobScheduler instance = profile.newInstance();

        List<Mock> jobs = new ArrayList<Mock>();
        jobs.add(new Mock("a"));
        jobs.add(new Mock("b"));
        jobs.add(new Mock("c"));
        instance.execute(PhaseMonitor.NULL, CONTEXT, jobs, JobScheduler.STRICT);
        Set<String> rest = collectRest(jobs);
        assertThat(rest.size(), is(0));
    }

    /**
     * with dependencies.
     * @throws Exception if failed
     */
    @Test
    public void dependencies() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        ServiceProfile<JobScheduler> profile = new ServiceProfile<JobScheduler>(
                "testing", BasicJobScheduler.class, conf, ProfileContext.system(getClass().getClassLoader()));

        JobScheduler instance = profile.newInstance();

        AtomicInteger group = new AtomicInteger();
        List<Mock> jobs = new ArrayList<Mock>();
        jobs.add(new Mock(group, "b", "a"));
        jobs.add(new Mock(group, "d", "b", "c"));
        jobs.add(new Mock(group, "a"));
        jobs.add(new Mock(group, "c", "a"));
        instance.execute(PhaseMonitor.NULL, CONTEXT, jobs, JobScheduler.STRICT);
        Set<String> rest = collectRest(jobs);
        assertThat(rest.size(), is(0));

        assertThat(ordinary(jobs, "a"), lessThan(ordinary(jobs, "b")));
        assertThat(ordinary(jobs, "a"), lessThan(ordinary(jobs, "c")));
        assertThat(ordinary(jobs, "b"), lessThan(ordinary(jobs, "d")));
        assertThat(ordinary(jobs, "c"), lessThan(ordinary(jobs, "d")));
    }

    /**
     * with cyclic dependencies.
     * @throws Exception if failed
     */
    @Test
    public void cyclic() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        ServiceProfile<JobScheduler> profile = new ServiceProfile<JobScheduler>(
                "testing", BasicJobScheduler.class, conf, ProfileContext.system(getClass().getClassLoader()));

        JobScheduler instance = profile.newInstance();

        AtomicInteger group = new AtomicInteger();
        List<Mock> jobs = new ArrayList<Mock>();
        jobs.add(new Mock(group, "a"));
        jobs.add(new Mock(group, "b", "a", "d"));
        jobs.add(new Mock(group, "c", "b"));
        jobs.add(new Mock(group, "d", "c"));
        jobs.add(new Mock(group, "e", "d"));
        try {
            instance.execute(PhaseMonitor.NULL, CONTEXT, jobs, JobScheduler.STRICT);
            fail();
        } catch (IOException e) {
            // ok.
        }
        Set<String> rest = collectRest(jobs);
        assertThat(rest.size(), is(4));
        assertThat(rest, hasItem("b"));
        assertThat(rest, hasItem("c"));
        assertThat(rest, hasItem("d"));
        assertThat(rest, hasItem("e"));
    }

    /**
     * Job failed.
     * @throws Exception if failed
     */
    @Test
    public void fail_job() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        ServiceProfile<JobScheduler> profile = new ServiceProfile<JobScheduler>(
                "testing", BasicJobScheduler.class, conf, ProfileContext.system(getClass().getClassLoader()));

        JobScheduler instance = profile.newInstance();

        List<Mock> jobs = new ArrayList<Mock>();
        jobs.add(new Mock("a") {
            @Override
            protected void hook() throws IOException {
                throw new IOException();
            }
        });
        try {
            instance.execute(PhaseMonitor.NULL, CONTEXT, jobs, JobScheduler.STRICT);
            fail();
        } catch (IOException e) {
            // ok.
        }
        Set<String> rest = collectRest(jobs);
        assertThat(rest.size(), is(0));
    }

    /**
     * Job failed.
     * @throws Exception if failed
     */
    @Test
    public void fail_besteffort() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        ServiceProfile<JobScheduler> profile = new ServiceProfile<JobScheduler>(
                "testing", BasicJobScheduler.class, conf, ProfileContext.system(getClass().getClassLoader()));

        JobScheduler instance = profile.newInstance();

        List<Mock> jobs = new ArrayList<Mock>();
        jobs.add(new Mock("a") {
            @Override
            protected void hook() throws IOException {
                throw new IOException();
            }
        });
        jobs.add(new Mock("b"));
        jobs.add(new Mock("c"));
        try {
            instance.execute(PhaseMonitor.NULL, CONTEXT, jobs, JobScheduler.BEST_EFFORT);
            fail();
        } catch (IOException e) {
            // ok.
        }
        Set<String> rest = collectRest(jobs);
        assertThat(rest.size(), is(0));
    }

    /**
     * Job failed and stucked.
     * @throws Exception if failed
     */
    @Test
    public void fail_stuck() throws Exception {
        Map<String, String> conf = new HashMap<String, String>();
        ServiceProfile<JobScheduler> profile = new ServiceProfile<JobScheduler>(
                "testing", BasicJobScheduler.class, conf, ProfileContext.system(getClass().getClassLoader()));

        JobScheduler instance = profile.newInstance();

        List<Mock> jobs = new ArrayList<Mock>();
        jobs.add(new Mock("a") {
            @Override
            protected void hook() throws IOException {
                throw new IOException();
            }
        });
        jobs.add(new Mock("b"));
        jobs.add(new Mock("c", "a", "b"));
        try {
            instance.execute(PhaseMonitor.NULL, CONTEXT, jobs, JobScheduler.BEST_EFFORT);
            fail();
        } catch (IOException e) {
            // ok.
        }
        Set<String> rest = collectRest(jobs);
        assertThat(rest.size(), is(1));
        assertThat(rest, hasItem("c"));
    }

    private int ordinary(List<Mock> jobs, String name) {
        for (Mock mock : jobs) {
            if (mock.getId().equals(name)) {
                return mock.count;
            }
        }
        throw new AssertionError(name);
    }

    private Set<String> collectRest(List<Mock> jobs) {
        Set<String> results = new HashSet<String>();
        for (Mock mock : jobs) {
            if (mock.executed == false) {
                results.add(mock.id);
            }
        }
        return results;
    }

    private static class Mock extends Job {

        private final AtomicInteger counter;

        final String id;

        final Set<String> blockers;

        volatile boolean executed;

        volatile int count;

        Mock(String id, String... blockers) {
            this(new AtomicInteger(), id, blockers);
        }

        Mock(AtomicInteger c, String id, String... blockers) {
            assert id != null;
            assert blockers != null;
            this.counter = c;
            this.id = id;
            this.blockers = new HashSet<String>(Arrays.asList(blockers));
        }

        @Override
        public void execute(ExecutionMonitor monitor, ExecutionContext context)
                throws InterruptedException, IOException {
            monitor.open(1);
            try {
                executed = true;
                count = counter.incrementAndGet();
                hook();
            } finally {
                monitor.close();
            }
        }

        /**
         * @throws InterruptedException if interrupted
         * @throws IOException if failed
         */
        protected void hook() throws InterruptedException, IOException {
            return;
        }

        @Override
        public String getLabel() {
            return id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Set<String> getBlockerIds() {
            return blockers;
        }

        @Override
        public String getResourceId() {
            return "testing";
        }
    }
}
