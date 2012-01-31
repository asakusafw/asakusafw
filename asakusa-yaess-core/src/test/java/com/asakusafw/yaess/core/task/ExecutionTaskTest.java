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
package com.asakusafw.yaess.core.task;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.yaess.core.CommandScript;
import com.asakusafw.yaess.core.ExecutionLock;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.ProfileContext;
import com.asakusafw.yaess.core.YaessProfile;
import com.asakusafw.yaess.core.task.ExecutionTracker.Record;

/**
 * Test for {@link ExecutionTask}.
 */
public class ExecutionTaskTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        SerialExecutionTracker.clear();
    }

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        SerialExecutionTracker.clear();
    }

    /**
     * Execute setup phase.
     * @throws Exception if failed
     */
    @Test
    public void phase_setup() throws Exception {
        ProfileBuilder prf = new ProfileBuilder(folder.getRoot());
        ExecutionTask task = prf.task();
        task.executePhase("batch", "testing", "f-setup", ExecutionPhase.SETUP);

        List<Record> results = SerialExecutionTracker.get(prf.trackingId);
        verifyPhaseOrder(results);

        assertThat(results.size(), is(2));
        List<Record> records = phase(results, "testing", ExecutionPhase.SETUP);
        assertThat(records, is(results));
    }

    /**
     * Execute initialize phase.
     * @throws Exception if failed
     */
    @Test
    public void phase_initialize() throws Exception {
        ProfileBuilder prf = new ProfileBuilder(folder.getRoot());
        ExecutionTask task = prf.task();
        task.executePhase("batch", "testing", "f-init", ExecutionPhase.INITIALIZE);

        List<Record> results = SerialExecutionTracker.get(prf.trackingId);
        verifyPhaseOrder(results);

        assertThat(results.size(), is(1));
        List<Record> records = phase(results, "testing", ExecutionPhase.INITIALIZE);
        assertThat(records, is(results));
    }

    /**
     * Execute import phase.
     * @throws Exception if failed
     */
    @Test
    public void phase_import() throws Exception {
        ProfileBuilder prf = new ProfileBuilder(folder.getRoot());
        ExecutionTask task = prf.task();
        task.executePhase("batch", "testing", "f-imp", ExecutionPhase.IMPORT);

        List<Record> results = SerialExecutionTracker.get(prf.trackingId);
        verifyPhaseOrder(results);

        assertThat(results.size(), is(2));
        List<Record> records = phase(results, "testing", ExecutionPhase.IMPORT);
        assertThat(records, is(results));
    }

    /**
     * Execute prologue phase.
     * @throws Exception if failed
     */
    @Test
    public void phase_prologue() throws Exception {
        ProfileBuilder prf = new ProfileBuilder(folder.getRoot());
        ExecutionTask task = prf.task();
        task.executePhase("batch", "testing", "f-pro", ExecutionPhase.PROLOGUE);

        List<Record> results = SerialExecutionTracker.get(prf.trackingId);
        verifyPhaseOrder(results);

        assertThat(results.size(), is(1));
        List<Record> records = phase(results, "testing", ExecutionPhase.PROLOGUE);
        assertThat(records, is(results));
    }

    /**
     * Execute main phase.
     * @throws Exception if failed
     */
    @Test
    public void phase_main() throws Exception {
        ProfileBuilder prf = new ProfileBuilder(folder.getRoot());
        ExecutionTask task = prf.task();
        task.executePhase("batch", "testing", "f1", ExecutionPhase.MAIN);

        List<Record> results = SerialExecutionTracker.get(prf.trackingId);
        verifyPhaseOrder(results);

        assertThat(results.size(), is(4));
        assertThat(id(results), is(set("a", "b", "c", "d")));
        checkScriptHappensBefore(results, "a", "b");
        checkScriptHappensBefore(results, "a", "c");
        checkScriptHappensBefore(results, "b", "d");
        checkScriptHappensBefore(results, "c", "d");

        List<Record> records = phase(results, "testing", ExecutionPhase.MAIN);
        assertThat(records, is(results));
    }

    /**
     * Execute epilogue phase.
     * @throws Exception if failed
     */
    @Test
    public void phase_epilogue() throws Exception {
        ProfileBuilder prf = new ProfileBuilder(folder.getRoot());
        ExecutionTask task = prf.task();
        task.executePhase("batch", "testing", "f-pro", ExecutionPhase.EPILOGUE);

        List<Record> results = SerialExecutionTracker.get(prf.trackingId);
        verifyPhaseOrder(results);

        assertThat(results.size(), is(1));
        List<Record> records = phase(results, "testing", ExecutionPhase.EPILOGUE);
        assertThat(records, is(results));
    }

    /**
     * Execute export phase.
     * @throws Exception if failed
     */
    @Test
    public void phase_export() throws Exception {
        ProfileBuilder prf = new ProfileBuilder(folder.getRoot());
        ExecutionTask task = prf.task();
        task.executePhase("batch", "testing", "f-exp", ExecutionPhase.EXPORT);

        List<Record> results = SerialExecutionTracker.get(prf.trackingId);
        verifyPhaseOrder(results);

        assertThat(results.size(), is(2));
        List<Record> records = phase(results, "testing", ExecutionPhase.EXPORT);
        assertThat(records, is(results));
    }

    /**
     * Execute finalize phase.
     * @throws Exception if failed
     */
    @Test
    public void phase_finalize() throws Exception {
        ProfileBuilder prf = new ProfileBuilder(folder.getRoot());
        ExecutionTask task = prf.task();
        task.executePhase("batch", "testing", "f-fin", ExecutionPhase.FINALIZE);

        List<Record> results = SerialExecutionTracker.get(prf.trackingId);
        verifyPhaseOrder(results);

        assertThat(results.size(), is(2));
        List<Record> records = phase(results, "testing", ExecutionPhase.FINALIZE);
        assertThat(records, is(results));
    }

    /**
     * Execute cleanup phase.
     * @throws Exception if failed
     */
    @Test
    public void phase_cleanup() throws Exception {
        ProfileBuilder prf = new ProfileBuilder(folder.getRoot());
        ExecutionTask task = prf.task();
        task.executePhase("batch", "testing", "f-clean", ExecutionPhase.CLEANUP);

        List<Record> results = SerialExecutionTracker.get(prf.trackingId);
        verifyPhaseOrder(results);

        assertThat(results.size(), is(2));

        List<Record> records = phase(results, "testing", ExecutionPhase.CLEANUP);
        assertThat(records, is(results));
    }

    /**
     * Executes flow.
     * @throws Exception if failed
     */
    @Test
    public void executeFlow() throws Exception {
        ProfileBuilder prf = new ProfileBuilder(folder.getRoot());
        ExecutionTask task = prf.task();
        task.executeFlow("batch", "testing", "flow");

        List<Record> results = SerialExecutionTracker.get(prf.trackingId);
        verifyPhaseOrder(results);

        assertThat(phase(results, "testing", ExecutionPhase.SETUP).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.INITIALIZE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.IMPORT).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.PROLOGUE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.MAIN).size(), is(4));
        assertThat(phase(results, "testing", ExecutionPhase.EPILOGUE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.EXPORT).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.FINALIZE).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.CLEANUP).size(), is(2));
    }

    /**
     * Executes flow but exporter is failed.
     * @throws Exception if failed
     */
    @Test
    public void executeFlow_failed_export() throws Exception {
        ProfileBuilder prf = new ProfileBuilder(folder.getRoot());
        prf.setTracker(ExporterFailed.class);
        ExecutionTask task = prf.task();
        try {
            task.executeFlow("batch", "testing", "flow");
            fail();
        } catch (IOException e) {
            // ok.
        }

        List<Record> results = SerialExecutionTracker.get(prf.trackingId);
        verifyPhaseOrder(results);

        assertThat(phase(results, "testing", ExecutionPhase.SETUP).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.INITIALIZE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.IMPORT).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.PROLOGUE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.MAIN).size(), is(4));
        assertThat(phase(results, "testing", ExecutionPhase.EPILOGUE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.EXPORT).size(), lessThan(2));
        assertThat(phase(results, "testing", ExecutionPhase.FINALIZE).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.CLEANUP).size(), is(0));
    }

    /**
     * Executes flow but finalizer is failed.
     * @throws Exception if failed
     */
    @Test
    public void executeFlow_failed_finalize() throws Exception {
        ProfileBuilder prf = new ProfileBuilder(folder.getRoot());
        prf.setTracker(FinalizerFailed.class);
        ExecutionTask task = prf.task();
        try {
            task.executeFlow("batch", "testing", "flow");
            fail();
        } catch (IOException e) {
            // ok.
        }

        List<Record> results = SerialExecutionTracker.get(prf.trackingId);
        verifyPhaseOrder(results);

        assertThat(phase(results, "testing", ExecutionPhase.SETUP).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.INITIALIZE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.IMPORT).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.PROLOGUE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.MAIN).size(), is(4));
        assertThat(phase(results, "testing", ExecutionPhase.EPILOGUE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.EXPORT).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.FINALIZE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.CLEANUP).size(), is(0));
    }

    /**
     * Executes flow but cleanup is failed.
     * @throws Exception if failed
     */
    @Test
    public void executeFlow_failed_cleanup() throws Exception {
        ProfileBuilder prf = new ProfileBuilder(folder.getRoot());
        prf.setTracker(CleanerFailed.class);
        ExecutionTask task = prf.task();
        task.executeFlow("batch", "testing", "flow");

        List<Record> results = SerialExecutionTracker.get(prf.trackingId);
        verifyPhaseOrder(results);

        assertThat(phase(results, "testing", ExecutionPhase.SETUP).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.INITIALIZE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.IMPORT).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.PROLOGUE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.MAIN).size(), is(4));
        assertThat(phase(results, "testing", ExecutionPhase.EPILOGUE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.EXPORT).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.FINALIZE).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.CLEANUP).size(), is(1));
    }

    /**
     * Executes batch.
     * @throws Exception if failed
     */
    @Test
    public void executeBatch() throws Exception {
        ProfileBuilder prf = new ProfileBuilder(folder.getRoot());
        ExecutionTask task = prf.task();
        task.executeBatch("batch");

        List<Record> results = SerialExecutionTracker.get(prf.trackingId);
        checkFlowHappensBefore(results, "testing", "left");
        checkFlowHappensBefore(results, "testing", "right");
        checkFlowHappensBefore(results, "left", "last");
        checkFlowHappensBefore(results, "right", "last");
        verifyPhaseOrder(results);

        assertThat(phase(results, "testing", ExecutionPhase.SETUP).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.INITIALIZE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.IMPORT).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.PROLOGUE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.MAIN).size(), is(4));
        assertThat(phase(results, "testing", ExecutionPhase.EPILOGUE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.EXPORT).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.FINALIZE).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.CLEANUP).size(), is(2));

        assertThat(phase(results, "left", ExecutionPhase.SETUP).size(), is(2));
        assertThat(phase(results, "left", ExecutionPhase.INITIALIZE).size(), is(0));
        assertThat(phase(results, "left", ExecutionPhase.IMPORT).size(), is(0));
        assertThat(phase(results, "left", ExecutionPhase.PROLOGUE).size(), is(0));
        assertThat(phase(results, "left", ExecutionPhase.MAIN).size(), is(1));
        assertThat(phase(results, "left", ExecutionPhase.EPILOGUE).size(), is(0));
        assertThat(phase(results, "left", ExecutionPhase.EXPORT).size(), is(0));
        assertThat(phase(results, "left", ExecutionPhase.FINALIZE).size(), is(0));
        assertThat(phase(results, "left", ExecutionPhase.CLEANUP).size(), is(2));

        assertThat(phase(results, "right", ExecutionPhase.SETUP).size(), is(2));
        assertThat(phase(results, "right", ExecutionPhase.INITIALIZE).size(), is(0));
        assertThat(phase(results, "right", ExecutionPhase.IMPORT).size(), is(0));
        assertThat(phase(results, "right", ExecutionPhase.PROLOGUE).size(), is(0));
        assertThat(phase(results, "right", ExecutionPhase.MAIN).size(), is(1));
        assertThat(phase(results, "right", ExecutionPhase.EPILOGUE).size(), is(0));
        assertThat(phase(results, "right", ExecutionPhase.EXPORT).size(), is(0));
        assertThat(phase(results, "right", ExecutionPhase.FINALIZE).size(), is(0));
        assertThat(phase(results, "right", ExecutionPhase.CLEANUP).size(), is(2));

        assertThat(phase(results, "last", ExecutionPhase.SETUP).size(), is(2));
        assertThat(phase(results, "last", ExecutionPhase.INITIALIZE).size(), is(0));
        assertThat(phase(results, "last", ExecutionPhase.IMPORT).size(), is(0));
        assertThat(phase(results, "last", ExecutionPhase.PROLOGUE).size(), is(0));
        assertThat(phase(results, "last", ExecutionPhase.MAIN).size(), is(1));
        assertThat(phase(results, "last", ExecutionPhase.EPILOGUE).size(), is(0));
        assertThat(phase(results, "last", ExecutionPhase.EXPORT).size(), is(0));
        assertThat(phase(results, "last", ExecutionPhase.FINALIZE).size(), is(0));
        assertThat(phase(results, "last", ExecutionPhase.CLEANUP).size(), is(2));
    }

    /**
     * Executes batch but exporter is failed.
     * @throws Exception if failed
     */
    @Test
    public void executeBatch_failed_export() throws Exception {
        ProfileBuilder prf = new ProfileBuilder(folder.getRoot());
        prf.setTracker(ExporterFailed.class);
        ExecutionTask task = prf.task();
        try {
            task.executeBatch("batch");
            fail();
        } catch (IOException e) {
            // ok.
        }

        List<Record> results = SerialExecutionTracker.get(prf.trackingId);
        checkFlowHappensBefore(results, "testing", "left");
        checkFlowHappensBefore(results, "testing", "right");
        checkFlowHappensBefore(results, "left", "last");
        checkFlowHappensBefore(results, "right", "last");
        verifyPhaseOrder(results);

        assertThat(phase(results, "testing", ExecutionPhase.SETUP).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.INITIALIZE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.IMPORT).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.PROLOGUE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.MAIN).size(), is(4));
        assertThat(phase(results, "testing", ExecutionPhase.EPILOGUE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.EXPORT).size(), lessThan(2));
        assertThat(phase(results, "testing", ExecutionPhase.FINALIZE).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.CLEANUP).size(), is(0));

        assertThat(flow(results, "left").size(), is(0));
        assertThat(flow(results, "right").size(), is(0));
        assertThat(flow(results, "last").size(), is(0));
    }

    /**
     * Executes batch but finalizer is failed.
     * @throws Exception if failed
     */
    @Test
    public void executeBatch_failed_finalize() throws Exception {
        ProfileBuilder prf = new ProfileBuilder(folder.getRoot());
        prf.setTracker(FinalizerFailed.class);
        ExecutionTask task = prf.task();
        try {
            task.executeBatch("batch");
            fail();
        } catch (IOException e) {
            // ok.
        }

        List<Record> results = SerialExecutionTracker.get(prf.trackingId);
        verifyPhaseOrder(results);

        assertThat(phase(results, "testing", ExecutionPhase.SETUP).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.INITIALIZE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.IMPORT).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.PROLOGUE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.MAIN).size(), is(4));
        assertThat(phase(results, "testing", ExecutionPhase.EPILOGUE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.EXPORT).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.FINALIZE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.CLEANUP).size(), is(0));

        assertThat(flow(results, "left").size(), is(0));
        assertThat(flow(results, "right").size(), is(0));
        assertThat(flow(results, "last").size(), is(0));
    }

    /**
     * Executes batch but cleanup is failed.
     * @throws Exception if failed
     */
    @Test
    public void executeBatch_failed_cleanup() throws Exception {
        ProfileBuilder prf = new ProfileBuilder(folder.getRoot());
        prf.setTracker(CleanerFailed.class);
        ExecutionTask task = prf.task();
        task.executeBatch("batch");

        List<Record> results = SerialExecutionTracker.get(prf.trackingId);
        verifyPhaseOrder(results);

        assertThat(phase(results, "testing", ExecutionPhase.SETUP).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.INITIALIZE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.IMPORT).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.PROLOGUE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.MAIN).size(), is(4));
        assertThat(phase(results, "testing", ExecutionPhase.EPILOGUE).size(), is(1));
        assertThat(phase(results, "testing", ExecutionPhase.EXPORT).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.FINALIZE).size(), is(2));
        assertThat(phase(results, "testing", ExecutionPhase.CLEANUP).size(), is(1));

        assertThat(phase(results, "left", ExecutionPhase.SETUP).size(), is(2));
        assertThat(phase(results, "left", ExecutionPhase.INITIALIZE).size(), is(0));
        assertThat(phase(results, "left", ExecutionPhase.IMPORT).size(), is(0));
        assertThat(phase(results, "left", ExecutionPhase.PROLOGUE).size(), is(0));
        assertThat(phase(results, "left", ExecutionPhase.MAIN).size(), is(1));
        assertThat(phase(results, "left", ExecutionPhase.EPILOGUE).size(), is(0));
        assertThat(phase(results, "left", ExecutionPhase.EXPORT).size(), is(0));
        assertThat(phase(results, "left", ExecutionPhase.FINALIZE).size(), is(0));
        assertThat(phase(results, "left", ExecutionPhase.CLEANUP).size(), lessThan(2));

        assertThat(phase(results, "right", ExecutionPhase.SETUP).size(), is(2));
        assertThat(phase(results, "right", ExecutionPhase.INITIALIZE).size(), is(0));
        assertThat(phase(results, "right", ExecutionPhase.IMPORT).size(), is(0));
        assertThat(phase(results, "right", ExecutionPhase.PROLOGUE).size(), is(0));
        assertThat(phase(results, "right", ExecutionPhase.MAIN).size(), is(1));
        assertThat(phase(results, "right", ExecutionPhase.EPILOGUE).size(), is(0));
        assertThat(phase(results, "right", ExecutionPhase.EXPORT).size(), is(0));
        assertThat(phase(results, "right", ExecutionPhase.FINALIZE).size(), is(0));
        assertThat(phase(results, "right", ExecutionPhase.CLEANUP).size(), lessThan(2));

        assertThat(phase(results, "last", ExecutionPhase.SETUP).size(), is(2));
        assertThat(phase(results, "last", ExecutionPhase.INITIALIZE).size(), is(0));
        assertThat(phase(results, "last", ExecutionPhase.IMPORT).size(), is(0));
        assertThat(phase(results, "last", ExecutionPhase.PROLOGUE).size(), is(0));
        assertThat(phase(results, "last", ExecutionPhase.MAIN).size(), is(1));
        assertThat(phase(results, "last", ExecutionPhase.EPILOGUE).size(), is(0));
        assertThat(phase(results, "last", ExecutionPhase.EXPORT).size(), is(0));
        assertThat(phase(results, "last", ExecutionPhase.FINALIZE).size(), is(0));
        assertThat(phase(results, "last", ExecutionPhase.CLEANUP).size(), lessThan(2));
    }

    private void checkScriptHappensBefore(List<Record> results, String head, String follow) {
        boolean sawFollow = false;
        for (Record r : results) {
            String id = id(r);
            if (head.equals(id)) {
                if (sawFollow) {
                    throw new AssertionError(head + "=>" + follow);
                }
            } else if (follow.equals(id)) {
                sawFollow = false;
            }
        }
    }

    private void checkFlowHappensBefore(List<Record> results, String head, String follow) {
        boolean sawFollow = false;
        for (Record r : results) {
            String id = r.context.getFlowId();
            if (head.equals(id)) {
                if (sawFollow) {
                    throw new AssertionError(head + "=>" + follow);
                }
            } else if (follow.equals(id)) {
                sawFollow = false;
            }
        }
    }

    private Set<String> id(List<Record> records) {
        TreeSet<String> results = new TreeSet<String>();
        for (Record r : records) {
            results.add(id(r));
        }
        return results;
    }

    private String id(Record r) {
        return r.script == null ? r.handler.getHandlerId() : r.script.getId();
    }

    private Set<String> set(String... values) {
        return new TreeSet<String>(Arrays.asList(values));
    }

    private void verifyPhaseOrder(List<Record> results) {
        Map<String, List<Record>> partitions = flowPartition(results);
        for (Map.Entry<String, List<Record>> entry : partitions.entrySet()) {
            String flowId = entry.getKey();
            List<Record> records = entry.getValue();
            ExecutionPhase last = ExecutionPhase.SETUP;
            for (Record r : records) {
                ExecutionPhase phase = r.context.getPhase();
                assertThat(flowId, phase, greaterThanOrEqualTo(last));
                last = phase;
            }
        }
    }

    private Map<String, List<Record>> flowPartition(List<Record> records) {
        Map<String, List<Record>> results = new HashMap<String, List<Record>>();
        for (Record r : records) {
            String flowId = r.context.getFlowId();
            List<Record> list = results.get(flowId);
            if (list == null) {
                list = new ArrayList<ExecutionTracker.Record>();
                results.put(flowId, list);
            }
            list.add(r);
        }
        return results;
    }

    private List<Record> flow(List<Record> records, String flowId) {
        List<Record> results = new ArrayList<Record>();
        for (Record r : records) {
            if (r.context.getFlowId().equals(flowId)) {
                results.add(r);
            }
        }
        return results;
    }

    private List<Record> phase(List<Record> records, String flowId, ExecutionPhase phase) {
        List<Record> results = new ArrayList<Record>();
        for (Record r : flow(records, flowId)) {
            if (r.context.getPhase() == phase) {
                results.add(r);
            }
        }
        return results;
    }

    static String profile(Record record) {
        return ((CommandScript) record.script).getProfileName();
    }

    /**
     * for {@link ExecutionTaskTest#executeFlow_failed_export()}.
     */
    public static class ExporterFailed extends SerialExecutionTracker {

        @Override
        public synchronized void add(Id id, Record record) throws IOException, InterruptedException {
            if (record.context.getPhase() == ExecutionPhase.EXPORT
                    && profile(record).equals("testing1")) {
                throw new IOException();
            }
            super.add(id, record);
        }
    }

    /**
     * for {@link ExecutionTaskTest#executeFlow_failed_finalize()}.
     */
    public static class FinalizerFailed extends SerialExecutionTracker {

        @Override
        public synchronized void add(Id id, Record record) throws IOException, InterruptedException {
            if (record.context.getPhase() == ExecutionPhase.FINALIZE
                    && profile(record).equals("testing1")) {
                throw new IOException();
            }
            super.add(id, record);
        }
    }

    /**
     * for {@link ExecutionTaskTest#executeFlow_failed_cleanup()}.
     */
    public static class CleanerFailed extends SerialExecutionTracker {

        @Override
        public synchronized void add(Id id, Record record) throws IOException, InterruptedException {
            if (record.context.getPhase() == ExecutionPhase.CLEANUP
                    && record.handler.getHandlerId().equals("hadoop")) {
                throw new IOException();
            }
            super.add(id, record);
        }
    }

    private static class ProfileBuilder {

        static final Pattern PLACEHOLDER = Pattern.compile("<<(.+?)>>");

        final File asakusaHome;

        final File lockDir;

        final ExecutionTracker.Id trackingId;

        final Map<String, String> replacement;

        final Properties override;

        ProfileBuilder(File working) {
            this.asakusaHome = new File(working, "asakusa");
            this.lockDir = new File(working, "lock");
            this.trackingId = ExecutionTracker.Id.get("testing");
            this.replacement = new HashMap<String, String>();
            this.replacement.put("home", asakusaHome.getAbsolutePath());
            this.replacement.put("scope", ExecutionLock.Scope.WORLD.getSymbol());
            this.replacement.put("lock", lockDir.getAbsolutePath());
            this.replacement.put("tracker", SerialExecutionTracker.class.getName());
            this.replacement.put("id", "testing");
            this.override = new Properties();
            SerialExecutionTracker.clear();
        }

        void setTracker(Class<? extends ExecutionTracker> tracker) {
            this.replacement.put("tracker", tracker.getName());
        }

        ExecutionTask task() throws IOException, InterruptedException {
            Properties properties = loadProfile();
            YaessProfile profile = YaessProfile.load(properties, ProfileContext.system(getClass().getClassLoader()));
            Map<String, String> arguments = Collections.emptyMap();
            Properties script = loadScript();
            return ExecutionTask.load(profile, script, arguments);
        }

        Properties loadScript() throws IOException {
            Properties result = load("script-template.properties");
            return result;
        }

        Properties loadProfile() throws IOException {
            Properties result = load("profile-template.properties");
            result.putAll(override);

            for (Map.Entry<Object, Object> entry : result.entrySet()) {
                String value = (String) entry.getValue();
                StringBuilder buf = new StringBuilder();
                int start = 0;
                Matcher matcher = PLACEHOLDER.matcher(value);
                while (matcher.find(start)) {
                    buf.append(value.subSequence(start, matcher.start()));
                    String rep = replacement.get(matcher.group(1));
                    if (rep == null) {
                        throw new AssertionError(matcher.group(1));
                    }
                    buf.append(rep);
                    start = matcher.end();
                }
                buf.append(value.substring(start));
                entry.setValue(buf.toString());
            }

            return result;
        }

        private Properties load(String name) throws IOException {
            Properties result = new Properties();
            InputStream in = getClass().getResourceAsStream(name);
            assertThat(in, is(notNullValue()));
            try {
                result.load(in);
            } finally {
                in.close();
            }
            return result;
        }
    }
}
