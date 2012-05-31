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
package com.asakusafw.yaess.flowlog;

import static com.asakusafw.yaess.flowlog.FlowLoggerProfile.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.yaess.core.ProfileContext;
import com.asakusafw.yaess.core.ServiceProfile;
import com.asakusafw.yaess.core.VariableResolver;

/**
 * Test for {@link FlowLoggerProfile}.
 */
public class FlowLoggerProfileTest {

    /**
     * Temporary folder.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Converts simple profile.
     * @throws Exception if failed
     */
    @Test
    public void convert() throws Exception {
        Map<String, String> conf = map(KEY_DIRECTORY, folder.getRoot().getAbsolutePath());
        Map<String, String> vars = map();
        ServiceProfile<FlowLoggerProvider> sp = profile(conf, vars);
        FlowLoggerProfile profile = FlowLoggerProfile.convert(sp);

        assertThat(profile.getDirectory().getCanonicalFile(), is(folder.getRoot().getCanonicalFile()));
        assertThat(profile.getEncoding(), is(Charset.forName(DEFAULT_ENCODING)));
        assertThat(profile.getDateFormat(), is((DateFormat) new SimpleDateFormat(DEFAULT_DATE_FORMAT)));
        assertThat(profile.getStepUnit(), closeTo(Double.parseDouble(DEFAULT_STEP_UNIT), 0.00001));
        assertThat(profile.isReportJob(), is(Boolean.parseBoolean(DEFAULT_REPORT_JOB)));
        assertThat(profile.isDeleteOnSetup(), is(Boolean.parseBoolean(DEFAULT_DELETE_ON_SETUP)));
        assertThat(profile.isDeleteOnCleanup(), is(Boolean.parseBoolean(DEFAULT_DELETE_ON_CLEANUP)));
    }

    /**
     * Converts full profile.
     * @throws Exception if failed
     */
    @Test
    public void convert_all() throws Exception {
        Map<String, String> conf = map(
                KEY_DIRECTORY, folder.getRoot().getAbsolutePath(),
                KEY_ENCODING, "ASCII",
                KEY_DATE_FORMAT, "yyyy",
                KEY_STEP_UNIT, "0.50",
                KEY_REPORT_JOB, "false",
                KEY_DELETE_ON_SETUP, "false",
                KEY_DELETE_ON_CLEANUP, "false");
        Map<String, String> vars = map();
        ServiceProfile<FlowLoggerProvider> sp = profile(conf, vars);
        FlowLoggerProfile profile = FlowLoggerProfile.convert(sp);

        assertThat(profile.getDirectory().getCanonicalFile(), is(folder.getRoot().getCanonicalFile()));
        assertThat(profile.getEncoding(), is(Charset.forName("ASCII")));
        assertThat(profile.getDateFormat(), is((DateFormat) new SimpleDateFormat("yyyy")));
        assertThat(profile.getStepUnit(), closeTo(Double.parseDouble("0.50"), 0.00001));
        assertThat(profile.isReportJob(), is(Boolean.parseBoolean("false")));
        assertThat(profile.isDeleteOnSetup(), is(Boolean.parseBoolean("false")));
        assertThat(profile.isDeleteOnCleanup(), is(Boolean.parseBoolean("false")));
    }

    /**
     * Converts parameterized profile.
     * @throws Exception if failed
     */
    @Test
    public void convert_param() throws Exception {
        Map<String, String> conf = map(
                KEY_DIRECTORY, "${dir}",
                KEY_ENCODING, "${enc}");
        Map<String, String> vars = map(
                "dir", folder.getRoot().getAbsolutePath(),
                "enc", "ASCII");
        ServiceProfile<FlowLoggerProvider> sp = profile(conf, vars);
        FlowLoggerProfile profile = FlowLoggerProfile.convert(sp);

        assertThat(profile.getDirectory().getCanonicalFile(), is(folder.getRoot().getCanonicalFile()));
        assertThat(profile.getEncoding(), is(Charset.forName("ASCII")));
    }

    /**
     * Converts empty profile.
     */
    @Test(expected = IllegalArgumentException.class)
    public void convert_empty() {
        Map<String, String> conf = map();
        Map<String, String> vars = map();
        ServiceProfile<FlowLoggerProvider> sp = profile(conf, vars);
        FlowLoggerProfile.convert(sp);
    }

    ServiceProfile<FlowLoggerProvider> profile(Map<String, String> conf, Map<String, String> vars) {
        ServiceProfile<FlowLoggerProvider> profile = new ServiceProfile<FlowLoggerProvider>(
                "testing",
                FlowLoggerProvider.class,
                conf,
                new ProfileContext(getClass().getClassLoader(), new VariableResolver(vars)));
        return profile;
    }

    private Map<String, String> map(String... keyValuePairs) {
        assert keyValuePairs.length % 2 == 0;
        Map<String, String> results = new HashMap<String, String>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            results.put(keyValuePairs[i + 0], keyValuePairs[i + 1]);
        }
        return results;
    }
}
