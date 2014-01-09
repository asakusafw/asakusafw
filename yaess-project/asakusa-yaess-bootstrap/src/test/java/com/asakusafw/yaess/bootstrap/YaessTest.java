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
package com.asakusafw.yaess.bootstrap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.yaess.bootstrap.ExecutionTracker.Record;
import com.asakusafw.yaess.bootstrap.Yaess.Configuration;
import com.asakusafw.yaess.bootstrap.Yaess.Mode;
import com.asakusafw.yaess.core.ExecutionLock;
import com.asakusafw.yaess.core.ExecutionPhase;

/**
 * Test for {@link Yaess}.
 */
public class YaessTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Parse simple batch configuration.
     * @throws Exception if failed
     */
    @Test
    public void config_batch() throws Exception {
        ProfileBuilder builder = new ProfileBuilder(folder.getRoot());
        File profile = builder.getProfile();
        File script = builder.getScript();

        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-profile", profile.getAbsolutePath());
        Collections.addAll(arguments, "-script", script.getAbsolutePath());
        Collections.addAll(arguments, "-batch", "tbatch");

        Configuration conf = Yaess.parseConfiguration(arguments.toArray(new String[arguments.size()]));
        assertThat(conf.mode, is(Mode.BATCH));
        assertThat(conf.batchId, is("tbatch"));
        assertThat(conf.flowId, is(nullValue()));
        assertThat(conf.executionId, is(nullValue()));
        assertThat(conf.phase, is(nullValue()));
        assertThat(conf.arguments.size(), is(0));
    }

    /**
     * Parse simple flow configuration.
     * @throws Exception if failed
     */
    @Test
    public void config_flow() throws Exception {
        ProfileBuilder builder = new ProfileBuilder(folder.getRoot());
        File profile = builder.getProfile();
        File script = builder.getScript();

        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-profile", profile.getAbsolutePath());
        Collections.addAll(arguments, "-script", script.getAbsolutePath());
        Collections.addAll(arguments, "-batch", "tbatch");
        Collections.addAll(arguments, "-flow", "tflow");
        Collections.addAll(arguments, "-execution", "texec");

        Configuration conf = Yaess.parseConfiguration(arguments.toArray(new String[arguments.size()]));
        assertThat(conf.mode, is(Mode.FLOW));
        assertThat(conf.batchId, is("tbatch"));
        assertThat(conf.flowId, is("tflow"));
        assertThat(conf.executionId, is("texec"));
        assertThat(conf.phase, is(nullValue()));
        assertThat(conf.arguments.size(), is(0));
    }

    /**
     * Parse simple phase configuration.
     * @throws Exception if failed
     */
    @Test
    public void config_phase() throws Exception {
        ProfileBuilder builder = new ProfileBuilder(folder.getRoot());
        File profile = builder.getProfile();
        File script = builder.getScript();

        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-profile", profile.getAbsolutePath());
        Collections.addAll(arguments, "-script", script.getAbsolutePath());
        Collections.addAll(arguments, "-batch", "tbatch");
        Collections.addAll(arguments, "-flow", "tflow");
        Collections.addAll(arguments, "-execution", "texec");
        Collections.addAll(arguments, "-phase", ExecutionPhase.MAIN.getSymbol());

        Configuration conf = Yaess.parseConfiguration(arguments.toArray(new String[arguments.size()]));
        assertThat(conf.mode, is(Mode.PHASE));
        assertThat(conf.batchId, is("tbatch"));
        assertThat(conf.flowId, is("tflow"));
        assertThat(conf.executionId, is("texec"));
        assertThat(conf.phase, is(ExecutionPhase.MAIN));
        assertThat(conf.arguments.size(), is(0));
    }

    /**
     * Parse configuration with arguments.
     * @throws Exception if failed
     */
    @Test
    public void config_arguments() throws Exception {
        ProfileBuilder builder = new ProfileBuilder(folder.getRoot());
        File profile = builder.getProfile();
        File script = builder.getScript();

        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-profile", profile.getAbsolutePath());
        Collections.addAll(arguments, "-script", script.getAbsolutePath());
        Collections.addAll(arguments, "-batch", "tbatch");
        Collections.addAll(arguments, "-A", "a=b");
        Collections.addAll(arguments, "-A", "c=d");
        Collections.addAll(arguments, "-A", "e=f");

        Configuration conf = Yaess.parseConfiguration(arguments.toArray(new String[arguments.size()]));
        assertThat(conf.mode, is(Mode.BATCH));
        assertThat(conf.batchId, is("tbatch"));
        assertThat(conf.flowId, is(nullValue()));
        assertThat(conf.executionId, is(nullValue()));
        assertThat(conf.phase, is(nullValue()));
        assertThat(conf.arguments.size(), is(3));
        assertThat(conf.arguments.get("a"), is("b"));
        assertThat(conf.arguments.get("c"), is("d"));
        assertThat(conf.arguments.get("e"), is("f"));
    }

    /**
     * Attempts to parse config with missing profile.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void config_invalid_profile() throws Exception {
        ProfileBuilder builder = new ProfileBuilder(folder.getRoot());
        File profile = builder.getProfile();
        File script = builder.getScript();

        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-profile", profile.getAbsolutePath() + ".MISSING");
        Collections.addAll(arguments, "-script", script.getAbsolutePath());
        Collections.addAll(arguments, "-batch", "tbatch");

        Yaess.parseConfiguration(arguments.toArray(new String[arguments.size()]));
    }

    /**
     * Attempts to parse config with missing script.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void config_invalid_script() throws Exception {
        ProfileBuilder builder = new ProfileBuilder(folder.getRoot());
        File profile = builder.getProfile();
        File script = builder.getScript();

        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-profile", profile.getAbsolutePath());
        Collections.addAll(arguments, "-script", script.getAbsolutePath() + ".MISSING");
        Collections.addAll(arguments, "-batch", "tbatch");

        Yaess.parseConfiguration(arguments.toArray(new String[arguments.size()]));
    }

    /**
     * Attempts to parse config with invalid script.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void config_invalid_phase() throws Exception {
        ProfileBuilder builder = new ProfileBuilder(folder.getRoot());
        File profile = builder.getProfile();
        File script = builder.getScript();

        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-profile", profile.getAbsolutePath());
        Collections.addAll(arguments, "-script", script.getAbsolutePath());
        Collections.addAll(arguments, "-batch", "tbatch");
        Collections.addAll(arguments, "-flow", "tflow");
        Collections.addAll(arguments, "-execution", "texec");
        Collections.addAll(arguments, "-phase", "__INVALID__");

        Yaess.parseConfiguration(arguments.toArray(new String[arguments.size()]));
    }

    /**
     * Attempts to parse batch config with execution ID.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void config_batch_invalid_exec() throws Exception {
        ProfileBuilder builder = new ProfileBuilder(folder.getRoot());
        File profile = builder.getProfile();
        File script = builder.getScript();

        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-profile", profile.getAbsolutePath());
        Collections.addAll(arguments, "-script", script.getAbsolutePath());
        Collections.addAll(arguments, "-batch", "tbatch");
        Collections.addAll(arguments, "-execution", "INVALID");

        Yaess.parseConfiguration(arguments.toArray(new String[arguments.size()]));
    }

    /**
     * Attempts to parse batch config with phase.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void config_batch_invalid_phase() throws Exception {
        ProfileBuilder builder = new ProfileBuilder(folder.getRoot());
        File profile = builder.getProfile();
        File script = builder.getScript();

        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-profile", profile.getAbsolutePath());
        Collections.addAll(arguments, "-script", script.getAbsolutePath());
        Collections.addAll(arguments, "-batch", "tbatch");
        Collections.addAll(arguments, "-phase", ExecutionPhase.MAIN.getSymbol());

        Yaess.parseConfiguration(arguments.toArray(new String[arguments.size()]));
    }

    /**
     * Attempts to parse flow config without execution ID.
     * @throws Exception if failed
     */
    @Test(expected = IllegalArgumentException.class)
    public void config_flow_missing_exec() throws Exception {
        ProfileBuilder builder = new ProfileBuilder(folder.getRoot());
        File profile = builder.getProfile();
        File script = builder.getScript();

        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-profile", profile.getAbsolutePath());
        Collections.addAll(arguments, "-script", script.getAbsolutePath());
        Collections.addAll(arguments, "-batch", "tbatch");
        Collections.addAll(arguments, "-flow", "invalid");

        Yaess.parseConfiguration(arguments.toArray(new String[arguments.size()]));
    }

    /**
     * Executes batch.
     * @throws Exception if failed
     */
    @Test
    public void execute_batch() throws Exception {
        ProfileBuilder builder = new ProfileBuilder(folder.getRoot());
        File profile = builder.getProfile();
        File script = builder.getScript();

        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-profile", profile.getAbsolutePath());
        Collections.addAll(arguments, "-script", script.getAbsolutePath());
        Collections.addAll(arguments, "-batch", "tbatch");

        int exit = Yaess.execute(arguments.toArray(new String[arguments.size()]));
        assertThat(exit, is(0));

        List<Record> records = SerialExecutionTracker.get(builder.trackingId);
        assertThat(flow(records, "testing").size(), is(greaterThan(0)));
        assertThat(flow(records, "left").size(), is(greaterThan(0)));
        assertThat(flow(records, "right").size(), is(greaterThan(0)));
        assertThat(flow(records, "last").size(), is(greaterThan(0)));

        Set<String> execs = new HashSet<String>();
        execs.add(flow(records, "testing").get(0).context.getExecutionId());
        execs.add(flow(records, "left").get(0).context.getExecutionId());
        execs.add(flow(records, "right").get(0).context.getExecutionId());
        execs.add(flow(records, "last").get(0).context.getExecutionId());
        assertThat(execs.size(), is(4));
    }

    /**
     * Executes flow.
     * @throws Exception if failed
     */
    @Test
    public void execute_flow() throws Exception {
        ProfileBuilder builder = new ProfileBuilder(folder.getRoot());
        File profile = builder.getProfile();
        File script = builder.getScript();

        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-profile", profile.getAbsolutePath());
        Collections.addAll(arguments, "-script", script.getAbsolutePath());
        Collections.addAll(arguments, "-batch", "tbatch");
        Collections.addAll(arguments, "-flow", "right");
        Collections.addAll(arguments, "-execution", "texec");

        int exit = Yaess.execute(arguments.toArray(new String[arguments.size()]));
        assertThat(exit, is(0));

        List<Record> records = SerialExecutionTracker.get(builder.trackingId);
        assertThat(flow(records, "testing").size(), is(0));
        assertThat(flow(records, "left").size(), is(0));
        assertThat(flow(records, "right").size(), is(greaterThan(0)));
        assertThat(flow(records, "last").size(), is(0));

        List<Record> flow = flow(records, "right");
        assertThat(flow.get(0).context.getExecutionId(), is("texec"));
    }

    /**
     * Executes flow With Arguments.
     * @throws Exception if failed
     */
    @Test
    public void execute_flow_with_args() throws Exception {
        ProfileBuilder builder = new ProfileBuilder(folder.getRoot());
        File profile = builder.getProfile();
        File script = builder.getScript();

        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-profile", profile.getAbsolutePath());
        Collections.addAll(arguments, "-script", script.getAbsolutePath());
        Collections.addAll(arguments, "-batch", "tbatch");
        Collections.addAll(arguments, "-flow", "right");
        Collections.addAll(arguments, "-execution", "texec");
        Collections.addAll(arguments, "-A", "a=b");
        Collections.addAll(arguments, "-A", "c=d");
        Collections.addAll(arguments, "-D", "skipFlows=testing");
        Collections.addAll(arguments, "-D", "verifyApplication=false");

        int exit = Yaess.execute(arguments.toArray(new String[arguments.size()]));
        assertThat(exit, is(0));

        List<Record> records = SerialExecutionTracker.get(builder.trackingId);
        assertThat(flow(records, "testing").size(), is(0));
        assertThat(flow(records, "left").size(), is(0));
        assertThat(flow(records, "right").size(), is(greaterThan(0)));
        assertThat(flow(records, "last").size(), is(0));

        List<Record> flow = flow(records, "right");
        assertThat(flow.get(0).context.getExecutionId(), is("texec"));
    }


    /**
     * Executes flow.
     * @throws Exception if failed
     */
    @Test
    public void execute_phase() throws Exception {
        ProfileBuilder builder = new ProfileBuilder(folder.getRoot());
        File profile = builder.getProfile();
        File script = builder.getScript();

        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-profile", profile.getAbsolutePath());
        Collections.addAll(arguments, "-script", script.getAbsolutePath());
        Collections.addAll(arguments, "-batch", "tbatch");
        Collections.addAll(arguments, "-flow", "testing");
        Collections.addAll(arguments, "-execution", "texec");
        Collections.addAll(arguments, "-phase", ExecutionPhase.MAIN.getSymbol());

        int exit = Yaess.execute(arguments.toArray(new String[arguments.size()]));
        assertThat(exit, is(0));

        List<Record> records = SerialExecutionTracker.get(builder.trackingId);
        assertThat(flow(records, "testing").size(), is(greaterThan(0)));
        assertThat(flow(records, "left").size(), is(0));
        assertThat(flow(records, "right").size(), is(0));
        assertThat(flow(records, "last").size(), is(0));

        assertThat(flow(records, "testing"), is(phase(records, "testing", ExecutionPhase.MAIN)));
    }

    /**
     * Attempts to execute but config is invalid.
     * @throws Exception if failed
     */
    @Test
    public void execute_invalid_config() throws Exception {
        List<String> arguments = new ArrayList<String>();
        int exit = Yaess.execute(arguments.toArray(new String[arguments.size()]));
        assertThat(exit, is(not(0)));
    }

    /**
     * Attempts to execute but job is invalid.
     * @throws Exception if failed
     */
    @Test
    public void execute_invalid_jobs() throws Exception {
        ProfileBuilder builder = new ProfileBuilder(folder.getRoot());
        builder.setTracker(InvalidTracker.class);

        File profile = builder.getProfile();
        File script = builder.getScript();

        List<String> arguments = new ArrayList<String>();
        Collections.addAll(arguments, "-profile", profile.getAbsolutePath());
        Collections.addAll(arguments, "-script", script.getAbsolutePath());
        Collections.addAll(arguments, "-batch", "tbatch");
        Collections.addAll(arguments, "-flow", "testing");
        Collections.addAll(arguments, "-execution", "texec");
        Collections.addAll(arguments, "-phase", ExecutionPhase.MAIN.getSymbol());

        int exit = Yaess.execute(arguments.toArray(new String[arguments.size()]));
        assertThat(exit, is(not(0)));
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

    private static class ProfileBuilder {

        static final Pattern PLACEHOLDER = Pattern.compile("<<(.+?)>>");

        final File asakusaHome;

        final File lockDir;

        final File tempDir;

        final ExecutionTracker.Id trackingId;

        final Map<String, String> replacement;

        final Properties override;

        ProfileBuilder(File working) {
            this.asakusaHome = new File(working, "asakusa");
            this.lockDir = new File(working, "lock");
            this.tempDir = new File(working, "properties");
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

        Properties loadScript() throws IOException {
            Properties result = load("script-template.properties");
            return result;
        }

        File getScript() throws IOException {
            Properties properties = loadScript();
            return createPropertiesFile("script.properties", properties);
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

        File getProfile() throws IOException {
            Properties properties = loadProfile();
            return createPropertiesFile("profile.properties", properties);
        }

        private File createPropertiesFile(String name, Properties properties) throws IOException {
            tempDir.mkdirs();
            File file = new File(tempDir, name);
            FileOutputStream out = new FileOutputStream(file);
            try {
                properties.store(out, name);
            } finally {
                out.close();
            }
            return file;
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

    /**
     * Always failure.
     */
    public static class InvalidTracker implements ExecutionTracker {
        @Override
        public void add(Id id, Record record) throws IOException, InterruptedException {
            throw new IOException();
        }
    }
}
