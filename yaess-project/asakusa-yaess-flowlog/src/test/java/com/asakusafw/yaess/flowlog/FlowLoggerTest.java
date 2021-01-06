/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.yaess.flowlog;

import static com.asakusafw.yaess.core.ExecutionPhase.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Scanner;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.yaess.core.ExecutionContext;
import com.asakusafw.yaess.core.ExecutionMonitor;
import com.asakusafw.yaess.core.ExecutionPhase;
import com.asakusafw.yaess.core.PhaseMonitor.JobStatus;

/**
 * Test for {@link FlowLogger}.
 */
public class FlowLoggerTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * simple senario.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        FlowLoggerProfile profile = profile(false, false, false);
        File log = profile.getLogFile(context(MAIN));
        File escape = profile.getEscapeFile(context(MAIN));

        int l00 = 0;

        int l11;
        int l12;
        int l21;
        try (FlowLogger log1 = new FlowLogger(context(MAIN), profile)) {
            log1.open(1);
            l11 = checkLines(log, l00, MAIN);
            log1.close();
            l12 = checkLines(log, l11, MAIN);
        }

        try (FlowLogger log2 = new FlowLogger(context(CLEANUP), profile)) {
            log2.open(1);
            l21 = checkLines(log, l12, CLEANUP);
            log2.close();
        }

        assertThat(log.isFile(), is(false));
        checkLines(escape, l21, CLEANUP);
    }

    /**
     * logs two phases.
     * @throws Exception if failed
     */
    @Test
    public void chain() throws Exception {
        FlowLoggerProfile profile = profile(false, false, false);
        File log = profile.getLogFile(context(MAIN));
        File escape = profile.getEscapeFile(context(MAIN));

        int l00 = 0;

        int l11;
        int l12;
        int l21;
        int l31;

        try (FlowLogger log1 = new FlowLogger(context(MAIN), profile)) {
            log1.open(1);
            l11 = checkLines(log, l00, MAIN);
            log1.close();
            l12 = checkLines(log, l11, MAIN);
        }
        try (FlowLogger log2 = new FlowLogger(context(CLEANUP), profile)) {
            log2.open(1);
            l21 = checkLines(log, l12, CLEANUP);
            log2.close();
            assertThat(log.isFile(), is(false));
            checkLines(escape, l21, CLEANUP);
        }
        try (FlowLogger log3 = new FlowLogger(context(SETUP), profile)) {
            log3.open(1);
            l31 = checkLines(log, l00, SETUP);
            log3.close();
            checkLines(log, l31, SETUP);
        }
    }

    /**
     * clear last log.
     * @throws Exception if failed
     */
    @Test
    public void deleteOnSetup() throws Exception {
        FlowLoggerProfile profile = profile(false, true, false);
        File log = profile.getLogFile(context(MAIN));

        int l00 = 0;

        int l11;
        int l12;
        int l21;

        try (FlowLogger log1 = new FlowLogger(context(MAIN), profile)) {
            log1.open(1);
            l11 = checkLines(log, l00, MAIN);
            log1.close();
            l12 = checkLines(log, l11, MAIN);
        }

        try (FlowLogger log2 = new FlowLogger(context(SETUP), profile)) {
            log2.open(1);
            l21 = checkLines(log, l00, SETUP);
            log2.close();
            checkLines(log, l21, SETUP);
        }

        assertThat(l21, is(lessThan(l12)));
    }

    /**
     * logs two phases, and cleanup.
     * @throws Exception if failed
     */
    @Test
    public void deleteOnCleanup() throws Exception {
        FlowLoggerProfile profile = profile(false, true, true);
        File log = profile.getLogFile(context(MAIN));
        File escape = profile.getEscapeFile(context(MAIN));

        int l00 = 0;
        int l11;
        int l12;

        try (FlowLogger log1 = new FlowLogger(context(MAIN), profile)) {
            log1.open(1);
            l11 = checkLines(log, l00, MAIN);
            log1.close();
            l12 = checkLines(log, l11, MAIN);
        }
        try (FlowLogger log2 = new FlowLogger(context(CLEANUP), profile)) {
            log2.open(1);
            checkLines(log, l12, CLEANUP);
            log2.close();
        }
        assertThat(log.isFile(), is(false));
        assertThat(escape.isFile(), is(false));
    }

    /**
     * logs two phases, and cleanup is prevented.
     * @throws Exception if failed
     */
    @Test
    public void deleteOnCleanup_error() throws Exception {
        FlowLoggerProfile profile = profile(true, true, true);
        File log = profile.getLogFile(context(MAIN));
        File escape = profile.getEscapeFile(context(MAIN));

        int l00 = 0;
        int l11;
        int l12;

        try (FlowLogger log1 = new FlowLogger(context(MAIN), profile)) {
            log1.open(1);
            l11 = checkLines(log, l00, MAIN);
            log1.close();
            l12 = checkLines(log, l11, MAIN);
        }

        try (FlowLogger log2 = new FlowLogger(context(CLEANUP), profile)) {
            log2.open(1);
            checkLines(log, l12, CLEANUP);
            try (ExecutionMonitor jm = log2.createJobMonitor("hoge", 1)) {
                jm.open(1);
            }
            log2.reportJobStatus("hoge", JobStatus.FAILED, new Exception());
        }
        assertThat(log.isFile(), is(true));
        assertThat(escape.isFile(), is(false));
    }

    private int checkLines(File log, int last, ExecutionPhase phase) throws IOException {
        int count = 0;
        boolean found = false;
        assertThat(log.isFile(), is(true));
        String pattern = phase.toString();
        try (Scanner scanner = new Scanner(log, "UTF-8")) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (found == false && count >= last) {
                    found = line.indexOf(pattern) >= 0;
                }
                count++;
            }
        }
        assertThat(pattern, found, is(true));
        assertThat(count, greaterThan(last));
        return count;
    }

    private ExecutionContext context(ExecutionPhase phase) {
        return new ExecutionContext("batch", "flow", "exec", phase, Collections.emptyMap());
    }

    private FlowLoggerProfile profile(boolean reportJob, boolean deleteOnSetup, boolean deleteOnCleanup) {
        return new FlowLoggerProfile(
                folder.getRoot(),
                StandardCharsets.UTF_8,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
                0.10,
                reportJob,
                deleteOnSetup,
                deleteOnCleanup);
    }
}
